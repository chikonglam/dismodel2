package gov.usgs.dismodel.gui.geoView;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.SurfaceIcon;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.geom.overlays.WWVector;
import gov.usgs.dismodel.geom.overlays.WWVectorLayer;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickFrier;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class GeoPanel extends Panel implements ZoomEventFirer,
		ZoomEventListener, DataChangeEventListener, GeoPosClickFrier {
	// constants
	// -----
	private static final double ZOOM_REFRESH_THRESHOLD = 0.08;
	private static final double ZOOM_MIN_SPAN = 10d;

	private static final long serialVersionUID = -7898383357800550284L;
	private Panel toolbar;
	final private WorldWindowGLCanvas wwd;
	private StatusBar statusBar;
	private SimulationDataModel simModel;
	private DisplayStateStore displaySettings;
	private WWClickRedirector wwClickRedirector;

	// layers
	// --------
	private RenderableLayer stationLayer = new RenderableLayer();
	private AnnotationLayer stationNameLayer = new AnnotationLayer();
	private WWVectorLayer measuredVectorLayer = new WWVectorLayer();

	final private View wwView;

	// Event Listeners
	private ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();

	// Constructs a JPanel to hold the WorldWindow
	public GeoPanel(Dimension canvasSize, boolean includeStatusBar,
			SimulationDataModel simModel, DisplayStateStore displaySettings) {
		super(new BorderLayout());

		// state vars
		this.simModel = simModel;
		this.displaySettings = displaySettings;

		// Create the toolbar
		this.toolbar = new Panel();
		this.add(toolbar, BorderLayout.NORTH);

		// Create the WorldWindow and set its preferred size.
		this.wwd = new WorldWindowGLCanvas();
		this.wwd.setPreferredSize(canvasSize);
		wwView = wwd.getView();

		// THIS IS THE TRICK: Set the panel's minimum size to (0,0);
		this.setMinimumSize(new Dimension(0, 0));
		this.setPreferredSize(canvasSize);

		// Create the default model as described in the current worldwind
		// properties.
		Model m = (Model) WorldWind
				.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		this.wwd.setModel(m);

		// Check the code below for click event handling
		// this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd,
		// WorldMapLayer.class));

		// Add the WorldWindow to this JPanel.
		this.add(wwd, BorderLayout.CENTER);

		// Add the status bar if desired.
		if (includeStatusBar) {
			statusBar = new StatusBar();
			this.add(statusBar, BorderLayout.PAGE_END);
			statusBar.setEventSource(wwd);
		}

		// making sure the right side zooms too
		wwView.setFieldOfView(Angle.fromDegrees(45));
		wwView.addPropertyChangeListener(changeListener);

		// setting up the layers
		insertBeforeCompass(this.wwd, stationLayer);
		insertBeforeCompass(this.wwd, stationNameLayer);
		measuredVectorLayer.initLayer(wwd);

		// setting the map at the default location, and default zoom level
		updateMapCenterAfterSettingsChanged(displaySettings);
		updateZoomLevelAfterSettingsChanged(displaySettings);

		// mouse click listener to serve location info, and remove one click
		// rotation
		wwClickRedirector = new WWClickRedirector(wwd, simModel);
		wwd.getInputHandler().addMouseListener(wwClickRedirector);

	}

	private PropertyChangeListener changeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SwingUtilities.invokeLater(threadedZoomLevelFirer);
		}
	};

	private Runnable threadedZoomLevelFirer = new Runnable() {

		public void run() {
			final double wwAxisSpan = wwView.getEyePosition().getAltitude();
			final double expectedAxisSpan = displaySettings.getChartSpan();

			if (wwAxisSpan > ZOOM_MIN_SPAN
					&& Math.abs((wwAxisSpan - expectedAxisSpan)
							/ expectedAxisSpan) > ZOOM_REFRESH_THRESHOLD) {
				displaySettings.setChartSpan(wwAxisSpan);
				for (ZoomEventListener listener : zoomListeners) {
					listener.updateZoomLevelAfterSettingsChanged(displaySettings);
				}
			}
		}

	};

	// zoom event handler
	@Override
	public void updateZoomLevelAfterSettingsChanged(
			DisplayStateStore displaySettings) {
		SwingUtilities.invokeLater(threadedupdateZoomLevel);
	}

	private Runnable threadedupdateZoomLevel = new Runnable() {

		@Override
		public void run() {
			double expectedAxisSpan = displaySettings.getChartSpan();
			System.out.println("WW received a request to zoom to span="
					+ expectedAxisSpan);

			final double wwAxisSpan = wwView.getEyePosition().getElevation();
			if (Math.abs((wwAxisSpan - expectedAxisSpan) / expectedAxisSpan) < ZOOM_REFRESH_THRESHOLD) {
				return;
			}

			if (wwView.isAnimating()) {
				wwView.stopAnimations();
			}

			Position pos = ((BasicOrbitView) wwView).getCenterPosition();

			Position tmpPos = Position.fromRadians(pos.getLatitude().radians,
					pos.getLongitude().radians, expectedAxisSpan);
			wwView.setEyePosition(tmpPos);

			wwd.redraw();

		}
	};

	// handlers zoomListener
	@Override
	public void addZoomListener(ZoomEventListener listener) {
		zoomListeners.add(listener);
	}

	@Override
	public void removeZoomListener(ZoomEventListener listener) {
		zoomListeners.remove(listener);
	}

	public void updateMapCenterAfterSettingsChanged(
			DisplayStateStore displaySettings) {
		SwingUtilities.invokeLater(threadedUpdateMapCenter);
	}

	private Runnable threadedUpdateMapCenter = new Runnable() {
		@Override
		public void run() {

			double expectedAxisSpan = displaySettings.getChartSpan();

			Position pos = ((BasicOrbitView) wwView).getCenterPosition();

			Position tmpPos = Position.fromRadians(pos.getLatitude().radians,
					pos.getLongitude().radians, expectedAxisSpan);
			wwView.setEyePosition(tmpPos);

			wwd.redraw();

		}
	};

	@Override
	public void updateAfterDataChange() {
		SwingUtilities.invokeLater(updateEverything);
	}

	private Runnable updateEverything = new Runnable() {
		@Override
		public void run() {
			// Add everything
			addStations();
			addMeasuredDispAndEllipses();
			addVectorScaleBar();

			// Draw them all out
			wwd.redraw();

		}

	};

	private void addStations() {
		stationLayer.removeAllRenderables();
		stationNameLayer.removeAllAnnotations();

		List<Label> stations = simModel.getStations();
		if (stations != null && stations.size() > 0) {

			AnnotationAttributes stationLabAttr = new AnnotationAttributes(); // TODO: move this out to a seperate class

			stationLabAttr.setFrameShape(FrameFactory.SHAPE_NONE);
			stationLabAttr.setTextAlign(AVKey.CENTER);
			stationLabAttr.setDrawOffset(new Point(0, 5));
			Color color = displaySettings.getStationColor();
			stationLabAttr.setTextColor(color);
			String stationIcon = "gov/usgs/dismodel/resources/station.png";

			for (Label label : stations) {
				Position position = toPosition(label.getLocation());
				SurfaceIcon icon = new SurfaceIcon(stationIcon, position);
				stationLayer.addRenderable(icon);
				GlobeAnnotation textLab = new GlobeAnnotation(label.getName(),
						position, stationLabAttr);
				stationNameLayer.addAnnotation(textLab);
			}

		}
	}

	private void addMeasuredDispAndEllipses() {
		List<VectorXyz> vectors = simModel.getMeasuredRefdDispVectors();
		if (vectors != null && vectors.size() > 0) {
			Color color = displaySettings.getRealDisplacementVectorColor();
			double scale = displaySettings.getDisplacementVectorScale(); // TODO:
																			// rework
																			// the
																			// ref
																			// mechanism
																			// if
																			// time
																			// allows

			ArrayList<WWVector> outVectors = new ArrayList<WWVector>(
					vectors.size());

			// Redraw renderables.
			for (VectorXyz vector : vectors) {
				outVectors.add(new WWVector(vector, scale, color));
			}

			this.measuredVectorLayer.setVectors(outVectors);
		}

	}

	private void addVectorScaleBar() {
		// TODO Auto-generated method stub

	}

	public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
		// Insert the layer into the layer list just before the compass.
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof CompassLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition, layer);
	}

	public static Position toPosition(gov.usgs.dismodel.geom.LLH llh) {
		gov.nasa.worldwind.geom.LatLon ll = gov.nasa.worldwind.geom.LatLon
				.fromDegrees(llh.getLatitude().toDeg(), llh.getLongitude()
						.toDeg());
		return new Position(ll, llh.getHeight());
	}

	@Override
	public void addGeoPosClickListener(GeoPosClickListener listener) {
		this.wwClickRedirector.addGeoPosClickListener(listener);

	}

	@Override
	public void removeGeoPosClickListener(GeoPosClickListener listener) {
		this.wwClickRedirector.removeGeoPosClickListener(listener);
	}

}

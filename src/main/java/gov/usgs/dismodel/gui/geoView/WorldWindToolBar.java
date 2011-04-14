package gov.usgs.dismodel.gui.geoView;

import gov.nasa.worldwind.examples.util.SectorSelector;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;
import gov.usgs.dismodel.gui.events.RecenterEventFirer;
import gov.usgs.dismodel.gui.events.RecenterEventListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JToolBar;

public class WorldWindToolBar extends JToolBar {
	protected AllGUIVars allGuiVars;

	public WorldWindToolBar(AllGUIVars allGuiVars) {
		super();
		this.allGuiVars = allGuiVars;

		this.add(new SetOriginButton("Set origin",
				"/gov/usgs/dismodel/resources/target.png"));
		this.addSeparator();
		this.add(new CenterAtOriginButton("Center at the origin",
				"/gov/usgs/dismodel/resources/center.png"));
		this.add(new BoxZoomButton("Box zoom",
				"/gov/usgs/dismodel/resources/dragbox.gif"));
	}

	private class SetOriginButton extends IconButton {
		public SetOriginButton(String toolTip, String IconLocation) {
			super(toolTip, IconLocation);

		}

		@Override
		protected void buttonClicked() {
			OriginSetter originSetter = new OriginSetter(allGuiVars);
			originSetter.setVisible(true);
		}

	}

	private class CenterAtOriginButton extends IconButton implements
			RecenterEventFirer {
		private ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>();

		public CenterAtOriginButton(String toolTip, String IconLocation) {
			super(toolTip, IconLocation);
			this.addRecenterEventListener(allGuiVars.getMainFrame());
		}

		@Override
		protected void buttonClicked() {
			LLH origin = allGuiVars.getSimModel().getOrigin();
			allGuiVars.getDisplaySettings().setCenterOfMap(
					new LatLon(Angle.fromDegrees(origin.getLatitude().toDeg()),
							Angle.fromDegrees(origin.getLongitude().toDeg())));
			fireRecenterEvent();
		}

		private void fireRecenterEvent() {
			DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();
			for (RecenterEventListener listener : recenterListeners) {
				listener.recenterAfterChange(displaySettings);
			}
		}

		@Override
		public void addRecenterEventListener(RecenterEventListener listener) {
			recenterListeners.add(listener);

		}

		@Override
		public void removeRecenterEventListener(RecenterEventListener listener) {
			recenterListeners.remove(listener);
		}

	}

	private class BoxZoomButton extends IconButton {
		public BoxZoomButton(String toolTip, String IconLocation) {
			super(toolTip, IconLocation);

		}

		@Override
		protected void buttonClicked() {
			(new BoxZoomSelector()).enable();
		}

	}

	private class BoxZoomSelector extends SectorSelector implements
			ZoomEventFirer, RecenterEventFirer {
		ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();
		ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>();

		public BoxZoomSelector() {
			super(allGuiVars.getWwjPanel().getWorldWind());
			setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
			setBorderColor(new Color(1f, 0f, 0f, 0.5f));
			setBorderWidth(3);
			Dismodel2 mainFrame = allGuiVars.getMainFrame();
			this.addZoomListener(mainFrame);
			this.addRecenterEventListener(mainFrame);
			

			addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY,
					sectorChangeListener);
			
		}

		private PropertyChangeListener sectorChangeListener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Sector sector = (Sector) evt.getNewValue();
				Sector oldSector = (Sector) evt.getOldValue();
				if ((sector == null && oldSector != null)) {
					System.out.println("Selection made: " + oldSector);
					gov.nasa.worldwind.geom.LatLon[] corners = oldSector
							.getCorners();
					double latDist = gov.nasa.worldwind.geom.LatLon
							.ellipsoidalDistance(
									corners[0],
									corners[3],
									gov.nasa.worldwind.globes.Earth.WGS84_EQUATORIAL_RADIUS,
									gov.nasa.worldwind.globes.Earth.WGS84_POLAR_RADIUS);
					double lngDist = gov.nasa.worldwind.geom.LatLon
							.ellipsoidalDistance(
									corners[0],
									corners[1],
									gov.nasa.worldwind.globes.Earth.WGS84_EQUATORIAL_RADIUS,
									gov.nasa.worldwind.globes.Earth.WGS84_POLAR_RADIUS);
					double chartSpan = Math.max(latDist, lngDist);
					Position center = new Position(oldSector.getCentroid(), 0);
					
					DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();
					displaySettings.setCenterOfMap(center);
					displaySettings.setChartSpan(chartSpan);

					fireZoomNRecenter();
					
					BoxZoomSelector.this.disable();

				}
			};

		};
		

		
		private void fireZoomNRecenter(){
			DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();
			for (ZoomEventListener listener : zoomListeners) {
				listener.updateZoomLevelAfterSettingsChanged(displaySettings);
			}
			for (RecenterEventListener listener : recenterListeners) {
			    listener.recenterAfterChange(displaySettings);
			}

		}

		@Override
		public void addRecenterEventListener(RecenterEventListener listener) {
			recenterListeners.add(listener);
		}

		@Override
		public void removeRecenterEventListener(RecenterEventListener listener) {
			recenterListeners.remove(listener);
		}

		@Override
		public void addZoomListener(ZoomEventListener listener) {
			zoomListeners.add(listener);
		}

		@Override
		public void removeZoomListener(ZoomEventListener listener) {
			zoomListeners.remove(listener);
		}

	}

}

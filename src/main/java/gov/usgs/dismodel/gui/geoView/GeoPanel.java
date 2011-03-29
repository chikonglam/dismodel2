package gov.usgs.dismodel.gui.geoView;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.rules.ExpectedException;

 public class GeoPanel extends JPanel implements ZoomEventFirer, ZoomEventListener{
	 //constants
	 //-----
	 private static final double ZOOM_REFRESH_THRESHOLD = 0.10;

	private static final long serialVersionUID = -7898383357800550284L;
	private JPanel toolbar;
    final private WorldWindowGLJPanel wwd;
    private StatusBar statusBar;
    private SimulationDataModel simModel;
    private DisplayStateStore displaySettings;
    
    final private View wwView;
    
    //Event Listeners
    private ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();
    
    // Constructs a JPanel to hold the WorldWindow
    public GeoPanel(Dimension canvasSize, boolean includeStatusBar, SimulationDataModel simModel, DisplayStateStore displaySettings)
    {
        super(new BorderLayout());
        //state vars
        this.simModel = simModel;
        this.displaySettings = displaySettings;
        
        // Create the toolbar
        this.toolbar = new JPanel();
        this.add(toolbar, BorderLayout.NORTH);

        // Create the WorldWindow and set its preferred size.
        this.wwd = new WorldWindowGLJPanel();
        this.wwd.setPreferredSize(canvasSize);
        wwView = wwd.getView();

        // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
        this.setMinimumSize(new Dimension(0, 0));

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        //  Check the code below for click event handling
        //this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

        // Add the WorldWindow to this JPanel.
        this.add(this.wwd, BorderLayout.CENTER);

        // Add the status bar if desired.
        if (includeStatusBar)
        {
            statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            statusBar.setEventSource(wwd);
        }
        
        
        //making sure the right side zooms too
        wwView.setFieldOfView(Angle.fromDegrees(45));
        wwd.addRenderingListener(zoomLevelEventFirer);
        
        
        //setting the map at the default location, and default zoom level
        updateMapCenterAfterSettingsChanged(displaySettings);
        updateZoomLevelAfterSettingsChanged(displaySettings);
        
    }
    
    private RenderingListener zoomLevelEventFirer = new RenderingListener() {
    	@Override
        public void stageChanged(RenderingEvent event) {
    		SwingUtilities.invokeLater(threadedZoomLevelFirer);
    	}
    };
    
    private Runnable threadedZoomLevelFirer = new Runnable(){
    	public void run() { 
	    	final double wwAxisSpan = wwView.getEyePosition().getElevation();
	    	final double expectedAxisSpan = displaySettings.getChartSpan();
	    	
	    	if (Math.abs(( wwAxisSpan - expectedAxisSpan ) / expectedAxisSpan) > ZOOM_REFRESH_THRESHOLD){
	    		displaySettings.setChartSpan(wwAxisSpan);
	    		for(ZoomEventListener listener : zoomListeners){
	    			listener.updateZoomLevelAfterSettingsChanged(displaySettings);
	    		}
	    	}
    	}
    	
    };
    
    
    

	
	//zoom event handler
	@Override
	public void updateZoomLevelAfterSettingsChanged(DisplayStateStore displaySettings) {
		SwingUtilities.invokeLater(threadedupdateZoomLevel);
	}
	
	private Runnable threadedupdateZoomLevel = new Runnable() {
		
		@Override
		public void run() {
			double expectedAxisSpan = displaySettings.getChartSpan();
			final double wwAxisSpan = wwView.getEyePosition().getElevation();
			if (Math.abs(( wwAxisSpan - expectedAxisSpan ) / expectedAxisSpan) < ZOOM_REFRESH_THRESHOLD){
				return;
			}
			
			if (wwView.isAnimating()) {
	            wwView.stopAnimations();
	        }
	        Position pos = ((BasicOrbitView)wwView).getCenterPosition();
	        
	        Position tmpPos = Position.fromRadians(pos.getLatitude().radians, pos
	                .getLongitude().radians, expectedAxisSpan);
	        System.out.println("Zooming to axis span=" + expectedAxisSpan );		////debug
	        wwView.setEyePosition(tmpPos);
	        
	        wwd.redraw();

		}
	};


	//handlers zoomListener
	@Override
	public void addZoomListener(ZoomEventListener listener) {
		zoomListeners.add(listener);
	}


	@Override
	public void removeZoomListener(ZoomEventListener listener) {
		zoomListeners.remove(listener);
	}
	
    public void updateMapCenterAfterSettingsChanged(DisplayStateStore displaySettings) {
    	SwingUtilities.invokeLater(threadedUpdateMapCenter);
    }
    
    private Runnable threadedUpdateMapCenter = new Runnable() {
		@Override
		public void run() {
	    	LatLon centerLL = displaySettings.getCenterOfMap();
	    	Position center = new Position(centerLL, 0);
	        BasicOrbitView view = (BasicOrbitView) wwView;
	        view.setFieldOfView(Angle.fromDegrees(45));
	        view.setCenterPosition(center);
	        
	        wwd.redraw();
		}
	};
    
    
}





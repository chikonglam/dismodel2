package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.events.GeoPosClickFrier;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.gui.events.ZoomEventFirer;
import gov.usgs.dismodel.gui.events.ZoomEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.media.opengl.glu.GLU;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartView;
import org.jzy3d.chart.controllers.mouse.ChartMouseController;
import org.jzy3d.chart.controllers.mouse.MouseUtilities;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.rendering.view.Camera;

    public class JzyMouseListener extends ChartMouseController implements ZoomEventFirer, GeoPosClickFrier{
    	private final ENUPanel enuPanel;
    	private final DisplayStateStore displaySettings;
    	private final SimulationDataModel simModel;
    	private boolean dragXy = false;
		final private Chart chart;
		
		

    	//listeners
    	ArrayList<ZoomEventListener> zoomListeners = new ArrayList<ZoomEventListener>();
    	ArrayList<GeoPosClickListener> geoPosListeners = new ArrayList<GeoPosClickListener>();

    	
        public JzyMouseListener(ENUPanel enuPanel, DisplayStateStore displaySettings, SimulationDataModel simModel) {
    		super();
    		this.enuPanel = enuPanel;
    		this.displaySettings = displaySettings;
    		this.simModel = simModel;
    		this.chart = enuPanel.getChart();
    	}

		@Override
    	public void mousePressed(MouseEvent e) {
			if (MouseUtilities.isDoubleClick(e)){
				dragXy = !dragXy;					//TODO remove this after the GUI is ready
				System.out.println("Drag mode on:" + dragXy);
			} else if(MouseUtilities.isLeftDown(e)){
    			int mouseX = e.getX();
    			int mouseY = e.getY();
    			
    			//fire click event
    			fireGeoPosClick(mouseX, mouseY);
    			
    		}
    			
    		prevMouse.x  = e.getX();
    		prevMouse.y  = e.getY();
    	}
        
        private void fireGeoPosClick(int mouseX, int mouseY) {
        	ChartView view = this.chart.getView();
        	Camera camera = view.getCamera();
        	Coord3d screenXY = new Coord3d(mouseX, mouseY, 0d);
        	Coord3d modelCoord = camera.screenToModel(view.getCurrentGL(), new GLU(), screenXY);
        	double modelX = (double)modelCoord.x ;
        	double modelY = 2.0* displaySettings.getyCenter() - ((double) modelCoord.y) ;

        	LLH origin = simModel.getOrigin();
        	LocalENU pointClicked = new LocalENU( modelX, modelY, 0d, origin );
        	LLH pointLLH = pointClicked.toLLH();
        	System.out.println("[mouse click @ x=" + mouseX + " y=" + mouseY +"] = E:"+ modelX + " N:" + modelY );

        	
        	for (GeoPosClickListener listener : this.geoPosListeners){
        		listener.LocalENUClicked(pointClicked);
        		listener.latLonClicked(pointLLH);
        	}
        	
		}

		@Override
    	protected void zoom(final float factor){
    		double oldAxisSpan = displaySettings.getChartSpan();
    		double newAxisSpan = factor * oldAxisSpan;
    		displaySettings.setChartSpan(newAxisSpan);
    		enuPanel.setAxesFromDisplaySettings();
    		for (ZoomEventListener listener : zoomListeners) {
    			listener.updateZoomLevelAfterSettingsChanged(displaySettings); 
    		}
    	}
    	
        @Override
    	public void mouseDragged(MouseEvent e) {
    		Coord2d mouse = new Coord2d(e.getX(),e.getY());
    		if(MouseUtilities.isLeftDown(e)){
    			if (dragXy){			//drag
        			Coord2d move  = mouse.sub(prevMouse);		//TODO make drag better
        			if( move.x != 0d &&  move.y != 0d ){
        				double x = displaySettings.getxCenter();
        				double y = displaySettings.getyCenter();
        				double axisSpan = displaySettings.getChartSpan();
        				
        				Dimension parentSize = enuPanel.getSize();
        				double width = parentSize.getWidth();
        				double height = parentSize.getHeight();
        				
        				double newX = x + move.x / width * axisSpan;
        				double newY = y - move.y / height * axisSpan;
        				
        				displaySettings.setxCenter(newX);
        				displaySettings.setyCenter(newY);

        				enuPanel.setAxesFromDisplaySettings();
        			}
    			} else {				// Rotate
	    			Coord2d move  = mouse.sub(prevMouse).div(100);
	    			rotate( move );
    			}
    		}

    		prevMouse = mouse;
    	}

    	@Override
    	public void addZoomListener(ZoomEventListener listener) {
    		zoomListeners.add(listener);
    	}

    	@Override
    	public void removeZoomListener(ZoomEventListener listener) {
    		zoomListeners.remove(listener);
    	}
    	
    	//getters and setters
    	//--------------------
        public boolean isDragXy() {
			return dragXy;
		}

		public void setDragXy(boolean dragXy) {
			this.dragXy = dragXy;
		}

		@Override
		public void addGeoPosClickListener(GeoPosClickListener listener) {
			geoPosListeners.add(listener);
			
		}

		@Override
		public void removeGeoPosClickListener(GeoPosClickListener listener) {
			geoPosListeners.remove(listener);
		}


    }
package gov.usgs.dismodel.gui.geoView;

import java.util.ArrayList;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;
import gov.usgs.dismodel.gui.events.RecenterEventFirer;
import gov.usgs.dismodel.gui.events.RecenterEventListener;
import gov.usgs.dismodel.state.DisplayStateStore;

import javax.swing.JToolBar;

public class WorldWindToolBar extends JToolBar{
    protected AllGUIVars allGuiVars;
    
    public WorldWindToolBar(AllGUIVars allGuiVars){
        super();
        this.allGuiVars = allGuiVars;
        
        this.add( new SetOriginButton("Set origin", "/gov/usgs/dismodel/resources/target.png") );
        this.addSeparator();
        this.add( new CenterAtOriginButton("Center at the origin", "/gov/usgs/dismodel/resources/center.png") );
        this.add( new BoxZoomButton("Box zoom", "/gov/usgs/dismodel/resources/dragbox.gif") );
    }
    
    
    private class SetOriginButton extends IconButton{
        public SetOriginButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            
        }
        @Override
        protected void buttonClicked() {
        	OriginSetter originSetter = new OriginSetter(allGuiVars);
        	originSetter.setVisible(true);
        }
        
    }
    
    private class CenterAtOriginButton extends IconButton implements RecenterEventFirer{
    	private ArrayList<RecenterEventListener> recenterListeners = new ArrayList<RecenterEventListener>(); 
    	
        public CenterAtOriginButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            this.addRecenterEventListener(allGuiVars.getMainFrame());
        }
        @Override
        protected void buttonClicked() {
        	LLH origin = allGuiVars.getSimModel().getOrigin();
        	allGuiVars.getDisplaySettings().setCenterOfMap(
        			new LatLon(Angle.fromDegrees(origin.getLatitude().toDeg()), Angle.fromDegrees(origin.getLongitude().toDeg())));
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
    
    private class BoxZoomButton extends IconButton{
        public BoxZoomButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            
        }
        @Override
        protected void buttonClicked() {
            
        }
        
    }
    
    
    

}

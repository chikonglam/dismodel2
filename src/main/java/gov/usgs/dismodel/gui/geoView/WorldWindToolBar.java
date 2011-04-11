package gov.usgs.dismodel.gui.geoView;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;

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
            
        }
        
    }
    
    private class CenterAtOriginButton extends IconButton{
        public CenterAtOriginButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            
        }
        @Override
        protected void buttonClicked() {
            
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

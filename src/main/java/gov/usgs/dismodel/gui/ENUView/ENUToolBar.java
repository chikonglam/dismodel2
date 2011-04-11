package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;

import javax.swing.JToolBar;

public class ENUToolBar extends JToolBar{
    protected AllGUIVars allGuiVars;

    public ENUToolBar(AllGUIVars allGuiVars) {
        super();
        this.allGuiVars = allGuiVars;
        
        this.add( new SnapToXyButton("Snap to X Y", "/gov/usgs/dismodel/resources/cartesian.png") );
        this.add( new DragXyButton("Drag X Y space", "/gov/usgs/dismodel/resources/hand.png") );
        this.addSeparator();
        this.add( new ForwardModelButton("Forward model", "/gov/usgs/dismodel/resources/forward.png") );
        this.add( new SolveButton("Solve", "/gov/usgs/dismodel/resources/equals.png") );
        this.add( new CrossValButton("Cross validate", "/gov/usgs/dismodel/resources/cross_val.png"));
        
    }
    
    private class SnapToXyButton extends IconButton{

        public SnapToXyButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private class DragXyButton extends IconButton{

        public DragXyButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private class ForwardModelButton extends IconButton{

        public ForwardModelButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private class SolveButton extends IconButton{

        public SolveButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private class CrossValButton extends IconButton{

        public CrossValButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
    
    
}

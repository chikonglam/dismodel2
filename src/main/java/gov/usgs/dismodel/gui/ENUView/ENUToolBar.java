package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.inversion.CrossValidator2;
import gov.usgs.dismodel.calc.inversion.DistSlipSolveWorker;
import gov.usgs.dismodel.calc.inversion.SASolveWorker;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;

import javax.swing.JOptionPane;
import javax.swing.JToolBar;

public class ENUToolBar extends JToolBar{
    protected AllGUIVars allGuiVars;
    protected SimulationDataModel simModel;

    public ENUToolBar(AllGUIVars allGuiVars) {
        super();
        this.allGuiVars = allGuiVars;
        this.simModel = allGuiVars.getSimModel();
        
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
            if (!simModel.isDistributedFaultProblem()) {
                SASolveWorker saSolver = new SASolveWorker(allGuiVars);
                saSolver.execute();
            } else {
                DistSlipSolveWorker dsSolver = new DistSlipSolveWorker(allGuiVars);
                dsSolver.execute();
            }
            
        }
        
    }
    
    private class CrossValButton extends IconButton implements GuiUpdateRequestListener{

        public CrossValButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void buttonClicked() {
            CrossValidator2 cvSolver = new CrossValidator2(simModel, simModel.getOrigin());
            
            //TODO multithread this
            double minCvssGam = cvSolver.calculate();
            JOptionPane.showMessageDialog(allGuiVars.getMainFrame(),
                    "CVSS is min when Gamma is: " + minCvssGam,
                    "Cross Validation Result",
                    JOptionPane.ERROR_MESSAGE);
            return;
            
        }

        @Override
        public void guiUpdateAfterStateChange() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
    
    
}

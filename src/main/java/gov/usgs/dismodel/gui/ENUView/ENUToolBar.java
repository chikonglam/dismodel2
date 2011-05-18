package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.calc.ForwardModel;
import gov.usgs.dismodel.calc.inversion.CVResult;
import gov.usgs.dismodel.calc.inversion.CrossValidator3;
import gov.usgs.dismodel.calc.inversion.DistSlipSolveWorker;
import gov.usgs.dismodel.calc.inversion.SASolveWorker;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.components.IconButton;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JToolBar;

public class ENUToolBar extends JToolBar {
    protected AllGUIVars allGuiVars;
    protected SimulationDataModel simModel;

    public ENUToolBar(AllGUIVars allGuiVars) {
        super();
        this.allGuiVars = allGuiVars;
        this.simModel = allGuiVars.getSimModel();

        this.add(new SnapToXyButton("Snap to X Y", "/gov/usgs/dismodel/resources/cartesian.png"));
        this.add(new DragXyButton("Toggle 'drag XY mode' ", "/gov/usgs/dismodel/resources/hand.png"));
        this.addSeparator();
        this.add(new ForwardModelButton("Forward model", "/gov/usgs/dismodel/resources/forward.png"));
        this.add(new SolveButton("Solve", "/gov/usgs/dismodel/resources/equals.png"));
        this.add(new CrossValButton("Cross validate", "/gov/usgs/dismodel/resources/cross_val.png"));

    }

    private class SnapToXyButton extends IconButton {

        public SnapToXyButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
        }

        @Override
        protected void buttonClicked() {
            allGuiVars.getEnuPanel().snapToXy();
        }

    }

    private class DragXyButton extends IconButton {

        public DragXyButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
        }

        @Override
        protected void buttonClicked() {
            allGuiVars.getEnuPanel().toggleDragMode();
        }

    }

    private class ForwardModelButton extends IconButton implements DataChangeEventFrier {
        private ArrayList<DataChangeEventListener> dataChangeListeners = new ArrayList<DataChangeEventListener>();

        public ForwardModelButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
            this.addDataChangeEventListener(allGuiVars.getMainFrame());
        }

        @Override
        protected void buttonClicked() {
            boolean allFixed = ForwardModel.forwardAllFixedSrcs(simModel);
            fireDataChangeEvent();
            
            if (!allFixed) {
                JOptionPane.showMessageDialog(allGuiVars.getMainFrame(),
                        "Some sources are not fixed; only fixed sources will be modeled.", "Not all sources are fixed",
                        JOptionPane.WARNING_MESSAGE);
            }

            
        }

        private void fireDataChangeEvent() {
            for (DataChangeEventListener listener : dataChangeListeners) {
                listener.updateAfterDataChange();
            }

        }

        @Override
        public void addDataChangeEventListener(DataChangeEventListener listener) {
            dataChangeListeners.add(listener);

        }

        @Override
        public void removeDataChangeEventListener(DataChangeEventListener listener) {
            dataChangeListeners.remove(listener);

        }

    }

    private class SolveButton extends IconButton {

        public SolveButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
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

    private class CrossValButton extends IconButton implements GuiUpdateRequestListener {

        public CrossValButton(String toolTip, String IconLocation) {
            super(toolTip, IconLocation);
        }

        @Override
        protected void buttonClicked() {
            CrossValidator3 cvSolver = new CrossValidator3(simModel, simModel.getOrigin());

            // TODO multithread this
            CVResult result = cvSolver.crossValidate();
            JOptionPane.showMessageDialog(allGuiVars.getMainFrame(), "CVSS is min when Gamma is: " + result.getGam() + "\n  (CVSS=" + result.getCvss() + ")",
                    "Cross Validation Result", JOptionPane.ERROR_MESSAGE);
            return;

        }

        @Override
        public void guiUpdateAfterStateChange() {
            // TODO Auto-generated method stub

        }

    }

}

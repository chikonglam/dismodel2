package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.calc.inversion.CVResult;
import gov.usgs.dismodel.calc.inversion.CrossValidator3;
import gov.usgs.dismodel.calc.inversion.DistSlipSolveWorker;
import gov.usgs.dismodel.calc.inversion.SASolveWorker;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

public class CrossValMenuItem extends ActionMenuItem implements GuiUpdateRequestListener {
    private static final long serialVersionUID = -33958112440444633L;

    private SimulationDataModel simModel;

    public CrossValMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.simModel = allGuiVars.getSimModel();

        allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
	CrossValidator3 cvSolver = new CrossValidator3(simModel, simModel.getOrigin());

        // TODO multithread this
        CVResult result = cvSolver.crossValidate();
        JOptionPane.showMessageDialog(allGuiVars.getMainFrame(), "CVSS is min when Gamma is: " + result.getGam() + "\n  (CVSS=" + result.getCvss() + ")",
                "Cross Validation Result", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    @Override
    public void guiUpdateAfterStateChange() {
        //TODO implement this later
    }
    

}


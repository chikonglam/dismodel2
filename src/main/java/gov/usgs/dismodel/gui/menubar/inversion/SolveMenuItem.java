package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.inversion.DistSlipSolveWorker;
import gov.usgs.dismodel.calc.inversion.SASolveWorker;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

import java.awt.event.ActionEvent;

public class SolveMenuItem extends ActionMenuItem implements GuiUpdateRequestListener {
    private static final long serialVersionUID = -33958112440444633L;

    private SimulationDataModel simModel;

    public SolveMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.simModel = allGuiVars.getSimModel();

        allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        if (!simModel.isDistributedFaultProblem()) {
            SASolveWorker saSolver = new SASolveWorker(allGuiVars);
            saSolver.execute();
        } else {
            DistSlipSolveWorker dsSolver = new DistSlipSolveWorker(allGuiVars);
            dsSolver.execute();
        }
    }

    @Override
    public void guiUpdateAfterStateChange() {
        //TODO implement this later
    }
    

}

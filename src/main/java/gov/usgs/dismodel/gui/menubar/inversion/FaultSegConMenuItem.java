package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.calc.inversion.dialogs.FaultConnectionSpecDialog;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;

public class FaultSegConMenuItem extends ActionMenuItem{

    private static final long serialVersionUID = 6180580451203736456L;

    public FaultSegConMenuItem (String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SimulationDataModel simModel = allGuiVars.getSimModel();
        FaultConnectionSpecDialog connDiag = new FaultConnectionSpecDialog(allGuiVars.getMainFrame(), true, simModel);
        connDiag.setVisible(true);
    }
}

package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.inversion.dialogs.DistSlipConstDialog;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

import java.awt.event.ActionEvent;

public class DistSlipConsMenuItem extends ActionMenuItem{

    public DistSlipConsMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SimulationDataModel simModel = allGuiVars.getSimModel();
        DistSlipConstDialog dscWin = new DistSlipConstDialog(simModel);
        dscWin.setVisible(true);
    }

    
}

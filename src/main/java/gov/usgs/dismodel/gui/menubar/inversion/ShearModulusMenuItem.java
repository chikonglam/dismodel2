package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.calc.inversion.dialogs.ShearModulusDialogBase;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;

public class ShearModulusMenuItem extends ActionMenuItem{

    public ShearModulusMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SimulationDataModel simModel = allGuiVars.getSimModel();
        ShearModulusDialogBase smd = new ShearModulusDialogBase();
        smd.setVisible(true);
        //TODO tie into other parts
    }

    
}

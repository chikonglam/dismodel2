package gov.usgs.dismodel.gui.menubar.source;

import gov.usgs.dismodel.calc.greens.dialogs.DislocationDialogRestorable;
import gov.usgs.dismodel.calc.greens.dialogs.MogiSourceDialog2;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

import java.awt.event.ActionEvent;

public class DislocationMenuItem extends ActionMenuItem{
    public DislocationMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        DislocationDialogRestorable dialog = new DislocationDialogRestorable(allGuiVars.getMainFrame(), "Add a Dislocation", allGuiVars);
        dialog.setVisible(true);
    }

}

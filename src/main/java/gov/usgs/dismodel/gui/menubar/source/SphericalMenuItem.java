package gov.usgs.dismodel.gui.menubar.source;

import gov.usgs.dismodel.calc.greens.dialogs.SphericalSourceDialog2;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

import java.awt.event.ActionEvent;

public class SphericalMenuItem extends ActionMenuItem {

    public SphericalMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SphericalSourceDialog2 dialog = new SphericalSourceDialog2(allGuiVars.getMainFrame(), "Add a Mogi", allGuiVars);
        dialog.setVisible(true);
    }
}

package gov.usgs.dismodel.gui.menubar.file;

import java.awt.event.ActionEvent;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;

public class ExitMenuItem extends ActionMenuItem{

    public ExitMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        System.exit(0);
    }

}

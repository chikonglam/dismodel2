package gov.usgs.dismodel.gui.menubar.data;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;

import java.awt.event.ActionEvent;

public class ProcessGreensFilesMenuItem extends DataChangingMenuItem {
    private static final long serialVersionUID = -2785317942959011686L;

    public ProcessGreensFilesMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        //TODO implement this
    }

}

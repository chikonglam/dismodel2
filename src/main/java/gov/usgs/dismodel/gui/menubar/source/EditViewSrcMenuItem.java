package gov.usgs.dismodel.gui.menubar.source;

import gov.usgs.dismodel.calc.greens.dialogs.SourceViewer;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

import java.awt.event.ActionEvent;

public class EditViewSrcMenuItem extends ActionMenuItem {

    public EditViewSrcMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SourceViewer editDialog = new SourceViewer(allGuiVars.getMainFrame(), "Edit / View Sources", allGuiVars);
        editDialog.setVisible(true);
        
    }


}

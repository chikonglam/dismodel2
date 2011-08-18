package gov.usgs.dismodel.gui.menubar.file;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

public class ExportKMLMenuItem extends ActionMenuItem{
    public ExportKMLMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
	try {
	    SaveAndLoad.exportToKML(allGuiVars);
        } catch (FileNotFoundException e1) {
	    e1.printStackTrace();
        }
	
    }

}

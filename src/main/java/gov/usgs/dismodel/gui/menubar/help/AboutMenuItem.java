package gov.usgs.dismodel.gui.menubar.help;

import java.awt.event.ActionEvent;

import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.help.AboutDismodel;

public class AboutMenuItem extends ActionMenuItem {

    private static final long serialVersionUID = -1422394576995268002L;

    public AboutMenuItem(String title, AllGUIVars allGuiVars) {
	super(title, allGuiVars);

    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
	AboutDismodel ad = new AboutDismodel();
	ad.setVisible(true);
    }

}

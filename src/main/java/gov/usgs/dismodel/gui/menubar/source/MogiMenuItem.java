package gov.usgs.dismodel.gui.menubar.source;

import java.awt.event.ActionEvent;

import gov.usgs.dismodel.calc.greens.dialogs.MogiSourceDialog2;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

public class MogiMenuItem extends ActionMenuItem{
	public MogiMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
	}

	@Override
	public void menuItemClickAction(ActionEvent e) {
        MogiSourceDialog2 dialog = new MogiSourceDialog2(allGuiVars.getMainFrame(), "Add a Mogi", allGuiVars);
        dialog.setVisible(true);		
	}

	
}

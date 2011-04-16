package gov.usgs.dismodel.gui.menubar.file;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LoadProjectMenuItem extends DataChangingMenuItem{

	public LoadProjectMenuItem(String title, AllGUIVars allGuiVars) {
		super(title, allGuiVars);
		this.addDataChangeEventListener(allGuiVars.getMainFrame());
	}

	@Override
	public void menuItemClickAction(ActionEvent e) {
		JFrame frame = allGuiVars.getMainFrame();
        try {
            SimulationDataModel newSimModel = SaveAndLoad.loadProject(frame);
            SimulationDataModel oldSimModel = allGuiVars.getSimModel();
            oldSimModel.replaceWith(newSimModel);
            
            fireDataChangeEvent();
        } catch (Exception exp) {
        	exp.printStackTrace();
            JOptionPane.showMessageDialog(frame, exp.getMessage(),
                    "Load error", JOptionPane.ERROR_MESSAGE);
        }
		
	}

}

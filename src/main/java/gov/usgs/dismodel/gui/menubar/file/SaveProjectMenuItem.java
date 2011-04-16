package gov.usgs.dismodel.gui.menubar.file;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;

public class SaveProjectMenuItem extends ActionMenuItem{

    public SaveProjectMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        JFrame mainFrame = allGuiVars.getMainFrame();
        SimulationDataModel simModel = allGuiVars.getSimModel();
        
        try {
            SaveAndLoad.saveProject(mainFrame, simModel);
        } catch (Exception exp) {
            exp.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, exp.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

}

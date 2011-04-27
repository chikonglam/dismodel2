package gov.usgs.dismodel.gui.menubar.inversion;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import gov.usgs.dismodel.calc.ForwardModel;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

public class ForwardModelMenuItem extends DataChangingMenuItem{

    public ForwardModelMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        SimulationDataModel simModel = allGuiVars.getSimModel();
        boolean allFixed = ForwardModel.forwardAllFixedSrcs(simModel);
        
        fireDataChangeEvent();
        
        if (!allFixed) {
            JOptionPane.showMessageDialog(allGuiVars.getMainFrame(),
                    "Some sources are not fixed; only fixed sources will be modeled.", "Not all sources are fixed",
                    JOptionPane.WARNING_MESSAGE);
        }
        
        
    }
    

}

package gov.usgs.dismodel.gui.menubar.inversion;

import gov.usgs.dismodel.SmoothingDialog;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.GuiUpdateRequestListener;
import gov.usgs.dismodel.gui.menubar.ActionMenuItem;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

public class SmoothingMenuItem extends ActionMenuItem implements GuiUpdateRequestListener {

    private SimulationDataModel simModel;
    private SmoothingDialog dialog;

    public SmoothingMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.simModel = allGuiVars.getSimModel();

        allGuiVars.getMainFrame().addGuiUpdateRequestListener(this);
    }

    @Override
    public void guiUpdateAfterStateChange() {
        // TODO Auto-generated method stub

    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        dialog = new SmoothingDialog(allGuiVars.getMainFrame(), true);
        dialog.setParams(simModel.getSmoothingParams());
        dialog.getButtonOkay().addActionListener(diaLogOKListener);
        dialog.setVisible(true);
    }

    ActionListener diaLogOKListener = new ActionListener() {    //XXX if time allows, move this logic back into the dialog itself
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder errorMessageOut = new StringBuilder();
            if (dialog.areParamsValid(errorMessageOut)) {
                simModel.setCrossValidationParams(dialog);
            } else {
                JOptionPane.showMessageDialog(allGuiVars.getMainFrame(), errorMessageOut, "Cross Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    };
}

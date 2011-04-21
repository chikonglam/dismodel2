package gov.usgs.dismodel.gui.menubar.map;

import gov.usgs.dismodel.gui.VectorScaleAndColorChooser;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.menubar.DataChangingMenuItem;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class AdjMeasVectorsMenuItem extends DataChangingMenuItem {

    public AdjMeasVectorsMenuItem(String title, AllGUIVars allGuiVars) {
        super(title, allGuiVars);
        this.addDataChangeEventListener(allGuiVars.getMainFrame());
    }

    @Override
    public void menuItemClickAction(ActionEvent e) {
        JFrame frame = allGuiVars.getMainFrame();
        final DisplayStateStore displaySettings = allGuiVars.getDisplaySettings();

        final VectorScaleAndColorChooser chooser = new VectorScaleAndColorChooser(frame, displaySettings,
                displaySettings.getRealDisplacementVectorColor());

        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                displaySettings.setRealDisplacementVectorColor(chooser.getColor());
                int vectorScale = chooser.getScale();
                displaySettings.setDisplacementVectorScale(vectorScale);
                chooser.dispose();
                fireDataChangeEvent();
            }
        };
        chooser.getButtonOkay().addActionListener(okListener);
        
        chooser.setVisible(true);
    }
}

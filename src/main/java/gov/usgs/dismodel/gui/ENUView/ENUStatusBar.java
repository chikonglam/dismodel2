package gov.usgs.dismodel.gui.ENUView;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ENUStatusBar extends JPanel implements DataChangeEventListener {
    private static final long serialVersionUID = -3393413941160757828L;
    private static final Dimension MIN_SIZE = new Dimension(0, 8);

    private String label = " ";
    private JLabel status = new JLabel(label);
    private AllGUIVars allGUIVars;

    public ENUStatusBar(AllGUIVars allGUIVars) {
        super(new BorderLayout());
        this.add(status, BorderLayout.CENTER);
        this.setMinimumSize(MIN_SIZE);
        this.allGUIVars = allGUIVars;
    }

    @Override
    public void updateAfterDataChange() {
        SwingUtilities.invokeLater(updateThread);
    }

    private Runnable updateThread = new Runnable() {
        public void run() {
            SimulationDataModel simModel = allGUIVars.getSimModel();
            label = " ";

            if (simModel.getModeledDisplacements().size() > 0) {
                label += String.format("Chi^2=%.3e  ", simModel.getChi2());
            }

            if (simModel.getRefH() != 0d) {
                label += String.format("RefH=%.2f  ", simModel.getRefH());
            }

            int modelsAdded = simModel.getSourceModels().size();
            int modelsDisped = simModel.getFittedModels().size();
            if (modelsAdded > 0 || modelsDisped > 0) {
                label += String.format("#Models=%d(%d drawn)  ", modelsAdded, modelsDisped);
            }

            status.setText(label);
        }
    };

}

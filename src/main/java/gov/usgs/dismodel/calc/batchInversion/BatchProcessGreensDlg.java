package gov.usgs.dismodel.calc.batchInversion;

import gov.usgs.dismodel.OkayDialog;
import gov.usgs.dismodel.SaveAndLoad;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to configure the calculation of sub-fault slips from measured
 * displacement data.
 * 
 * @author cforden
 */
public class BatchProcessGreensDlg extends OkayDialog {

    private static final long serialVersionUID = 4934818406772298456L;

    /*
     * Instance variables
     * ***********************************************************************
     */

    private JCheckBox procAllFiles;
    private JCheckBox quadProgCkBx; // Use EqualityAndBoundsSlipSolver
    // Constrain all subfaults to be non-negative
    private JCheckBox allBlksNonNegCkBx;
    private JCheckBox useMomentFile;

    private JButton loadGreensFunctBtn;

    File greensFile = null;
    JFrame frame;
    SimulationDataModel simModel;

    /*
     * Methods
     * ******************************************************************
     * *****************
     */

    public BatchProcessGreensDlg(JFrame owner, String title, boolean modal, final SimulationDataModel simModel) {
        super(owner, title, modal);
        frame = owner;
        this.simModel = simModel;

        getButtonCancel().setVisible(false);

        quadProgCkBx = new JCheckBox("Quadratic program");
        quadProgCkBx.setAlignmentY(TOP_ALIGNMENT);
        quadProgCkBx.setAlignmentX(LEFT_ALIGNMENT);
        quadProgCkBx.setSelected(true);
        quadProgCkBx.setEnabled(false);

        allBlksNonNegCkBx = new JCheckBox("Constrain all subfaults to be " + "non-negative");
        allBlksNonNegCkBx.setAlignmentY(TOP_ALIGNMENT);
        allBlksNonNegCkBx.setSelected(false);

        useMomentFile = new JCheckBox("Use constraint files");
        useMomentFile.setAlignmentY(TOP_ALIGNMENT);
        useMomentFile.setAlignmentX(LEFT_ALIGNMENT);
        useMomentFile.setSelected(false);

        procAllFiles = new JCheckBox("Process all Gmatrix_ and matching data " + "files");
        procAllFiles.setAlignmentY(BOTTOM_ALIGNMENT);
        procAllFiles.setAlignmentX(LEFT_ALIGNMENT);
        procAllFiles.setSelected(false);
        procAllFiles.setToolTipText("Process all sets of files in the directory "
                + "from which you load the Green's function file");

        loadGreensFunctBtn = new JButton("Load Green's function(s)...");
        loadGreensFunctBtn.setAlignmentY(BOTTOM_ALIGNMENT);
        loadGreensFunctBtn.setAlignmentX(LEFT_ALIGNMENT);
        loadGreensFunctBtn.setToolTipText("Navigate to a file whose name begins "
                + "with \"Gmatrix_\", specifying a Green's function.");
        loadGreensFunctBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                greensFile = SaveAndLoad.loadFile(frame);
                try {
                    simModel.setAndRead1stGreensFile(greensFile);
                    getButtonOkay().setText("Solve");
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel body = new JPanel();
        final int INSET = 25;
        body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
        body.setBorder(new EmptyBorder(INSET, INSET, INSET, INSET));

        body.add(quadProgCkBx);
        JPanel quadProgPanel = new JPanel();
        quadProgPanel.setLayout(new BoxLayout(quadProgPanel, BoxLayout.PAGE_AXIS));
        quadProgPanel.setBorder(new EmptyBorder(0, INSET, 0, 0));
        quadProgPanel.add(allBlksNonNegCkBx);
        quadProgPanel.add(useMomentFile);
        quadProgPanel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(quadProgPanel);

        body.add(Box.createVerticalGlue());
        body.add(procAllFiles);
        body.add(Box.createVerticalGlue());
        body.add(loadGreensFunctBtn);

        this.add(body, BorderLayout.WEST);

    }

    public boolean quadProgCkBx() {
        return quadProgCkBx.isSelected();
    }

    public boolean allBlksNonNegCkBx() {
        return allBlksNonNegCkBx.isSelected();
    }

    public boolean useMomentFile() {
        return useMomentFile.isSelected();
    }

    public boolean procAllFiles() {
        return procAllFiles.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (getButtonOkay() == e.getSource()) {
            getButtonOkay().setText("Okay");
        }
    }

}

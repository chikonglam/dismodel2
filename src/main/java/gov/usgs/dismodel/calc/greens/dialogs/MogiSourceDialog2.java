package gov.usgs.dismodel.calc.greens.dialogs;

import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.RestorableSourceDialog;
import gov.usgs.dismodel.SourceDialogUtils;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.MogiPoint;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.SimulationDataModel;

import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class MogiSourceDialog2 extends MogiSourceDialogBase implements RestorableSourceDialog, GeoPosClickListener,
        DataChangeEventFrier {

    private boolean createNew = true;
    private int modelIndex = -1;
    private boolean unitsAreXY = true;
    private javax.swing.JTextField filledXField = null;
    private javax.swing.JTextField filledYField = null;
    private AllGUIVars allGuiVars;
    private SimulationDataModel simModel;
    private ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();

    /**
	 * 
	 */
    private static final long serialVersionUID = -188080137648952347L;

    public MogiSourceDialog2(Window owner, String title, AllGUIVars allGuiVars) {
        super(owner, title);
        setTitle(title);
        this.allGuiVars = allGuiVars;

        this.simModel = allGuiVars.getSimModel();

        Dismodel2 mainFrame = allGuiVars.getMainFrame();
        mainFrame.addGeoPosClickListener(this);
        this.addDataChangeEventListener(mainFrame);

    }

    public MogiSourceDialog2(Window owner, String title, int modelIndex, AllGUIVars allGuiVars) {
        this(owner, title, allGuiVars);
        this.modelIndex = modelIndex;
        this.createNew = false;

        MogiPoint curMogi = (MogiPoint) simModel.getSourceModels().get(modelIndex);
        MogiPoint curLB = (MogiPoint) simModel.getSourceLowerbound().get(modelIndex);
        MogiPoint curUB = (MogiPoint) simModel.getSourceUpperbound().get(modelIndex);

        SourceDialogUtils.parseValsIntoUIBoxes(curMogi.getEast(), curLB.getEast(), curUB.getEast(), CBFixX, txtValX,
                txtLBX, txtUBX);
        SourceDialogUtils.parseValsIntoUIBoxes(curMogi.getNorth(), curLB.getNorth(), curUB.getNorth(), CBFixY, txtValY,
                txtLBY, txtUBY);
        SourceDialogUtils.parseValsIntoUIBoxes(-curMogi.getUp(), -curUB.getUp(), -curLB.getUp(), CBFixC, txtValC,
                txtLBC, txtUBC);

        txtVolChg.setText(String.format("%.2f", curMogi.getVolumeChange()));

        String name = curMogi.getName();
        if (name != null && !name.isEmpty()) {
            txtName.setText(name);
        }
    }

    public boolean boundsAreValid() {
        // TODO make it really check the bounds
        return true;
    }

    public MogiPoint getSource() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;
        double volChange; // Change this line for UB and LB

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);
        volChange = parseJTextField(txtVolChg); // Change this line for UB and
                                                // LB
        if (Double.isNaN(volChange))
            volChange = 0d;

        MogiPoint sourceRtn = new MogiPoint(east.number, north.number, -depth.number, volChange); // Change
                                                                                                  // this
                                                                                                  // line
                                                                                                  // for
                                                                                                  // UB
                                                                                                  // and
                                                                                                  // LB
        return sourceRtn;
    }

    public MogiPoint getUpperBound() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);

        MogiPoint sourceRtn = new MogiPoint(east.ub, north.ub, -depth.lb, Double.MAX_VALUE); // Change
                                                                                             // this
                                                                                             // line
                                                                                             // for
                                                                                             // UB
                                                                                             // and
                                                                                             // LB
        return sourceRtn;
    }

    public MogiPoint getLowerBound() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);

        MogiPoint sourceRtn = new MogiPoint(east.lb, north.lb, -depth.ub, -Double.MAX_VALUE); // Change
                                                                                              // this
                                                                                              // line
                                                                                              // for
                                                                                              // UB
                                                                                              // and
                                                                                              // LB
        return sourceRtn;
    }

    // TODO: really make bounds work
    public void showSource(DisplacementSolver sourceIn, int indexInSimModel, DisplacementSolver sourceLB,
            DisplacementSolver sourceUB) {
        // TODO improve this
        if (sourceIn == null)
            return;

        this.modelIndex = indexInSimModel;
        this.createNew = false;

        MogiPoint curMogi = (MogiPoint) sourceIn;

        // now put the stuff in, and leave the bounds alone
        this.txtValX.setText(String.format("%.2f", curMogi.getEast()));
        this.txtValY.setText(String.format("%.2f", curMogi.getNorth()));
        this.txtValC.setText(String.format("%.2f", -curMogi.getUp()));
        this.txtVolChg.setText(String.format("%.2f", curMogi.getVolumeChange()));

        getButtonOkay().setText("Save");
        setTitle("Edit a Mogi source");

        this.setVisible(true);
    }

    public int getModelIndex() {
        return this.modelIndex;
    }

    public boolean isCreateNew() {
        return this.createNew;
    }

    public boolean isUnitsAreXY() {
        return unitsAreXY;
    }

    // @Override
    // public void mouseClick(java.awt.geom.Point2D.Double coords, boolean
    // isLatLng) {
    // String x, y;
    // if (isLatLng) {
    // x = String.format("%4.5f", coords.x);
    // y = String.format("%4.5f", coords.y);
    // } else {
    // x = String.format("%4.2f", coords.x);
    // y = String.format("%4.2f", coords.y);
    // }
    //
    // if (filledXField != null && filledXField.isEnabled())
    // filledXField.setText(x);
    // if (filledYField != null && filledYField.isEnabled())
    // filledYField.setText(y);
    // }

    // GUI functions
    // ----------------
    protected void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
        if (boundsAreValid()) {
            MogiPoint ms = getSource();
            MogiPoint lb = getLowerBound();
            MogiPoint ub = getUpperBound();
            String name = txtName.getText();
            if (name != null && !name.isEmpty()) {
                ms.setName(name);
            }

            if (isCreateNew()) {
                System.out.println("Source Added: " + ms);
                System.out.println("   LB: " + lb);
                System.out.println("   UB: " + ub);
                simModel.getSourceModels().add(ms);
                simModel.getSourceLowerbound().add(lb);
                simModel.getSourceUpperbound().add(ub);
            } else {
                simModel.getSourceModels().set(modelIndex, ms);
                simModel.getSourceLowerbound().set(modelIndex, lb);
                simModel.getSourceUpperbound().set(modelIndex, ub);
            }
            fireDataChangeEvent();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid bounds and guesses.  Please doublecheck.", "Invalid bounds",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    @Override
    public void dispose() {
        Dismodel2 mainFrame = allGuiVars.getMainFrame();
        mainFrame.removeGeoPosClickListener(this);
        this.removeDataChangeEventListener(mainFrame);
        super.dispose();
    }

    public JButton getButtonOkay() {
        return btnOK;
    }

    @Override
    protected void CBFixXActionPerformed(java.awt.event.ActionEvent evt) {
        adjustTxtBoxesWithChkbox(CBFixX, txtValX, txtLBX, txtUBX);
    }

    @Override
    protected void CBFixYActionPerformed(java.awt.event.ActionEvent evt) {
        adjustTxtBoxesWithChkbox(CBFixY, txtValY, txtLBY, txtUBY);
    }

    @Override
    protected void CBFixCActionPerformed(java.awt.event.ActionEvent evt) {
        adjustTxtBoxesWithChkbox(CBFixC, txtValC, txtLBC, txtUBC);
    }

    @Override
    protected void cbxUnitActionPerformed(java.awt.event.ActionEvent evt) {
        if (cbxUnit.getSelectedIndex() == 0) {
            unitsAreXY = true;
        } else {
            unitsAreXY = false;
        }
    }

    @Override
    protected void txtValXFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtValX;
        filledYField = txtValY;
    }

    @Override
    protected void txtValYFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtValX;
        filledYField = txtValY;
    }

    @Override
    protected void txtLBXFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtLBX;
        filledYField = txtLBY;
    }

    @Override
    protected void txtLBYFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtLBX;
        filledYField = txtLBY;
    }

    @Override
    protected void txtUBXFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtUBX;
        filledYField = txtUBY;
    }

    @Override
    protected void txtUBYFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtUBX;
        filledYField = txtUBY;
    }

    protected void noMouseClickFills() {
        filledXField = null;
        filledYField = null;
    }

    @Override
    protected void txtValCFocusGained(java.awt.event.FocusEvent evt) {
        noMouseClickFills();
    }

    @Override
    protected void txtUBCFocusGained(java.awt.event.FocusEvent evt) {
        noMouseClickFills();
    }

    @Override
    protected void txtLBCFocusGained(java.awt.event.FocusEvent evt) {
        noMouseClickFills();
    }

    @Override
    protected void txtVolChgFocusGained(java.awt.event.FocusEvent evt) {
        noMouseClickFills();
    }

    @Override
    protected void nofillFocusGain(java.awt.event.FocusEvent evt) {
        noMouseClickFills();
    }

    protected void adjustTxtBoxesWithChkbox(javax.swing.JCheckBox fixedChkBox, javax.swing.JTextField valTxt,
            javax.swing.JTextField lbTxt, javax.swing.JTextField ubTxt) {
        final boolean fixed = fixedChkBox.isSelected();
        if (fixed) {
            valTxt.setEnabled(true);
            ubTxt.setEnabled(false);
            lbTxt.setEnabled(false);
        } else {
            valTxt.setEnabled(false);
            ubTxt.setEnabled(true);
            lbTxt.setEnabled(true);
        }
    }

    // supporting functions
    // ----------------------
    private NumberWithRange parseChkNTxtBoxes(javax.swing.JCheckBox fixedChkBox, javax.swing.JTextField valTxt,
            javax.swing.JTextField lbTxt, javax.swing.JTextField ubTxt) {
        final boolean fixed = fixedChkBox.isSelected();
        if (fixed) {
            return new NumberWithRange(parseJTextField(valTxt), Double.NaN, Double.NaN);
        } else {
            double lowerbound = parseJTextField(lbTxt);
            double upperbound = parseJTextField(ubTxt);
            double guess = (lowerbound + upperbound) * 0.5d;
            return new NumberWithRange(guess, lowerbound, upperbound);
        }
    }

    private double parseJTextField(final javax.swing.JTextField valueBox) {
        String valueText = valueBox.getText();
        if (valueText.equals("")) {
            return Double.NaN;
        } else {
            return Double.parseDouble(valueText);
        }
    }

    private static class NumberWithRange {
        public double number;
        public double lb;
        public double ub;

        public NumberWithRange() {
        }

        public NumberWithRange(double number, double lb, double ub) {
            this.number = number;
            this.lb = lb;
            this.ub = ub;
        }

    }

    // Event handlers
    // --------------

    @Override
    public void latLonClicked(LLH location) {
        if (unitsAreXY)
            return;
        String x = String.format("%.6f", location.getLongitude());
        String y = String.format("%.6f", location.getLatitude());

        if (filledXField != null && filledXField.isEnabled())
            filledXField.setText(x);
        if (filledYField != null && filledYField.isEnabled())
            filledYField.setText(y);
    }

    @Override
    public void LocalENUClicked(LocalENU location) {
        if (!unitsAreXY)
            return;

        String x = String.format("%.2f", location.getEasting());
        String y = String.format("%.2f", location.getNorthing());

        if (filledXField != null && filledXField.isEnabled())
            filledXField.setText(x);
        if (filledYField != null && filledYField.isEnabled())
            filledYField.setText(y);
    }

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.add(listener);
    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.remove(listener);
    }

    private void fireDataChangeEvent() {
        for (DataChangeEventListener listener : dataChgListeners) {
            listener.updateAfterDataChange();
        }
    }

}

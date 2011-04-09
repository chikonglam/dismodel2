package gov.usgs.dismodel.calc.greens.dialogs;

import gov.usgs.dismodel.Dismodel2;
import gov.usgs.dismodel.RestorableSourceDialog;
import gov.usgs.dismodel.SourceDialogUtils;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.McTigueSphere;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.gui.events.DataChangeEventFrier;
import gov.usgs.dismodel.gui.events.DataChangeEventListener;
import gov.usgs.dismodel.gui.events.GeoPosClickListener;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class SphericalSourceDialog2 extends SphericalSourceDialogBase implements RestorableSourceDialog,
        GeoPosClickListener, DataChangeEventFrier {

    /**
	 * 
	 */
    private static final long serialVersionUID = -336068134974255797L;
    private boolean createNew = true;
    private int modelIndex = -1;
    private boolean unitsAreXY = true;
    private javax.swing.JTextField filledXField = null;
    private javax.swing.JTextField filledYField = null;
    private AllGUIVars allGuiVars;
    private SimulationDataModel simModel = null;
    private ArrayList<DataChangeEventListener> dataChgListeners = new ArrayList<DataChangeEventListener>();

    public SphericalSourceDialog2(Window owner, String title, AllGUIVars allGuiVars) {
        super(owner, title);
        this.allGuiVars = allGuiVars;

        this.simModel = allGuiVars.getSimModel();

        Dismodel2 mainFrame = allGuiVars.getMainFrame();
        mainFrame.addGeoPosClickListener(this);
        this.addDataChangeEventListener(mainFrame);

    }

    public SphericalSourceDialog2(Window owner, String title, int modelIndex, AllGUIVars allGuiVars) {
        this(owner, title, allGuiVars);
        this.modelIndex = modelIndex;
        this.createNew = false;

        McTigueSphere curSphere = (McTigueSphere) simModel.getSourceModels().get(modelIndex);
        McTigueSphere curLB = (McTigueSphere) simModel.getSourceLowerbound().get(modelIndex);
        McTigueSphere curUB = (McTigueSphere) simModel.getSourceUpperbound().get(modelIndex);

        SourceDialogUtils.parseValsIntoUIBoxes(curSphere.getEast(), curLB.getEast(), curUB.getEast(), CBFixX, txtValX,
                txtLBX, txtUBX);
        SourceDialogUtils.parseValsIntoUIBoxes(curSphere.getNorth(), curLB.getNorth(), curUB.getNorth(), CBFixY,
                txtValY, txtLBY, txtUBY);
        SourceDialogUtils.parseValsIntoUIBoxes(-curSphere.getUp(), -curUB.getUp(), -curLB.getUp(), CBFixC, txtValC,
                txtLBC, txtUBC);

        txtRadius.setText(String.format("%.2f", curSphere.getRadius()));
        txtVolChg.setText(String.format("%.2f", curSphere.getVolumeChange()));

        String name = curSphere.getName();
        if (name != null && !name.isEmpty()) {
            txtName.setText(name);
        }
    }

    public boolean boundsAreValid() {
        // TODO make it really check the bounds
        return true;
    }

    public McTigueSphere getSphericalSource() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;
        double volChange; // Change this line for UB and LB
        double radius; // Change this line for UB and LB

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);
        radius = parseJTextField(txtRadius);
        volChange = parseJTextField(txtVolChg); // Change this line for UB and
                                                // LB
        if (Double.isNaN(volChange))
            volChange = 0d;

        McTigueSphere sourceRtn = new McTigueSphere(east.number, north.number, -depth.number, volChange, radius); // Change
                                                                                                                  // this
                                                                                                                  // line
                                                                                                                  // for
                                                                                                                  // UB
                                                                                                                  // and
                                                                                                                  // LB
        return sourceRtn;
    }

    public McTigueSphere getUpperBound() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);

        McTigueSphere sourceRtn = new McTigueSphere(east.ub, north.ub, -depth.lb, Double.MAX_VALUE, Double.NaN); // Change
                                                                                                                 // this
                                                                                                                 // line
                                                                                                                 // for
                                                                                                                 // UB
                                                                                                                 // and
                                                                                                                 // LB
        return sourceRtn;
    }

    public McTigueSphere getLowerBound() {
        NumberWithRange east;
        NumberWithRange north;
        NumberWithRange depth;

        east = parseChkNTxtBoxes(CBFixX, txtValX, txtLBX, txtUBX);
        north = parseChkNTxtBoxes(CBFixY, txtValY, txtLBY, txtUBY);
        depth = parseChkNTxtBoxes(CBFixC, txtValC, txtLBC, txtUBC);

        McTigueSphere sourceRtn = new McTigueSphere(east.lb, north.lb, -depth.ub, -Double.MAX_VALUE, Double.NaN); // Change
                                                                                                                  // this
                                                                                                                  // line
                                                                                                                  // for
                                                                                                                  // UB
                                                                                                                  // and
                                                                                                                  // LB
        return sourceRtn;
    }

    // TODO really make bounds work
    public void showSource(DisplacementSolver sourceIn, int modelIndex, DisplacementSolver sourceLB,
            DisplacementSolver sourceUB) {
        // TODO improve this
        if (sourceIn == null)
            return;

        this.modelIndex = modelIndex;
        this.createNew = false;

        McTigueSphere curSphere = (McTigueSphere) sourceIn;

        // now put the stuff in, and leave the bounds alone
        this.txtValX.setText(String.format("%.2f", curSphere.getEast()));
        this.txtValY.setText(String.format("%.2f", curSphere.getNorth()));
        this.txtValC.setText(String.format("%.2f", -curSphere.getUp()));
        this.txtVolChg.setText(String.format("%.2f", curSphere.getVolumeChange()));
        this.txtRadius.setText(String.format("%.2f", curSphere.getRadius()));

        getButtonOkay().setText("Save");
        setTitle("Edit a spherical source");

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
    @Override
    protected void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
        if (boundsAreValid()) {
            McTigueSphere ss = getSphericalSource();
            McTigueSphere lb = getLowerBound();
            McTigueSphere ub = getUpperBound();

            String name = txtName.getText();
            if (name != null && !name.isEmpty()) {
                ss.setName(name);
            }

            if (isCreateNew()) {
                System.out.println("Source Added: " + ss);
                System.out.println("   LB: " + lb);
                System.out.println("   UB: " + ub);
                simModel.getSourceModels().add(ss);
                simModel.getSourceLowerbound().add(lb);
                simModel.getSourceUpperbound().add(ub);
            } else {
                simModel.getSourceModels().set(modelIndex, ss);
                simModel.getSourceLowerbound().set(modelIndex, lb);
                simModel.getSourceUpperbound().set(modelIndex, ub);
            }
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid bounds and guesses.  Please doublecheck.", "Invalid bounds",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    @Override
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
    protected void noFillsFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = null;
        filledYField = null;
    }

    @Override
    protected void valFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtValX;
        filledYField = txtValY;
    }

    @Override
    protected void lbFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtLBX;
        filledYField = txtLBY;
    }

    @Override
    protected void ubFocusGained(java.awt.event.FocusEvent evt) {
        filledXField = txtUBX;
        filledYField = txtUBY;
    }

    @Override
    protected void cbxUnitActionPerformed(java.awt.event.ActionEvent evt) {
        if (cbxUnit.getSelectedIndex() == 0) {
            unitsAreXY = true;
        } else {
            unitsAreXY = false;
        }
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

    @Override
    public void addDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.add(listener);

    }

    @Override
    public void removeDataChangeEventListener(DataChangeEventListener listener) {
        dataChgListeners.remove(listener);

    }

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

}

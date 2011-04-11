/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SmoothingDialog.java
 *
 * Created on Sep 10, 2010, 4:03:23 PM
 */

package gov.usgs.dismodel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author cforden
 */
public class SmoothingDialog extends javax.swing.JDialog
            implements ActionListener {


    public static class Params {
        public boolean useSmoothing = false;
        public double  gamma = 1.0;

        public boolean shouldCrossValidate = false;
        public int     numGammaValues = 10;
        public double  minGamma = 0.01;
        public double  maxGamma = 1.0;
    }


    /** Creates new form SmoothingDialog */
    public SmoothingDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        smoothingGammaEditText.setEnabled(false);
        enableCrossValEditFields(false);
        buttonCancel.addActionListener(this);
        buttonOkay.addActionListener(this);
    }


    public void actionPerformed(ActionEvent e) {
        if (buttonOkay == e.getSource()) {
            setVisible(false);
        } else if (buttonCancel == e.getSource()) {
            this.dispose();
        }
    }


    public SmoothingDialog.Params getParams() {
        SmoothingDialog.Params retVal = new SmoothingDialog.Params();

        retVal.useSmoothing = isSmoothingCkBx();
        retVal.gamma = getSingleGamma();

        retVal.shouldCrossValidate = isCrossValCkBx();
        retVal.numGammaValues = getNumGammaValues();
        retVal.minGamma = getMinGamma();
        retVal.maxGamma = getMaxGamma();

        return retVal;
    }


    public void setParams(Params crossValidationParams) {
        setSmoothingCkBx(crossValidationParams.useSmoothing);
        setSingleGamma(crossValidationParams.gamma);

        setCrossValCkBx(crossValidationParams.shouldCrossValidate);
        setNumGammaValues(crossValidationParams.numGammaValues);
        setMinGamma(crossValidationParams.minGamma);
        setMaxGamma(crossValidationParams.maxGamma);
        smoothingChkBxActionPerformed(null);
        enblCrossValChkBxActionPerformed(null);
    }


    public boolean areParamsValid(StringBuilder errorMessageOut) {
        try {
            this.getSingleGamma(); // throws if bad format
            
            if (getNumGammaValues() < 1) {
                errorMessageOut.append("The number of gamma values should be " +
                		"greater than zero.");
                return false;
            }
            if (getMinGamma() >= getMaxGamma()) {
                errorMessageOut.append("The minimum gamma value should be "
                        + "less than the maximum.");
                return false;
            }
            if (getMinGamma() <= 0.0) {
                errorMessageOut.append("The minimum gamma value should be "
                        + "greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            errorMessageOut.append("An incorrectly formatted number " +
            		"had been entered in a numeric field.");
            return false;
        }
        return true;
    }


    public boolean isSmoothingCkBx() {
        return smoothingChkBx.isSelected();
    }

    public void setSmoothingCkBx(boolean b) {
        smoothingChkBx.setSelected(b);
    }

    /** "Single" values of gamma are used for smoothing during solution of
     * distributed faults, for example after (multiple) values were tried
     * during cross-validation which finds gamma's optimal value.     */
    public double getSingleGamma() {
        return Double.parseDouble(smoothingGammaEditText.getText());
    }
    public void setSingleGamma(double max) {
        smoothingGammaEditText.setText(Double.toString(max));
    }


    public boolean isCrossValCkBx() {
        return enblCrossValChkBx.isSelected();
    }

    public void setCrossValCkBx(boolean b) {
        enblCrossValChkBx.setSelected(b);
    }

    public int getNumGammaValues() {
        return Integer.parseInt(numGamValsEditTxt.getText());
    }
    public void setNumGammaValues(int n) {
        numGamValsEditTxt.setText(Integer.toString(n));
    }

    public double getMinGamma() {
        return Double.parseDouble(minGamValEditTxt.getText());
    }
    public void setMinGamma(double min) {
        minGamValEditTxt.setText(Double.toString(min));
    }

    public double getMaxGamma() {
        return Double.parseDouble(maxGamValEditTxt.getText());
    }
    public void setMaxGamma(double max) {
        maxGamValEditTxt.setText(Double.toString(max));
    }


    public JButton getButtonOkay() {
        return buttonOkay;
    }

    public JButton getButtonCancel() {
        return buttonCancel;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        smoothingPnl = new javax.swing.JPanel();
        smoothingChkBx = new javax.swing.JCheckBox();
        smoothingGammaEditText = new javax.swing.JTextField();
        crossValPnl = new javax.swing.JPanel();
        enblCrossValChkBx = new javax.swing.JCheckBox();
        numGamValsEditTxt = new javax.swing.JTextField();
        numGamValsLabel = new javax.swing.JTextField();
        maxGamValLabel = new javax.swing.JTextField();
        maxGamValEditTxt = new javax.swing.JTextField();
        minGamValLabel = new javax.swing.JTextField();
        minGamValEditTxt = new javax.swing.JTextField();
        buttonOkay = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Smoothing");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setName("Smoothing"); // NOI18N

        smoothingChkBx.setText("Use smoothing when solving distributed slip");
        smoothingChkBx.setToolTipText("When this is checked, ");
        smoothingChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smoothingChkBxActionPerformed(evt);
            }
        });

        smoothingGammaEditText.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        smoothingGammaEditText.setText("1.0");
        smoothingGammaEditText.setToolTipText("When solving slip distributed across multiple subfaults, the inverse of this value will multiply the smoothing equations, assigning them greater weighting.");

        javax.swing.GroupLayout smoothingPnlLayout = new javax.swing.GroupLayout(smoothingPnl);
        smoothingPnl.setLayout(smoothingPnlLayout);
        smoothingPnlLayout.setHorizontalGroup(
            smoothingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(smoothingPnlLayout.createSequentialGroup()
                .addGroup(smoothingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(smoothingPnlLayout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(smoothingGammaEditText, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(smoothingChkBx))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        smoothingPnlLayout.setVerticalGroup(
            smoothingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(smoothingPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(smoothingChkBx)
                .addGap(18, 18, 18)
                .addComponent(smoothingGammaEditText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        enblCrossValChkBx.setText("Enable cross-validation");
        enblCrossValChkBx.setToolTipText("When this is checked, the cross-validation button will be enabled in the main, right, toolbar, above the 3D, cartesian view.");
        enblCrossValChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enblCrossValChkBxActionPerformed(evt);
            }
        });

        numGamValsEditTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        numGamValsEditTxt.setText("10");
        numGamValsEditTxt.setToolTipText("The number of different gamma values to be tried during cross validation");

        numGamValsLabel.setEditable(false);
        numGamValsLabel.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        numGamValsLabel.setText("Number of gamma values");

        maxGamValLabel.setEditable(false);
        maxGamValLabel.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxGamValLabel.setText("Maximum gamma value");

        maxGamValEditTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        maxGamValEditTxt.setText("10.0");
        maxGamValEditTxt.setToolTipText("The largest gamma value to be tried during cross validation");

        minGamValLabel.setEditable(false);
        minGamValLabel.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        minGamValLabel.setText("Minimum gamma value");

        minGamValEditTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        minGamValEditTxt.setText("0.01");
        minGamValEditTxt.setToolTipText("The smallest gamma value to be tried during cross validation");

        javax.swing.GroupLayout crossValPnlLayout = new javax.swing.GroupLayout(crossValPnl);
        crossValPnl.setLayout(crossValPnlLayout);
        crossValPnlLayout.setHorizontalGroup(
            crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(crossValPnlLayout.createSequentialGroup()
                .addComponent(enblCrossValChkBx)
                .addContainerGap(165, Short.MAX_VALUE))
            .addGroup(crossValPnlLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(maxGamValLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(minGamValLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(numGamValsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(numGamValsEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxGamValEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minGamValEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );
        crossValPnlLayout.setVerticalGroup(
            crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(crossValPnlLayout.createSequentialGroup()
                .addComponent(enblCrossValChkBx)
                .addGap(18, 18, 18)
                .addGroup(crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numGamValsEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numGamValsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxGamValLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxGamValEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(crossValPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minGamValLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minGamValEditTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        buttonOkay.setText("Okay");

        buttonCancel.setText("Cancel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smoothingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(crossValPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(buttonOkay, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(smoothingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(crossValPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonOkay)
                    .addComponent(buttonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enblCrossValChkBxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enblCrossValChkBxActionPerformed
        enableCrossValEditFields(enblCrossValChkBx.isSelected());
    }//GEN-LAST:event_enblCrossValChkBxActionPerformed

    private void enableCrossValEditFields(boolean enable) {
        numGamValsEditTxt.setEnabled(enable);
        maxGamValEditTxt.setEnabled(enable);
        minGamValEditTxt.setEnabled(enable);
    }

    private void smoothingChkBxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smoothingChkBxActionPerformed
        smoothingGammaEditText.setEnabled(smoothingChkBx.isSelected());
    }//GEN-LAST:event_smoothingChkBxActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SmoothingDialog dialog = new SmoothingDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOkay;
    private javax.swing.JPanel crossValPnl;
    private javax.swing.JCheckBox enblCrossValChkBx;
    private javax.swing.JTextField maxGamValEditTxt;
    private javax.swing.JTextField maxGamValLabel;
    private javax.swing.JTextField minGamValEditTxt;
    private javax.swing.JTextField minGamValLabel;
    private javax.swing.JTextField numGamValsEditTxt;
    private javax.swing.JTextField numGamValsLabel;
    private javax.swing.JCheckBox smoothingChkBx;
    private javax.swing.JTextField smoothingGammaEditText;
    private javax.swing.JPanel smoothingPnl;
    // End of variables declaration//GEN-END:variables

}

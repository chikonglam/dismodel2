package gov.usgs.dismodel.calc.inversion.dialogs;

import gov.usgs.dismodel.state.SimulationDataModel;

public class ShearModulusDialog extends ShearModulusDialogBase{

    private static final long serialVersionUID = -145143064414620424L;
    protected SimulationDataModel simModel;
    
    public ShearModulusDialog(SimulationDataModel simModel) {
	super();
	this.simModel = simModel;
	this.txtShearModulus.setText( Double.toString(simModel.getShearModulus()) );
    }

    protected void initComponents(){
	super.initComponents();
    }
    
    protected void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
	try {
	    double parsedNum = Double.parseDouble(this.txtShearModulus.getText());
	    simModel.setShearModulus(parsedNum);
	    this.dispose();
	} catch (Exception e){
	}
	
    }
    
    
}

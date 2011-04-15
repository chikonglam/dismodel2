package gov.usgs.dismodel.calc.inversion.dialogs;

import javax.swing.JWindow;

import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.calc.inversion.ConstraintType;

public class DistSlipConstDialog extends DistSlipConstDialogBase {
	private SimulationDataModel simModel;

	public DistSlipConstDialog(SimulationDataModel simModel) {
		this.simModel = simModel;
		
		this.nonNeg.setSelected(simModel.getNonNeg());
		double moment = simModel.getMonentConstraint();
		if (Double.isNaN(moment)){
			this.conMmt.setSelected(false);
		} else {
			this.conMmt.setSelected(true);
			this.moment.setText(Double.toString(moment));
			
			ConstraintType conType = simModel.getMonentConType();
			if (conType == ConstraintType.LESS_THAN_OR_EQUAL){
				le.setSelected(true);
			} else if (conType == ConstraintType.EQUAL){
				eq.setSelected(true);
			} else if (conType == ConstraintType.GREATER_THAN_OR_EQUAL){
				ge.setSelected(true);
			}
			
		}
	}
	
    @Override
	protected void OKbtnActionPerformed(java.awt.event.ActionEvent evt) {
    	simModel.setNonNeg( this.nonNeg.isSelected() );
    	if ( this.conMmt.isSelected() ){
    		simModel.setMonentConstraint( Double.parseDouble(this.moment.getText()) );
    		
    		ConstraintType conType = ConstraintType.LESS_THAN_OR_EQUAL;
    		if (this.le.isSelected()) {
    			conType = ConstraintType.LESS_THAN_OR_EQUAL;
    		} else if (this.eq.isSelected()) {
    			conType = ConstraintType.EQUAL;
    		} else if (this.ge.isSelected()) { 
    			conType = ConstraintType.GREATER_THAN_OR_EQUAL;
    		}
    		simModel.setMonentConType(conType);
    			
    	} else {
    		simModel.setMonentConstraint(Double.NaN);
    	}
    	
    	dispose();
    }

}

package gov.usgs.dismodel;

import javax.swing.JCheckBox;
import javax.swing.JTextField;



public class SourceDialogUtils {

	public static void parseValsIntoUIBoxes(double valX, double lbX, double ubX, double valY, double lbY, double ubY, 
			JCheckBox fixedCheckbox, JTextField valXTxt, JTextField lbXTxt, JTextField ubXTxt,
			JTextField valYTxt, JTextField lbYTxt, JTextField ubYTxt){
		if (isDisabled(valX, lbX, ubX) || isDisabled(valY, lbY, ubY)){
			defaultVal(valX, lbX, ubX, fixedCheckbox, valXTxt, lbXTxt, ubXTxt);
			defaultVal(valY, lbY, ubY, fixedCheckbox, valYTxt, lbYTxt, ubYTxt);
		} else if (isFixed(valX, lbX, ubX) && isFixed(valY, lbY, ubY)) {
			fixedVal(valX, lbX, ubX, fixedCheckbox, valXTxt, lbXTxt, ubXTxt);
			fixedVal(valY, lbY, ubY, fixedCheckbox, valYTxt, lbYTxt, ubYTxt);
		} else if (isFixed(valX, lbX, ubX) ) { // !isFixed(valY, lbY, ubY)
			boundedVal(valX, valX, valX, fixedCheckbox, valXTxt, lbXTxt, ubXTxt);
			boundedVal(valY, lbY, ubY, fixedCheckbox, valYTxt, lbYTxt, ubYTxt);
		} else if (isFixed(valY, lbY, ubY) ) { // !isFixed(valX, lbX, ubX)
			boundedVal(valX, lbX, ubX, fixedCheckbox, valXTxt, lbXTxt, ubXTxt);
			boundedVal(valY, valY, valY, fixedCheckbox, valYTxt, lbYTxt, ubYTxt);
		} else {
			boundedVal(valX, lbX, ubX, fixedCheckbox, valXTxt, lbXTxt, ubXTxt);
			boundedVal(valY, lbY, ubY, fixedCheckbox, valYTxt, lbYTxt, ubYTxt);
		}
	}
	
	public static void parseValsIntoUIBoxes(double val, double lb, double ub, JCheckBox fixedCheckbox, JTextField valTxt, JTextField lbTxt, JTextField ubTxt){
		if (isDisabled(val, lb, ub)){
			defaultVal(val, lb, ub, fixedCheckbox, valTxt, lbTxt, ubTxt);
		} else if (isFixed(val, lb, ub)) {
			fixedVal(val, lb, ub, fixedCheckbox, valTxt, lbTxt, ubTxt);
		} else {
			boundedVal(val, lb, ub, fixedCheckbox, valTxt, lbTxt, ubTxt);
		}
	
 }
	
	private static void fixedVal(double val, double lb, double ub, JCheckBox fixedCheckbox, JTextField valTxt, JTextField lbTxt, JTextField ubTxt){
		fixedCheckbox.setSelected(true);
		
		valTxt.setEnabled(true);
		valTxt.setText(String.format("%.2f", val));
		
		lbTxt.setEnabled(false);
		lbTxt.setText("");
		
		ubTxt.setEnabled(false);
		ubTxt.setText("");
	}
	
	private static void boundedVal(double val, double lb, double ub, JCheckBox fixedCheckbox, JTextField valTxt, JTextField lbTxt, JTextField ubTxt){
		fixedCheckbox.setSelected(false);
		
		valTxt.setEnabled(false);
		valTxt.setText(String.format("%.2f", val));
		
		lbTxt.setEnabled(true);
		lbTxt.setText(String.format("%.2f", lb));
		
		ubTxt.setEnabled(true);
		ubTxt.setText(String.format("%.2f", ub));
	}
	
	private static void defaultVal(double val, double lb, double ub, JCheckBox fixedCheckbox, JTextField valTxt, JTextField lbTxt, JTextField ubTxt){
		fixedCheckbox.setSelected(false);
		
		valTxt.setEnabled(false);
		valTxt.setText("");
		
		lbTxt.setEnabled(true);
		lbTxt.setText("");
		
		ubTxt.setEnabled(true);
		ubTxt.setText("");
	}

	private static boolean isFixed(double val, double lb, double ub){
		if (!Double.isNaN(val) && Double.isNaN(lb) && Double.isNaN(ub)){
			return true;
		} else if (Double.compare(val, lb) == 0 && Double.compare(val, ub) == 0){
			return true;
		}else if (!Double.isNaN(val) && Double.compare(0.0, lb) == 0 && Double.compare(0.0, ub) == 0 ){
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean isDisabled(double val, double lb, double ub){
		return (Double.isNaN(val) && Double.isNaN(lb) && Double.isNaN(ub) );
	}
	
	
}
package gov.usgs.dismodel.calc.greens.dialogs;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;
import gov.usgs.dismodel.calc.inversion.ConstraintType;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;

import java.util.ArrayList;
import java.util.List;

/**
 * Class named with the idea of using the Builder Pattern, not yet implemented.
 */
public class DislocationBuilder {
    
    private DislocationDialog d;
    
    //TODO: Change these 3 below to a wrapper one that support the other user options 
    // (as of now, only the fundamental 10 are supported)
    private DisplacementSolver faultInitVal;
    private DisplacementSolver faultUB;
    private DisplacementSolver faultLB;
    private boolean isDistedSlip;
    //private boolean nonNeg;
    //private double targetMoment = Double.NaN;
    //private ConstraintType momentConType = null;
    
    
    public DislocationBuilder(DislocationDialog d) {
        this.d = d;
    }
    
//    public boolean isNonNeg() {
//        return nonNeg;
//    }
//
//    public double getTargetMoment() {
//        return targetMoment;
//    }
//
//    public ConstraintType getMomentConType() {
//        return momentConType;
//    }

    public boolean isDistedSlip() {
        return isDistedSlip;
    }

    public DisplacementSolver getDislocation(LLH origin) {
        calcOutSrc(origin);
        return faultInitVal;
    }
    
    public DisplacementSolver getDislocationUB(LLH origin) {
        calcOutSrc(origin);
        return faultUB;
    }
    
    public DisplacementSolver getDislocationLB(LLH origin) {
        calcOutSrc(origin);
        return faultLB;
    }
    
    private void toLocalENU(boolean isAlreadyXy, DoubleWithRange x,
            DoubleWithRange y, LLH origin) {
        if (isAlreadyXy)
            return;

        double xVal = x.value;
        double yVal = y.value;
        if (!Double.isNaN(xVal) && !Double.isNaN(yVal)){
            LocalENU localCoordVal = new LLH(xVal, yVal, 0d).toLocalENU(origin);
            x.value = localCoordVal.getEasting();
            y.value = localCoordVal.getNorthing();
        }
        
        
        double xUB = x.UpperBound;
        double yUB = y.UpperBound;
        if (!Double.isNaN(xUB) && !Double.isNaN(yUB)){
            LocalENU localCoordVal = new LLH(xUB, yUB, 0d).toLocalENU(origin);
            x.UpperBound = localCoordVal.getEasting();
            y.UpperBound = localCoordVal.getNorthing();
        }

        
        double xLB = x.LowerBound;
        double yLB = y.LowerBound;
        if (!Double.isNaN(xLB) && !Double.isNaN(yLB)){
            LocalENU localCoordVal = new LLH(xLB, yLB, 0d).toLocalENU(origin);
            x.LowerBound = localCoordVal.getEasting();
            y.LowerBound = localCoordVal.getNorthing();
        }
    }
    
    private void toLocalENU(boolean isAlreadyXy , DoubleWithRange depth, LLH origin){
        if (isAlreadyXy) return;
        
        double depVal = depth.value;
        if (!Double.isNaN(depVal) ){
            LocalENU localCoordVal = new LLH(origin.getLatitude(), origin.getLongitude(), -depVal).toLocalENU(origin);
            depth.value = -localCoordVal.getUp();
        }
        
        double depUB = depth.UpperBound;
        if (!Double.isNaN(depUB) ){
            LocalENU localCoordVal = new LLH(origin.getLatitude(), origin.getLongitude(), -depUB).toLocalENU(origin);
            depth.UpperBound = -localCoordVal.getUp();
        }
        
        double depLB = depth.LowerBound;
        if (!Double.isNaN(depLB) ){
            LocalENU localCoordVal = new LLH(origin.getLatitude(), origin.getLongitude(), -depLB).toLocalENU(origin);
            depth.LowerBound = -localCoordVal.getUp();
        }
        
        
    }
    
    
    private void calcOutSrc(LLH origin){
//        if (alreadyCalced ){  //only calculate if it's not already done
//            return;
//        }
//        alreadyCalced = true;
        //temp vars
        DoubleWithRange x1, y1, x2, y2, xC, yC, dep, strike, dip, aspectRatio, len, wid, ss, ds, ts;
        
        //now parse in the vars
        if (d.endPointsRB.isSelected()){                    //end points are given
            x1 = parseUIchkNTxtBox(d.endpoint1Fixed, d.endpoint1xValue, d.endpoint1xUB, d.endpoint1xLB);
            y1 = parseUIchkNTxtBox(d.endpoint1Fixed, d.endpoint1yValue, d.endpoint1yUB, d.endpoint1yLB);
            toLocalENU(d.isUnitsAreXY(), x1, y1, origin);
            
            if (d.specifyEndpoint2.isSelected()){           //if both points are defined
                x2 = parseUIchkNTxtBox(d.endpoint2Fixed, d.endpoint2xValue, d.endpoint2xUB, d.endpoint2xLB);
                y2 = parseUIchkNTxtBox(d.endpoint2Fixed, d.endpoint2yValue, d.endpoint2yUB, d.endpoint2yLB);
                toLocalENU(d.isUnitsAreXY(), x2, y2, origin);
                strike = new DoubleWithRange();
            } else {                                        //one point is defined
                x2 = new DoubleWithRange();
                y2 = new DoubleWithRange();
                strike = parseUIchkNTxtBox(d.strikeFixed, d.strikeValue, d.strikeUB, d.strikeLB);
            }
            
            
            xC = new DoubleWithRange();
            yC = new DoubleWithRange();
        } else {                                            //middle point is defined
            x1 = new DoubleWithRange();
            y1 = new DoubleWithRange();
            x2 = new DoubleWithRange();
            y2 = new DoubleWithRange();
            xC = parseUIchkNTxtBox(d.midpointFixed, d.midpointxValue, d.midpointxUB, d.midpointxLB);
            yC = parseUIchkNTxtBox(d.midpointFixed, d.midpointyValue, d.midpointyUB, d.midpointyLB);
            toLocalENU(d.isUnitsAreXY(), xC, yC, origin);
            strike = parseUIchkNTxtBox(d.strikeFixed, d.strikeValue, d.strikeUB, d.strikeLB);
        }
        dep = parseUIchkNTxtBox(d.depthFixed, d.depthValue, d.depthUB, d.depthLB);
        
        boolean isUpperCoord = d.IsCoordinatesAreUpperEdge();

        dip = parseUIchkNTxtBox(d.dipFixed, d.dipValue, d.dipUB, d.dipLB);
        
        aspectRatio = new DoubleWithRange();
        if (d.aspectRatioCB.isSelected()) aspectRatio = parseUIchkNTxtBox(d.aspectRatioFixed, d.aspectRatioValue, d.aspectRatioUB, d.aspectRatioLB);
        len = new DoubleWithRange();
        if (d.lengthCB.isSelected()) len = parseUIchkNTxtBox(d.lengthFixed, d.lengthValue, d.lengthUB, d.lengthLB);
        wid = new DoubleWithRange();
        if (d.widthCB.isSelected()) wid = parseUIchkNTxtBox(d.widthFixed, d.widthValue, d.widthUB, d.widthLB);

        ss = parseUISlips(d.strikeSlipCB, d.SSValue);
        ds = parseUISlips(d.dipSlipCB, d.DSValue);
        ts = parseUISlips(d.openingCB, d.TSValue);
        
        String dLtxt = d.deltaLength.getText();
        String dWtxt = d.deltaWidth.getText();
        
        OkadaFault3 tempFault = new OkadaFault3(x1.value, y1.value, x2.value, y2.value, xC.value, yC.value, dep.value, isUpperCoord, strike.value, dip.value, aspectRatio.value, len.value, wid.value, ss.value, ds.value, ts.value);
        OkadaFault3 tempUB = new OkadaFault3(x1.UpperBound, y1.UpperBound, x2.UpperBound, y2.UpperBound, xC.UpperBound, yC.UpperBound, dep.UpperBound, isUpperCoord, strike.UpperBound, dip.UpperBound, aspectRatio.UpperBound, len.UpperBound, wid.UpperBound, ss.UpperBound, ds.UpperBound, ts.UpperBound);
        OkadaFault3 tempLB = new OkadaFault3(x1.LowerBound, y1.LowerBound, x2.LowerBound, y2.LowerBound, xC.LowerBound, yC.LowerBound, dep.LowerBound, isUpperCoord, strike.LowerBound, dip.LowerBound, aspectRatio.LowerBound, len.LowerBound, wid.LowerBound, ss.LowerBound, ds.LowerBound, ts.LowerBound);

        if ( dLtxt.equals("") || dWtxt.equals("")){
            isDistedSlip = false;
            faultInitVal = tempFault ;
            faultUB = tempUB;
            faultLB = tempLB;
        } else {
            isDistedSlip = true;
            double dLVal = Double.parseDouble(dLtxt);
            double dWVal = Double.parseDouble(dWtxt);
            //faultInitVal = divideFault(tempFault, dLVal, dWVal) ;
            faultInitVal =  new DistributedFault(tempFault, dLVal, dWVal) ;
            
            //TODO fix the constriant and GMatrix

            //TODO: move the below code to another dialog (because they are per-problem, not per-fault)
//            if (d.lsDialog != null){
//                this.nonNeg = d.lsDialog.applyNonNegativity();
//                if (d.lsDialog.applyMomentConstraints()){
//                    this.targetMoment = d.lsDialog.getTargetMoment();
//                    //TODO: remove switch case, put in a more universial type inside lsDialog
//                    switch (d.lsDialog.getEqualityType()){
//                    case equality:
//                        this.momentConType = ConstraintType.EQUAL;
//                        break;
//                    case greatherThanOrEqual:
//                        this.momentConType = ConstraintType.GREATER_THAN_OR_EQUAL;
//                        break;
//                    case lessThanOrEqual:
//                        this.momentConType = ConstraintType.LESS_THAN_OR_EQUAL;
//                        break;
//                    }
//                }
//            }
                faultLB = d.lsDialog.getLB();
                faultUB = d.lsDialog.getUB();
                faultLB.setMsp(tempLB.getMsp());
                faultUB.setMsp(tempUB.getMsp());

            //TODO implement bounds better
            //TODO remove this hackish way of checking if it's distributed faults problems
        }
    }
    
    
    

    /**
     * Returns a copy of the model sources
     * @param faultInitVal2
     * @return
     */
    private List<DisplacementSolver> copyOf(List<DisplacementSolver> faultInitVal2){
        final int sourceLen = faultInitVal2.size();
        List<DisplacementSolver> ret = new ArrayList<DisplacementSolver>(sourceLen);
        for (int iter = 0; iter < sourceLen; iter++){
            try {
                ret.add( faultInitVal2.get(iter).clone() ) ;
            } catch (Exception e){  //tossing it, because it won't happen
                e.printStackTrace();
            }
        }
        return ret;
    }
    
    private DoubleWithRange parseUISlips(
            final javax.swing.JCheckBox presentCheckBox,
            final javax.swing.JRadioButton radioOfPositive) { 
        boolean present = presentCheckBox.isSelected();
        if (present){
            if (radioOfPositive == null || radioOfPositive.isSelected()){
                return new DoubleWithRange(1.0d, 0.0d, Double.MAX_VALUE);
            } else {
                return new DoubleWithRange(-1.0d, -Double.MAX_VALUE, 0.0d );
            }
        } else {
            return new DoubleWithRange(0.0d, Double.NaN, Double.NaN);
        }
    }
    
    private DoubleWithRange parseUISlips(
            final javax.swing.JCheckBox presentCheckBox,
            javax.swing.JTextField slipValue) {
        boolean present = presentCheckBox.isSelected();
        String slipString = slipValue.getText();
        if (present){
            if (slipString.equals("")){
                return new DoubleWithRange(0.0d, -Double.MAX_VALUE, Double.MAX_VALUE);
            } else {
                return new DoubleWithRange(Double.parseDouble(slipString), -Double.MAX_VALUE, Double.MAX_VALUE);
            }
        }else {
            return new DoubleWithRange(0.0d, Double.NaN, Double.NaN);
        }
    }
    
    private DoubleWithRange parseUIchkNTxtBox(
            final javax.swing.JCheckBox fixedCheckBox,
            final javax.swing.JTextField valueBox,
            final javax.swing.JTextField UBBox, final javax.swing.JTextField LBBox) {
        boolean fixed = fixedCheckBox.isSelected();
        
        if (fixed){
            return new DoubleWithRange(parseJTextField(valueBox), Double.NaN, Double.NaN);
        } else {
            double lowerbound = parseJTextField(LBBox);
            double upperbound = parseJTextField(UBBox);
            double guess = (upperbound + lowerbound) * 0.5d;
            return new DoubleWithRange(guess, lowerbound,upperbound);
        }
    }
    
    private double parseJTextField(final javax.swing.JTextField valueBox){
        String valueText = valueBox.getText();
        if (valueText.equals("")){
            return Double.NaN;
        } else {
            return Double.parseDouble(valueText);
        }
    }
    
    private static class DoubleWithRange{
        public double value;
        public double LowerBound;
        public double UpperBound;
        
        public DoubleWithRange(double value, double lowerBound,
                double upperBound) {
            super();
            this.value = value;
            LowerBound = lowerBound;
            UpperBound = upperBound;
        }
        
        public DoubleWithRange(double val2FillAll){
            super();
            this.value = val2FillAll;
            LowerBound = val2FillAll;
            UpperBound = val2FillAll;
        }
        
        public DoubleWithRange(){
            this(Double.NaN);
        }
    }
}
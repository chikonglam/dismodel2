package gov.usgs.dismodel.calc.greens;

import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.jzy.DistributedFaultViewable;
import gov.usgs.dismodel.state.DisplayStateStore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.AbstractDrawable;

/**
 * @author clam-PR
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = { "rowCt", "colCt", "dLength", "dWidth", "subfaults"})
public class DistributedFault extends OkadaFault3 {
    private double dLength;
    private double dWidth;
    private int rowCt;
    private int colCt;
    
    private OkadaFault3[][] subfaults;
    
    
    public DistributedFault() {
        super();
        name = "DistFault" + super.getClassCount();
    }
    
    public DistributedFault(OkadaFault3 overallFault, double dL, double dW) {
        super(overallFault);
        name = "DistFault" + super.getClassCount();
        double length = overallFault.getLength();
        double width = overallFault.getWidth();
        this.colCt = (int) Math.ceil( length / dL );
        this.rowCt = (int) Math.ceil( width / dW );
        this.dLength = length / colCt;
        this.dWidth = width / rowCt;
        
        this.subfaults = divideFault(overallFault, colCt, rowCt);
    }
    
    public DistributedFault(int rowCt, int colCt){      //for distributed slip bounds
        this();
        this.colCt = colCt;
        this.rowCt = rowCt;
        this.dLength = Double.NaN;
        this.dWidth = Double.NaN;
        
        subfaults = new OkadaFault3[rowCt][colCt];
        for (int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter = 0; colIter < colCt; colIter++){
                OkadaFault3 curSubFault = new OkadaFault3(Double.NaN, Double.NaN, Double.NaN, Double.NaN, 
                        Double.NaN, Double.NaN, Double.NaN, false, Double.NaN, Double.NaN, Double.NaN, 
                        Double.NaN, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                subfaults[rowIter][colIter] = curSubFault;
            }
        }
    }
    
    public DistributedFault(double length, int dL, double width, double dW) {       //for distributed slip bounds
        this();
        this.colCt = (int) Math.ceil( length / dL );
        this.rowCt = (int) Math.ceil( width / dW );
        this.dLength = length / colCt;
        this.dWidth = width / rowCt;
        
        this.dLength = Double.NaN;
        this.dWidth = Double.NaN;
        
        subfaults = new OkadaFault3[rowCt][colCt];
        for (int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter = 0; colIter < colCt; colIter++){
                OkadaFault3 curSubFault = new OkadaFault3(Double.NaN, Double.NaN, Double.NaN, Double.NaN, 
                        Double.NaN, Double.NaN, Double.NaN, false, Double.NaN, Double.NaN, Double.NaN, 
                        Double.NaN, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                subfaults[rowIter][colIter] = curSubFault;
            }
        }
    }
    
    
    
    public DistributedFault(DistributedFault that){
        super(that);
        this.dLength = that.dLength;
        this.dWidth = that.dWidth;
        this.rowCt = that.rowCt;
        this.colCt = that.colCt;
        this.subfaults = copyOf(that.subfaults);
    }
    
    public DistributedFault clone() throws CloneNotSupportedException {
        DistributedFault newClone = (DistributedFault) super.clone();
        newClone.msp = this.getModeledSourceParamsCopy();
        subfaults = copyOf(this.subfaults);
        return newClone;
     }

    static public OkadaFault3[][] divideFault(OkadaFault3 overallFault, int lengthStep, int widthStep){
        final double x1 = overallFault.getUpperX1();
        final double y1 = overallFault.getUpperY1();
        final double x2 = overallFault.getUpperX2();
        final double y2 = overallFault.getUpperY2();
        
        int direction = Double.compare(x2, x1);
        if (direction == 0 ) direction = Double.compare(y2, y1);
        double startX = x1;
        double startY = y1;
        double dirComp = 1.0;
        double strikeDeg = overallFault.getStrike();
        
        if (direction < 0){
        	startX = x2;
        	startY = y2;
        	dirComp = -1.0;
        }
                
        
        final double u1 = overallFault.getUpperUp();
        final double length = overallFault.getLength();
        final double width = overallFault.getWidth();
        final double dipDeg = overallFault.getDip();
        final double SS = overallFault.getStrikeSlip();
        final double DS = overallFault.getDipSlip();
        final double TS = overallFault.getOpening();
        
        final double dip = Math.toRadians(dipDeg);
        final double strike = Math.toRadians(strikeDeg);
        
        final double realDx = length * Math.sin(strike) / lengthStep;
        final double realDy = length * Math.cos(strike) / lengthStep;
        final double realDw = width / widthStep;
        final double realDU = -realDw * Math.sin(dip);
        final double projectedDw = realDw * Math.cos(dip);
        final double dxPerUIter = projectedDw * Math.cos(strike);
        final double dyPerUIter = -projectedDw * Math.sin(strike);
        
        final double shearMod = overallFault.getShearModulus();
        
        
        
        OkadaFault3[][] dividedFaults = new OkadaFault3[widthStep][lengthStep];
        for (int dLIter = 0; dLIter < lengthStep; dLIter++){
            double curUpperX1 = startX + (realDx * dLIter) * dirComp;
            double curUpperY1 = startY + (realDy * dLIter)* dirComp;
            double curUpperX2 = curUpperX1 + realDx * dirComp;
            double curUpperY2 = curUpperY1 + realDy * dirComp;
            
            for(int dUIter = 0; dUIter < widthStep; dUIter++){
                double curU = u1 + dUIter * realDU;
                double curXAdj = dxPerUIter * dUIter;
                double curYAdj = dyPerUIter * dUIter;
                double curX1 = curUpperX1 + curXAdj;
                double curY1 = curUpperY1 + curYAdj;
                double curX2 = curUpperX2 + curXAdj;
                double curY2 = curUpperY2 + curYAdj;
                
                dividedFaults[dUIter][dLIter].setShearModulus(shearMod);
                if (direction > 0){
	                dividedFaults[dUIter][dLIter] = new OkadaFault3(curX1, curY1, curX2, curY2, 
	                        Double.NaN, Double.NaN, -curU, true, Double.NaN, dipDeg, Double.NaN, 
	                        Double.NaN, realDw, SS, DS, TS);
                } else {
	                dividedFaults[dUIter][dLIter] = new OkadaFault3(curX2, curY2, curX1, curY1, 
	                        Double.NaN, Double.NaN, -curU, true, Double.NaN, dipDeg, Double.NaN, 
	                        Double.NaN, realDw, SS, DS, TS);
                }
            }
        }
        return dividedFaults;
    }
    
    static public OkadaFault3[][] copyOf(OkadaFault3[][] sourceArray){
        final int rowCt = sourceArray.length;
        final int colCt = sourceArray[0].length;
        
        OkadaFault3[][] targetArray = new OkadaFault3[rowCt][colCt];
     
        for(int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter = 0; colIter < colCt; colIter++){
                targetArray[rowIter][colIter] = new OkadaFault3( sourceArray[rowIter][colIter]  ); 
            }
        }
        return targetArray;
    }
    
    @Override
    public XyzDisplacement solveDisplacement(LocalENU dataStationLocation) {
        XyzDisplacement curDisp = new XyzDisplacement();
        for (int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter = 0; colIter < colCt; colIter++){
                XyzDisplacement subFaultDisp = subfaults[rowIter][colIter].solveDisplacement(dataStationLocation);
                curDisp.addInto(subFaultDisp);
            }
        }
        return curDisp;
    }
    
    @XmlElement
    public double getdLength() {
        return dLength;
    }

    @XmlElement
    public double getdWidth() {
        return dWidth;
    }

    @XmlElement
    public int getRowCt() {
        return rowCt;
    }

    @XmlElement
    public int getColCt() {
        return colCt;
    }

    @XmlElementWrapper(name = "subFaults")
    @XmlElement(name = "row")
    public OkadaFault3[][] getSubfaults() {
        return subfaults;
    }

    public void setdLength(double dLength) {
        this.dLength = dLength;
    }

    public void setdWidth(double dWidth) {
        this.dWidth = dWidth;
    }

    public void setRowCt(int rowCt) {
        this.rowCt = rowCt;
    }

    public void setColCt(int colCt) {
        this.colCt = colCt;
    }

    public void setSubfaults(OkadaFault3[][] subfaults) {
        this.subfaults = subfaults;
    }

    @Override
    public AbstractDrawable toAbstractDrawable(DisplayStateStore displaySettings) {
        return new DistributedFaultViewable(this, null, this.getName());
    }
    
    @Override
    public String toString() {
        String outString =  getName() + " (DistributedFault) [colCt=" + colCt + ", dLength=" + dLength
                + ", dWidth=" + dWidth + ", rowCt=" + rowCt + ", " + super.toString() + " \n";
        for (int rowIter = 0; rowIter < rowCt; rowIter++){
            for (int colIter = 0; colIter < colCt; colIter++){
                outString += "[R" + rowIter + "C" + colIter + ":";
                OkadaFault3 curFault = subfaults[rowIter][colIter];
                double ss = curFault.getStrikeSlip();
                if (!Double.isNaN(ss) && !Double.isInfinite(ss) && ss != 0d  ){
                    outString += String.format("SS=%.3e ", ss); 
                }
                double ds = curFault.getDipSlip();
                if (!Double.isNaN(ds) && !Double.isInfinite(ds) && ds != 0d  ){
                    outString += String.format("DS=%.3e ", ds); 
                }
                double ts = curFault.getOpening();
                if (!Double.isNaN(ts) && !Double.isInfinite(ts) && ts != 0d  ){
                    outString += String.format("TS=%.3e ", ts);
                }
                outString += "]";
            }
        }
        outString += "]";
        return outString;
    }
}
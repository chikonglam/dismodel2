package gov.usgs.dismodel.calc.greens;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.usgs.dismodel.DisModel.ENUPanel;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.WWPanel;
import gov.usgs.dismodel.calc.greens.dialogs.DislocationDialogRestorable;
import gov.usgs.dismodel.geom.Angle;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.jzy.FaultViewable;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;


@XmlRootElement
@XmlType(propOrder = { "isTopCoords", "shearModulus"})
@XmlSeeAlso({DistributedFault.class})
public class OkadaFault3 extends DisplacementSolver implements Fault {
    //INDEX nums
    //-----------
    final public static int NUM_PARAMS                  = 15;
    final public static int X1_LOC_IDX                  = 0;
    final public static int Y1_LOC_IDX                  = 1;
    final public static int X2_LOC_IDX                  = 2;
    final public static int Y2_LOC_IDX                  = 3;
    final public static int XC_LOC_IDX                  = 4;
    final public static int YC_LOC_IDX                  = 5;
    final public static int DEP_LOC_IDX                 = 6;
    final public static int STRIKE_IDX                  = 7; // angle (direction) of strike movement
    final public static int DIP_IDX                     = 8; // angle in degrees from horizontal, of slip
    final public static int ASPECT_RATIO_IDX            = 9;
    final public static int LENGTH_IDX                  = 10;
    final public static int WIDTH_IDX                   = 11; // typically down (or up depending on convention used)
    final public static int STRIKE_SLIP_IDX             = 12; // horizontal distance slipped
    final public static int DIP_SLIP_IDX                = 13; // distance slipped, typically somewhat vertically
    final public static int OPENING_IDX                 = 14;
    
    
    //class constant
    //--------------
    final private static ArrayList<Integer> LINVAR = 
            new ArrayList<Integer>(Arrays.asList(new Integer[]
            {STRIKE_SLIP_IDX, DIP_SLIP_IDX, OPENING_IDX}));
    final private static double M2KM = 1e-3d;  
    
    //non-optimzed vars
    //-----------------
    private boolean isTopCoords = false;
    private double shearModulus = 1;
    private int group = -1;

    
    //Constructor
    public OkadaFault3() {
        super(NUM_PARAMS);
        name = "Fault" + super.getClassCount();
    }
    
    public OkadaFault3(double x1, double y1, double x2, double y2, double xC, double yC, double depth, boolean isTopCoords,
            double strike, double dip, double aspectRatio, double length, double width, double strikeSlip, double dipSlip, 
            double opening) {
        this();
        this.msp[X1_LOC_IDX] = x1;
        this.msp[Y1_LOC_IDX] = y1;
        this.msp[X2_LOC_IDX] = x2;
        this.msp[Y2_LOC_IDX] = y2;
        this.msp[XC_LOC_IDX] = xC;
        this.msp[YC_LOC_IDX] = yC;
        this.msp[DEP_LOC_IDX] = depth;
        this.isTopCoords = isTopCoords;
        this.msp[STRIKE_IDX]= strike;
        this.msp[DIP_IDX] = dip;
        this.msp[ASPECT_RATIO_IDX] = aspectRatio;
        this.msp[LENGTH_IDX] = length;
        this.msp[WIDTH_IDX] = width;
        this.msp[STRIKE_SLIP_IDX] = strikeSlip;
        this.msp[DIP_SLIP_IDX] = dipSlip;
        this.msp[OPENING_IDX] = opening;
    }
        
    public OkadaFault3(OkadaFault3 that){
        super(that);
        this.isTopCoords = that.isTopCoords;
        this.shearModulus = that.shearModulus;
    }
    
    public OkadaFault3 clone() throws CloneNotSupportedException {
        OkadaFault3 newClone = (OkadaFault3) super.clone();
        newClone.msp = this.getModeledSourceParamsCopy();
        return newClone;
    }
    
    @Override
    public ArrayList<Integer> getLinearParameterIndices() {
        return LINVAR;
    }
    
    @Override
    public XyzDisplacement solveDisplacement(LocalENU dataStationLocation) {
        OkadaFundamentalVars realParams = toFundamentalVars();
        displacement disp = realParams.calcDisplacement(dataStationLocation);
        return disp.toXyzDisplacement();
    }
    
    @Override
    public AbstractDrawable toAbstractDrawable(float minAxis, Color color) {
        double bottomX = getLowerXC();
        double bottomY = getLowerYC();
        double bottomZ = getLowerUp();
        
        double length = getLength();
        double width = getWidth();
        Angle strike = Angle.fromDeg(getStrike());
        Angle dip = Angle.fromDeg(getDip());
        
        
        Coord3d bottomCenter = new Coord3d(bottomX, bottomY, bottomZ);
        return new FaultViewable(bottomCenter , length, width, strike, dip, color, this.getName());
    }
    
    @Override
    public Renderable toWWJRenderable(double minAxis, LLH origin,
            java.awt.Color color) {
        OkadaFault3 tempUpper = new OkadaFault3(this);
        tempUpper.toUpperEdgeCoords();
        double x1 = tempUpper.getX1();
        double y1 = tempUpper.getY1();
        double x2 = tempUpper.getX2();
        double y2 = tempUpper.getY2();
        
        final LLH end1 = new LocalENU(x1, y1, 0, origin).toLLH();
        final LLH end2 = new LocalENU(x2, y2, 0, origin).toLLH();
 
        final LatLon latlon1 = LatLon.fromDegrees(end1.getLatitude().toDeg(), end1.getLongitude().toDeg());
        final LatLon latlon2 = LatLon.fromDegrees(end2.getLatitude().toDeg(), end2.getLongitude().toDeg());
        
        SurfacePolyline lineTemp = new SurfacePolyline(new ArrayList<LatLon>(Arrays.asList(latlon1, latlon2 )));
        ShapeAttributes lineAttrs = new BasicShapeAttributes();
        lineAttrs.setOutlineMaterial(new Material(color));
        lineAttrs.setOutlineWidth(5);       //TODO: Change this to be more modular
        lineTemp.setAttributes(lineAttrs);
        
        return lineTemp;
    }
    
    @Override
    public int getNumSolutionParams() {
        return NUM_PARAMS;
    }
    
    //other public methods
    @Override       //TODO Make a real toString
    public String toString() {
        String outStr = getName() + " (OkadaFault3) [";
        String coordsuffix;
        if (isTopCoords){
            coordsuffix = "U";
        } else {
            coordsuffix = "L";
        }
        
        if (Double.isNaN(msp[X1_LOC_IDX])){     //center point
            outStr += "Xc" + coordsuffix + String.format("=%.3e, ", msp[XC_LOC_IDX]);
            outStr += "Yc" + coordsuffix + String.format("=%.3e, ", msp[YC_LOC_IDX]);
        } else {                                //2 points
            outStr += "X1" + coordsuffix + String.format("=%.3e, ", msp[X1_LOC_IDX]);
            outStr += "Y1" + coordsuffix + String.format("=%.3e, ", msp[Y1_LOC_IDX]);
            outStr += "X2" + coordsuffix + String.format("=%.3e, ", msp[X2_LOC_IDX]);
            outStr += "Y2" + coordsuffix + String.format("=%.3e, ", msp[Y2_LOC_IDX]);
        }
        outStr += "Dep" + coordsuffix + String.format("=%.3e, ", msp[DEP_LOC_IDX]);
        if (!Double.isNaN(msp[STRIKE_IDX])) outStr += String.format("Strike(deg)=%.3e, ", msp[STRIKE_IDX]);
        if (!Double.isNaN(msp[DIP_IDX])) outStr += String.format("Dip(Deg)=%.3e, ", msp[DIP_IDX]);
        if (!Double.isNaN(msp[ASPECT_RATIO_IDX])) outStr += String.format("AR=%.3e, ", msp[ASPECT_RATIO_IDX]);
        if (!Double.isNaN(msp[LENGTH_IDX])) outStr += String.format("Length=%.3e, ", msp[LENGTH_IDX]);
        if (!Double.isNaN(msp[WIDTH_IDX])) outStr += String.format("Width=%.3e, ", msp[WIDTH_IDX]);
        outStr += String.format("SS=%.3e, DS=%.3e, TS=%.3e, ShearModulus=%.3e]", msp[STRIKE_SLIP_IDX], msp[DIP_SLIP_IDX], msp[OPENING_IDX], shearModulus);
        return outStr;
    }
    
    public double getX1(){
        if (isTopCoords){
            return getUpperX1();
        } else {
            return getLowerX1();
        }
    }
    
    public double getY1(){
        if (isTopCoords){
            return getUpperY1();
        } else {
            return getLowerY1();
        }
    }
    
    public double getX2(){
        double x1 = getX1();
        double L = getLength();
        double strike = getStrike();
        double sinStrike = Math.sin(Math.toRadians(strike));
        double x2 = x1 + L * sinStrike;
        return x2;
    }
    
    public double getY2(){
        double y1 = getY1();
        double L = getLength();
        double strike = getStrike();
        double cosStrike = Math.cos( Math.toRadians(strike) );
        double y2 = y1 + L * cosStrike;
        return y2;
    }
    
    public double getXc(){
        double xC = (getX1() + getX2()) / 2.0;
        return xC;
    }
    
    public double getYc(){
        double yC = (getY1() + getY2()) / 2.0;
        return yC;
    }
    
    public double getDepth(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        double depth = realParams.z0 / M2KM;
        if (!isTopCoords){
            return depth;
        } else {
            double dip = realParams.delta;
            double width = realParams.W / M2KM;
            double sinDip = Math.sin( Math.toRadians(dip) );
            return depth - width * sinDip;
        }
    }
    
    public double getStrike(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.phi;
    }
    
    public double getDip(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.delta;
    }
    
    public double getAspectRatio(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return (realParams.L / realParams.W);
    }
    
    public double getLength(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.L / M2KM;
    }
    
    public double getWidth(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.W / M2KM;
    }
    
    public double getStrikeSlip(){
        return this.msp[STRIKE_SLIP_IDX];
    }
    
    public double getDipSlip(){
        return this.msp[DIP_SLIP_IDX];
    }
    
    public double getOpening(){
        return this.msp[OPENING_IDX];
    }
    
    public double getLowerX1(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.x0 / M2KM;
    }
    
    public double getUpperX1(){
        double lowerX1 = getLowerX1();
        double width = getWidth();
        double dip = Math.toRadians(getDip());
        double strike = Math.toRadians(getStrike());
        
        double projSurface = width * Math.cos(dip);
        double dX = -projSurface * Math.cos(strike);
        
        return lowerX1 + dX;
    }
 
    public double getLowerY1(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return realParams.y0 / M2KM;
    }

    public double getUpperY1(){
        double lowerY1 = getLowerY1();
        double width = getWidth();
        double dip = Math.toRadians(getDip());
        double strike = Math.toRadians(getStrike());
        
        double projSurface = width * Math.cos(dip);
        double dY = projSurface * Math.sin(strike);
        
        return lowerY1 + dY;
    }

    public double getLowerX2(){
        double x1 = getLowerX1();
        double L = getLength();
        double strike = getStrike();
        double sinStrike = Math.sin(Math.toRadians(strike));
        double x2 = x1 + L * sinStrike;
        return x2;
    }

    public double getUpperX2(){
        double x1 = getUpperX1();
        double L = getLength();
        double strike = getStrike();
        double sinStrike = Math.sin(Math.toRadians(strike));
        double x2 = x1 + L * sinStrike;
        return x2;
    }
    
    public double getLowerY2(){
        double y1 = getLowerY1();
        double L = getLength();
        double strike = getStrike();
        double cosStrike = Math.cos( Math.toRadians(strike) );
        double y2 = y1 + L * cosStrike;
        return y2;
    }
    
    public double getUpperY2(){
        double y1 = getUpperY1();
        double L = getLength();
        double strike = getStrike();
        double cosStrike = Math.cos( Math.toRadians(strike) );
        double y2 = y1 + L * cosStrike;
        return y2;
    }
    
    public double getLowerXC(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        double x0 = realParams.x0 / M2KM;
        double L = realParams.L / M2KM ;
        double strikeRad = Math.toRadians(realParams.phi);
        double dx = L * Math.sin(strikeRad) / 2.0;
        return x0 + dx;
    }
    
    public double getLowerYC(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        double y0 = realParams.y0 / M2KM;
        double L = realParams.L / M2KM;
        double strikeRad = Math.toRadians(realParams.phi);
        double dy = L * Math.cos(strikeRad) / 2.0;
        return y0 + dy;
    }
    
    public double getUpperXC(){
        return (getUpperX1() + getUpperX1())/2.0;
    }
    
    public double getUpperYC(){
        return (getUpperY1() + getUpperY1())/2.0;
    }
    
    public double getLowerUp(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        return -realParams.z0 / M2KM;
    }
    
    public double getUpperUp(){
        OkadaFundamentalVars realParams = toFundamentalVars();
        double depth = realParams.z0 / M2KM;
        double dip = realParams.delta;
        double width = realParams.W / M2KM;
        double sinDip = Math.sin( Math.toRadians(dip) );
        return -depth + width * sinDip;
    }
    
    public double getUp(){
        return -getDepth();
    }
    
    @Override
    public double getFaultSize(){
        double length = getLength();
        double width = getWidth();
        return length * width;
    }
    
    @Override
    public double getMagnitude(){
        double ss = msp[STRIKE_SLIP_IDX];
        double ds = msp[DIP_SLIP_IDX];
        double ts = msp[OPENING_IDX];
        
        double slipMag = Math.sqrt(ss * ss + ds * ds + ts * ts); 
        return slipMag;
    }
    
    public boolean isTopCoords() {
        return isTopCoords;
    }

    public double[] getCenterENU(){
        double upperE = getUpperXC();       //TODO: make this more direct => faster
        double upperN = getUpperYC();
        double upperU = getUpperUp();
        double lowerE = getLowerXC();
        double lowerN = getLowerYC();
        double lowerU = getLowerUp();
        
        double[] enuCoordRet = new double[]{(upperE + lowerE)/2d, (upperN + lowerN)/2d, (upperU + lowerU)/2d };
        
        return enuCoordRet;        
    }

    public int getGroup(){
    	return group;
    }

    //mutators
    public void toUpperEdgeCoords(){
        if (isTopCoords) return;
        
        OkadaFundamentalVars realParams = toFundamentalVars();
        
        double width = realParams.W / M2KM;
        double strike = Math.toRadians(realParams.phi);
        double dip = Math.toRadians(realParams.delta);
        double projectedWidth = width * Math.cos(dip);
        
        double dX = -projectedWidth * Math.cos(strike);
        double dY = projectedWidth * Math.sin(strike);
        double dZ = width * Math.sin(dip);
        
        if (Double.isNaN(msp[X1_LOC_IDX])) {        //only the center is defined
            msp[XC_LOC_IDX] += dX;
            msp[YC_LOC_IDX] += dY;
        } else {
            msp[X1_LOC_IDX] += dX;
            msp[Y1_LOC_IDX] += dY;
            msp[X2_LOC_IDX] += dX;
            msp[Y2_LOC_IDX] += dY;
        }
        msp[DEP_LOC_IDX] -= dZ;
        
        isTopCoords = true;
    }
    
    public void toLowerEdgeCoords(){
        if (!isTopCoords ) return;
        OkadaFundamentalVars realParams = toFundamentalVars();
        
        double width = realParams.W / M2KM;
        double strike = Math.toRadians(realParams.phi);
        double dip = Math.toRadians(realParams.delta);
        double projectedWidth = width * Math.cos(dip);
        
        double dX = projectedWidth * Math.cos(strike);
        double dY = -projectedWidth * Math.sin(strike);
        double dZ = -width * Math.sin(dip);
        
        if (Double.isNaN(msp[X1_LOC_IDX])) {        //only the center is defined
            msp[XC_LOC_IDX] += dX;
            msp[YC_LOC_IDX] += dY;
        } else {
            msp[X1_LOC_IDX] += dX;
            msp[Y1_LOC_IDX] += dY;
            msp[X2_LOC_IDX] += dX;
            msp[Y2_LOC_IDX] += dY;
        }
        msp[DEP_LOC_IDX] -= dZ;
        
        isTopCoords = false;
    }
    
    @Override
    public void offsetLocation(double east, double north, double up) {
        msp[X1_LOC_IDX] += east;
        msp[Y1_LOC_IDX] += north;
        msp[X2_LOC_IDX] += east;
        msp[Y2_LOC_IDX] += north;
        msp[XC_LOC_IDX] += east;
        msp[YC_LOC_IDX] += north;
        msp[DEP_LOC_IDX] -= up;
    }
    
    @XmlTransient
    public void setStrikeSlip(double strikeSlip){
        msp[STRIKE_SLIP_IDX] = strikeSlip;
    }
    
    @XmlTransient
    public void setDipSlip(double dipSlip){
        msp[DIP_SLIP_IDX] = dipSlip;
    }
    
    @XmlTransient
    public void setOpening(double opening){
        msp[OPENING_IDX] = opening;
    }
    
    public void setGroup(int group){
    	this.group = group;
    }
    
    public void setNoGroup(){
    	this.group = -1;
    }
    
    

	//setters and getters for JAXB compliance
    //------------------------
    public double getShearModulus() {
		return shearModulus;
	}


	public void setShearModulus(double shearModulus) {
		this.shearModulus = shearModulus;
	}
	
    public boolean getIsTopCoords() {
		return this.isTopCoords;
	}

	
	public void setIsTopCoords(boolean isTopCoords) {
		this.isTopCoords = isTopCoords;
	}

	//okada 85 stuff
    private displacement okada85(final double x0, final double y0, final double z0, final double L, final double W, final double phiDeg, final double deltaDeg, final double Us, final double Ud, final double Ut, final double mu, final double nu, final double x, final double y){
        //% [1] Set the parameters for the Okada (1985) dislocation model ***********
        //% Lame's first parameter
        double lambda = (((2d * mu) * nu) / (1d - (2d * nu)));
        //% translate the coordinates of the points where the displacement is computed
        //% in the coordinates systen centered in (x0,y0)
        double xxn = (x - x0);
        double yyn = (y - y0);
        
        double d = z0;

        //temp vars
        double phi = Math.toRadians(phiDeg);
        double delta = Math.toRadians(deltaDeg);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        double sinDelta = Math.sin(delta);
        double cosDelta = Math.cos(delta);
        double tanDelta = Math.tan(delta);
        
        
        //% rotate the coordinate system to be coherent with the model coordinate
        //% system of Figure 1 (Okada, 1985)
        double xxp = (((sinPhi) * xxn) + ((cosPhi) * yyn));
        double yyp = (((-((cosPhi))) * xxn) + ((sinPhi) * yyn));

        //% [2] Compute the displacement and displacement gradient matrix ***********
        //% Okada (1985), equation (30)
        double p = ((yyp * (Math.cos(delta))) + (d * (Math.sin(delta))));
        double q = ((yyp * (Math.sin(delta))) - (d * (Math.cos(delta))));
        
        displacement fd1 = ok85feeder(xxp,p,q,mu,lambda,Us, Ud, Ut, sinDelta, cosDelta, tanDelta);
        displacement fd2 = ok85feeder(xxp,p-W,q,mu,lambda,Us, Ud, Ut,sinDelta, cosDelta, tanDelta);
        displacement fd3 = ok85feeder(xxp-L,p,q,mu,lambda,Us, Ud, Ut, sinDelta, cosDelta, tanDelta);
        displacement fd4 = ok85feeder(xxp-L,p-W,q,mu,lambda,Us, Ud, Ut, sinDelta, cosDelta, tanDelta);
        
        //% displacement, Chinnery's notation, Okada (1985), equation (24)
        double Upx = (fd1.x - fd2.x - fd3.x + fd4.x);
        double Upy = (fd1.y - fd2.y - fd3.y + fd4.y);
        
        //% Rotate the horizontal displacement components Upx and Upy back
        double ux = (((sinPhi) * Upx) - ((cosPhi) * Upy));
        double uy = (((sinPhi) * Upy) + ((cosPhi) * Upx));
        double uz = (fd1.z - fd2.z - fd3.z + fd4.z);
        
        return new displacement(ux, uy, uz);
    }
    
    
    private displacement ok85feeder(final double csi, final double eta, final double q, final double mu, final double lambda, final double Us, final double Ud, final double Ut, final double sinDelta, final double cosDelta, final double tanDelta){
        //% *** GENERAL PARAMETERS **************************************************
        //% Okada (1985), equation (30)
        double ytilde = ((eta * (cosDelta)) + (q * (sinDelta)));
        double dtilde = ((eta * (sinDelta)) - (q * (cosDelta)));
        double R2 = (((Math.pow(csi, 2d)) + (Math.pow(ytilde, 2d))) + (Math.pow(dtilde, 2d)));
        double R = (Math.sqrt(R2));
        double X2 = ((Math.pow(csi, 2d)) + (Math.pow(q, 2d)));
        double X = (Math.sqrt(X2));
        
        double alpha = (mu / (lambda + mu));
        double Rcsi = (1d / (R + csi));
        
        //% check singularity condition (iii), pg 1148, Okada (1985)
        double Reta, lnReta;
        if (Math.abs(R+eta) < 1E-16){
            Reta = 0;
            lnReta = (-((Math.log((R - eta)))));
        } else {
            Reta = (1d / (R + eta));
            lnReta = (Math.log((R + eta)));
        }
        
        //% Okada (1985), equation (36)
        //double Ac = (((2d * R) + csi) / ((Math.pow(R, 3d)) * (Math.pow((R + csi), 2d))));             //not used for just displacements
        //double An = (((2d * R) + eta) / ((Math.pow(R, 3d)) * (Math.pow((R + eta), 2d))));             //not used for just displacements

        /*                                                                                              //not used for just displacements
        //% Okada (1985), equation (40) and (41)
        //% check singularity for cos(delta)=0
        double K1, K2, K3;
        if (cosDelta < 1E-16){        //% Okada (1985), equation (41)
            K1 = (((alpha * csi) * q) / (R * (Math.pow((R + dtilde), 2d))));
            K3 = ((alpha * ((sinDelta) / (R + dtilde))) * (((Math.pow(csi, 2d)) / (R * (R + dtilde))) - 1d));
        } else {
            K3 = ((alpha * (1d / (cosDelta))) * (((q * Reta) / R) - (ytilde / (R * (R + dtilde)))));
            K1 = ((alpha * (csi / (cosDelta))) * ((1d / (R * (R + dtilde))) - (((sinDelta) * Reta) / R)));
        }
        K2 = ((alpha * (((-((sinDelta))) / R) + (((q * (cosDelta)) * Reta) / R))) - K3);
        
        */

        //% *** DISPLACEMENT PREP ********************************************************
        //% check singularity for cos(delta)=0
        double I1, I2, I3, I4, I5;
        if (cosDelta < 1E-16){        //% Okada (1985), equation (29)
            I1 = (((((-(0.5d)) * alpha) * csi) * q) / (Math.pow((R + dtilde), 2d)));
            I3 = ((0.5d * alpha) * (((eta / (R + dtilde)) + ((ytilde * q) / (Math.pow((R + dtilde), 2d)))) - lnReta));
            I4 = (((-(alpha)) * q) / (R + dtilde));
            I5 = ((((-(alpha)) * csi) * (sinDelta)) / (R + dtilde));
        } else {            //% Okada (1985), equation (28)   
            I4 = ((alpha * (1d / (cosDelta))) * ((Math.log((R + dtilde))) - ((sinDelta) * lnReta)));
            if (Math.abs(csi) < 1E-16){         //% check singularity condition (ii), pp 1148, Okada (1985)
                I5 = 0;
            } else {
                I5 = ((alpha * (2d / (cosDelta))) * (Math.atan((((eta * (X + (q * (cosDelta)))) + ((X * (R + X)) * (sinDelta))) / ((csi * (R + X)) * (cosDelta))))));
            }
            I3 = ((alpha * ((((1d / (cosDelta)) * ytilde) / (R + dtilde)) - lnReta)) + ((tanDelta) * I4));
            I1 = ((alpha * (((-(1d)) / (cosDelta)) * (csi / (R + dtilde)))) - ((tanDelta) * I5));
        }
        I2 = ((alpha * (-(lnReta))) - I3);
        
        //now the real calls
        displacement accumDisp = new displacement();
        if (Us != 0d){
            displacement curDisp = ok8525(csi, sinDelta, cosDelta, dtilde, eta, q, R, Reta, ytilde, Us, I1, I2, I4);
            accumDisp.add( curDisp );
        }
        
        if (Ud != 0d){
            displacement curDisp = ok8526(csi, sinDelta, cosDelta, dtilde, eta, q, R, Rcsi, ytilde, Ud, I1, I3, I5) ;
            accumDisp.add( curDisp );
        }
        
        if (Ut != 0d){
            displacement curDisp =  ok8527(csi, sinDelta, cosDelta, dtilde, eta, q, R, Rcsi, Reta, ytilde, Ut, I1, I3, I5);
            accumDisp.add( curDisp );
        }
        
        return accumDisp;
    }
    
    private OkadaFundamentalVars toFundamentalVars(){
        final double x1In =             msp[X1_LOC_IDX];
        final double y1In =             msp[Y1_LOC_IDX];
        final double x2In =             msp[X2_LOC_IDX];
        final double y2In =             msp[Y2_LOC_IDX];
        final double xCIn =             msp[XC_LOC_IDX];
        final double yCIn =             msp[YC_LOC_IDX];
        final double depthIn =          msp[DEP_LOC_IDX];
        final double strikeIn =         msp[STRIKE_IDX];
        final double dipIn =            msp[DIP_IDX];
        final double aspectRatioIn =    msp[ASPECT_RATIO_IDX];
        final double lengthIn =         msp[LENGTH_IDX];
        final double widthIn =          msp[WIDTH_IDX];
        final double ssIn =             msp[STRIKE_SLIP_IDX];
        final double dsIn =             msp[DIP_SLIP_IDX];
        final double tsIn =             msp[OPENING_IDX];
        
        //tempvars
        double cosStrike, sinStrike, phiRad, x0, y0, u0, L, W;
        
        //target vars
        double x0KM, y0KM, z0KM, LKM, WKM, phi, delta, Us, Ud, Ut;
        
        //no calculations involved
        delta = dipIn;
        Us = ssIn;
        Ud = dsIn;
        Ut = tsIn;
        u0 = -depthIn;
        
        
        if (!Double.isNaN(x1In)){                           //1 or 2 ends are given
        	//midpointGiven = false;
            x0 = x1In;
            y0 = y1In;
            if (!Double.isNaN(x2In)){                       //2 ends are given, calculate L and strike
//            	twopointsGiven = true;
                double dX = (x2In - x1In);
                double dY = (y2In - y1In);
                L  = Math.hypot(dX, dY);
                phiRad = Math.atan2(dX, dY);
                phi = Math.toDegrees(phiRad);
            } else {                                        //only 1 end is given => L and strike should be given too
//            	twopointsGiven = false;
                if (Double.isNaN(lengthIn)){                        //calculate length if it's not defined
//                    lengthGiven = false;
                	L = aspectRatioIn * widthIn;
                } else {
//                	lengthGiven = true;
                    L = lengthIn;
                }
                phi = strikeIn;
                phiRad = Math.toRadians(phi);
            }
            sinStrike = Math.sin(phiRad);
            cosStrike = Math.cos(phiRad);

            
        } else {                                           //the center is given
//        	midpointGiven = true;
            if (Double.isNaN(lengthIn)){                        //calculate length if it's not defined
//                lengthGiven = false;
            	L = aspectRatioIn * widthIn;
            } else {
//            	lengthGiven = true;
                L = lengthIn;
            }
            phi = strikeIn;
            sinStrike = Math.sin(Math.toRadians(strikeIn));
            cosStrike = Math.cos(Math.toRadians(strikeIn));
            x0 = xCIn - L / 2.0 * sinStrike;
            y0 = yCIn - L / 2.0 * cosStrike;
        }
        
        if (Double.isNaN(widthIn)){         //calc width
//        	widthGiven = false;
            W = lengthIn / aspectRatioIn;
        } else {
//        	widthGiven = true;
            W = widthIn;
        }
        
        if (isTopCoords){      //convert to bottom coord if not
            final double cosDip = Math.cos( Math.toRadians(delta) );
            final double sinDip = Math.sin( Math.toRadians(delta) );
            final double projSurfaceWidth = W * cosDip;
            
            x0 = x0 + projSurfaceWidth * cosStrike;
            y0 = y0 - projSurfaceWidth * sinStrike;
            u0 = u0 - W * sinDip;
        }
        
        //convert to Okada scale
        x0KM = x0 * M2KM;
        y0KM = y0 * M2KM;
        z0KM = -u0 * M2KM;
        LKM = L * M2KM;
        WKM = W * M2KM;
        
        return new OkadaFundamentalVars(x0KM, y0KM, z0KM, LKM, WKM, phi, delta, Us, Ud, Ut);
    }
    
    //private OkadaFundamentalVars class
    private class OkadaFundamentalVars{
        public double x0=0;
        public double y0=0;
        public double z0=0;
        public double L=0;
        public double W=0;
        public double phi=0;
        public double delta=0;
        public double Us=0;
        public double Ud=0;
        public double Ut=0;
        
        
        public OkadaFundamentalVars(double x0, double y0, double z0, double l,
                double w, double phi, double delta, double us, double ud,
                double ut) {
            super();
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            L = l;
            W = w;
            this.phi = phi;
            this.delta = delta;
            Us = us;
            Ud = ud;
            Ut = ut;
        }
        
        public displacement calcDisplacement(LocalENU stationLoc){
            double x = stationLoc.getEasting() * M2KM; 
            double y = stationLoc.getNorthing() * M2KM;
            return okada85(x0, y0, z0, L, W, phi, delta, Us, Ud, Ut, shearModulus, getPoissonRatio(), x, y);
        }
        
    }
    
    
    //private displacement class
    private static class displacement{
        public double x = 0;
        public double y = 0;
        public double z = 0;
        
        public displacement(){
        }
        
        public displacement(double x, double y, double z) {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public void add(final displacement that){
            this.x += that.x;
            this.y += that.y;
            this.z += that.z;
        }
        
        public XyzDisplacement toXyzDisplacement(){
            return new XyzDisplacement(this.x, this.y, this.z);
        }
 
    }
    
    //individual fault slip methods that only calculates displacements
    private displacement ok8525(double csi,  final double sinDelta, final double cosDelta, double dtilde, double eta, double q, double R, double Reta,  double ytilde, double U1, double I1, double I2,  double I4){
        double ux;
        if (Math.abs(q ) < 1E-16){
            ux = ((-((U1 / (2d * Math.PI)))) * ((((csi * q) * Reta) / R) + (I1 * (sinDelta))));
        } else {
            ux = ((-((U1 / (2d * Math.PI)))) * (((((csi * q) * Reta) / R) + (Math.atan(((csi * eta) / (q * R))))) + (I1 * (sinDelta))));
        }
        double uy = ((-((U1 / (2d * Math.PI)))) * (((((ytilde * q) * Reta) / R) + ((q * (cosDelta)) * Reta)) + (I2 * (sinDelta))));
        double uz = ((-((U1 / (2d * Math.PI)))) * (((((dtilde * q) * Reta) / R) + ((q * (sinDelta)) * Reta)) + (I4 * (sinDelta))));
        
        return new displacement(ux, uy, uz);
    }
    
    private displacement ok8526(double csi, final double sinDelta, final double cosDelta, double dtilde, double eta, double q, double R, double Rcsi, double ytilde, double U2, double I1, double I3, double I5 ){
        double ux, uy, uz;
        ux = ((-((U2 / (2d * Math.PI)))) * ((q / R) - ((I3 * (sinDelta)) * (cosDelta))));

        if (Math.abs(q) < 1E-16)  {           //% check singularity condition (i), pp 1148, Okada (1985)
            uy = ((-((U2 / (2d * Math.PI)))) * ((((ytilde * q) * Rcsi) / R) + (((-(I1)) * (sinDelta)) * (cosDelta))));
            uz = ((-((U2 / (2d * Math.PI)))) * ((((dtilde * q) * Rcsi) / R) + (((-(I5)) * (sinDelta)) * (cosDelta))));
        } else {
            uy = ((-((U2 / (2d * Math.PI)))) * (((((ytilde * q) * Rcsi) / R) + ((cosDelta) * (Math.atan(((csi * eta) / (q * R)))))) - ((I1 * (sinDelta)) * (cosDelta))));
            uz = ((-((U2 / (2d * Math.PI)))) * (((((dtilde * q) * Rcsi) / R) + ((sinDelta) * (Math.atan(((csi * eta) / (q * R)))))) - ((I5 * (sinDelta)) * (cosDelta))));
        }
        return new displacement(ux, uy, uz);
    }
    
    private displacement ok8527(double csi, final double sinDelta, final double cosDelta, double dtilde, double eta, double q, double R, double Rcsi, double Reta, double ytilde, double U3, double I1, double I3, double I5){
        double ux = ((U3 / (2d * Math.PI)) * ((((Math.pow(q, 2d)) * Reta) / R) - (I3 * (Math.pow((sinDelta), 2d)))));
        double uy, uz;
        if (Math.abs(q) < 1E-16){           //% check singularity condition (i), pp 1148, Okada (1985)
            uy = ((U3 / (2d * Math.PI)) * ((((((-(dtilde)) * q) * Rcsi) / R) - ((sinDelta) * (((csi * q) * Reta) / R))) - (I1 * (Math.pow((sinDelta), 2d)))));
            uz = ((U3 / (2d * Math.PI)) * (((((ytilde * q) * Rcsi) / R) + ((cosDelta) * (((csi * q) * Reta) / R))) - (I5 * (Math.pow((sinDelta), 2d)))));
        } else {
            uy = ((U3 / (2d * Math.PI)) * ((((((-(dtilde)) * q) * Rcsi) / R) - ((sinDelta) * ((((csi * q) * Reta) / R) - (Math.atan(((csi * eta) / (q * R))))))) - (I1 * (Math.pow((sinDelta), 2d)))));
            uz = ((U3 / (2d * Math.PI)) * (((((ytilde * q) * Rcsi) / R) + ((cosDelta) * ((((csi * q) * Reta) / R) - (Math.atan(((csi * eta) / (q * R))))))) - (I5 * (Math.pow((sinDelta), 2d)))));
        }
        return new displacement(ux, uy, uz);
    }

	@Override
	public JDialog toJDialog(Window owner, String title,
			SimulationDataModel simModel, int modelIndex, WWPanel wwjPanel,
			ENUPanel enuPanel) {
		return new DislocationDialogRestorable(owner, title, simModel, modelIndex, wwjPanel, enuPanel);
	}
}

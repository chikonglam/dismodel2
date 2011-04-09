package gov.usgs.dismodel.calc.greens;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceIcon;
import gov.usgs.dismodel.DisModel.ENUPanel;
import gov.usgs.dismodel.SimulationDataModel;
import gov.usgs.dismodel.WWPanel;
import gov.usgs.dismodel.calc.greens.dialogs.SphericalSourceDialog2;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.jzy.CrossHair;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;

/**
 * Compute the deformation due to a pressurized finite spheroid
 * forward model based on eq. (52) and (53) by McTigue (1988)
 * @author clam-PR
 *
 */
@XmlRootElement
@XmlType(propOrder = { "shearModulus", "radius"})
public class McTigueSphere extends DisplacementSolver{
    final public static int NUM_PARAMS              = 4;
    
    final public static int EAST_LOC_IDX            = 0;
    final public static int NORTH_LOC_IDX           = 1;
    final public static int UP_LOC_IDX              = 2;
    final public static int VOLUME_IDX              = 3;
    //final public static int RADIUS_IDX              = 3;
    //final public static int PRESSURE_IDX            = 4;    
    final private static ArrayList<Integer> LINVAR =  new ArrayList<Integer>(Arrays.asList((new Integer[]{new Integer(VOLUME_IDX)})));
    

    private double shearModulus = 1;
    private double radius = 250;

    public McTigueSphere(double east, double north, double height, double volChange, double radius) {
    	this();
        msp[EAST_LOC_IDX]= east;
        msp[NORTH_LOC_IDX] = north;
        msp[UP_LOC_IDX] = height;
        msp[VOLUME_IDX] = volChange;
        this.radius = radius;
        this.shearModulus = 1d;
    }
    
    /**
     * Copy constructor
     * @param that
     */
    public McTigueSphere(McTigueSphere that) {
        super(that);
        this.shearModulus = that.getShearModulus();
        this.radius = that.getRadius();
    };
    
    /**
     * Construct a McTigue Sphere with a reference to the parameter array
     * @param refParams Reference to the array, the sphere will change whenever this changes
     * @param shearModulus
     */
    public McTigueSphere(double[] refParams, double shearModulus) {
        this();
        this.msp = refParams;
        this.shearModulus = shearModulus;
    }
    
    /**
     * Construct a McTigue Sphere by specifying all the parameters
     * @param center
     * @param volChange
     * @param radius
     * @param shearModulus
     */
    public McTigueSphere(LocalENU center, double volChange, double radius, double shearModulus){
       this();
        msp[EAST_LOC_IDX]= center.getEasting();
        msp[NORTH_LOC_IDX] = center.getNorthing();
        msp[UP_LOC_IDX] = center.getUp();
        msp[VOLUME_IDX] = volChange;
        this.shearModulus = shearModulus;
        this.radius = radius;
    }
    
    /**
     * Construct a McTigue Sphere by specifying all the parameters, and set shearModulus to 1
     * @param center
     * @param radius
     * @param pressure
     */
    public McTigueSphere(LocalENU center, double volChange, double radius){
        this(center,volChange, radius, 1d);
       
    }
    
    /**
     * Construct an empty McTigue object, and set shearModulus = 1, radius = 250
     */
    public McTigueSphere(){
    	super(NUM_PARAMS);
    	this.setName( "McTigue" + DisplacementSolver.getClassCount() );
    }

    @Override
    public XyzDisplacement solveDisplacement(LocalENU dataStationLocation) {
        
        final double x = dataStationLocation.getX();
        final double y = dataStationLocation.getY();
        //final double z = dataStationLocation.getZ();

        final double x0 = this.msp[EAST_LOC_IDX];
        final double y0 = this.msp[NORTH_LOC_IDX];
        final double z0 = -this.msp[UP_LOC_IDX];
        final double volChg = this.msp[VOLUME_IDX]; 

        final double pressure = shearModulus * volChg / (Math.PI * Math.pow(radius, 3d)); 
        
        final double xxn = (x - x0);
        final double yyn = (y - y0);
        final double angleNorth = Math.atan2(xxn, yyn);
        final double R = Math.hypot(xxn, yyn);
        
        final double rho = R/z0;
        final double e = radius/z0;
        
        final double f1 = (1d / (Math.pow(((Math.pow(rho, 2d)) + 1d), 1.5d)));
        final double f2 = (1d / (Math.pow(((Math.pow(rho, 2d)) + 1d), 2.5d)));
        final double c1 = (((0.5d * (1d - getPoissonRatio())) * (1d + getPoissonRatio())) / (7d - (5d * getPoissonRatio())));
        final double c2 = (((15d * (2d - getPoissonRatio())) * (1d - getPoissonRatio())) / (4d * (7d - (5d * getPoissonRatio()))));
        
        final double uzTemp = ((((Math.pow(e, 3d)) * (1d - getPoissonRatio())) * f1) - ((Math.pow(e, 6d)) * ((c1 * f1) - (c2 * f2))));
        final double urTemp =  rho*uzTemp;
        
        final double uz = ((pressure/shearModulus)*z0) * uzTemp;
        final double ur = ((pressure/shearModulus)*z0) * urTemp;
        
        final double ux = ur * Math.sin(angleNorth);
        final double uy = ur * Math.cos(angleNorth);
        return new XyzDisplacement(ux, uy, uz);
    }


    @Override
    public int getNumSolutionParams() {
        return NUM_PARAMS;
    }
    
    public double getShearModulus() {
        return shearModulus;
    }

    public void setShearModulus(double shearModulus) {
        this.shearModulus = shearModulus;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    public double getEast(){
        return this.msp[EAST_LOC_IDX];
    }
    
    public double getNorth(){
        return this.msp[NORTH_LOC_IDX];
    }
    
    public double getUp(){
        return this.msp[UP_LOC_IDX];
    }

    public double getVolumeChange(){
        return this.msp[VOLUME_IDX];
    }


    @Override
    public McTigueSphere clone() throws CloneNotSupportedException {
       return (McTigueSphere) super.clone();
    }

    @Override
    public String toString() {
        return getName() + " (McTigueSphere)[E=" + msp[EAST_LOC_IDX] + ", N=" + msp[NORTH_LOC_IDX] + ", U=" + msp[UP_LOC_IDX] + ", DV=" + msp[VOLUME_IDX] + ", R=" + radius + ", shearModulus="
                + shearModulus + "]";
    }

    @Override
    public AbstractDrawable toAbstractDrawable(DisplayStateStore displaySettings) {
        return new CrossHair( new Coord3d(msp[EAST_LOC_IDX], msp[NORTH_LOC_IDX], msp[UP_LOC_IDX]), (float)radius, getName(), displaySettings ) ;
    }

    @Override
    public void offsetLocation(double east, double north, double up) {
        this.msp[EAST_LOC_IDX] += east;
        this.msp[NORTH_LOC_IDX] += north;
        this.msp[UP_LOC_IDX] += up;
    }

    @Override
    public ArrayList<Integer> getLinearParameterIndices() {
        return  LINVAR;
    }

    @Override
    public Renderable toWWJRenderable(SimulationDataModel simModel, DisplayStateStore displaySettings) {
        final double east = msp[EAST_LOC_IDX];
        final double north = msp[NORTH_LOC_IDX];
        final double up = msp[UP_LOC_IDX];
        
        LLH origin = simModel.getOrigin();
        
        final String mogiIcon = "gov/usgs/dismodel/resources/plus.png";
        
        final LLH centerLLH = new LocalENU(east, north, up, origin).toLLH();
        
        final LatLon centerLatLon = LatLon.fromDegrees(centerLLH.getLatitude().toDeg(), centerLLH.getLongitude().toDeg()); 
        SurfaceIcon icon = new SurfaceIcon(mogiIcon, centerLatLon);
        java.awt.Color color = displaySettings.getSourceColor();
        icon.setOpacity(color.getAlpha()/255);
        icon.setScale(3);
        icon.setMaxSize(50e3);
        icon.setMinSize(radius*2);
        
        return icon;
    }

	@Override
	public JDialog toJDialog(Window owner, String title,  int modelIndex, AllGUIVars allGuiVars)  {
		return new SphericalSourceDialog2(owner, title, modelIndex, allGuiVars);
	}
    
    
    



}

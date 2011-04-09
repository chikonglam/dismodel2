package gov.usgs.dismodel.calc.greens;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceIcon;
import gov.usgs.dismodel.state.SimulationDataModel;
import gov.usgs.dismodel.calc.greens.dialogs.MogiSourceDialog2;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.jzy.CrossHair;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

import javax.swing.JDialog;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.ojalgo.matrix.jama.JamaMatrix;


/** Calculates displacements from a Mogi source which is a point-source 
approximation to a spherical magma chamber. */
public class MogiPoint extends DisplacementSolver {
	

    /* Class constants *//////////////////////////////////////////////////////////////////////////////////////////
    
    /** Index into the solution-vector */
    static public final int MODEL_EASTING_IDX   = 0; /** Index into the solution-vector */
    static public final int MODEL_NORTHING_IDX  = 1; /** Index into the solution-vector */
    static public final int MODEL_ELEVATION_IDX = 2; /** Index into the solution-vector */
    /** Index into the solution-vector.   */
    static public final int VOLUME_CHANGE_IDX   = 3; 
    // Volume change is the last parameter.
    /** The number of elements in the solution-vector */
    static final public int NUM_PARAMS = VOLUME_CHANGE_IDX + 1;
    
    // Elevation is the last spatial axis parameter.
    /** 3 axes = x, y, z */
    static public final int SPATIAL_DIM_AXES = MODEL_ELEVATION_IDX + 1;
    
    private final static ArrayList<Integer> LINVAR = new ArrayList<Integer>( Arrays.asList(new Integer[]{VOLUME_CHANGE_IDX}) );

    public MogiPoint(){
    	super(NUM_PARAMS);
    	this.name = "Mogi" + super.getClassCount();
    }
    
    
    /**
     * @param MogiLocation where this source is located.
     */
    public MogiPoint(final LocalENU MogiLocation) {
        this();
        msp[MODEL_EASTING_IDX]  = MogiLocation.getEasting();
        msp[MODEL_NORTHING_IDX] = MogiLocation.getNorthing();
        msp[MODEL_ELEVATION_IDX]= MogiLocation.getUp();
    }
    
    public MogiPoint(double east, double north, double up, double volChg) {
        this();
        msp[MODEL_EASTING_IDX] = east;
        msp[MODEL_NORTHING_IDX] = north;
        msp[MODEL_ELEVATION_IDX] = up;
        msp[VOLUME_CHANGE_IDX] = volChg;
    }

    /**
     * Copy constructor
     * @param that
     */
    public MogiPoint(MogiPoint that){
        super(that);
    }
    
    
    @Override
    public XyzDisplacement solveDisplacement(final LocalENU dataStationLocation) {
        final double distX = dataStationLocation.getEasting() - msp[MODEL_EASTING_IDX];
        final double distY = dataStationLocation.getNorthing() - msp[MODEL_NORTHING_IDX];
        final double distZ = -msp[MODEL_ELEVATION_IDX];
        final double distSq = distX * distX + distY *distY + distZ *distZ;
        final double multiplier = msp[VOLUME_CHANGE_IDX] * (1 - getPoissonRatio()) / (Math.PI * Math.pow(distSq, 3.0/2.0));

        final double dispX = distX * multiplier;
        final double dispY = distY * multiplier;
        final double dispZ = distZ * multiplier;
        XyzDisplacement ret = new XyzDisplacement(dispX, dispY, dispZ);
        return ret;     
    }

    @Override
    public int getNumSolutionParams() {
        return NUM_PARAMS;
    }
    
    @Override
    public ArrayList<Integer> getLinearParameterIndices() {
        return LINVAR;
    }

    @Override
    public int logGetModelParamColsWidth() {
        return 37; // see logPrintSourceParamVals format string
    }
    
    
    @Override
    public void logLabelModelParamsAndDisplacementAxes(Formatter f) {
        f.format("%8s %8s %8s %10s%11s "
                + " %7s %7s %7s %7s %7s %7s %7s %7s %7s"
                + " %7s %7s %7s %7s %7s %7s %7s %7s %7s", 
                "east", "north", "elev", "vol", "SSE", 
                "e1", "n1", "z1", "e2", "n2", "z2", "e3", "n3", "z3",
                "e4", "n4", "z4", "e5", "n5", "z5", "e6", "n6", "z6");
        System.out.println();
    }

    
    @Override
    public void logPrintSourceParamVals(JamaMatrix x, Formatter f) {
        f.format("%8.1f %8.1f %8.1f %10.3g", x.doubleValue(MODEL_EASTING_IDX, 0), 
                x.doubleValue(MODEL_NORTHING_IDX, 0), 
                x.doubleValue(MODEL_ELEVATION_IDX, 0), 
                x.doubleValue(VOLUME_CHANGE_IDX, 0));
}

    @Override
    public MogiPoint clone() throws CloneNotSupportedException {
        return (MogiPoint) super.clone();
    }

    @Override
    public String toString() {
        return getName() + " (MogiPoint) [E=" + msp[MODEL_EASTING_IDX] + ", N=" + msp[MODEL_NORTHING_IDX] + ", U=" + msp[MODEL_ELEVATION_IDX] + ", DV=" + msp[VOLUME_CHANGE_IDX] + "]";
    }

    @Override
    public AbstractDrawable toAbstractDrawable(DisplayStateStore displaySettings) {
        return new CrossHair(new Coord3d(msp[MODEL_EASTING_IDX], msp[MODEL_NORTHING_IDX], msp[MODEL_ELEVATION_IDX]), 0f, getName(), displaySettings);
    }

    @Override
    public void offsetLocation(double east, double north, double up) {
        this.msp[MODEL_EASTING_IDX] += east;
        this.msp[MODEL_NORTHING_IDX] += north;
        this.msp[MODEL_ELEVATION_IDX] += up;
    }

    @Override
    public Renderable toWWJRenderable(SimulationDataModel simModel, DisplayStateStore displaySettings) {
        final double east = msp[MODEL_EASTING_IDX];
        final double north = msp[MODEL_NORTHING_IDX];
        final double up = msp[MODEL_ELEVATION_IDX];
        
        LLH origin = simModel.getOrigin();
        
        final String mogiIcon = "gov/usgs/dismodel/resources/center.png";
        
        final LLH centerLLH = new LocalENU(east, north, up, origin).toLLH();
        
        final LatLon centerLatLon = LatLon.fromDegrees(centerLLH.getLatitude().toDeg(), centerLLH.getLongitude().toDeg()); 
        SurfaceIcon icon = new SurfaceIcon(mogiIcon, centerLatLon);
        java.awt.Color color = displaySettings.getSourceColor();
        icon.setOpacity(color.getAlpha()/255);
        icon.setScale(3);
        icon.setMaxSize(50e3);
        
        return icon; 
    }
    
    //getters
    //--------
    public double getEast(){
        return this.msp[MODEL_EASTING_IDX];
    }
    
    public double getNorth(){
        return this.msp[MODEL_NORTHING_IDX];
    }

    public double getUp(){
        return this.msp[MODEL_ELEVATION_IDX];
    }
    
    public double getVolumeChange(){
        return this.msp[VOLUME_CHANGE_IDX];
    }


    @Override
    public JDialog toJDialog(Window owner, String title,  int modelIndex, AllGUIVars allGuiVars) {
        JDialog dialog = new MogiSourceDialog2(owner, title, modelIndex, allGuiVars);
	return dialog;
    }


  

}

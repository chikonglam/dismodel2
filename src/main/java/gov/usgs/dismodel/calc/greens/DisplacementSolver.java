package gov.usgs.dismodel.calc.greens;

import gov.nasa.worldwind.render.Renderable;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.gui.components.AllGUIVars;
import gov.usgs.dismodel.state.DisplayStateStore;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

import javax.swing.JDialog;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import gov.usgs.dismodel.SimulationDataModel;

import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.ojalgo.matrix.jama.JamaMatrix;

/**
 * Calculates displacements due to various modeled subterranean sources.
 * 
 * A Green's function calculates the displacement at the surface, or other
 * points where sensors are placed, due to movement ("dislocations") in the bulk
 * of the earth.
 * 
 * Typically dismodel is used to predict surface-displacements at points where
 * measuring instruments such as GPS stations are located, and compare their
 * measurements with the predictions from a source-model represented by a class
 * derived from this one.
 * 
 * @author cforden
 * 
 */
@XmlRootElement
@XmlType(propOrder = { "msp", "poissonRatio"})
@XmlSeeAlso({MogiPoint.class, McTigueSphere.class, OkadaFault3.class, DistributedFault.class})
public abstract class DisplacementSolver 
            implements Cloneable, IndexableParameters {

    //class variable
        protected static int classCount = 0;
        
        /* Instance variables */
        protected double[] msp; // Modeled source parameters
    private double poissonRatio = 0.25;
    protected String name = "Displacement";

    // TODO: calc various moduli from others

    public DisplacementSolver(int numParams) {
        this();
        msp = new double[numParams];
    }
    
    
    /**
     * Copy Constructor
     * @param that
     */
    public DisplacementSolver(DisplacementSolver that) {
        msp = that.getModeledSourceParamsCopy();
        this.poissonRatio = that.poissonRatio;
        this.name = that.name;
    }
    
    /** Special derived classes, for example those modeling multiple sources,
     * might not know how many params they will eventually model.  They will
     * be held responsible for later allocating protected double[] msp, if 
     * they use this constructor instead of calling the one that allocates msp.
     */
    protected DisplacementSolver() {
        classCount++;
        name = "Displacement" + Double.toString(classCount);
    }
   
    /** A deep copy */
    @Override
    public DisplacementSolver clone() throws CloneNotSupportedException { 
        DisplacementSolver retObj = (DisplacementSolver) super.clone();
        if (msp == null)
            return retObj;
        retObj.msp = Arrays.copyOf(msp, msp.length);
        return retObj;
    }
    
    /** Typically a client will call this for every GPS station. 
     * Call setVolumeChange() first, or the displacements will always be zero. */
    abstract public XyzDisplacement solveDisplacement(LocalENU dataStationLocation);

    /** This overload is not yet implemented; it only throws an Exception
     * explaining that it does not handle ElasticityModel 
     * nonUniformitiesAndTopography.
     */
    public XyzDisplacement solveDisplacement(LocalENU dataStationLocation,
            ElasticityModel nonUniformitiesAndTopography) throws Exception {
        throw new Exception("solveDisplacement is not yet implemented "
                + " to use ElasticityModel nonUniformitiesAndTopography");
    }

    public double getPoissonRatio() {
        return poissonRatio;
    }

    public void setPoissonRatio(double poissonRatio) {
        this.poissonRatio = poissonRatio;
    }


    /**
     * Loads from this modeled source, its point in the search for a best-fit
     * solution in a multi-dimensioned parameter-space. That point is typically
     * a starting point for the search.
     * 
     * @param x
     *            An output variable, a column-vector to receive a suggested 
     *            solution, typically from which to begin iteration.
     *            
     * @throws Exception 
     */
    public void writeIntoColumnVector(JamaMatrix x) throws Exception {
        for (int i = 0; i < x.getRowDim(); i++) {
            x.set(i, 0, getSolutionParam(i));
        }
    }
    

    public void incrementSolutionParam(int paramIdx, double delta) 
            throws Exception {
        setSolutionParam(paramIdx, getSolutionParam(paramIdx) + delta);
    }


    /**
     * The user might need to specify several values for each of these
     * parameters.
     * 
     * Sometimes the user will want to specify a starting point. Usually the
     * user should specify min and max numerical values for these parameters.
     * Implementors in derived classes should consider calling
     * getCommonParamNames() in this base class, for a partial implementation.
     * 
     * @return GUI names of parameters for which the user might specify min,
     *         max, and starting values. After the user adjusts them, call
     *         {@link #setSearchBounds}() using the same order of parameters as
     *         returned here.
     */
    public String[] getUserAdjustableParamNames() {
        return new String[0];
    }

    @Override
    public void setSolutionParam(int paramIdx, double newValue) throws Exception {
        msp[paramIdx] = newValue;
    }

    @Override
    public double getSolutionParam(int paramIdx) throws Exception {
        return msp[paramIdx];
    }

    /**
     * Return the reference of the ModeledSourceParams array
     * Changing the array returned will change the model's parameters
     * @return the reference of the msp array
     */
        @XmlElementWrapper(name = "ModeledSourceParams")
    @XmlElement(name = "param")
    public double[] getMsp(){
        return this.msp;
    }

    /**
     * Return a copy of the ModeledSourceParams array (Recommended)
     * Changing the array returned will not change the model's parameters 
     * @return a copy of the msp array
     */
        @XmlTransient
        public double[] getModeledSourceParamsCopy(){
        return Arrays.copyOf(this.msp, this.msp.length);
    }
    
    /**
     * Change the reference of the ModeledSourceParams to the specified array
     * Changing the specified array after the function call will change the model's parameters 
     * @param msp the array Reference
     */
    public void setMsp(double[] msp){
        this.msp = msp;
    }
 
    /**
     * Copy the specified array into the ModelSourceParams array (Recommended)
     * Changing the specified array after the function call will not change the model's parameters
     * @param msp the array being copied
     */
    public void setModeledSourceParamsCopy(double[] msp){
        this.msp = Arrays.copyOf(msp, msp.length);
    }
 
    
    public String getName() {
                return name;
        }


        public void setName(String name) {
                this.name = name;
        }


        /** Typically returns an array with one element, the index of the 
     * volume-change parameter.  Derived classes modeling multiple sources
     * should be expected to return an array with several elements.  As of
     * this writing, this might return the index of the pressure, as a 
     * substitute for volume.
     * @return Can return null.
     */
    abstract public ArrayList<Integer> getLinearParameterIndices();
    
    // TODO: make abstract?
    public void logPrintSourceParamVals(JamaMatrix x, Formatter f) {}

    /**
     * Tells the width of all the modeled-parameter columns, combined. Typically
     * used to pad with blanks, a label(s) for the station (displacement)
     * columns.  // TODO: make abstract?
     */
    public int logGetModelParamColsWidth() {
        return 0;  // TODO: make abstract?
    }

    
    /**
     * Label the individual columns of logged data. For diagnosing inversion
     * algorithms by pretty-printing iteration states.
     */
    public void logLabelModelParamsAndDisplacementAxes(Formatter f) {}


    @Override
    public String toString() {
        return getName() + " (DisplacementSolver) [msp=" + Arrays.toString(msp)
                + ", poissonRatio=" + poissonRatio + "]";
    }
    
    abstract public AbstractDrawable toAbstractDrawable(DisplayStateStore displaySettings);
    
    abstract public Renderable toWWJRenderable(SimulationDataModel simModel, DisplayStateStore displaySettings);

    abstract public void offsetLocation(double east, double north, double up);

    abstract public JDialog toJDialog(Window owner, String title, int modelIndex, AllGUIVars allGuiVars);
    
    public double getMagnitude() {
            // TODO make into abstract, and then put it to everything else          
            return 0;  //good enough for now
    }

    public double getFaultSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    protected static int getClassCount() {
            return classCount;
    }
    
    
    
    
}

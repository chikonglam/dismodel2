package gov.usgs.dismodel.state;

import gov.usgs.dismodel.ModelSolution;
import gov.usgs.dismodel.RestorableSourceDialog;
import gov.usgs.dismodel.SmoothingDialog;
import gov.usgs.dismodel.calc.SolverException;
import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.calc.inversion.ConstraintType;
import gov.usgs.dismodel.calc.inversion.CovarianceWeighter;
import gov.usgs.dismodel.calc.inversion.CrossValResults;
import gov.usgs.dismodel.calc.inversion.DistributedSlipsBatchGreensIO;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.Label;
import gov.usgs.dismodel.geom.overlays.VectorXyz;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "sourceModels", "sourceLowerbound", "sourceUpperbound", "fittedModels"})
public class SimulationDataModel implements ModelSolution {
    //TODO make sure all vars are here

    private ArrayList<DisplacementSolver> sourceModels = new ArrayList<DisplacementSolver>();
    private ArrayList<RestorableSourceDialog> sourceDialogs = new ArrayList<RestorableSourceDialog>();
    private ArrayList<DisplacementSolver> fittedModels = new ArrayList<DisplacementSolver>();
    private ArrayList<DisplacementSolver> sourceLowerbound = new ArrayList<DisplacementSolver>();
    private ArrayList<DisplacementSolver> sourceUpperbound = new ArrayList<DisplacementSolver>();
    private DistributedSlipsBatchGreensIO distributedSlipBatchIoProcessor = 
        new DistributedSlipsBatchGreensIO();
    
        
    private List<Label> stations = new ArrayList<Label>();
    private CovarianceWeighter covarWeighter = new CovarianceWeighter();
    private List<VectorXyz> measuredUnrefdDispVectors = new ArrayList<VectorXyz>();

    
    /*
     * Displacement vectors, with the reference-station's displacements
     * subtracted. This is kept separate from, and somewhat duplicate of,
     * CovarianceWeighter.dSubtracted and dOrig, for various reasons. */
    private List<VectorXyz> measuredRefdDispVectors = new ArrayList<VectorXyz>();
    
    private List<XyzDisplacement> modeledDisplacements = new ArrayList<XyzDisplacement>();
    private double refH;
    
    private double chi2;
    
    //distributed slip stuff
    private boolean nonNeg;
    private double monentConstraint = Double.NaN;
    private ConstraintType monentConType;
    
    private SmoothingDialog.Params smoothingParams = 
        new SmoothingDialog.Params();
    private CrossValResults cvResults;
    
    public SimulationDataModel() {
        // empty
    }
    
    /**
     * Returns null if all data is loaded.
     * @return
     */
    public String isDataLoaded() {
        if (stations != null && stations.size() > 0 && covarWeighter != null) {
            return null;
        } else {
            return "Station locations and displacements are not loaded";
        }
    }
    
    
    @XmlTransient
    public boolean getNonNeg() {
        return nonNeg;
    }

    public void setNonNeg(boolean nonNeg) {
        this.nonNeg = nonNeg;
    }
    
    @XmlTransient
    public List<RestorableSourceDialog> getSourceDialogs() {
        return sourceDialogs;
    }

    public void setSourceDialogs(ArrayList<RestorableSourceDialog> sourceDialog) {
        this.sourceDialogs = sourceDialog;
    }
    
    
    @XmlTransient
    public boolean isDistributedFaultProblem() {
    	for (DisplacementSolver curModel: sourceModels){
    		if (curModel instanceof DistributedFault) return true;
    	}
        return false;
    }

    @XmlTransient
    public double getMonentConstraint() {
        return monentConstraint;
    }

    @XmlTransient
    public ConstraintType getMonentConType() {
        return monentConType;
    }

     public void setMonentConstraint(double monentConstraint) {
        this.monentConstraint = monentConstraint;
    }

    public void setMonentConType(ConstraintType monentConType) {
        this.monentConType = monentConType;
    }

    public ArrayList<DisplacementSolver> getSourceModels() {
        return sourceModels;
    }
    
    public ArrayList<DisplacementSolver> getFittedModels() {
        return fittedModels;
    }
    public ArrayList<DisplacementSolver> getSourceLowerbound() {
        return sourceLowerbound;
    }
    public ArrayList<DisplacementSolver> getSourceUpperbound() {
        return sourceUpperbound;
    }
    public List<LocalENU> getStationLocations(LLH origin) {
        if (origin == null)
            throw new SolverException("You must first set the origin; " +
            		"click the button above the map");        
        List<LocalENU> stationsENU = new ArrayList<LocalENU>();
        for (Label llh : stations) {
            stationsENU.add(llh.getLocation().toLocalENU(origin));
        }
        return stationsENU;
    }
    
    @XmlTransient
    public CovarianceWeighter getCovarWeighter() {
        return covarWeighter;
    }
    
    @XmlTransient
    public List<XyzDisplacement> getModeledDisplacements() {
        return modeledDisplacements;
    }
    public List<VectorXyz> getModeledDispVectors() {
        if (modeledDisplacements == null || stations == null || 
                stations.size() != modeledDisplacements.size())
            return new ArrayList<VectorXyz>();
        
        List<VectorXyz> vectors = new ArrayList<VectorXyz>(modeledDisplacements.size());
        for (int i = 0; i < modeledDisplacements.size(); i++) {
            VectorXyz v = new VectorXyz(stations.get(i), modeledDisplacements.get(i));
            vectors.add(v);
        }
        return vectors;
    }

    public void setSourceModels(ArrayList<DisplacementSolver> sourceModels) {
        this.sourceModels = sourceModels;
    }
    public void setFittedModels(ArrayList<DisplacementSolver> fittedModels) {
        this.fittedModels = fittedModels;
    }
    public void setSourceLowerbound(ArrayList<DisplacementSolver> sourceLowerbound) {
        this.sourceLowerbound = sourceLowerbound;
    }
    public void setSourceUpperbound(ArrayList<DisplacementSolver> sourceUpperbound) {
        this.sourceUpperbound = sourceUpperbound;
    }
    public void setCovarWeighter(CovarianceWeighter covarWeighter) {
        this.covarWeighter = covarWeighter;
    }
    public void setModeledDisplacements(List<XyzDisplacement> modeledDisplacements) {
    	int refIdx = getCovarWeighter().getReferenceStationIdx();
    	if (refIdx != -1){
    		int arraySize = modeledDisplacements.size();
    		this.modeledDisplacements = new ArrayList<XyzDisplacement>(arraySize);
    		XyzDisplacement refDisp = modeledDisplacements.get(refIdx);
    		for (int i = 0; i < arraySize; i++) {
    			XyzDisplacement changedDisp = new XyzDisplacement();
    			changedDisp.setToDifference(modeledDisplacements.get(i), refDisp);
    			this.modeledDisplacements.add( changedDisp );
    		}
    		
    	} else {
    		this.modeledDisplacements = modeledDisplacements;
    	}
        
    }
    public void setChi2(double chi2) {
        this.chi2 = chi2;
    }
    
    @XmlTransient
    public double getRefH() {
        return refH;
    }

    public void setRefH(double refH) {
        this.refH = refH;
    }

    @Override
    public double getChiSquared() {
        return chi2;
    }
    @Override
    public String getSolutionDescription() {
        return toString();
    }
    @Override
    public String toString() {
        return "SimulationModel [chi2=" + chi2 + ", fittedModels="
                + fittedModels + "RefH=" + refH + "\n, modeledDisplacements="
                + modeledDisplacements + "]";
    }

    public void setStations(List<Label> stations) {
        this.stations = stations;
    }

    @XmlTransient
    public List<Label> getStations() {
        return stations;
    }

    /** Also subtracts any reference-station's displacements.
     * @param measuredDispVectors 
     *              XYZ displacements, typically as measured by instruments.
     * @return displacements, after subtracting any reference-station's 
     *          displacements.
     */
    public List<VectorXyz> setMeasuredDispVectors(List<VectorXyz> measuredDispVectors) {
        measuredUnrefdDispVectors = measuredDispVectors;
        if (measuredRefdDispVectors == null || 
                measuredRefdDispVectors.size() != measuredDispVectors.size()) {
            measuredRefdDispVectors = new ArrayList<VectorXyz> (
                    measuredDispVectors.size());
            for (int i = 0; i < measuredDispVectors.size(); i++)
                measuredRefdDispVectors.add(new VectorXyz(
                        measuredDispVectors.get(i)));
        }
        int refIdx = getCovarWeighter().getReferenceStationIdx();
        for (int i = 0; i < measuredDispVectors.size(); i++) {
            if (refIdx != -1) // Subtract the reference-station's displacements
                measuredRefdDispVectors.get(i).setToDifference(
                        measuredDispVectors.get(i),
                        measuredDispVectors.get(refIdx));
            else // There is no reference-station selected
                measuredRefdDispVectors.get(i).setDisplacement(
                    measuredDispVectors.get(i).getDisplacement());
        }
        return measuredRefdDispVectors;
    }

    /** Returns displacements from which the reference-station's, 
     * displacements have been subtracted, if appropriate. */
    @XmlTransient
    public List<VectorXyz> getMeasuredDispVectors() {
        return measuredRefdDispVectors;
    }


    public void selectReferenceStation(int refStationIdx) {
        getCovarWeighter().selectReferenceStation(
                refStationIdx);
        setMeasuredDispVectors(measuredUnrefdDispVectors);
        return;
    }

    @XmlTransient
	public DistributedSlipsBatchGreensIO getDistributedSlipBatchIoProcessor() {
		return distributedSlipBatchIoProcessor;
	}

	/** Records the input parameter as being the first Green's function
	 * to be read, and reads it in.
	 * 
	 * @param greensFile contains a Green's function as a matrix.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void setAndRead1stGreensFile(File greensFile) throws NumberFormatException, IOException {
	    /* For now at least, we forbid reference stations when using Green's
	     * functions loaded from files; we don't even associate such Green's
	     * functions with stations so far, although maybe that could be useful. 
	     * For now, the user can have the Green's function and data in the files, 
	     * already weighted by a covariance matrix, and with the reference station
	     * already subtracted out. */
	    getCovarWeighter().selectReferenceStation(-1);
	    measuredRefdDispVectors = null;
	    
	    distributedSlipBatchIoProcessor.set1stGreensFile(greensFile);
	    distributedSlipBatchIoProcessor.readGreensFile(greensFile);
	}

    public void setCrossValidationParams(SmoothingDialog dialog) {
        smoothingParams = dialog.getParams();        
    }

    @XmlTransient
    public SmoothingDialog.Params getSmoothingParams() {
        return smoothingParams;
    }

    public boolean isCrossValidate() {
        return smoothingParams.shouldCrossValidate;
    }    

    public boolean isUseSmoothing() {
        return smoothingParams.useSmoothing;
    }
    
    public double getSmoothingGamma(){
    	return smoothingParams.gamma;
    }
    
    public void setCrossValResults(CrossValResults cvResults) {
        this.cvResults = cvResults;
    }
    
    //Persistence stuff
    public static void writeToXML(SimulationDataModel simModel, File xmlFile) throws JAXBException, IOException{
    	BufferedOutputStream outStream = new BufferedOutputStream( new FileOutputStream(xmlFile));
    	JAXBContext context = JAXBContext.newInstance(SimulationDataModel.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(simModel, outStream);
		outStream.close();
    }
    
    public static SimulationDataModel readXML(File xmlFile) throws IOException, JAXBException {
    	BufferedInputStream inStream = new BufferedInputStream( new FileInputStream(xmlFile));
    	JAXBContext context = JAXBContext.newInstance(SimulationDataModel.class);
    	Unmarshaller unmarshaller = context.createUnmarshaller();
    	//note: setting schema to null will turn validator off
    	unmarshaller.setSchema(null);
    	//unmarshaller.setProperty(Unmarshaller., value)
    	SimulationDataModel readSimModel = (SimulationDataModel)(unmarshaller.unmarshal(inStream));
    	inStream.close();
    	return readSimModel;
    }
    
    
    @XmlTransient
	public List<VectorXyz> getMeasuredUnrefdDispVectors() {
		return measuredUnrefdDispVectors;
	}

	public void setMeasuredUnrefdDispVectors(
			List<VectorXyz> measuredUnrefdDispVectors) {
		this.measuredUnrefdDispVectors = measuredUnrefdDispVectors;
	}

	@XmlTransient
	public List<VectorXyz> getMeasuredRefdDispVectors() {
		return measuredRefdDispVectors;
	}

	public void setMeasuredRefdDispVectors(List<VectorXyz> measuredRefdDispVectors) {
		this.measuredRefdDispVectors = measuredRefdDispVectors;
	}

	@XmlTransient
	public CrossValResults getCvResults() {
		return cvResults;
	}

	public void setCvResults(CrossValResults cvResults) {
		this.cvResults = cvResults;
	}
	
	
	@XmlTransient
	public double getChi2() {
		return chi2;
	}

	public void setDistributedSlipBatchIoProcessor(
			DistributedSlipsBatchGreensIO distributedSlipBatchIoProcessor) {
		this.distributedSlipBatchIoProcessor = distributedSlipBatchIoProcessor;
	}

	public void setSmoothingParams(SmoothingDialog.Params smoothingParams) {
		this.smoothingParams = smoothingParams;
	}
    
}

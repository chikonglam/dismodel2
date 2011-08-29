package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.SolverException;
import gov.usgs.dismodel.calc.overlays.ojalgo.JamaUtil;
import gov.usgs.dismodel.geom.overlays.VectorXyz;
import gov.usgs.dismodel.state.SimulationDataModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.jama.JamaMatrix;
import org.ojalgo.type.context.NumberContext;

import cern.colt.Arrays;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleCholeskyDecomposition;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;

/**
 * Weights data so that more reliable data (with less noise and/or less
 * correlated in ways troublesome to a given data-vector) get more weight.
 * 
 * <p>
 * MISC. FUNCTIONS:
 * <ul>
 * <li>Calculates sum squared errors (SSE) between measured and predicted
 * displacements. Can use a full covariance matrix to weight the axes of data,
 * or not, as the user chooses.
 * <li>Can include or exclude a specifiable reference-station.
 * <li>This class can read a covariance matrix from a special .txt file.
 * <li>Selecting a reference-station, typically one not strongly affected by the
 * deformation source, from the stations that are substantially affected by the
 * deformation source.
 * </ul>
 * 
 * <p>
 * One of the few things the client cannot change about this class, at a time of
 * the client's choosing, is covariance data after setting it; for that the
 * client should abandon this object, and construct a new instance.
 * 
 * @author cforden
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = { "covarianceMatrix", "referenceStationIdx" })
public class CovarianceWeighter {

    /*
     * Class constants
     * **********************************************************
     * *********************
     */

    /** For 14 places of precision */
    public static final NumberContext nc14 = new NumberContext(15, 14, RoundingMode.HALF_DOWN);
    public static final int STATION_AXES = 3;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    /*
     * Instance variables
     * *******************************************************
     * **********************
     */

    /**
     * The matrix originally set, made symmetrical if it had been triangular,
     * before removing reference data or inverting.
     */
    protected JamaMatrix origCovar;
    /** Internally calculated pseudo-inverse of the covariance matrix. */
    protected JamaMatrix invcov;
    protected JamaMatrix origInvcov;

    /**
     * Matrix W for linear scaling. This something like a square-root of the
     * inverse of the covariance matrix.
     */
    private JamaMatrix weighterMatrix = null;
    private JamaMatrix derefedWeighterMatrix = null;

    DifferenceOperator diffOp = null;

    /**
     * Measured data, as a column-vector, before differencing out the reference-
     * station's data.
     */
    protected JamaMatrix dOrig;
    /**
     * Measured data, as a column-vector, with data from the reference-station,
     * subtracted from every other station's data.
     */
    protected JamaMatrix dSubtracted;

    /**
     * Init'd to its canonical invalid index. Note that even when a valid index
     * has been selected, that it might not be active; see
     * subtractReferenceStationData.
     */
    private int referenceStationIdx = -1;

    /*
     * Making the following temporary, intermediate values, members as opposed
     * to locals, saves a lot of time vs. reallocating them in calcSSE which
     * gets called in inner loops.
     */
    /**
     * Intermediate calc values, sized for predicted minus measured, deformation
     * data, with ref-station data
     */
    private JamaMatrix residual;
    /** Same as above, but with ref-station data removed */
    private JamaMatrix excludedResidual;

    /* For calcErrorEllipsoid() */
    private ArrayList<Ellipsoider> ellipsers;

    /*
     * Methods
     * ******************************************************************
     * *********************
     */

    public CovarianceWeighter() {
    }

    /**
     * This constructor reads a data-covariance matrix from a file. Also
     * performs the Cholesky decomposition so that repetitive calls to
     * calcSSE(), later, can be fast. Can take a triangularly sparse matrix, and
     * fill in the other half from symmetry.
     * 
     * @param file
     *            containing a data-covariance matrix.
     * @throws SolverException
     * @throws IOException
     */
    public CovarianceWeighter(File file) throws SolverException, IOException {
        this();
        readCovFile(file);
    }

    public CovarianceWeighter(double[][] covar) {
        this();
        this.setCovarianceMatrix(covar);
    }

    public void readCovFile(File file) throws SolverException, IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String lineIn;
            int iRow = 0;
            int iColumn = 0;
            double dValue = 0.0;
            class dataEntry {
                public int myRow;
                public int myColumn;
                public double myValue;

                public dataEntry(int row, int column, double value) {
                    myRow = row;
                    myColumn = column;
                    myValue = value;
                }
            }
            final int typicalMatrixSize = 2000; // a priori guess at number of
            // data elements plus smoothing
            // matrix elements
            ArrayList<dataEntry> dataList = new ArrayList<dataEntry>(typicalMatrixSize);
            while ((lineIn = in.readLine()) != null) {
                if (lineIn.startsWith("#"))
                    continue;

                StringTokenizer toks = new StringTokenizer(lineIn, " \t(,)");
                String sData = toks.nextToken();
                dValue = Double.valueOf(sData);

                int idxOpenParen = lineIn.indexOf("(");
                if (idxOpenParen == -1)
                    continue;
                String sRow = toks.nextToken();
                iRow = Integer.decode(sRow);
                String sColumn = toks.nextToken();
                iColumn = Integer.decode(sColumn);
                dataList.add(new dataEntry(iRow, iColumn, dValue));
            }
            /*
             * Assume the largest row, column indices were the last entry in the
             * data file!
             */
            double covar[][] = new double[iRow][iColumn];
            for (int i = 0; i < dataList.size(); i++) {
                covar[dataList.get(i).myRow - 1][dataList.get(i).myColumn - 1] = dataList.get(i).myValue;
            }
            setCovarianceMatrix(covar);
        } finally {
            in.close();
        }
    } // readCovFile(File file)

    /**
     * Stores a copy of the matrix passed in then calculates weights to be used
     * later. Can take a triangularly sparse matrix, and fill in the other half
     * by assuming symmetry. Also performs the Cholesky decomposition so that
     * repetitive calls to calcSSE(), later, can be fast. Overwrites anything
     * readCovarianceMatrix() might have set. If a reference station has been
     * set, this differences-out its errors before inversion.
     * 
     * <p>
     * Do not call this method more than once; results are not guaranteed.
     * Instead, create a new instance if you need to use a different covariance
     * matrix.
     * 
     * @param covar
     *            A data-covariance matrix.
     * @see readCovarianceMatrix
     * @throws SolverException
     */
    public void setCovarianceMatrix(double[][] covar) throws SolverException {
        origCovar = JamaMatrix.FACTORY.copy(covar);
        checkSymmetryFillTriangle(origCovar);
        calcRefSubtractedInverse(this.referenceStationIdx);
    }
    
    @XmlElementWrapper(name = "covarianceMatrix")
    @XmlElement(name = "covRow")
    public double[][] getCovarianceMatrix() {
	if (origCovar != null){
	    return JamaUtil.toRawCopy(origCovar);
	} else {
	    return null;
	}
        
    }

    protected void calcInverse(JamaMatrix in) throws SolverException {
        /*
         * ojAlgo JamaCholesky.getInverse() seemed to be throwing "not symmetric
         * positive definite", so I changed to regular matrix inversion at least
         * for now. I think the disadvantage is just performance. Chris Forden
         * 2010 Sept. 17.
         */
        invcov = null; // in case the inverse (below) throws
        invcov = in.invert();
    }

    /**
     * @return W matrix to be used in linear scaling
     */
    private JamaMatrix getWeighterMatrix() {
        if (invcov == null) { // return null if it invcov is not there
            return null;
        } else if (weighterMatrix == null) { // calculate it now if it's not
                                             // already done
            calcWeighterMatrix();
        }
        return weighterMatrix;
    }

    /**
     * @return an weighter matrix with ref station diff'ed out, if necessary.
     */
    public JamaMatrix getAutoWeighterMatrix() {
        if (referenceStationIdx != -1) {
            return getWeighterMatrix();
        } else {
            return getDerefedWeighterMatrix();
        }
    }

    /**
     * Calculate what our MATLAB scripts call the W matrix, for solving our
     * systems of linear equations. This weights equations by the inverse of the
     * data covariance matrix.
     */
    protected void calcWeighterMatrix() {
        /* Parallel Colt's Cholesky Decomposition: */
        DenseDoubleMatrix2D in = new DenseDoubleMatrix2D( JamaUtil.toRawCopy(origInvcov) );
        DenseDoubleCholeskyDecomposition coltCho = new DenseDoubleCholeskyDecomposition(in);
        weighterMatrix = JamaMatrix.FACTORY.copy(coltCho.getL().toArray());

    }

    public JamaMatrix getDerefedWeighterMatrix() {
        if (derefedWeighterMatrix == null) {
            calcDerefWeighterMatrix();
        }
        return derefedWeighterMatrix;
    }

    protected void calcDerefWeighterMatrix() {
        /* Parallel Colt's Cholesky Decomposition: */
        DenseDoubleMatrix2D in = new DenseDoubleMatrix2D( JamaUtil.toRawCopy(invcov) );
        DenseDoubleCholeskyDecomposition coltCho = new DenseDoubleCholeskyDecomposition(in);
        derefedWeighterMatrix = JamaMatrix.FACTORY.copy(coltCho.getL().toArray());
    }

    /**
     * @param numOfSubFaultsAndSlips
     * @param gamma
     * @return Smoothed weighter that is deref'ed, if necessary
     */
    public JamaMatrix getAutoSmoothedWeighter(int numOfSubFaultsAndSlips, double gamma) {
        JamaMatrix derefMatrix = origCovar;
        if (referenceStationIdx != -1) {
            derefMatrix = getDifferenceOperator().multiplyRight(origCovar);
        }
        int origCol = derefMatrix.getColDim();
        int origRow = derefMatrix.getRowDim();

        int outCol = origCol + numOfSubFaultsAndSlips;
        int outRow = origRow + numOfSubFaultsAndSlips;

        double[][] covWithGam = new double[outRow][outCol];
        for (int rowIter = 0; rowIter < origRow; rowIter++) { // copy stuff in;
                                                              // can find a
                                                              // faster way
            for (int colIter = 0; colIter < origCol; colIter++) {
                covWithGam[rowIter][colIter] = derefMatrix.get(rowIter, colIter);
            }
        }

        if (!Double.isNaN(gamma)){
            double gam2 = gamma * gamma;
            for (int diagIter = origRow; diagIter < outRow; diagIter++) {
                covWithGam[diagIter][diagIter] = gam2;
            }
        }

        JamaMatrix covWithGamInv = JamaMatrix.FACTORY.copy(covWithGam).invert();

        DenseDoubleCholeskyDecomposition smoothedChol = new DenseDoubleCholeskyDecomposition(new DenseDoubleMatrix2D(
        	JamaUtil.toRawCopy(covWithGamInv)));
        JamaMatrix smoothedWeighter = JamaMatrix.FACTORY.copy(smoothedChol.getL().toArray());
        return smoothedWeighter;
    }

    /**
     * Checks whether the matrix is triangular or symmetric. If it is neither,
     * throws an exception. If it is triangular, it fills in the zeros to make
     * it symmetrical.
     * 
     * @param in_out
     *            Input and output variable.
     * @throws SolverException
     */
    public static void checkSymmetryFillTriangle(JamaMatrix in_out) throws SolverException {
        JamaMatrix t = in_out.transpose();
        if (in_out.equals((BasicMatrix) (t), nc14))		//already transpose-Symmetric
            return;
        if ( isUpperTriangularZeros(in_out))			//lower triangular => fill upper triangular
            fillUpperTriangularFromLower(in_out);		
        else if (isLowerTriangularZeros(in_out))		//upper triangular => fill lower triangular
            fillLowerTriangularFromUpper(in_out);
        else if (!in_out.isSymmetric())
            throw new SolverException("Tried to set a covariance matrix " + "that was neither symmetric nor triangular");
    }
    
    private static boolean isLowerTriangularZeros(JamaMatrix in_out) {
        for (int row = 0; row < in_out.getRowDim(); row++) {
            for (int col = 0; col < row; col++) {
                if (in_out.doubleValue(row, col) != 0.0)
                    return false;
            }
        }
        return true;
    }
    
    private static void fillLowerTriangularFromUpper(JamaMatrix in_out){
        for (int row = 0; row < in_out.getRowDim(); row++) {
            for (int col = 0; col < row; col++) {
        	in_out.set(row, col, in_out.doubleValue(col, row));
            }
        }
    }
    
    private static boolean isUpperTriangularZeros(JamaMatrix in_out) {
        for (int row = 0; row < in_out.getRowDim(); row++) {
            for (int col = row + 1; col < in_out.getColDim(); col++) {
                if (in_out.doubleValue(row, col) != 0.0)
                    return false;
            }
        }
        return true;
    }
    
    private static void fillUpperTriangularFromLower(JamaMatrix in_out){
        for (int row = 0; row < in_out.getRowDim(); row++) {
            for (int col = row + 1; col < in_out.getColDim(); col++) {
                in_out.set(row, col, in_out.doubleValue(col, row));
            }
        }
    }
    

    /*
     * The covariance weighter keeps its own copy of measured data because after
     * a Green's function is loaded, there might be rows for smoothing
     * equations, and corresponding pseudo-data elements, in which case measured
     * vectors could not be extracted from this class.
     */
    /**
     * Include any reference-station's data in this vector; this class will
     * account for the reference-station, including even later when
     * selectReferenceStation() gets called. Note that when loading a Green's
     * function from a file, that the reference-station must be invalidated.
     * 
     * @param data
     * @throws SolverException
     */
    public void setMeasuredDisplacementData(double data[]) throws SolverException {
        /*
         * In case a differently sized measurement vector had been set while the
         * covariance matrix was empty.
         */
        residual = null;
        excludedResidual = null;
        if (origCovar != null && (origCovar.getColDim() != data.length))
            throw new SolverException("Wrong size of displacement data set");

        dOrig = JamaMatrix.FACTORY.makeColumn(data);
        dSubtracted = JamaMatrix.FACTORY.copy(dOrig);
        if (referenceStationIdx == -1)
            return;
        dSubtracted = subtractReferenceData();
    }

    protected JamaMatrix subtractReferenceData() {
        if (dSubtracted.getRowDim() % STATION_AXES != 0)
            throw new SolverException("Data not a multiple of axes-per-station");

        dSubtracted = getDifferenceOperator().multiplyRight(dOrig);
        // System.out.println("index:\n" + referenceStationIdx); ////DEBUG
        // System.out.println("\ndSub:\n" + dSubtracted.toString()); ////DEBUG
        // System.out.println("\ndOrig:\n" + dOrig.toString()); ////DEBUG
        // String someString =
        // JOptionPane.showInputDialog("Stuff is happening now"); ////DEBUG

        return dSubtracted;
    }

    /**
     * Each data set can have a reference station whose movement will be
     * subtracted from all the others' data, typically to remove tectonic drift.
     * The effect of calling this method, can be undone, by calling it again,
     * providing a different index.
     * 
     * @param refStationIdx
     *            the (zero-based) index into the original array of stations.
     * @throws SolverException
     */
    public void selectReferenceStation(int refStationIdx) throws SolverException {

        if (refStationIdx == referenceStationIdx)
            return;
        weighterMatrix = null;
        diffOp = null;

        if (origCovar != null && refStationIdx * STATION_AXES >= origCovar.getColDim())
            throw new SolverException("Selected reference station idx too big.");

        referenceStationIdx = refStationIdx;

        if (dOrig != null) {
            if (referenceStationIdx != -1)
                subtractReferenceData();
            else
                dSubtracted = JamaMatrix.FACTORY.copy(dOrig);
        }

        calcRefSubtractedInverse(refStationIdx);
    }

    /**
     * Builds a new S matrix, if one is not set already.
     * 
     * @param totalAxes
     *            typically 3 times the number of GPS stations
     * @return
     */
    protected DifferenceOperator getDifferenceOperator() {
        final int totalAxes = getSize();
        if (diffOp == null) {
            diffOp = new DifferenceOperator(totalAxes / STATION_AXES, referenceStationIdx);
        }
        return diffOp;
    }

    /**
     * Tries to return the number of measurements (including those of any
     * reference-station) this object has or expects.
     * 
     * @return
     */
    int getSize() {
        if (this.origCovar != null)
            return origCovar.getColDim();
        if (dOrig != null)
            return dOrig.getRowDim();
        throw new SolverException("Didn't have a size for a Difference Operator");
    }

    /**
     * Calculate the inverse of the covariance matrix after subtracting the
     * reference data, thereby propagating its errors through the covariance
     * matrix.
     * 
     * @throws SolverException
     */
    private void calcRefSubtractedInverse(int refStationIdx) {
        if (origCovar == null)
            return; // nothing to work on

        if (refStationIdx == -1) {
            calcInverse(origCovar);
            origInvcov = invcov;
            return;
        }

        /* Now remove the reference-station from the covariance matrix. */

        calcInverse(propagateCovarianceErrors());
    }

    protected JamaMatrix propagateCovarianceErrors() {
        JamaMatrix intermed = getDifferenceOperator().multiplyRight(origCovar);
        origInvcov = origCovar.invert();
        return intermed.multiplyRight((getDifferenceOperator().getTranspose()));
    }

    /**
     * Calculates the Sum Squared Error.
     * 
     * <p>
     * Usually the differences between measured and predicted values, are
     * weighted by the covariance data, before squaring them. Call
     * setMeasuredDisplacementData() before calling this method.
     * 
     * @param predictedData
     *            typically displacement data predicted by a model of a
     *            deformation-source. Include the reference station, even if it
     *            has been passed to selectReferenceStation().
     * @return the sum of the squared errors between the measured data elements
     *         and the ones predicted, typically from a modeled deformation
     *         source
     * @throws SolverException
     * @see setMeasuredDisplacementData
     */
    public double calcSSE(double predictedData[]) throws SolverException {
        JamaMatrix r; // A temp ref to one of two scratch members

        if (origCovar != null && (origCovar.getColDim() != predictedData.length))
            throw new SolverException("Wrong size predictedData passed to " + "CovarianceWeighter.calcSSE()");

        if (referenceStationIdx != -1) {
            if (dSubtracted == null)
                /*
                 * The actual subtraction, immediately below, would have thrown
                 * an out-of-bounds exception too, but this one carries a more
                 * explanatory message.
                 */
                throw new SolverException("Reference or measured data might not have been "
                        + "previously set. (RefStat set.)");
            if (excludedResidual == null) {
                excludedResidual = JamaMatrix.FACTORY.makeZero(predictedData.length - STATION_AXES, 1);

            }

            double[] derefedDisp = derefDispVector(predictedData, referenceStationIdx);
            final int end = derefedDisp.length;
            for (int i = 0; i < end; i++) {
                excludedResidual.set(i, 0, dSubtracted.doubleValue(i, 0) - derefedDisp[i]);
            }

            r = excludedResidual;
        } else {
            if (residual == null) {
                residual = JamaMatrix.FACTORY.makeZero(predictedData.length, 1);
            }
            for (int i = 0; i < predictedData.length; i++) {
                residual.set(i, 0, dOrig.doubleValue(i, 0) - predictedData[i]);
            }
            r = residual;
        }

        if (invcov == null) {
            return r.transpose().multiplyRight((BasicMatrix) r).doubleValue(0, 0);
        }
        JamaMatrix intermed = r.transpose().multiplyRight((BasicMatrix) invcov);
        double sse = intermed.multiplyRight((BasicMatrix) r).doubleValue(0, 0);
        return sse;
    } // calcSSE()

    /**
     * Calculation for the ovals drawn around measured displacement vector
     * arrows in the GUI. Set a covariance matrix first; the standard deviations
     * for each axis, and the covariances between that station's axes (if any),
     * will be included in each VectorXyz.error output.
     * 
     * @param vectorsInErrorsOut
     *            Inputs the XYZ displacements, and receives the XYZ errors out
     *            from this method. Always include any reference-station's
     *            displacement vectors; users might want to see reference-
     *            reliability, too. Note that the order of the vectors in the
     *            list must be canonical.
     */
    public void calcErrorEllipsoids(List<VectorXyz> vectorsInErrorsOut) {
        final int end = vectorsInErrorsOut.size();

        if (origCovar == null || end != origCovar.getColDim() / STATION_AXES)
            throw new SolverException("Vector size must match the covariance"
                    + "matrix size, including any reference-station.");

        if (ellipsers == null) {
            ellipsers = new ArrayList<Ellipsoider>();
            for (int station = 0; station < end; station++) {
                Ellipsoider elip = new Ellipsoider(origCovar, station);
                ellipsers.add(elip);
            }
        }
        for (int i = 0; i < vectorsInErrorsOut.size(); i++) {
            VectorXyz vec = vectorsInErrorsOut.get(i);
            Ellipsoider elip = ellipsers.get(i);
            elip.getErrorEllipsoid(vec);
        }
        return;
    }

    /**
     * Calculate the X^2 value, dividing SSE by the sum of N, the number of
     * data-points, and p, the number of modeled parameters being solved-for.
     * Note that the extra division might take a little time, yet is not needed
     * while iteratively solving for the modeled parameter values. Therefore
     * clients might want to call calcSSE() while iterating, then call
     * calcChiSquared() before saving or displaying the accuracy result for an
     * inversion.
     * 
     * @param predictedData
     *            typically calculated from the model being iteratively solved.
     *            Include the reference station, even if it has been passed to
     *            selectReferenceStation().
     * @param numParameters
     *            the number of modeled parameters being solved-for.
     * @return a calculated value for Chi-squared.
     * @throws SolverException
     */
    public double calcChiSquared(double predictedData[], int numParameters) throws SolverException {
        double N = predictedData.length;
        if (referenceStationIdx != -1) {
            N -= STATION_AXES;
        }
        return calcSSE(predictedData) / (double) (N - numParameters);
    }

    /**
     * @return the reference-Station index, zero-based. An invalid index, -1,
     *         indicates no reference station is selected.
     * @see selectReferenceStation
     * @see setSubtractReferenceStationData
     */
    @XmlElement
    public int getReferenceStationIdx() {
        return referenceStationIdx;
    }

    /**
     * Uses covariance weighting to calculate modeled, linear variables from
     * displacement measurements.
     * 
     * <p>
     * Linear variables can be slips of faults or subfaults, or volume changes
     * of magma intrusions. The contribution to surface displacement, of the
     * slip of each subfault, is a linear function of that slip.
     * 
     * @param gTran
     *            Green's matrix transposed
     * @param dispWOLinVars
     * @return Slips (and/or delta volumes?) calculated by inversion of the
     *         measured displacements.
     * */
    public double[] getLinVars(double[][] gTran, double[] dispWOLinVars) {
        // JamaMatrix dNoLinVars =
        // excludeStation(JamaMatrix.FACTORY.makeColumn(dispWOLinVars),
        // referenceStationIdx);
        JamaMatrix dNoLinVars = JamaMatrix.FACTORY.makeColumn(dispWOLinVars);
        /*
         * Remove the effect of sources other than this fault which has
         * distributed slips ("linear variables"):
         */
        JamaMatrix dOnlyLinVars = dOrig.subtract(dNoLinVars);
        // TODO Test this
        JamaMatrix W = getWeighterMatrix();
        JamaMatrix G = JamaMatrix.FACTORY.copy(gTran).transpose();
        JamaMatrix WD = W.multiplyRight((BasicMatrix) dOnlyLinVars);
        JamaMatrix WG = W.multiplyRight((BasicMatrix) G);
        JamaMatrix WGTran = WG.transpose();

        JamaMatrix invWGTxWG = (WGTran.multiplyRight((BasicMatrix) WG)).invert();
        JamaMatrix result = invWGTxWG.multiplyRight((BasicMatrix) WGTran).multiplyRight((BasicMatrix) WD);
        double[][] outTemp = JamaUtil.toRawCopy( result.transpose() );

        return outTemp[0];
    }

    private JamaMatrix excludeStation(JamaMatrix in, int stationExcluded) {
        if (stationExcluded == -1)
            return in;
        int ansRowCt = in.getRowDim() - STATION_AXES; // TODO: make this faster
        JamaMatrix out = JamaMatrix.FACTORY.makeEmpty(ansRowCt, 1);
        int offset = 0;
        for (int curAnsRow = 0; curAnsRow < ansRowCt; curAnsRow++) { // TODO:
                                                                     // make
                                                                     // this
                                                                     // faster
            if (curAnsRow == stationExcluded * STATION_AXES)
                offset = STATION_AXES;
            out.set(curAnsRow, 0, in.doubleValue(curAnsRow + offset, 0));
        }
        return out;
    }

    private JamaMatrix excludeStation2D(JamaMatrix in, int stationExcluded) {
        if (stationExcluded == -1)
            return in;
        int ansRowCt = in.getRowDim() - STATION_AXES; // TODO: make this faster
        int ansColCt = in.getColDim();
        JamaMatrix out = JamaMatrix.FACTORY.makeEmpty(ansRowCt, ansColCt);

        int offset = 0;

        for (int curAnsRow = 0; curAnsRow < ansRowCt; curAnsRow++) { // TODO:
                                                                     // make
                                                                     // this
                                                                     // faster
            if (curAnsRow == stationExcluded * STATION_AXES)
                offset = STATION_AXES;
            for (int curAnsCol = 0; curAnsCol < ansColCt; curAnsCol++) {
                out.set(curAnsRow, curAnsCol, in.doubleValue(curAnsRow + offset, curAnsCol));
            }
        }
        return out;

    }

    /**
     * Weights the Green's function with the covariance matrix.
     * 
     * <p>
     * Use with the one-dimensional overload, weight(double[]), to solve a
     * weighted equation for distributed slips or other linear inversion.
     * 
     * <p>
     * If a reference-station had been selected, this also subtracts its data
     * from the Green's matrix.
     * 
     * @param gMatrix
     *            Green's function
     * @return A matrix roughly corresponding to a Green's function after
     *         weighting by the inverse of the covariance matrix times the
     *         transpose of the Green's function. This can be passed to a method
     *         that would solve Gm = d, in place of G, if Gt * invcov * d is
     *         also substituted for d.
     */
    // public double[][] weight(double[][] gMatrix) {
    // JamaMatrix G; /* Will be the Green's function */
    // JamaMatrix Weighter;
    // if (referenceStationIdx != -1) { // Subtract the reference data
    // G = differenceOutReferenceData(gMatrix);
    // Weighter = getDerefedWeighterMatrix();
    // }
    // else {
    // G = JamaMatrix.FACTORY.copyRaw(gMatrix);
    // Weighter = getWeighterMatrix();
    // }
    // JamaMatrix wG = Weighter.multiplyRight((BasicMatrix)G);
    // return wG.toRawCopy();
    // }

    /**
     * Difference out the contribution from the ref station of a G matrix, if
     * necessary
     * 
     * @param gMatrix
     * @return
     */
    public JamaMatrix autoDifferenceOutReferenceData(double[][] gMatrix) {
        JamaMatrix G = JamaMatrix.FACTORY.copy(gMatrix);
        if (referenceStationIdx != -1) {
            JamaMatrix retVal = getDifferenceOperator().multiplyRight(G);
            return retVal;
        } else {
            return G;
        }
    }

    /**
     * Weights the measurements by multiplying by both the inverse of the
     * covariance and also by the transpose of the Green's function.
     * 
     * <p>
     * Use with the two-dimensional overload, weight(double[][]), to solve a
     * weighted equation for distributed slips or other linear inversion.
     * 
     * <p>
     * Also subtracts the reference-station's data from that of the other
     * stations, if a reference-station had been selected.
     * 
     * @param displacements
     *            An array of data from all the stations
     * @return An array of data from the stations except the reference-station.
     */
    public double[] weight(double[] displacements) {
        if (displacements.length % STATION_AXES != 0)
            throw new SolverException("displacements not a multiple of " + "axes-per-station");

        JamaMatrix d; /* Will be the displacements column-vector. */
        JamaMatrix weighter;

        if (referenceStationIdx == -1) {
            weighter = getWeighterMatrix();
            d = JamaMatrix.FACTORY.makeColumn(displacements);
        } else { // subtract reference-data
            weighter = getDerefedWeighterMatrix();
            d = diffOp.multiplyRight(JamaMatrix.FACTORY.makeColumn(displacements));
        }
        // TODO:: JUnit test both overloads of weight()
        JamaMatrix weightedDisplacements = weighter.multiplyRight((BasicMatrix) (d));
        return JamaUtil.toRawCopy(weightedDisplacements.transpose())[0];
    }

    private double[] derefDispVector(double[] dispVect, int station2Exclude) {
        double refX = dispVect[station2Exclude * STATION_AXES + AXIS_X];
        double refY = dispVect[station2Exclude * STATION_AXES + AXIS_Y];
        double refZ = dispVect[station2Exclude * STATION_AXES + AXIS_Z];

        final int dispOrigLen = dispVect.length;
        final int derefedLen = dispOrigLen - STATION_AXES;
        final int numOfStation = dispOrigLen / STATION_AXES;
        double[] derefedDisp = new double[derefedLen];

        int derefedStationIter = 0;
        derefLoop: for (int origStationIter = 0; origStationIter < numOfStation; origStationIter++) {
            if (origStationIter == station2Exclude) {
                origStationIter++;
                if (origStationIter >= numOfStation)
                    continue derefLoop;
            }

            derefedDisp[derefedStationIter * STATION_AXES + AXIS_X] = dispVect[origStationIter * STATION_AXES + AXIS_X]
                    - refX;
            derefedDisp[derefedStationIter * STATION_AXES + AXIS_Y] = dispVect[origStationIter * STATION_AXES + AXIS_Y]
                    - refY;
            derefedDisp[derefedStationIter * STATION_AXES + AXIS_Z] = dispVect[origStationIter * STATION_AXES + AXIS_Z]
                    - refZ;
            derefedStationIter++;
        }
        return derefedDisp;
    }
    
    public void setCovarToIdentMatrixIfUnset(SimulationDataModel simModel){
	double[][] cov = getCovarianceMatrix();
	if (cov==null){
	    int dim = simModel.getMeasuredDispVectors().size() * STATION_AXES;
	    cov = JamaUtil.genIdentMatrix(dim);
	    setCovarianceMatrix(cov);
	}
    }
    

    // setters and getters for jaxb
    // -----------------------------
    public void setReferenceStationIdx(int referenceStationIdx) {
        selectReferenceStation(referenceStationIdx);
    }

}

/** Created by the United States Geological Survey. 2010. */
package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.SolverException;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.jama.JamaMatrix;

/**
 * Base class for solving a system of equations of the linear algebra form 
 * <li>     Ax - b
 * <p>typically by finding a least-squares best fit. 
 * 
 * <p>The equations are linear
 * except for constraints which concrete, extending classes typically
 * facilitate. Typically solves the slips for sub-faults within one fault, given
 * a Green's function, which in most of our contexts, is a forward-model of the
 * effects of unit-slips of the sub-faults, on the surface deformation measured
 * by data gathering instruments ("stations"). Like most solvers in this
 * package, methods typically can throw SolverException, an unchecked type of
 * exception, extending RuntimeException.
 * 
 * @author cforden
 * 
 */
public abstract class ConstrainedLinearLeastSquaresSolver {
	
	/* Instance variables ************************************************************************/
	protected JamaMatrix greensFunct;
	protected JamaMatrix measuredDisplacements;
	private int numEqs;
	private int solutionSize; // Columns in the greensMatrix
	private boolean logIterations = false; // TODO Elim

	/* Methods ***********************************************************************************/

    /* We require the constructor to take the main parameters because it might 
    * be difficult to wring the bugs out of the class if it allowed setting
    * the Greens function more than once. */
	/**
	 * Input the linear program, Ax - b, to be minimized, to this constructor, 
	 * where typically:
	 * 		A is the Greens function
	 * 		b is the measured displacements
	 * 		x is the distributed slips to be estimated, 
	 * 
	 * @param greensMatrix
	 *            Can be non-square, typically over-determined, that is with
	 *            more rows than columns. Often, Laplacian smoothing elements
	 *            are included in this matrix so that noise in the data will
	 *            perturb solution-elements less. Try to avoid calling this more
	 *            than once on an instance; reliability is likely to be better
	 *            if you instantiate another instance and populate it, instead.
	 *            Some concrete overrides might even throw a SolverException if
	 *            you try to re-populate the Green's function.
	 *            
	 * @param measuredDisplacements
	 *            a column vector, that is a matrix whose elements all have
	 *            column-indices of zero. Typically the elements are
	 *            measurements gathered by data stations, such as GPS stations,
	 *            strain-gauges, tilt-meters, etc.
	 *            
	 */
	public ConstrainedLinearLeastSquaresSolver(double[][] greensMatrix, 
			double[] measuredDisplacements) {
		setGreensFunction(greensMatrix);
		this.measuredDisplacements = JamaMatrix.FACTORY.makeColumn(
				measuredDisplacements);
	}

    /**
     * This constructor allows a client (for example, cross validation) to set
     * constraints before setting Green's function and measured data.
     */
	public ConstrainedLinearLeastSquaresSolver() {}
	
	
    /**
     * This method allows a client (for example, cross validation) to use this
     * class to store constraints across many settings and solvings of Green's
     * function and measured data.
     */
	public void setGreensFunction(double[][] greensMatrix) {
        numEqs = greensMatrix.length;
        solutionSize = greensMatrix[0].length;
        if (getNumEqs() < getSolutionSize())
            /* A Green's function, by definition, predicts all the 
             * displacements at all the instrument locations. */
            throw new SolverException ("The Green's function had more " + 
                    "subfaults than measurable displacements.");  
        this.greensFunct = JamaMatrix.FACTORY.copyRaw(greensMatrix);
	}
	

    /**
     * This method allows a client (for example, cross validation) to use this
     * class to store constraints across many settings and solvings of Green's
     * function and measured data.
     */
	public void setMeasuredDisplacements(double[] disps) {
	    this.measuredDisplacements = JamaMatrix.FACTORY
                .makeColumn(disps);
	}

	/* It would be feasible to add a reduceNumberOfModeledParameters() method,
	 * although it would not be feasible to increase them after setting AE and AI
	 * unless you reimplemented constraints without prepopulating those matrices
	 * with zeroes.  --Chris Forden 2010 Aug. 13	 */
    /**
     * If you set constraints before setting the Green's function, you must
     * first call this
     * 
     * so the implementation can know the size of the constraints matrices to
     * build.
     * 
     * @param nmp
     *            number of modeled parameters. The number of columns in the
     *            Green's function, to be set later, will equal this.
     */
	public void setNumberOfModeledParameters(int nmp) {
	    solutionSize = nmp;
	}
	
	
	/**
	 * @return A short name, suitable for prefacing the names of output files.
	 */
	public abstract String getAbbreviatedSolverName();

	
	/**
	 * Finds the best-fit solution, typically minimizing the sum of
	 * least-squares, subject to constraints particular to the derived solvers.
	 * 
	 * @return The best solution, typically of strengths of slips distributed
	 *         among sub-faults. The column vector x, in the system Ax - b,
	 *         being minimized.
	 */
	public abstract double[] solve();

	
    /**
	 * @return A message suitable for the header comments of an output file,
	 *         typically indicating the quality of the solution, and/or
	 *         constraints applied.  "\n" can be included, indicating new-lines.
	 */
	public abstract String getSolutionNotes();


    /**
     * @param solution
     *            estimated model, typically distributed slips of many
     *            sub-faults.
     * @return The Sum Squared Error
     */
    public double calcSSE(double[] solution) {
        JamaMatrix solMat = JamaMatrix.FACTORY.makeColumn(solution);
        return calcSSE(solMat);
    }
    

    /**
     * @param solution
     *            estimated model, typically distributed slips of many
     *            sub-faults.  A column-vector.
     * @return The Sum Squared Error
     */
	public double calcSSE(BasicMatrix solution) {
		/* rPseudo has extra rows (from zero-valued "pseudo data")
		 * corresponding to smoothing equations:	 */
		JamaMatrix rPseudo = measuredDisplacements.subtract(
		        greensFunct.multiplyRight(solution));
		// r will become the residual, when those extra rows are pared
		JamaMatrix r = JamaMatrix.FACTORY.makeZero(
		        greensFunct.getRowDim() - greensFunct.getColDim(), 1);
		for (int i = 0; i < r.getRowDim(); i++ )
		    r.set(i, 0, rPseudo.doubleValue(i, 0));
		double sse = r.transpose().multiplyRight((BasicMatrix) r).
				doubleValue(0, 0);
		return sse;
	}


	/**
	 * @return the number of equations, which is the number of displacement
	 *         measurements and also the number of rows of the Green's matrix.
	 */
	public int getNumEqs() {
		return numEqs;
	}

	
	/**
	 * @return true if the solution and perhaps other values at each iteration
	 *         will be logged.
	 */
	public boolean isLogIterations() {
		return logIterations;
	}


	/**
	 * @param logIterations
	 *            true to print solution values to the console at each
	 *            iteration. Useful for verify convergence to a stable solution.
	 */
	public void setLogIterations(boolean logIterations) {
		this.logIterations = logIterations;
	}


    /**
     * @return the number of elements that are, or will be, 
     *         in the solution vector.  Equal to the number of columns
     *         in the Green's function matrix.
     */
    public int getSolutionSize() {
        return solutionSize;
    }

    /**
     * @param subfaultIndex
     *            An input, only. The zero-based index of the block of interest
     *            in the grid of sub-faults.
     * @return The lower-bound if one has been set, NaN otherwise.
     */
    abstract public double getLowerBound(final int subfaultIndex);

    /**
     * @param subfaultIndex
     *            zero-based index of the block of interest in the grid of
     *            sub-faults.
     * @return The upper bound if one has been set, NaN otherwise.
     */
    abstract public double getUpperBound(final int subfaultIndex);

    public double[] getDisplacements() {
        return measuredDisplacements.transpose().toRawCopy()[0];
    }

    public double[][] getGreens() {
        return greensFunct.toRawCopy();
    }

}

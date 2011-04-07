package gov.usgs.dismodel.calc.greens;

/* Since SourceInverter was moved to /ehz_gps/trunk/modeling/alt/ 
 * only getNumSolutionParams() is used for production (non JUnit test) code
 *  --Chris Forden 2010 Sept. 3   */
public interface IndexableParameters {
    
    public void setSolutionParam(int paramIdx, double newValue) throws Exception;

    public double getSolutionParam(int paramIdx) throws Exception;
    
    public int getNumSolutionParams();

	/**
	 * The user might need to specify several values for each of these
	 * parameters; this allows the GUI to get names for them.
	 * 
	 * Sometimes the user will want to specify a starting point. Usually the
	 * user should specify min and max numerical values for these parameters.
	 * 
	 * @return GUI names of parameters for which the user might specify min,
	 *         max, and starting values. After the user adjusts them, call
	 *         setStartingValues(), setSearchBounds() as needed, using the same
	 *         order of parameters as returned here.
	 */
    public String[] getUserAdjustableParamNames();

}

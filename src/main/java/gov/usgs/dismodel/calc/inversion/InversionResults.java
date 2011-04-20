package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;

public class InversionResults {
    private DisplacementSolver[] modelsB4ShiftingHeight;
    private double chi2;
    private XyzDisplacement[] modeledDisplacements;
    private int curSolnPos;
    private boolean continueWrapping;
    
    public InversionResults(DisplacementSolver[] modelsB4ShiftingHeight, double chi2,
             XyzDisplacement[] modeledDisplacements) {
        super();
        this.modelsB4ShiftingHeight = modelsB4ShiftingHeight;
        this.chi2 = chi2;
        this.modeledDisplacements = modeledDisplacements;
    }

    public InversionResults() {
        super();
    }

    public DisplacementSolver[] getModels() {
        return modelsB4ShiftingHeight;
    }

    public double getChi2() {
        return chi2;
    }


    public XyzDisplacement[] getModeledDisplacements() {
        return modeledDisplacements;
    }

    public DisplacementSolver[] getFittedModels(){
        return getModels();
    }
    
    public DisplacementSolver[] getFittedModelsB4ShiftedHeight(){
        return modelsB4ShiftingHeight;
    }
    
    
    public double getChiSquared(){
        return getChi2();
    }

    public XyzDisplacement[] getStationDisplacements(){
        return getModeledDisplacements();
    }

    @Override
    public String toString() {
        String solnStr = "InversionResults [chi2=" + chi2 + ",\n models=\n";
        for (DisplacementSolver curModel : modelsB4ShiftingHeight) {
            solnStr += curModel.toString() + "\n"; 
        }
        solnStr +=  "]";
        return solnStr;
    }


    /**
     * Text output, optimized for display in a dialog box without scroll-bars.
     * 
	 * Wraps long lines, indenting the wrapped parts, so that each model's
	 * solution has a hanging indentation.
	 * 
	 * @return The solution with each model's solution beginning at the left of
	 *         its line, and continuing with indentations preceding its wrapped
	 *         parts.
     */
    public String toWrappedStrings() {
    	curSolnPos = 0;
    	StringBuilder wrappedStrBldr = new StringBuilder(
    			"Inversion Results [chi2=" + chi2 + ",  models=\n");
        for (DisplacementSolver curModel : modelsB4ShiftingHeight) {
            String theModelSolnStr = new String(curModel.toString() + "\n");
            continueWrapping = true;
            while (continueWrapping) {
                wrappedStrBldr.append(getSolnPart(theModelSolnStr));
            }
        }
        return wrappedStrBldr.toString();
    }

	/**
	 * @param theModelSolnStr
	 *            the full (long) line listing all params and their values found
	 *            by solving a dislocation model.
	 * @return a part of theModelSolnStr, prefixed with space for indentation if
	 *         not the first part of the (original) theModelSolnStr.
	 */
	private String getSolnPart(String theModelSolnStr) {
		StringBuilder retValBldr = new StringBuilder();
		int remainingSpace = 80;
		if (curSolnPos != 0) {
			retValBldr.append("   ");
			remainingSpace -= 3;
		}
		int nextComma = theModelSolnStr.indexOf(',', curSolnPos + remainingSpace);
		if (nextComma == -1) {
			continueWrapping = false;
			retValBldr.append(theModelSolnStr.substring(curSolnPos));
		}
		else {
			retValBldr.append(theModelSolnStr.substring(curSolnPos, nextComma + 1));
			retValBldr.append('\n');
		}
		curSolnPos = nextComma + 1;
		return retValBldr.toString();
	}

     
}

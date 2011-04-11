package gov.usgs.dismodel.calc.inversion;

import gov.usgs.dismodel.calc.greens.DisplacementSolver;

public class InversionProgressStats {
    private double chi2;
    private int run;
    private DisplacementSolver[] curParams;
    
    public InversionProgressStats(double chi2, int run,
            DisplacementSolver[] curParams) {
        super();
        this.chi2 = chi2;
        this.run = run;
        this.curParams = curParams;
    }
    
    public double getChi2() {
        return chi2;
    }
    public int getRun() {
        return run;
    }
    public DisplacementSolver[] getCurParams() {
        return curParams;
    }
    public void setChi2(double chi2) {
        this.chi2 = chi2;
    }
    public void setRun(int run) {
        this.run = run;
    }
    public void setCurParams(DisplacementSolver[] curParams) {
        this.curParams = curParams;
    }
 
    
}

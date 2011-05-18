package gov.usgs.dismodel.calc.inversion;

public class CVResult {
    private double cvss;
    private double gam;
    public CVResult(double cvss, double gam) {
	super();
	this.cvss = cvss;
	this.gam = gam;
    }
    public double getCvss() {
        return cvss;
    }
    public void setCvss(double cvss) {
        this.cvss = cvss;
    }
    public double getGam() {
        return gam;
    }
    public void setGam(double gam) {
        this.gam = gam;
    }
    
    
}

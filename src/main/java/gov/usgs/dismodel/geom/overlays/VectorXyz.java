package gov.usgs.dismodel.geom.overlays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import gov.nasa.worldwind.geom.LatLon;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.Convert;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;

/**
 * Vector class containing the vector, error ellipsoid and scale (for display).
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = {"nameAndLLH", "disp", "error"})
public class VectorXyz {
    private Label nameAndLLH;
    
    private XyzDisplacement disp;
    private XyzDisplacement error;
    
    public VectorXyz(){
    }
    
    public VectorXyz(Label nameAndLLH, XyzDisplacement disp) {
        this(nameAndLLH, disp, new XyzDisplacement());
    }
    
    public VectorXyz(Label nameAndLLH, XyzDisplacement disp, XyzDisplacement error) {
        this.nameAndLLH = nameAndLLH;
        this.disp = disp;
        this.error = error;
    }
    
    /** A deep-copy constructor */
    public VectorXyz(VectorXyz src) {
        this(new Label(src.nameAndLLH), 
                src.getDisplacement() == null ? null : 
                    new XyzDisplacement(src.getDisplacement()), 
                src.getError() == null ? null : 
                    new XyzDisplacement(src.getError()));
    }
    
    public LLH getStart() {
        return nameAndLLH.getLocation();
    }

    @XmlTransient
    public XyzDisplacement getDisplacement() {
        return disp;
    }

    @XmlElement
    public XyzDisplacement getError() {
        return error;
    }
    
    @Override
    public String toString() {
        return "VectorXyz [disp=" + disp + ", error=" + error + ", name="
                + nameAndLLH + "]";
    }

	/**
	 * @param error the error to set
	 */
	public void setError(XyzDisplacement error) {
		this.error = error;
	}

    public void setToDifference(VectorXyz v, VectorXyz vRef) {
        disp.setToDifference(v.disp, vRef.disp);
    }

    public void setDisplacement(XyzDisplacement displacement) {
        disp.setValuesTo(displacement);        
    }
    
    //output methods
    public ArrowLine toArrowLine(double scale){
    	LLH startPoint = nameAndLLH.getLocation();
    	LocalENU endPointENU = new LocalENU(disp.getX() * scale, disp.getY() * scale, disp.getZ() * scale,
    			startPoint); 
    	LLH endPoint = Convert.toLLH(endPointENU);
    	LatLon startLatLon = LatLon.fromDegrees(startPoint.getLatitude().toDeg(), startPoint.getLongitude().toDeg());
    	LatLon endLatLon = LatLon.fromDegrees(endPoint.getLatitude().toDeg(), endPoint.getLongitude().toDeg());
    	return new ArrowLine(startLatLon, endLatLon);
    }

    @XmlElement
    public Label getNameAndLLH() {
        return nameAndLLH;
    }

    public void setNameAndLLH(Label nameAndLLH) {
        this.nameAndLLH = nameAndLLH;
    }

    @XmlElement
    public XyzDisplacement getDisp() {
        return disp;
    }

    public void setDisp(XyzDisplacement disp) {
        this.disp = disp;
    }
    
   
    
}

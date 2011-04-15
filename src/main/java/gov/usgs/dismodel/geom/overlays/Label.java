package gov.usgs.dismodel.geom.overlays;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import gov.usgs.dismodel.geom.Angle;
import gov.usgs.dismodel.geom.LLH;
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType(propOrder = { "location", "name" })
public class Label implements Serializable {
    private static final long serialVersionUID = 1L;
    private LLH location;
    private String name;
    
    public Label(){
    }
    
    public Label(LLH location, String name) {
        this.location = location;
        this.name = name;
    }
    
    /** A deep-copy constructor */
    public Label(Label src) {
        location = src.location;
        name = src.name;
    }

    @XmlElement
    public LLH getLocation() {
        return location;
    }

    @XmlElement
    public String getName() {
        return name;
    }
    
    
    
    public void setLocation(LLH location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Label other = (Label) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Label [location=" + location + ", name=" + name + "]";
    }
    
    public static LLH centroidLLH(List<Label> allStations){
    	double sumLatDeg = 0;
    	double sumLonDeg = 0;
    	double sumHeight = 0;
    	
    	final int length = allStations.size();
    	
    	for (int iter = 0; iter < length; iter++){
    		LLH curLoc = allStations.get(iter).getLocation();
    		sumLatDeg += curLoc.getLatitude().toDeg(); 
    		sumLonDeg += curLoc.getLongitude().toDeg();
    		sumHeight += curLoc.getHeight();
    	}
    	
    	return new LLH(sumLatDeg / length, sumLonDeg / length, sumHeight / length);
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 3387393557753260493L;
        private final LLH location;
        private final String name;
        
        public SerializationProxy(Label label) {
            this.location = label.getLocation();
            this.name = label.getName();
        }
        private Object readResolve() {
            return new Label(location, name);
        }
    }
}

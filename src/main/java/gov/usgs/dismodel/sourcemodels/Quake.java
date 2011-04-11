package gov.usgs.dismodel.sourcemodels;

import gov.usgs.dismodel.geom.LLH;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Quake implements java.io.Serializable {
    private static final long serialVersionUID = 1658946577480577737L;
    
    private Date date;
    private LLH center;
    private double magnitude;
    
    public Quake(Date date, LLH center, double magnitude) {
        this.date = date;
        this.center = center;
        this.magnitude = magnitude;
    }
    
    // 06-Jan-2005 13:43:37 -153.4315  59.3592  0.54  0.6
    public Quake(String text) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        String [] columns = text.split(" +");
        String dateS = columns[0] + " " + columns[1];
        try {
            date = df.parse(dateS);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        double height = Double.parseDouble(columns[4]) * 1000; 
        double latitude = Double.parseDouble(columns[3]);
        double longitude = Double.parseDouble(columns[2]);
        center = new LLH(latitude, longitude, height);
        
        magnitude = Double.parseDouble(columns[5]) + 1.0;
    }

    public Date getDate() {
        return date;
    }

    public LLH getCenter() {
        return center;
    }

    public double getMagnitude() {
        return magnitude;
    }
     
}

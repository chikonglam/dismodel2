package gov.usgs.dismodel.geom.overlays.jzy;

public class ColorUtil {
    static public java.awt.Color toAWTColor(org.jzy3d.colors.Color color){
        return new java.awt.Color (color.r, color.g, color.b, color.a);
    }
    
    static public org.jzy3d.colors.Color toJzyColor(java.awt.Color color){
        return new org.jzy3d.colors.Color(color.getRed(), color.getGreen() ,color.getBlue(), color.getAlpha());
    }
}

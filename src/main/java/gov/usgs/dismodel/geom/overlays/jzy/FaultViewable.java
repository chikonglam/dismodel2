package gov.usgs.dismodel.geom.overlays.jzy;

import gov.usgs.dismodel.geom.Angle;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Quad;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.text.TextBitmap;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.align.Valign;

/**
 * Displays a dislocation as a 3D plane.
 * <BR>
 * Various constructors exist for specifying the plane by it's 4 corners, bottom corners and width and dip, 
 * or bottom center, etc.
 */
public class FaultViewable extends AbstractDrawable{
    protected Quad plane;
    protected LineStrip outline;
    protected LineStrip topLine;
    protected boolean hasTopLine = true;
    protected String name;
    protected Coord3d nameLocation;
    
    public FaultViewable(Coord3d bottomPoint1, Coord3d bottomPoint2, double width,
            Angle dip, Color color) {
        final double x1 = bottomPoint1.x;
        final double y1 = bottomPoint1.y;
        final double x2 = bottomPoint2.x;
        final double y2 = bottomPoint2.y;
        final double yDist = y2 - y1;
        final double xDist = x2 - x1;
        double strikeRad = Math.atan2(xDist, yDist);
        if (strikeRad < 0){
            strikeRad += 2.0 * Math.PI; 
        }
        Angle strike = Angle.fromRad(strikeRad);
        double length = Math.hypot(xDist, yDist);
        float centerX = (float)(x1 + xDist/2.0);
        float centerY = (float)(y1 + yDist/2.0);
        //should be the same, but do this just in case
        float centerZ = (float)((bottomPoint1.z + bottomPoint2.z) / 2.0);       
        
        Coord3d bottomCenter = new Coord3d(centerX, centerY, centerZ);
        
        calcElements(bottomCenter, length, width, strike, dip, color);
    }
    
    public FaultViewable(Coord3d bottomCenter, double length, double width,
            Angle strike, Angle dip, Color color, String name) {
    	this.name = name;
        calcElements(bottomCenter, length, width, strike, dip, color);
    }
    
    public FaultViewable(Coord3d bottomCenter, double length, double width,
            Angle strike, Angle dip, Color color, boolean hasTopLine) {
        this.hasTopLine = hasTopLine;           //Make this top line hiding more moduluar
        calcElements(bottomCenter, length, width, strike, dip, color);
    }
    
    public FaultViewable(Coord3d upper1, Coord3d upper2, Coord3d lower3,
            Coord3d lower4, Color color) {
        setElements(upper1, upper2, lower3, lower4, color);
    }
    
    public void setHasTopLine(boolean hasTopLine) {
        this.hasTopLine = hasTopLine;
    }

    private void calcElements(Coord3d bottomCenter, double length, double width,
            Angle strike, Angle dip, Color color) {
        final double sinStrike = Math.sin(strike.toRad());
        final double cosStrike = Math.cos(strike.toRad());
        final double sinDip = Math.sin(dip.toRad());
        final double cosDip = Math.cos(dip.toRad());
        final double ctrX = bottomCenter.x;
        final double ctrY = bottomCenter.y;
        final double ctrZ = bottomCenter.z;
        
        final float lowerZ = (float) ctrZ;
        final float lower3x = (float) (ctrX + length * sinStrike/2.0d);
        final float lower3y = (float) (ctrY + length * cosStrike/2.0d);
        final float lower4x = (float) (ctrX - length * sinStrike/2.0d);
        final float lower4y = (float) (ctrY - length * cosStrike/2.0d);
        Coord3d lower3 = new Coord3d(lower3x, lower3y, lowerZ);
        Coord3d lower4 = new Coord3d(lower4x, lower4y, lowerZ);
        
        final float upperZ = (float) (ctrZ + width * sinDip);
        final double projSurfaceWidth = width * cosDip;
        final float upper2x = (float) (lower3x - projSurfaceWidth * cosStrike);
        final double upper2y = (float) (lower3y + projSurfaceWidth * sinStrike);
        final double upper1x = (float) (lower4x - projSurfaceWidth * cosStrike);
        final double upper1y = (float) (lower4y + projSurfaceWidth * sinStrike);
        Coord3d upper1 = new Coord3d(upper1x, upper1y, upperZ);
        Coord3d upper2 = new Coord3d(upper2x, upper2y, upperZ);

        setElements(upper1, upper2, lower3, lower4, color);   
    }
    
    private void setElements(Coord3d upper1, Coord3d upper2, Coord3d lower3,
            Coord3d lower4, Color color) {
        plane = new Quad();
        plane.add(new Point(upper1, color));
        plane.add(new Point(upper2, color));
        plane.add(new Point(lower3, color));
        plane.add(new Point(lower4, color));
        plane.setColor(color);
        
        outline = new LineStrip();
        outline.add(new Point(upper1, Color.BLACK));
        outline.add(new Point(upper2, Color.BLACK));
        outline.add(new Point(lower3, Color.BLACK));
        outline.add(new Point(lower4, Color.BLACK));
        outline.add(new Point(upper1, Color.BLACK));
        
        topLine = new LineStrip();
        topLine.setWidth(3);
        topLine.add(new Point(upper1, Color.BLACK));
        topLine.add(new Point(upper2, Color.BLACK));
        
        this.nameLocation = lower3;
    }
    
    @Override
    public void draw(GL gl, GLU glu, Camera cam) {
        if (transform != null)
            transform.execute(gl);
        
        plane.draw(gl, glu, cam);
        outline.draw(gl, glu, cam);
        if (hasTopLine) topLine.draw(gl, glu, cam);
        
        if (name != null){
	        TextBitmap txt = new TextBitmap();
	        txt.drawText(gl, glu, cam, name, nameLocation, Halign.RIGHT, Valign.BOTTOM, Color.BLACK);
        }
    }


 
    
}
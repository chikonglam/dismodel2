package gov.usgs.dismodel.geom.overlays.jzy;

import gov.usgs.dismodel.state.DisplayStateStore;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.text.TextBitmap;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.align.Valign;

public class CrossHair extends AbstractDrawable{
    private static final double RADIUS_TO_BOUNDING_RADIUS = 0.02d;
    
    DisplayStateStore displaySettings;
    private String name;
    private Coord3d center;
    private float radius;
    private float width = 2;
    private TextBitmap txt = new TextBitmap();

    public CrossHair(Coord3d center, float radius, String name, DisplayStateStore displaySettings) {
        this.center = center;
        this.radius = radius;
        
        this.displaySettings = displaySettings;
        this.name = name;
        
    }

    @Override
    public void draw(GL gl, GLU glu, Camera cam) {
        if(transform!=null)
            transform.execute(gl);
        
        float radius = this.radius;
        double minRadius = cam.getRenderingSphereRadius() * RADIUS_TO_BOUNDING_RADIUS;
        if (minRadius > radius) radius = (float) minRadius;
        
        Color color = ColorUtil.toJzyColor( displaySettings.getSourceColor() );
        
        Coord3d xStart = new Coord3d(center.x - radius, center.y, center.z);
        Coord3d xEnd = new Coord3d(center.x + radius, center.y, center.z);
        drawline(gl, xStart, xEnd, color);

        Coord3d yStart = new Coord3d(center.x, center.y - radius, center.z);
        Coord3d yEnd = new Coord3d(center.x, center.y + radius, center.z);
        drawline(gl, yStart, yEnd, color);
        
        Coord3d zStart = new Coord3d(center.x, center.y, center.z - radius);
        Coord3d zEnd = new Coord3d(center.x, center.y, center.z + radius);
        drawline(gl, zStart, zEnd, color);
        
        if (name != null){
            txt.drawText(gl, glu, cam, name, yStart, Halign.RIGHT, Valign.BOTTOM, Color.BLACK);
        }
        
        
    }
    
    public void drawline(GL gl, Coord3d start, Coord3d end, Color color){
        gl.glLineWidth(width);  
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glColor4f(color.r, color.g, color.b, color.a);
        gl.glVertex3f(start.x, start.y, start.z);
        gl.glVertex3f(end.x, end.y, end.z);   
        gl.glEnd();
    }
    
    
}

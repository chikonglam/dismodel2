package gov.usgs.dismodel.geom.overlays.jzy;

import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.Label;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractWireframeable;
import org.jzy3d.plot3d.rendering.view.Camera;
import org.jzy3d.plot3d.text.TextBitmap;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.align.Valign;

/**
 * Simple location marker that displays a point and the name of the point.
 * 
 * @author dmcmanamon
 */
public class Marker extends AbstractWireframeable {
    private final Label label;
    private final Coord3d center;
    private Color color;
    
    public Marker(LLH origin, Label label, Color color) {
        super();
        this.label = label;
        this.color = color;
        
        LocalENU localENU = label.getLocation().toLocalENU(origin);
        Coord3d loc = new Coord3d(localENU.getEasting(), localENU.getNorthing(), localENU.getUp());
        this.center = loc;
        
        bbox = new BoundingBox3d();
        bbox.add(center);
    }
    
    @Override
    public void draw(GL gl, GLU glu, Camera cam) {
        if(transform!=null)
            transform.execute(gl);
        
        gl.glPointSize(4.0f);
        
        gl.glBegin(GL.GL_POINTS);
        gl.glColor4f(color.r, color.g, color.b, color.a);
        gl.glVertex3f(center.x, center.y, center.z);
        gl.glEnd();
        
        TextBitmap txt = new TextBitmap();
        txt.drawText(gl, glu, cam, label.getName(), center, Halign.LEFT, Valign.TOP, color);
    }

    public Label getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Marker [center=" + center + ", color=" + color + ", name="
                + label.getName() + "]";
    }
}

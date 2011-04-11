package gov.usgs.dismodel.geom.overlays.jzy;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractWireframeable;
import org.jzy3d.plot3d.rendering.view.Camera;

public class Ellipsoid extends AbstractWireframeable {
    private float xRadius;
    private float yRadius;
    private float zRadius;
    private Coord3d center;
    
    private GLUquadric qobj;
    protected int slices = 15;
    protected int stacks = 15;   
    
    protected Color color = Color.GRAY;
    
    public Ellipsoid(Coord3d center, double xRadius, double yRadius, double zRadius) {
        super();
        this.center = center;
        this.xRadius = (float) xRadius;
        this.yRadius = (float) yRadius;
        this.zRadius = (float) zRadius;
        
        color.a = 0.5f;
        
        this.bbox = new BoundingBox3d(
                center.x - this.xRadius, center.x + this.xRadius,
                center.y - this.yRadius, center.y + this.yRadius, 
                center.z - this.zRadius, center.z + this.zRadius);
        setWireframeDisplayed(false);
    }
    
    @Override
    public void draw(GL gl, GLU glu, Camera camera) {
        if(transform!=null)
            transform.execute(gl);
        
        gl.glTranslatef(center.x,center.y,center.z);
        gl.glScalef(1.0f, yRadius/xRadius, zRadius/xRadius);
        
        if(qobj==null)
            qobj = glu.gluNewQuadric();
        
        if(facestatus){
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
            gl.glColor4f(color.r, color.g, color.b, color.a);
            glu.gluSphere(qobj, xRadius, slices, stacks);
            //glut.glutSolidSphere(radius, slices, stacks);
        }
        if(wfstatus){
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
            gl.glLineWidth(wfwidth);
            gl.glColor4f(wfcolor.r, wfcolor.g, wfcolor.b, wfcolor.a);
            glu.gluSphere(qobj, xRadius, slices, stacks);
            //glut.glutSolidSphere(radius, slices, stacks);
        }       
    }
}

package gov.usgs.dismodel.geom.overlays.jzy;

import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;
import gov.usgs.dismodel.geom.overlays.VectorXyz;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.view.Camera;

/**
 * For displaying a vector in 3D.<BR> Published papers are limited to showing 3D vectors in 2D, 
 * a line and arrow; however, technology brings us a 3D displacement vector (line w/cone) and 
 * and error ellipsoid.
 */
public class Vector3D extends AbstractDrawable {
    private static final double ARROW_HEAD_TO_BOUNDING_RADIUS = 0.02d;
    
	private Coord3d start;
	private Coord3d end;
	
	private Ellipsoid error = null; // optional
	
	private Color color;
	private VectorXyz vector; // for re-drawing when origin is updated
	private double scale;
	
	/**
	 * Constructor.
	 * 
	 * @param vector displacement vector in lat/long
	 * @param origin
	 * @param scale
	 * @param color
	 */
	public Vector3D(VectorXyz vector, LLH origin, double scale, Color color) {
	    this.vector = vector;
	    this.scale = scale;
	    this.color = color;
	    LocalENU startENU = vector.getStart().toLocalENU(origin);
	    this.start = new Coord3d(startENU.getX(), startENU.getY(), startENU.getZ());
	    
	    XyzDisplacement disp = vector.getDisplacement();
	    double endEast = startENU.getEasting() + disp.getX() * scale;
        double endNorth = startENU.getNorthing() + disp.getY() * scale;
        double endUp = startENU.getUp() + disp.getZ() * scale;
        LocalENU endENU = new LocalENU(endEast, endNorth, endUp, origin);
        this.end = new Coord3d(endENU.getX(), endENU.getY(), endENU.getZ());
        
        XyzDisplacement errorRads = vector.getError();
        if (errorRads != null) {
            this.error = new Ellipsoid(end, errorRads.getX() * scale, 
                    errorRads.getY() * scale, errorRads.getZ() * scale);
        }

        setBBox(start, end);
	}
	
	/**
     * Constructor for a vector with no Lat/Lng, scale or error ellipsoids.
     */
    public Vector3D(Coord3d start, Coord3d end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
        
        setBBox(start, end);
    }
	
	public double getLength() {
        double xDelta = end.x - start.x;
        double yDelta = end.y - start.y;
        double zDelta = end.z - start.z;
        double dist = Math.sqrt(Math.pow(xDelta, 2.0) + Math.pow(yDelta, 2.0) + Math.pow(zDelta, 2.0));
        return dist;
	}

	
	private void setBBox(Coord3d start, Coord3d end) {
	    bbox = new BoundingBox3d();
        bbox.add(start);
        bbox.add(end);
	}
	
	public Coord3d getStart() {
	    return start;
	}

	public Coord3d getEnd() {
	    return end;
	}

	public VectorXyz getVector() {
        return vector;
    }

    public double getScale() {
        return scale;
    }

    public Color getColor() {
        return color;
    }

    @Override
	public void draw(GL gl, GLU glu, Camera cam) {
		if (transform != null)
			transform.execute(gl);
		
		//draw the line first
		//-------------------
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glColor4f(color.r, color.g, color.b, color.a);
		gl.glVertex3f(start.x, start.y, start.z);
		gl.glVertex3f(end.x, end.y, end.z);
		gl.glEnd();
		
		//now draw the arrowhead
		//----------------------
		double dx = (double) (end.x - start.x);
		double dy = (double) (end.y - start.y);
		double dz = (double) (end.z - start.z);
		double totalLen = Math.sqrt(dx*dx + dy*dy + dz*dz);
		
		Coord3d camEye = cam.getEye();
		double camX = camEye.x;
		double camY = camEye.y;
		double camZ = camEye.z;
		
		double crossX = dy*camZ - dz*camY;						//cross product: perpendicular to both vector and the eye
		double crossY = dz*camX - dx*camZ;
		double crossZ = dx*camY - dy*camX;
		double crossLen = Math.sqrt(crossX*crossX + crossY*crossY + crossZ*crossZ);
		
		double arrowHeadLen = cam.getRenderingSphereRadius() * ARROW_HEAD_TO_BOUNDING_RADIUS;
		
		double hx1 = end.x + arrowHeadLen*(-dx/totalLen +  0.5d * crossX/crossLen);		//the endpoint of the arrow head 1
		double hy1 = end.y + arrowHeadLen*(-dy/totalLen +  0.5d * crossY/crossLen);
		double hz1 = end.z + arrowHeadLen*(-dz/totalLen +  0.5d * crossZ/crossLen);
		
		double hx2 = end.x + arrowHeadLen*(-dx/totalLen -  0.5d * crossX/crossLen);		//the endpoint of the arrow head 1
		double hy2 = end.y + arrowHeadLen*(-dy/totalLen -  0.5d * crossY/crossLen);
		double hz2 = end.z + arrowHeadLen*(-dz/totalLen -  0.5d * crossZ/crossLen);

		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(end.x, end.y, end.z);
		gl.glVertex3f((float)hx1, (float)hy1, (float)hz1);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(end.x, end.y, end.z);
		gl.glVertex3f((float)hx2, (float)hy2, (float)hz2);
		gl.glEnd();
		
		if (error != null){
		    error.draw(gl, glu, cam);
		}
	}
}
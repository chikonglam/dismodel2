package gov.usgs.dismodel.geom.overlays;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.examples.util.DirectedPath;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Position.PositionList;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;


public class ArrowHeadLine extends DirectedPath {
    /** Default arrow length, in meters. */
    public static final double DEFAULT_ARROW_LENGTH = 300;
    /** Default arrow angle. */
    public static final Angle DEFAULT_ARROW_ANGLE = Angle.fromDegrees(45.0);
    /** Default maximum screen size of the arrowheads, in pixels. */
    public static final double DEFAULT_MAX_SCREEN_SIZE = 20.0;
    /** Default minimum screen size of the arrowheads, in pixels. */
    public static final double DEFAULT_MIN_SCREEN_SIZE = 20.0;
    
    private double minScreenSize = DEFAULT_MIN_SCREEN_SIZE; 
    
	
    public ArrowHeadLine() {
		super();
		postConstructor();
	}

	public ArrowHeadLine(Iterable<? extends Position> positions) {
		super(positions);
		postConstructor();
	}

	public ArrowHeadLine(Position ptA, Position ptB) {
		super(ptA, ptB);
		postConstructor();
	}

	public ArrowHeadLine(PositionList positions) {
		super(positions);
		postConstructor();
	}
	
	protected void postConstructor(){
	    arrowLength = DEFAULT_ARROW_LENGTH;
	    arrowAngle = DEFAULT_ARROW_ANGLE;
	    maxScreenSize = DEFAULT_MAX_SCREEN_SIZE;
	}

	protected void computeArrowheadGeometry(DrawContext dc, Vec4 polePtA, Vec4 polePtB, Vec4 ptA, Vec4 ptB,
            double arrowLength, double arrowBase, FloatBuffer buffer, PathData pathData)
        {
            // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
            // segment, and one perpendicular to it. The plane of the arrowhead will be parallel to the surface.

            double poleDistance = polePtA.distanceTo3(polePtB);

            // Compute parallel component
            Vec4 parallel = ptA.subtract3(ptB);

            Vec4 surfaceNormal = dc.getGlobe().computeSurfaceNormalAtPoint(ptB);

            // Compute perpendicular component
            Vec4 perpendicular = surfaceNormal.cross3(parallel);

            // Compute midpoint of segment
            Vec4 arrowHead = polePtB;		
            
            if (!this.isArrowheadSmall(dc, arrowHead, 1))
            {
                // Compute the size of the arrowhead in pixels to make ensure that the arrow does not exceed the maximum
                // screen size.
                View view = dc.getView();
                double arrowHeadDistance = view.getEyePoint().distanceTo3(arrowHead);
                double pixelSize = view.computePixelSizeAtDistance(arrowHeadDistance);

                if (arrowLength / pixelSize < this.minScreenSize){
                	arrowLength = this.minScreenSize * pixelSize;
                    arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
                }

                
                if (arrowLength / pixelSize > this.maxScreenSize)
                {
                    arrowLength = this.maxScreenSize * pixelSize;
                    arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
                }

                // Don't draw an arrowhead if the path segment is smaller than the arrow's base or length
                if (poleDistance <= arrowLength || poleDistance <= arrowBase)
                    return;

                perpendicular = perpendicular.normalize3().multiply3(arrowBase);
                parallel = parallel.normalize3().multiply3(arrowLength);

                // Compute geometry of direction arrow
                Vec4 vertex1 = arrowHead.add3(parallel).add3(perpendicular);
                Vec4 vertex2 = arrowHead.add3(parallel).add3(perpendicular.multiply3(-1.0));

                // Add geometry to the buffer
                Vec4 referencePoint = pathData.getReferencePoint();
                buffer.put((float) (vertex1.x - referencePoint.x));
                buffer.put((float) (vertex1.y - referencePoint.y));
                buffer.put((float) (vertex1.z - referencePoint.z));

                buffer.put((float) (vertex2.x - referencePoint.x));
                buffer.put((float) (vertex2.y - referencePoint.y));
                buffer.put((float) (vertex2.z - referencePoint.z));

                buffer.put((float)( arrowHead.x - referencePoint.x )); 
                buffer.put((float)( arrowHead.y - referencePoint.y )); 
                buffer.put((float)( arrowHead.z -referencePoint.z ));
            }
        }

	@Override
	public String toString() {
		return "ArrowHeadLine [" + super.toString() + "]";
	}
}

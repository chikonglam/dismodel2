package gov.usgs.dismodel.geom.overlays.jzy;

import gov.usgs.dismodel.calc.greens.XyzDisplacement;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.rendering.view.Camera;

/**
 * When instances of this class are added to a chart
 * they can be used to map mouse clicks (in pixels) 
 * back to x,y locations on the graph. <BR>
 * An OpenGL call to glu.gluUnProject() does the actual conversion.
 * 
 * @author dmcmanamon
 */
public class ScreenToGraphMap extends AbstractDrawable {

    private List<Float> xMap;
    private List<Float> yMap;
    private Rectangle r;
    
    private double pixelSize;// pixel size in meters along x
    
    public ScreenToGraphMap() {
    }
    
    /**
     * Converts x, y in pixels to graph coordinates
     * @param x
     * @param y
     * @return
     */
    public Coord2d map(int x, int y) {
        // TODO any error checking?
        x = x - r.x;
        y = y - r.y;
        if (x < 0 || x >= xMap.size() || y < 0 || y > yMap.size()) return null;
        float xVal = xMap.get(x);
        float yVal = yMap.get(y);
        return new Coord2d(xVal, yVal);
    }

    @Override
    public void draw(GL gl, GLU glu, Camera cam) {
        r = cam.getRectangle();
        xMap = new ArrayList<Float>(r.width);
        yMap = new ArrayList<Float>(r.height);
        
        for (int i=0; i < r.width; i++) {
            for (int j = 0; j < r.height; j++) {
                if (i == j) { // just diagonal
                    Coord3d mapXYZ = cam.screenToModel(gl, glu, new Coord3d(i+r.x, j+r.y,0.0));
                    xMap.add(i, mapXYZ.x);
                    yMap.add(j, -mapXYZ.y);
                }
            }
        }
        
        Coord3d mapX0 = cam.screenToModel(gl, glu, new Coord3d(0.0, 0.0,0.0));
        Coord3d mapXMax = cam.screenToModel(gl, glu, new Coord3d(r.width, 0.0,0.0));
        double distance = new XyzDisplacement(mapXMax.x - mapX0.x, mapXMax.y - mapX0.y, mapXMax.z - mapX0.z).distance();
        
        this.pixelSize = distance/r.width;
    }

    public double getPixelSize() {
        return pixelSize;
    }
    
    public int getMapWidth() {
        return r.width;
    }
}

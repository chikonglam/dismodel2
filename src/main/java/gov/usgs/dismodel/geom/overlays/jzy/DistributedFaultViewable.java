package gov.usgs.dismodel.geom.overlays.jzy;

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
import gov.usgs.dismodel.calc.greens.DistributedFault;
import gov.usgs.dismodel.calc.greens.OkadaFault3;

public class DistributedFaultViewable extends AbstractDrawable {
    protected LineStrip outline;
    protected LineStrip topLine;
    protected LineStrip[] rowDivLines;
    protected LineStrip[] colDivLines;
    protected Quad[][] subFaultSurface;
    
    private OkadaFault3[][] subfaults;
    private int rowCt;
    private int colCt;
    private double maxMag=0;
    private double minMag=Double.POSITIVE_INFINITY;
    private double[][] relMag;
    protected String name;
    Coord3d nameLocation;
    
//    private Color color;

    
    public DistributedFaultViewable(DistributedFault fault, Color color, String name) {
        //stats vars
        rowCt = fault.getRowCt();
        colCt = fault.getColCt();
        subfaults = fault.getSubfaults();
        this.name = name;
        
        //overall frame
        //--------------
        double lowerX1 = fault.getLowerX1();
        double lowerY1 = fault.getLowerY1();
        double lowerX2 = fault.getLowerX2();
        double lowerY2 = fault.getLowerY2();
        double lowerU = fault.getLowerUp();

        double upperX1 = fault.getUpperX1();
        double upperY1 = fault.getUpperY1();
        double upperX2 = fault.getUpperX2();
        double upperY2 = fault.getUpperY2();
        double upperU = fault.getUpperUp();
        
        //setting the points
        Coord3d upper1 = new Coord3d(upperX1, upperY1, upperU);
        Coord3d upper2 = new Coord3d(upperX2, upperY2, upperU);
        Coord3d lower3 = new Coord3d(lowerX2, lowerY2, lowerU);
        Coord3d lower4 = new Coord3d(lowerX1, lowerY1, lowerU);
        
        //setting the label
        nameLocation = lower3;
        
        //setting the frame
        outline = new LineStrip();
        outline.add(new Point(upper1, Color.BLACK));
        outline.add(new Point(upper2, Color.BLACK));
        outline.add(new Point(lower3, Color.BLACK));
        outline.add(new Point(lower4, Color.BLACK));
        outline.add(new Point(upper1, Color.BLACK));
        
        //setting the top line
        topLine = new LineStrip();
        topLine.setWidth(3);        
        topLine.add(new Point(upper1, Color.BLACK));
        topLine.add(new Point(upper2, Color.BLACK));
        
        //horizontal divs
        if (rowCt > 1){
            rowDivLines = new LineStrip[rowCt-1];
            for (int rowIter = 0; rowIter < rowCt-1 ; rowIter++ ){
                double x1 = subfaults[rowIter][0].getLowerX1();
                double y1 = subfaults[rowIter][0].getLowerY1();
                double u1 = subfaults[rowIter][0].getLowerUp();
                double x2 = subfaults[rowIter][colCt-1].getLowerX2();
                double y2 = subfaults[rowIter][colCt-1].getLowerY2();
                double u2 = subfaults[rowIter][colCt-1].getLowerUp();
                LineStrip curLine = new LineStrip();
                Coord3d pt1 = new Coord3d(x1, y1, u1);
                Coord3d pt2 = new Coord3d(x2, y2, u2);
                curLine.add(new Point(pt1, Color.BLACK));
                curLine.add(new Point(pt2, Color.BLACK));
                rowDivLines[rowIter] = curLine;
            }
        }
        
        //vertical divs
        if (colCt>1){
            colDivLines = new LineStrip[colCt - 1];
            for (int colIter = 0; colIter < colCt - 1; colIter++ ){
                double x1 = subfaults[0][colIter+1].getUpperX1();
                double y1 = subfaults[0][colIter+1].getUpperY1();
                double u1 = subfaults[0][colIter+1].getUpperUp();
                double x2 = subfaults[rowCt-1][colIter+1].getLowerX1();
                double y2 = subfaults[rowCt-1][colIter+1].getLowerY1();
                double u2 = subfaults[rowCt-1][colIter+1].getLowerUp();
                LineStrip curLine = new LineStrip();
                Coord3d pt1 = new Coord3d(x1, y1, u1);
                Coord3d pt2 = new Coord3d(x2, y2, u2);
                curLine.add(new Point(pt1, Color.BLACK));
                curLine.add(new Point(pt2, Color.BLACK));
                colDivLines[colIter] = curLine;
            }
        }
        
        //Coloring the surface
        //--------------------
        relMag = new double[rowCt][colCt];
        
        //find max mag and fill relMag
        for(int rowIter = 0; rowIter < rowCt; rowIter++){
            for(int colIter = 0; colIter < colCt; colIter++){
                double curMag = subfaults[rowIter][colIter].getMagnitude();
                relMag[rowIter][colIter] = curMag;
                if (curMag > maxMag){
                    maxMag = curMag;
                }
                if (curMag < minMag){
                    minMag = curMag;
                }
            }
        }
        
        //find realMag's value
        for(int rowIter = 0; rowIter < rowCt; rowIter++){
            for(int colIter = 0; colIter < colCt; colIter++){
                relMag[rowIter][colIter] -= minMag;
                relMag[rowIter][colIter] /= (maxMag - minMag);
            }
        }
        
        //fill the quads
        subFaultSurface = new Quad[rowCt][colCt];
        for(int rowIter = 0; rowIter < rowCt; rowIter++){
            for(int colIter = 0; colIter < colCt; colIter++){
                Quad curSurf = new Quad();
                OkadaFault3 curSubFault = subfaults[rowIter][colIter];
                double curLowerX1 = curSubFault.getLowerX1();
                double curLowerY1 = curSubFault.getLowerY1();
                double curLowerX2 = curSubFault.getLowerX2();
                double curLowerY2 = curSubFault.getLowerY2();
                double curLowerU = curSubFault.getLowerUp();

                double curUpperX1 = curSubFault.getUpperX1();
                double curUpperY1 = curSubFault.getUpperY1();
                double curUpperX2 = curSubFault.getUpperX2();
                double curUpperY2 = curSubFault.getUpperY2();
                double curUpperU = curSubFault.getUpperUp();
                
                Coord3d curUpper1 = new Coord3d(curUpperX1, curUpperY1, curUpperU);
                Coord3d curUpper2 = new Coord3d(curUpperX2, curUpperY2, curUpperU);
                Coord3d curLower1 = new Coord3d(curLowerX1, curLowerY1, curLowerU);
                Coord3d curLower2 = new Coord3d(curLowerX2, curLowerY2, curLowerU);
                
                Color fillColor;
                
                if (color == null){
                    double scaledMag = relMag[rowIter][colIter];
                    if (Double.isNaN(scaledMag)){
                        fillColor = new Color(0.75f, 0.75f, 0.75f, 0.2f);
                    } else {
                        fillColor = new Color((float)scaledMag, (float)(1.0f-scaledMag), 0.0f, 0.2f);
                    }
                } else {
                    fillColor = color;
                }
                
                curSurf.setColor(fillColor);
                
                
                curSurf.setColor(fillColor);
                curSurf.add(new Point(curUpper1,fillColor));      
                curSurf.add(new Point(curUpper2,fillColor));
                curSurf.add(new Point(curLower2,fillColor));
                curSurf.add(new Point(curLower1,fillColor));
                
                System.out.println("Surface:(" + rowIter + "," + colIter + ")" + curSurf);
                
                subFaultSurface[rowIter][colIter] = curSurf;
            }
        }
        
        
        
    }
    

    public double getMaxMag() {
		return maxMag;
	}


	public double getMinMag() {
		return minMag;
	}
	
	
	public OkadaFault3[][] getSubfaults() {
		return subfaults;
	}


	public int getRowCt() {
		return rowCt;
	}


	public int getColCt() {
		return colCt;
	}


	public void setsubFaultColor(int row, int col, Color color){
		subFaultSurface[row][col].setColor(color);
	}
	
	
	


	@Override
    public void draw(GL gl, GLU glu, Camera cam) {
        if (transform != null)
            transform.execute(gl);
        
        //draw the color first
        for(int rowIter = 0; rowIter < rowCt; rowIter++){
            for(int colIter = 0; colIter < colCt; colIter++){
                subFaultSurface[rowIter][colIter].draw(gl, glu, cam);
            }
        }
        
        //draw the dividers
        for (LineStrip curRowLine : rowDivLines) {
            curRowLine.draw(gl, glu, cam);
        }
        for (LineStrip curColLine : colDivLines){
            curColLine.draw(gl, glu, cam);
        }
        
        //draw the outline
        outline.draw(gl, glu, cam);
        topLine.draw(gl, glu, cam);
        
        if (name != null){
	        TextBitmap txt = new TextBitmap();
	        txt.drawText(gl, glu, cam, name, nameLocation, Halign.RIGHT, Valign.BOTTOM, Color.BLACK);
        }
    }

    
    
    
    
    
    
}

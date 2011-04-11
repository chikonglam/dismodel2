package gov.usgs.dismodel.geom.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.NEW;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

public class ArrowLine
{
	private static final double VECTOR_WIDTH = 4;
    private LatLon startLatLon;
	private LatLon endLatLon;
	private SurfacePolyline line;
	//SurfacePolygon arrow;
	private MarkerRenderable arrowhead;
	

    public ArrowLine(LatLon startLatLon, LatLon endLatLon) {
    	this.startLatLon = startLatLon;
    	this.endLatLon = endLatLon;

    	// Make the line.
        this.line = new SurfacePolyline(new ArrayList<LatLon>(Arrays.asList(
    		this.startLatLon, 
    		this.endLatLon)));
        

        
        
        
    }
    
    public void draw(RenderableLayer layer, Color color, double scale, List<Renderable> accountingList){
    	draw(layer, color, scale);
    	accountingList.add(line);
    	accountingList.add(arrowhead);
    	//accountingList.add(arrow);
    }

    public void draw(RenderableLayer layer, Color color, double scale)
    {
        Material material = new Material(color);
        
        ShapeAttributes lineAttrs = new BasicShapeAttributes();
        lineAttrs.setOutlineMaterial(material);
        lineAttrs.setOutlineWidth(VECTOR_WIDTH);
        

        this.line.setAttributes(lineAttrs);
        
        BasicMarkerAttributes markerAttributes = new BasicMarkerAttributes ( material, BasicMarkerShape.HEADING_ARROW, 1d, 40, 20);
        Angle heading = LatLon.greatCircleAzimuth(this.startLatLon, this.endLatLon);
        arrowhead = new MarkerRenderable(
        		new BasicMarker(new Position( this.endLatLon, 0d), markerAttributes, heading) 
        );
        
         layer.addRenderable(this.line);
         layer.addRenderable(arrowhead);
    }

    public double getLength(Globe wwGlobe) {
    	return this.line.getLength(wwGlobe);
    }

	public LatLon getStartLatLon() {
		return startLatLon;
	}

	public LatLon getEndLatLon() {
		return endLatLon;
	}
	
	private static class MarkerRenderable implements Renderable{
		private MarkerRenderer renderer = new MarkerRenderer();
		private ArrayList<Marker> marker = new ArrayList<Marker>();
		public MarkerRenderable(Marker marker) {
			super();
			this.marker.add(marker);
		}
		
		@Override
		public void render(DrawContext dc) {
			renderer.render(dc, marker);
		}
		
		
	}
    
    
    
    
}

package gov.usgs.dismodel.geom.overlays;

import java.awt.Color;
import java.util.ArrayList;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceEllipse;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.usgs.dismodel.calc.greens.XyzDisplacement;
import gov.usgs.dismodel.geom.Convert;
import gov.usgs.dismodel.geom.LLH;
import gov.usgs.dismodel.geom.LocalENU;

public class WWVector {
	private static final int VECTOR_WIDTH = 3;
	private static final double ARROW_OPACITY = 0.7;
	private static final double ELLIPSE_OPACITY = 0.5;
	
	private ArrowHeadLine vectorLine;
	private SurfaceEllipse errorEllipse;

	
		
	public WWVector(VectorXyz vector, double scale, Color color){
		this(vector.getStart(), vector.getDisplacement(), vector.getError(), scale, color);
	}

	public WWVector(LLH startPoint, XyzDisplacement disp, XyzDisplacement error, double scale, Color color) {
		Material bodyMaterial = new Material(color);
    	LocalENU endPointENU = new LocalENU(disp.getX() * scale, disp.getY() * scale, disp.getZ() * scale,
    			startPoint); 
    	LLH endPoint = Convert.toLLH(endPointENU);
    	LatLon start = LatLon.fromDegrees(startPoint.getLatitude().toDeg(), startPoint.getLongitude().toDeg());
    	LatLon end = LatLon.fromDegrees(endPoint.getLatitude().toDeg(), endPoint.getLongitude().toDeg());
		
		//The arrowed line first
		vectorLine = new ArrowHeadLine( new Position(start, 0), new Position(end, 0) );
        ShapeAttributes lineAttrs = new BasicShapeAttributes();
        lineAttrs.setOutlineMaterial(bodyMaterial);
        lineAttrs.setOutlineWidth(VECTOR_WIDTH);
        lineAttrs.setOutlineOpacity(ARROW_OPACITY);
        vectorLine.setAttributes(lineAttrs);
        vectorLine.setVisible(true);
        vectorLine.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        vectorLine.setPathType(AVKey.GREAT_CIRCLE);
		
		//The errorEllipse
		if (error != null){
            ShapeAttributes errorEllipseAttrs = new BasicShapeAttributes();
            Color elipseColor = (  new Color( color.getRed(), color.getGreen(), color.getBlue() )  ).brighter();
            Material ellipseMaterial = new Material( elipseColor );
            errorEllipseAttrs.setOutlineMaterial(ellipseMaterial);		//see if these are necessary
            errorEllipseAttrs.setInteriorMaterial(ellipseMaterial);
            errorEllipseAttrs.setOutlineWidth(0);
            errorEllipseAttrs.setDrawInterior(true);
            errorEllipseAttrs.setInteriorOpacity(ELLIPSE_OPACITY);
            errorEllipseAttrs.setDrawOutline(false);
			
			errorEllipse = new SurfaceEllipse(
    				errorEllipseAttrs,
    				end,
    				error.getX() * scale,
    				error.getY() * scale,
    				Angle.ZERO);
		}
	}

	public ArrowHeadLine getVectorLine() {
		return vectorLine;
	}

	public SurfaceEllipse getErrorEllipse() {
		return errorEllipse;
	}

	
}

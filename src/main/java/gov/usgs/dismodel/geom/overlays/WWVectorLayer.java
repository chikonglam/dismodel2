package gov.usgs.dismodel.geom.overlays;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.SurfaceEllipse;

import java.util.Iterator;

public class WWVectorLayer {
    private RenderableLayer arrowLayer = new RenderableLayer();
    private RenderableLayer ellipseLayer = new RenderableLayer();

    public WWVectorLayer() {

    }

    public void setVectors(Iterable<WWVector> vectors) {
        Iterator<WWVector> vectIter = vectors.iterator();

        while (vectIter.hasNext()) {
            WWVector curVect = vectIter.next();
            ArrowHeadLine vectorLine = curVect.getVectorLine();
            arrowLayer.addRenderable(vectorLine);
            SurfaceEllipse errorEllipse = curVect.getErrorEllipse();
            if (errorEllipse != null)
                ellipseLayer.addRenderable(errorEllipse);

        }

    }

    public void removeAll() {

        arrowLayer.removeAllRenderables();
        ellipseLayer.removeAllRenderables();

    }

    public void initLayer(WorldWindow wwd) {

        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        int placeNamePosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer) {
                compassPosition = layers.indexOf(l);
            } else if (l instanceof PlaceNameLayer) {
                placeNamePosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, ellipseLayer);
        layers.add(compassPosition, arrowLayer);
    }

}

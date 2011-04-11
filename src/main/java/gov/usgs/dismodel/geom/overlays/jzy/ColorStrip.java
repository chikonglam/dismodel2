package gov.usgs.dismodel.geom.overlays.jzy;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.plot2d.primitive.ColorbarImageGenerator;
import org.jzy3d.plot3d.primitives.axes.layout.providers.SmartTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.DefaultDecimalTickRenderer;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.rendering.legends.colorbars.ColorbarLegend;
import org.jzy3d.plot3d.rendering.view.Renderer2d;

public class ColorStrip implements Renderer2d {

	private static final int XCOORD = 20;
	private static final int YCOORD = 10;
	private static final int IMG_WIDTH = 100;
	private static final int IMG_HEIGHT = 300;
	private static final int STRIP_WIDTH = 30;
	private static final int TICK_SIZE = 5;

	private ColorMapper colorMapper;
	private float zmin;
	private float zmax;
	private Image image;
	private String unitString;

	public ColorStrip( float zmin, float zmax, String unitString){
		this.zmin = zmin;
		this.zmax = zmax;
		this.unitString = unitString;

		ColorMapRainbow colorMapRainbow = new ColorMapRainbow();

		colorMapper = new ColorMapper(
			colorMapRainbow,
			zmin, zmax
		);

		ColorbarImageGenerator colorBar = new ColorbarImageGenerator(
				colorMapper,
			new SmartTickProvider(3),
			new ColorBarTickRenderer()
		);

		image = colorBar.toImage(IMG_WIDTH, IMG_HEIGHT);
	}

	public ColorStrip(float zmin, float zmax){
		this(zmin, zmax, "Meters");
	}

	public ColorStrip(){
		this(0, 255, "Meters");
	}

	public Color getColor(float znum){
		return(colorMapper.getColorMap().getColor(colorMapper, 0f, 0f, znum));
	}

	public float getZmin(){
		return zmin;
	}

	public float getZmax(){
		return zmax;
	}

	@Override
	public void paint(Graphics g){
		ImageObserver imageObserver = new ImageObserver(){
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x,
				int y, int width, int height){
				return false;
			}
		};

		g.drawImage(
			image,
			XCOORD, YCOORD,
			imageObserver
		);

		int X1 = XCOORD;
		int X2 = X1 + 5;
		int X3 = X1 + STRIP_WIDTH - 15;
		int X4 = X3 + 10;
		int X5 = X4 + 3;							// drawString X offset
		int YB = YCOORD+IMG_HEIGHT-1;				// Y bottom
		int TS = TICK_SIZE - 1;
		int YS = 0;									// drawString Y offset

		ITickRenderer tr = new DefaultDecimalTickRenderer(4);

		g.setColor(java.awt.Color.BLACK);
		g.drawLine(X3, YCOORD + 5, X4, YCOORD + 5);			// Top ticks
		g.drawString(tr.format(zmax), X5, YCOORD+10);

		int k = (YB - YCOORD) / TS;
		int s = YCOORD + k;
		for(int i = 1; i < TS; i++){
			g.drawLine(X1, s, X2, s);
			g.drawLine(X3, s, X4, s);
			g.drawString(tr.format(((zmax - zmin) / (TICK_SIZE - 1)) * (TICK_SIZE - 1 - i) + zmin), X5, s+YS);
			s += k;
		}

		g.drawLine(X3, YB-5, X4, YB-5);					// Bottom ticks
		g.drawString(tr.format(zmin), X5, YB);

		g.drawString(unitString, XCOORD, (YCOORD + IMG_HEIGHT + 17));
	}

	private class ColorBarTickRenderer implements ITickRenderer{
		@Override
		public String format(float value){
			return "";
		}
	}
}



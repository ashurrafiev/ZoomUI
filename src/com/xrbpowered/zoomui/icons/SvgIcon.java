package com.xrbpowered.zoomui.icons;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class SvgIcon {

	public final String uri;
	public IconPalette palette;
	public final int baseSize;
	
	private Path2D fgPath;
	private Path2D bgPath;
	private Rectangle2D bounds = null;

	private HashMap<Integer, BufferedImage> cache = new HashMap<>();
	
	public SvgIcon(String uri, int baseSize, IconPalette palette) {
		this.uri = uri;
		this.baseSize = baseSize;
		this.palette = palette;
	}
	
	public SvgIcon load() {
		SvgFile svg = new SvgFile(uri);
		fgPath = svg.getPath("fg", 1);
		bgPath = svg.getPath("bg", 1);
		bounds = fgPath.getBounds2D();
		if(bgPath!=null)
			bounds.add(bgPath.getBounds2D());
		return this;
	}
	
	private double getScale(float size, float pixelScale) {
		return size/(double)baseSize/pixelScale;
	}
	
	public BufferedImage createImage(int style, double scale) {
		if(bounds==null)
			load();
		BufferedImage img = new BufferedImage((int)Math.ceil(bounds.getWidth()*scale+2), (int)Math.ceil(bounds.getHeight()*scale+2), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D ig2 = (Graphics2D) img.getGraphics();
		ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		ig2.scale(scale, scale);
		ig2.translate(-bounds.getX()+1/scale, -bounds.getY()+1/scale);
		if(bgPath!=null) {
			ig2.setPaint(palette.getBgPaint(style, (float)bounds.getY(), (float)(bounds.getY()+baseSize)));
			ig2.fill(bgPath);
		}
		ig2.setPaint(palette.getFgPaint(style, (float)bounds.getY(), (float)(bounds.getY()+baseSize)));
		ig2.fill(fgPath);
		return img;
	}
	
	public BufferedImage createImage(int style, float size, float pixelScale) {
		return createImage(style, getScale(size, pixelScale));
	}
	
	public void paint(Graphics2D g2, int style, float x, float y, float size, float pixelScale, boolean useCache) {
		if(bounds==null)
			load();
		if(useCache) {
			int imgSize = (int)(size/pixelScale);
			double scale = getScale(size, pixelScale);
			
			int key = imgSize*palette.colors.length+style;
			BufferedImage img = cache.get(key);
			if(img==null) {
				img = createImage(style, scale);
				cache.put(key, img);
			}
			AffineTransform tx = g2.getTransform();
			g2.setTransform(new AffineTransform());
			g2.translate(tx.getTranslateX()+bounds.getX()*scale+x/pixelScale, tx.getTranslateY()+bounds.getY()*scale+y/pixelScale);
			g2.drawImage(img, -1, -1, null);
			g2.setTransform(tx);
		}
		else {
			AffineTransform tx = g2.getTransform();
			g2.translate(x, y);
			double scale = size/(double)baseSize;
			g2.scale(scale, scale);
			if(bgPath!=null) {
				g2.setPaint(palette.getBgPaint(style, -baseSize, 0));
				g2.fill(bgPath);
			}
			g2.setPaint(palette.getFgPaint(style, -baseSize, 0));
			g2.fill(fgPath);
			g2.setTransform(tx);
		}
	}
	
}

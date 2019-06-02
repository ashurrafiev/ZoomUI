package com.xrbpowered.zoomui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

public class GraphAssist {
	
	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;
	
	protected static final Stroke defaultStroke = new BasicStroke(1f);
	
	public final Graphics2D graph;
	
	private LinkedList<AffineTransform> txStack = new LinkedList<>();
	private LinkedList<Rectangle> clipStack = new LinkedList<>();
	private LinkedList<Boolean> aaStack = new LinkedList<>();
	private LinkedList<Boolean> pureStrokeStack = new LinkedList<>();
	
	public GraphAssist(Graphics2D graph) {
		this.graph = graph;
	}
	
	public AffineTransform getTransform() {
		return graph.getTransform();
	}
	
	public void setTransform(AffineTransform t) {
		graph.setTransform(t);
	}
	
	public AffineTransform getTx() {
		return txStack.getFirst();
	}
	
	public void pushTx() {
		txStack.addFirst(getTransform());
	}
	
	public void popTx() {
		setTransform(txStack.removeFirst());
	}
	
	public void clearTransform() {
		graph.setTransform(new AffineTransform());
	}
	
	public void translate(double tx, double ty) {
		graph.translate(tx, ty);
	}
	
	public void scale(double scale) {
		graph.scale(scale, scale);
	}
	
	public Rectangle getClip() {
		return graph.getClipBounds();
	}
	
	public void setClip(Rectangle r) {
		graph.setClip(r);
	}
	
	public boolean pushClip(float x, float y, float w, float h) {
		Rectangle clip = getClip();
		Rectangle r = new Rectangle((int)x, (int)y, (int)w, (int)h);
		if(r.intersects(clip)) {
			clipStack.addFirst(clip);
			setClip(r.intersection(clip));
			return true;
		}
		else
			return false;
	}
	
	public void popClip() {
		setClip(clipStack.removeFirst());
	}
	
	public boolean isAntialisingOn() {
		return graph.getRenderingHint(RenderingHints.KEY_ANTIALIASING)==RenderingHints.VALUE_ANTIALIAS_ON;
	}
	
	public void pushAntialiasing(boolean aa) {
		aaStack.addFirst(isAntialisingOn());
		graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	
	public void popAntialiasing() {
		boolean aa = aaStack.removeFirst();
		graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public boolean isPureStrokeOn() {
		return graph.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL)==RenderingHints.VALUE_STROKE_PURE;
	}
	
	public void pushPureStroke(boolean pure) {
		pureStrokeStack.addFirst(isPureStrokeOn());
		graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, pure ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
	}
	
	public void popPureStroke() {
		boolean pure = pureStrokeStack.removeFirst();
		graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, pure ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
	}

	public void setColor(Color c) {
		graph.setColor(c);
	}

	public void setFont(Font f) {
		graph.setFont(f);
	}

	public FontMetrics getFontMetrics() {
		return graph.getFontMetrics();
	}
	
	public void setPaint(Paint p) {
		graph.setPaint(p);
	}
	
	public void setStroke(float width) {
		graph.setStroke(new BasicStroke(width));
	}
	
	public void resetStroke() {
		graph.setStroke(defaultStroke);
	}
	
	public void fillRect(float x, float y, float w, float h) {
		graph.fillRect((int)x, (int)y, (int)w, (int)h);
	}

	public void fillRect(float x, float y, float w, float h, Color c) {
		setColor(c);
		fillRect(x, y, w, h);
	}

	public void fill(UIElement e) {
		fillRect(0, 0, e.getWidth(), e.getHeight());
	}
	
	public void fill(UIElement e, Color c) {
		fillRect(0, 0, e.getWidth(), e.getHeight(), c);
	}
	
	public void drawRect(float x, float y, float w, float h) {
		graph.drawRect((int)x, (int)y, (int)w, (int)h);
	}

	public void drawRect(float x, float y, float w, float h, Color c) {
		setColor(c);
		drawRect(x, y, w, h);
	}

	public void border(UIElement e) {
		drawRect(0, 0, e.getWidth(), e.getHeight());
	}
	
	public void border(UIElement e, Color c) {
		drawRect(0, 0, e.getWidth(), e.getHeight(), c);
	}

	public void line(float x1, float y1, float x2, float y2) {
		graph.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
	}
	
	public void line(float x1, float y1, float x2, float y2, Color c) {
		setColor(c);
		line(x1, y1, x2, y2);
	}

	public void vborder(UIElement e, int halign) {
		float x = align(e.getWidth(), halign);
		line(x, 0, x, e.getHeight());
	}
	
	public void vborder(UIElement e, int halign, Color c) {
		setColor(c);
		vborder(e, halign);
	}

	public void hborder(UIElement e, int valign) {
		float y = align(e.getHeight(), valign);
		line(0, y, e.getWidth(), y);
	}
	
	public void hborder(UIElement e, int valign, Color c) {
		setColor(c);
		hborder(e, valign);
	}

	public float startPixelMode(UIElement e, boolean antialias) {
		pushAntialiasing(antialias);
		pushTx();
		clearTransform();
		translate(getTx().getTranslateX(), getTx().getTranslateY());
		return e.getPixelScale();
	}
	
	public float startPixelMode(UIElement e) {
		return startPixelMode(e, false);
	}
	
	public void finishPixelMode() {
		popTx();
		popAntialiasing();
	}
	
	public void pixelBorder(UIElement e, int thickness, Color fill, Color stroke) {
		float pixelScale = startPixelMode(e);
		
		int w = (int)Math.ceil(e.getWidth() / pixelScale);
		int h = (int)Math.ceil(e.getHeight() / pixelScale);
		
		if(fill!=null) {
			setColor(fill);
			graph.fillRect(0, 0, w, h);
		}
		if(stroke!=null) {
			resetStroke();
			setColor(stroke);
			for(int i=0; i<thickness; i++)
				graph.drawRect(i, i, w-i*2-1, h-i*2-1);
		}
		
		finishPixelMode();
	}
	
	public void drawString(String str, float x, float y) {
		graph.drawString(str, x, y);
	}

	public float drawString(String str, float x, float y, int halign, int valign) {
		FontMetrics fm = graph.getFontMetrics();
		float w = fm.stringWidth(str);
		float h = fm.getAscent() - fm.getDescent();
		float tx = x - align(w, halign);
		float ty = y + h - align(h, valign);
		graph.drawString(str, tx, ty);
		return y + fm.getHeight();
	}

	public static int ptToPixels(float pt) {
		return Math.round(96f * pt / 72f);
	}
	
	public static float align(float span, int align) {
		return span * (float)align / 2f;
	}
	
}

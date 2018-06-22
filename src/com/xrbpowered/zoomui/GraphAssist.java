package com.xrbpowered.zoomui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;

public class GraphAssist {
	
	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;
	
	private static final Stroke defaultStroke = new BasicStroke(1f);
	
	public final Graphics2D graph;
	
	private LinkedList<AffineTransform> txStack = new LinkedList<>();
	private LinkedList<Rectangle> clipStack = new LinkedList<>();
	
	public GraphAssist(Graphics2D graph) {
		this.graph = graph;
	}
	
	public AffineTransform getTx() {
		return txStack.getFirst();
	}
	
	public void pushTx() {
		txStack.addFirst(graph.getTransform());
	}
	
	public void popTx() {
		graph.setTransform(txStack.removeFirst());
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
	
	public boolean pushClip(float x, float y, float w, float h) {
		Rectangle clip = graph.getClipBounds();
		Rectangle r = new Rectangle((int)x, (int)y, (int)w, (int)h);
		if(r.intersects(clip)) {
			clipStack.addFirst(clip);
			graph.setClip(r.intersection(clip));
			return true;
		}
		else
			return false;
	}
	
	public void popClip() {
		graph.setClip(clipStack.removeFirst());
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
		drawRect(x, 0, x, e.getHeight());
	}
	
	public void vborder(UIElement e, int halign, Color c) {
		setColor(c);
		vborder(e, halign);
	}

	public void hborder(UIElement e, int valign) {
		float y = align(e.getHeight(), valign);
		drawRect(0, y, e.getWidth(), y);
	}
	
	public void hborder(UIElement e, int valign, Color c) {
		setColor(c);
		hborder(e, valign);
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

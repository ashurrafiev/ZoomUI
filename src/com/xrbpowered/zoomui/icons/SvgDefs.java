package com.xrbpowered.zoomui.icons;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SvgDefs {

	private static class GradientStop {
		public float offs;
		public Color color;
		public GradientStop(float offs, Color color) {
			this.offs = offs;
			this.color = color;
		}
	}
	
	public HashMap<String, Object> defs = new HashMap<>();
	
	public static double getAttrValue(Element e, String name, double scale, double def) {
		String s = e.getAttribute(name);
		return s.isEmpty() ? def : Double.parseDouble(s) * scale;
	}
	
	public static String getAttrValue(Element e, String name, String def) {
		String s = e.getAttribute(name);
		return s.isEmpty() ? def : s;
	}
	
	public static <T> T getAttrValue(Element e, String name, String[] keys, T[] vals, T def) {
		String s = e.getAttribute(name);
		if(s.isEmpty())
			return def;
		for(int i=0; i<keys.length; i++) {
			if(s.equals(keys[i]))
				return vals[i];
		}
		return def;
	}
	
	private ArrayList<GradientStop> readStops(Element e) {
		ArrayList<GradientStop> stops = new ArrayList<>();
		Node cn = e.getFirstChild();
		while(cn!=null) {
			if(cn.getNodeType()==Node.ELEMENT_NODE) {
				Element ce = (Element) cn;
				if(ce.getNodeName().equals("stop")) {
					float offs = (float)getAttrValue(ce, "offset", 1.0, 0.0);
					String attr = ce.getAttribute("style");
					if(!attr.isEmpty()) {
						Color color = null;
						double opacity = 1.0;
						String[] vals = attr.split(";");
						for(String val : vals) {
							String[] kv = val.split(":", 2);
							kv[0] = kv[0].trim();
							kv[1] = kv[1].trim();
							if(kv[0].equals("stop-color")) {
								color = SvgStyle.parseColor(kv[1]);
							}
							else if(kv[0].equals("stop-opacity")) {
								opacity = Double.parseDouble(kv[1]);
							}
						}
						if(color!=null) {
							color = new Color(
									color.getRed(),
									color.getGreen(),
									color.getBlue(),
									(int)Math.round(opacity*255.0)
								);
							stops.add(new GradientStop(offs, color));
						}
					}
				}
			}
			cn = cn.getNextSibling();
		}
		return stops;
	}
	
	public void addDefs(Element g, double scale) {
		Node n = g.getFirstChild();
		while(n!=null) {
			if(n.getNodeType()==Node.ELEMENT_NODE) {
				Element e = (Element) n;
				String id = e.getAttribute("id");
				Object obj = null;
				if(e.getNodeName().equals("linearGradient")) {
					Point2D p1 = new Point2D.Double(0, 0);
					Point2D p2 = new Point2D.Double(0, 0);
					CycleMethod spread = CycleMethod.NO_CYCLE;

					float[] fractions = null;
					Color[] colors = null;

					Object xref = defs.get(e.getAttribute("xlink:href"));
					if(xref!=null) {
						if(xref instanceof LinearGradientPaint) {
							LinearGradientPaint grad = (LinearGradientPaint) xref;
							p1 = (Point2D) grad.getStartPoint().clone();
							p2 = (Point2D) grad.getEndPoint().clone();
							spread = grad.getCycleMethod();
							fractions = grad.getFractions();
							colors = grad.getColors();
						}
					}
					
					p1.setLocation(
							getAttrValue(e, "x1", scale, p1.getX()),
							getAttrValue(e, "y1", scale, p1.getY())
						);
					p2.setLocation(
							getAttrValue(e, "x2", scale, p2.getX()),
							getAttrValue(e, "y2", scale, p2.getY())
						);
					spread = getAttrValue(e, "spreadMethod", new String[] {"pad", "reflect", "repeat"}, CycleMethod.values(), spread); 
					
					ArrayList<GradientStop> stops = readStops(e);
					if(!stops.isEmpty()) {
						int num = stops.size();
						fractions = new float[num];
						colors = new Color[num];
						for(int i=0; i<num; i++) {
							GradientStop stop = stops.get(i);
							fractions[i] = stop.offs;
							colors[i] = stop.color;
						}
					}
					
					AffineTransform tx = SvgFile.getTransform(e.getAttribute("gradientTransform"), scale);
					tx.transform(p1, p1);
					tx.transform(p2, p2);
					if(p1.equals(p2)) {
						p2.setLocation(p2.getX(), p2.getY()+0.001);
					}
					
					obj = new LinearGradientPaint((float)p1.getX(), (float)p1.getY(), (float)p2.getX(), (float)p2.getY(), fractions, colors, spread);
				}
				else if(e.getNodeName().equals("radialGradient")) {
					Point2D pc = new Point2D.Double(0, 0);
					double radius = 0.0;
					CycleMethod spread = CycleMethod.NO_CYCLE;

					float[] fractions = null;
					Color[] colors = null;
					
					Object xref = defs.get(e.getAttribute("xlink:href"));
					if(xref!=null) {
						if(xref instanceof LinearGradientPaint) {
							LinearGradientPaint grad = (LinearGradientPaint) xref;
							spread = grad.getCycleMethod();
							fractions = grad.getFractions();
							colors = grad.getColors();
						}
						else if(xref instanceof RadialGradientPaint) {
							RadialGradientPaint grad = (RadialGradientPaint) xref;
							pc = (Point2D) grad.getCenterPoint().clone();
							radius = grad.getRadius();
							spread = grad.getCycleMethod();
							fractions = grad.getFractions();
							colors = grad.getColors();
						}
					}
					
					pc.setLocation(
							getAttrValue(e, "cx", scale, pc.getX()),
							getAttrValue(e, "cy", scale, pc.getY())
						);
					radius = getAttrValue(e, "r", scale, radius);
					spread = getAttrValue(e, "spreadMethod", new String[] {"pad", "reflect", "repeat"}, CycleMethod.values(), spread); 
					
					ArrayList<GradientStop> stops = readStops(e);
					if(!stops.isEmpty()) {
						int num = stops.size();
						fractions = new float[num];
						colors = new Color[num];
						for(int i=0; i<num; i++) {
							GradientStop stop = stops.get(i);
							fractions[i] = stop.offs;
							colors[i] = stop.color;
						}
					}
					
					AffineTransform tx = SvgFile.getTransform(e.getAttribute("gradientTransform"), scale);
					tx.transform(pc, pc);
					radius *= tx.getScaleX();
					
					obj = new RadialGradientPaint((float)pc.getX(), (float)pc.getY(), (float)radius, fractions, colors, spread);
				}
				
				if(obj!=null && !id.isEmpty()) {
					defs.put("#"+id, obj);
				}
			}
			
			n = n.getNextSibling();
		}
	}
}

package com.xrbpowered.zoomui.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

import org.w3c.dom.Element;

public class SvgStyle {

	public Paint fill = Color.BLACK;
	public Color strokeColor = null;
	public double strokeWidth = 1.0;
	
	public SvgStyle() {
	}
	
	public SvgStyle(SvgStyle parent) {
		if(parent!=null) {
			this.fill = parent.fill;
			this.strokeColor = parent.strokeColor;
			this.strokeWidth = parent.strokeWidth;
		}
	}
	
	public boolean hasFill() {
		return fill!=null;
	}
	
	public void setFillStyle(Graphics2D g2) {
		g2.setPaint(fill);
	}
	
	public boolean hasStroke() {
		return strokeColor!=null;
	}
	
	public void setStrokeStyle(Graphics2D g2, double scale) {
		g2.setStroke(new BasicStroke((float)(strokeWidth*scale)));
		g2.setColor(strokeColor);
	}

	public static Paint parseFill(String v, SvgDefs defs) {
		if(v.startsWith("url")) {
			String id = v.substring(4, v.length()-1);
			Object obj = defs.defs.get(id);
			if(obj!=null && obj instanceof Paint)
				return (Paint) obj;
			else
				return null;
		}
		else
			return parseColor(v);
	}

	public static Color parseColor(String v) {
		if(v.startsWith("#")) {
			try {
				int c = Integer.parseInt(v.substring(1), 16);
				return new Color(c);
			}
			catch(NumberFormatException e) {
				return null;
			}
		}
		else
			return null;
	}
	
	public static SvgStyle forElement(SvgStyle parent, SvgDefs defs, Element e) {
		String attr = e.getAttribute("style");
		if(!attr.isEmpty()) {
			SvgStyle style = new SvgStyle(parent);
			String[] vals = attr.split(";");
			for(String val : vals) {
				String[] kv = val.split(":", 2);
				kv[0] = kv[0].trim();
				kv[1] = kv[1].trim();
				if(kv[0].equals("fill")) {
					style.fill = parseFill(kv[1], defs);
				}
				else if(kv[0].equals("stroke")) {
					style.strokeColor = parseColor(kv[1]);
				}
				else if(kv[0].equals("stroke-width")) {
					if(kv[1].endsWith("px"))
						kv[1] = kv[1].substring(0, kv[1].length()-2);
					style.strokeWidth = Double.parseDouble(kv[1]);
				}
			}
			return style;
		}
		else
			return parent;
	}
	
}

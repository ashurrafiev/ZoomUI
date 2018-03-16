package com.xrbpowered.zoomui.icons;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

public class IconPalette {

	public final Color[][] colors;
	
	public IconPalette(Color[][] colors) {
		this.colors = colors;
	}
	
	public Paint getBgPaint(int style, float y1, float y2) {
		return new GradientPaint(0, y1, colors[style][0], 0, y2, colors[style][1]);
	}

	public Paint getFgPaint(int style, float y1, float y2) {
		return new GradientPaint(0, y1, colors[style][2], 0, y2, colors[style][3]);
	}

}

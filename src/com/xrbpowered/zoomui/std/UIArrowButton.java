package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIArrowButtonBase;

public class UIArrowButton extends UIArrowButtonBase {

	public static int arrowSpan = 4;
	
	public static Color colorHover = new Color(0xcccccc);
	public static Color colorArrow = UIButton.colorText;
	public static Color colorArrowDisabled = UIButton.colorBorder;

	public UIArrowButton(UIContainer parent, boolean vertical, int delta) {
		super(parent, vertical, delta);
	}

	@Override
	public void paint(GraphAssist g) {
		if(down || hover)
			g.fill(this, down ? UIButton.colorDown : colorHover);
		g.setColor(isEnabled() ? colorArrow : colorArrowDisabled);
		drawArrow(g);
	}
	
	@Override
	protected void drawUpArrow(GraphAssist g) {
		drawUpArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
	}

	@Override
	protected void drawDownArrow(GraphAssist g) {
		drawDownArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
	}

	@Override
	protected void drawLeftArrow(GraphAssist g) {
		drawLeftArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
	}

	@Override
	protected void drawRightArrow(GraphAssist g) {
		drawRightArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
	}

	public static void drawUpArrow(GraphAssist g, int x, int y) {
		drawUpArrow(g, x, y, arrowSpan);
	}
	
	public static void drawDownArrow(GraphAssist g, int x, int y) {
		drawDownArrow(g, x, y, arrowSpan);
	}
	
	public static void drawLeftArrow(GraphAssist g, int x, int y) {
		drawLeftArrow(g, x, y, arrowSpan);
	}
	
	public static void drawRightArrow(GraphAssist g, int x, int y) {
		drawRightArrow(g, x, y, arrowSpan);
	}

	public static void drawUpArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span, x, x+span}, new int[] {y+span/2, y-span/2, y+span/2}, 3);
	}
	
	public static void drawDownArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span, x, x+span}, new int[] {y-span/2, y+span/2, y-span/2}, 3);
	}

	public static void drawLeftArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x+span/2, x-span/2, x+span/2}, new int[] {y-span, y, y+span}, 3);
	}
	
	public static void drawRightArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span/2, x+span/2, x-span/2}, new int[] {y-span, y, y+span}, 3);
	}

}

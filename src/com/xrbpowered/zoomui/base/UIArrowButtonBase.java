package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;

public abstract class UIArrowButtonBase extends UIButtonBase {

	public boolean vertical;
	public int delta;
	
	public UIArrowButtonBase(UIContainer parent, boolean vertical, int delta) {
		super(parent);
		this.vertical = vertical;
		this.delta = delta;
	}

	@Override
	public void paint(GraphAssist g) {
		drawArrow(g);
	}
	
	public void drawArrow(GraphAssist g) {
		if(vertical)
			if(delta<0)
				drawUpArrow(g);
			else
				drawDownArrow(g);
		else
			if(delta<0)
				drawLeftArrow(g);
			else
				drawRightArrow(g);
	}
	
	protected abstract void drawUpArrow(GraphAssist g);
	protected abstract void drawDownArrow(GraphAssist g);
	protected abstract void drawLeftArrow(GraphAssist g);
	protected abstract void drawRightArrow(GraphAssist g);
	
}

package com.xrbpowered.zoomui;

public abstract class UIHoverElement extends UIElement {

	public boolean hover = false;
	
	public UIHoverElement(UIContainer parent) {
		super(parent);
	}

	@Override
	public void onMouseIn() {
		hover = true;
		repaint();
	}
	
	@Override
	public void onMouseOut() {
		hover = false;
		repaint();
	}
	
}

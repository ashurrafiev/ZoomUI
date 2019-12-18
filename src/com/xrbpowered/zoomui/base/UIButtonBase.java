package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIButtonBase extends UIHoverElement {

	public boolean down = false;
	private boolean enabled = true;
	
	public UIButtonBase(UIContainer parent) {
		super(parent);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if(!enabled) {
			hover = false;
			down = false;
		}
	}
	
	public UIButtonBase disable() {
		setEnabled(false);
		return this;
	}
	
	public void onAction() {
	}
	
	@Override
	public void onMouseIn() {
		if(isEnabled())
			super.onMouseIn();
	}
	
	@Override
	public void onMouseReleased() {
		down = false;
		repaint();
	}
	
	@Override
	public boolean onMouseDown(float x, float y, Button button, int mods) {
		if(button==Button.left) {
			if(isEnabled()) {
				down = true;
				repaint();
			}
			return true;
		}
		else
			return false;
	}
	
	@Override
	public boolean onMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		if(initiator!=this)
			return false;
		if(button==Button.left) {
			down = false;
			if(isEnabled())
				onAction();
			repaint();
			return true;
		}
		else
			return false;
	}

}

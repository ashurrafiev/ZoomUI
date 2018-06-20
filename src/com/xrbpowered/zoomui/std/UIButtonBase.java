package com.xrbpowered.zoomui.std;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIHoverElement;

public abstract class UIButtonBase extends UIHoverElement {

	public boolean down = false;
	private boolean disabled = false;
	
	public UIButtonBase(UIContainer parent) {
		super(parent);
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public boolean isEnabled() {
		return !disabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.disabled = !enabled;
	}
	
	public UIButtonBase enable() {
		disabled = false;
		return this;
	}

	public UIButtonBase disable() {
		disabled = true;
		hover = false;
		down = false;
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
			if(isDisabled())
				return true;
			down = true;
			repaint();
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
			if(!isDisabled())
				onAction();
			repaint();
			return true;
		}
		else
			return false;
	}

}

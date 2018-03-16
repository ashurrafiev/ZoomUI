package com.xrbpowered.zoomui.std;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIButtonBase extends UIElement {

	protected boolean hover = false;
	protected boolean down = false;
	private boolean disabled = false;
	
	public UIButtonBase(UIContainer parent) {
		super(parent);
	}
	
	public boolean isDisabled() {
		return disabled;
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
	protected void onMouseIn() {
		if(isDisabled())
			return;
		hover = true;
		requestRepaint();
	}
	
	@Override
	protected void onMouseOut() {
		hover = false;
		requestRepaint();
	}
	
	@Override
	protected void onMouseReleased() {
		down = false;
		requestRepaint();
	}
	
	@Override
	protected boolean onMouseDown(float x, float y, int buttons) {
		if(buttons==mouseLeftMask) {
			if(isDisabled())
				return true;
			down = true;
			requestRepaint();
			return true;
		}
		else
			return false;
	}
	
	@Override
	protected boolean onMouseUp(float x, float y, int buttons, UIElement initiator) {
		if(initiator!=this)
			return false;
		if(buttons==mouseLeftMask) {
			down = false;
			if(!isDisabled())
				onAction();
			requestRepaint();
			return true;
		}
		else
			return false;
	}

}

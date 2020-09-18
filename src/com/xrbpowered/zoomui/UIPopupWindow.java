package com.xrbpowered.zoomui;

public abstract class UIPopupWindow extends UIWindow {

	public UIPopupWindow(UIWindowFactory factory) {
		super(factory);
	}

	@Override
	public final void show() {
		throw new UnsupportedOperationException();
	}
	
	public abstract void show(UIWindow invoker, float x, float y);
	
}

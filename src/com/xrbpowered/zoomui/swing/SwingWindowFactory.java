package com.xrbpowered.zoomui.swing;

import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIWindowFactory;

public class SwingWindowFactory extends UIWindowFactory {

	public SwingWindowFactory() {
	}

	public SwingWindowFactory(float baseScale) {
		this.setBaseScale(baseScale);
	}

	@Override
	public UIWindow create(String title, int w, int h, boolean canResize) {
		return new SwingFrame(this, title, w, h, canResize, false);
	}

	@Override
	public <A> UIModalWindow<A> createModal(String title, int w, int h, boolean canResize, ResultHandler<A> onResult) {
		return new SwingModalDialog<>(this, title, w, h, canResize, onResult);
	}

	@Override
	public UIWindow createUndecorated(int w, int h) {
		return new SwingFrame(this, null, w, h, false, true);
	}

}

package com.xrbpowered.zoomui.swing;

import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIWindowFactory;

public class SwingWindowFactory extends UIWindowFactory {

	@Override
	public SwingFrame create(String title, int w, int h, boolean canResize) {
		SwingFrame frame = new SwingFrame(this, title, w, h, canResize, false);
		frame.exitOnClose(false);
		return frame;
	}

	@Override
	public <A> SwingModalDialog<A> createModal(String title, int w, int h, boolean canResize, ResultHandler<A> onResult) {
		SwingModalDialog<A> dlg = new SwingModalDialog<>(this, title, w, h, canResize);
		dlg.onResult = onResult;
		return dlg;
	}

	@Override
	public UIWindow createUndecorated(int w, int h) {
		return new SwingFrame(this, null, w, h, false, true).exitOnClose(false);
	}

	public SwingFrame createFrame(String title, int w, int h, boolean canResize) {
		return new SwingFrame(this, title, w, h, canResize, false);
	}

	public SwingFrame createFrame(String title, int w, int h) {
		return new SwingFrame(this, title, w, h, true, false);
	}

	public SwingFrame createFullscreen() {
		return new SwingFrame(this, null, 1024, 600, false, true).maximize();
	}

	public static SwingWindowFactory use() {
		if(!(UIWindowFactory.instance instanceof SwingWindowFactory))
			UIWindowFactory.instance = new SwingWindowFactory();
		return (SwingWindowFactory) UIWindowFactory.instance;
	}

	public static SwingWindowFactory use(float baseScale) {
		SwingWindowFactory factory = use();
		factory.setBaseScale(baseScale);
		return factory;
	}
	
}

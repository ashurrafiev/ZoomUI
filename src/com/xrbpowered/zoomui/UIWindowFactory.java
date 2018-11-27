package com.xrbpowered.zoomui;

import java.awt.Toolkit;

import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public abstract class UIWindowFactory {

	public static UIWindowFactory instance = new SwingWindowFactory();
	
	private float baseScale = getSystemScale();
	
	public float getBaseScale() {
		return baseScale;
	}
	
	public void setBaseScale(float scale) {
		baseScale = (scale > 0f) ? scale : getSystemScale();
	}
	
	public static float getSystemScale() {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 96f;
	}
	
	public abstract UIWindow create(String title, int w, int h, boolean canResize);
	public abstract <A> UIModalWindow<A> createModal(String title, int w, int h, boolean canResize, ResultHandler<A> onResult);
	public abstract UIWindow createUndecorated(int w, int h);
	
}

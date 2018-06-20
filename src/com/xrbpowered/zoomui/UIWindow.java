package com.xrbpowered.zoomui;

import java.awt.Cursor;
import java.awt.Toolkit;

public abstract class UIWindow {

	protected BaseContainer createContainer() {
		return new BaseContainer(this, getBaseScale());
	}
	
	protected final BaseContainer container = createContainer();
	
	public BaseContainer getContainer() {
		return this.container;
	}

	public abstract int getClientWidth();
	public abstract int getClientHeight();
	public abstract void setClientSize(int width, int height);

	public abstract void center();
	
	public void notifyResized() {
		getContainer().invalidateLayout();
	}
	
	public abstract void show();
	public abstract void repaint();
	
	public abstract void setCursor(Cursor cursor);
	
	public boolean onClosing() {
		return true;
	}
	
	public void onClose() {
	}
	
	public boolean requestClosing() {
		if(onClosing()) {
			close();
			return true;
		}
		else
			return false;
	}
	
	public void close() {
		onClose();
	}
	
	private static float baseScale = getSystemScale();
	
	public static float getBaseScale() {
		return baseScale;
	}
	
	public static void setBaseScale(float scale) {
		baseScale = (scale > 0f) ? scale : getSystemScale();
	}
	
	public static float getSystemScale() {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 96f;
	}
	
}

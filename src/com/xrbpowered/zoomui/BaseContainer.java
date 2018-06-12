package com.xrbpowered.zoomui;

import java.awt.RenderingHints;
import java.awt.Toolkit;

public class BaseContainer extends UIContainer {

	private float baseScale = getAutoScale();
	
	protected BaseContainer(BasePanel basePanel) {
		super(basePanel);
	}
	
	@Override
	protected void addChild(UIElement c) {
		super.addChild(c);
		resetFocus();
	}

	public void resetFocus() {
		KeyInputHandler e = null;
		for(UIElement c : children)
			if(c instanceof KeyInputHandler)
				e = (KeyInputHandler) c;
		getBasePanel().setFocus(e);
	}
	
	public void setBaseScale(float baseScale) {
		this.baseScale = (baseScale>0f) ? baseScale : getAutoScale();
		invalidateLayout();
	}
	
	public static float getAutoScale() {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 96f; // 96 for px, 72 for pt
	}
	
	@Override
	public float getPixelScale() {
		return 1f / baseScale;
	}
	
	@Override
	protected float parentToLocalX(float x) {
		return x / baseScale;
	}
	
	@Override
	protected float parentToLocalY(float y) {
		return y / baseScale;
	}
	
	@Override
	public void layout() {
		for(UIElement c : children) {
			c.setLocation(0, 0);
			c.setSize(getWidth(), getHeight());
			c.layout();
		}
	}
	
	@Override
	public float getWidth() {
		return getBasePanel().getWidth() / baseScale;
	}
	
	@Override
	public float getHeight() {
		return getBasePanel().getHeight() / baseScale;
	}
	
	@Override
	public float getX() {
		return 0;
	}
	
	@Override
	public float getY() {
		return 0;
	}
	
	@Override
	public boolean isInside(float x, float y) {
		return true;
	}
	
	@Override
	public void paint(GraphAssist g) {
		g.graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		g.pushTx();
		g.scale(baseScale);
		super.paintChildren(g);
		g.popTx();
	}
	
}

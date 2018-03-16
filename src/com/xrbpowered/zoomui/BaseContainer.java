package com.xrbpowered.zoomui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

public class BaseContainer extends UIContainer {

	private final BasePanel basePanel;
	private float baseScale = getAutoScale();
	
	protected BaseContainer(BasePanel basePanel) {
		super(null);
		this.basePanel = basePanel;
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
		basePanel.setFocus(e);
	}
	
	@Override
	protected BasePanel getBasePanel() {
		return basePanel;
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
	protected void layout() {
		for(UIElement c : children) {
			c.setLocation(0, 0);
			c.setSize(getWidth(), getHeight());
			c.layout();
		}
	}
	
	@Override
	public float getWidth() {
		return basePanel.getWidth() / baseScale;
	}
	
	@Override
	public float getHeight() {
		return basePanel.getHeight() / baseScale;
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
	public void paint(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		// g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g2);
	}
	
	@Override
	protected void paintChildren(Graphics2D g2) {
		AffineTransform tx = g2.getTransform();
		g2.scale(baseScale, baseScale);
		super.paintChildren(g2);
		g2.setTransform(tx);
	}
	
}

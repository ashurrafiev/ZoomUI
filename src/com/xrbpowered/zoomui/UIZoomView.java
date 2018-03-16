package com.xrbpowered.zoomui;

import java.awt.Graphics2D;

public class UIZoomView extends UIPanView {

	protected float scale = 1f; // FIXME integer zoom steps
	private float minScale = 0.1f;
	private float maxScale = 3.0f;

	
	public UIZoomView(UIContainer parent) {
		super(parent);
	}
	
	public void setScaleRange(float min, float max) {
		if(min>1f) min = 1f;
		if(max<1f) max = 1f;
		this.minScale = min;
		this.maxScale = max;
		if(scale<minScale)
			scale = minScale;
		if(scale>maxScale)
			scale = maxScale;
	}

	public void resetScale() {
		scale = 1f;
	}
	
	@Override
	protected float parentToLocalX(float x) {
		return super.parentToLocalX(x)/scale;
	}

	@Override
	protected float parentToLocalY(float y) {
		return super.parentToLocalY(y)/scale;
	}

	@Override
	protected void applyTransform(Graphics2D g2) {
		super.applyTransform(g2);
		g2.scale(scale, scale);
	}

	@Override
	protected boolean onMouseScroll(float x, float y, float delta) {
		float ds = 1.0f+delta*0.2f;
		scale *= ds;
		if(scale<minScale) {
			ds *= minScale / scale;
			scale = minScale;
		}
		if(scale>maxScale) {
			ds *= maxScale / scale;
			scale = maxScale;
		}
		requestRepaint();
		return true;
	}
}

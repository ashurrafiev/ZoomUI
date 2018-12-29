package com.xrbpowered.zoomui;

public class UIZoomView extends UIPanView {

	protected float scale = 1f; // TODO integer zoom steps
	private float minScale = 0.1f;
	private float maxScale = 3.0f;

	
	public UIZoomView(UIContainer parent) {
		super(parent);
	}
	
	private void checkScaleRange() {
		if(scale<minScale)
			scale = minScale;
		if(scale>maxScale)
			scale = maxScale;
	}
	
	public void setScaleRange(float min, float max) {
		if(min>1f) min = 1f;
		if(max<1f) max = 1f;
		this.minScale = min;
		this.maxScale = max;
		checkScaleRange();
	}
	
	// FIXME scaled setPanRange? getPanRangeForClient?

	public void resetScale() {
		scale = 1f;
		checkScaleRange();
	}
	
	public void setScale(float s) {
		scale = s;
		checkScaleRange();
	}

	public void scale(float ds) {
		scale(ds, getWidth()/2f, getHeight()/2f);
	}

	public void scale(float ds, float x, float y) {
		float s = scale;
		scale *= ds;
		checkScaleRange();
		ds = scale / s;
		super.setPan(
				(ds-1f)*x+ds*panX,
				(ds-1f)*y+ds*panY
			);
	}
	
	public float getScale() {
		return scale;
	}
	
	public float getMinScale() {
		return minScale;
	}
	
	public float getMaxScale() {
		return maxScale;
	}
	
	@Override
	public void setPan(float x, float y) {
		super.setPan(x*scale, y*scale);
	}
	
	@Override
	public void pan(float dx, float dy) {
		super.pan(dx*scale, dy*scale);
	}
	
	@Override
	public float getPixelScale() {
		return super.getPixelScale()/scale;
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
	protected void applyTransform(GraphAssist g) {
		super.applyTransform(g);
		g.scale(scale);
	}

	@Override
	public boolean onMouseScroll(float x, float y, float delta, int mods) {
		if(mods==modCtrlMask) {
			scale(1.0f+delta*0.2f, x, y);
			repaint();
			return true;
		}
		else
			return false;
	}
}

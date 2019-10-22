package com.xrbpowered.zoomui.base;

import java.awt.RenderingHints;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public class UIFitScaleContainer extends UIContainer {

	protected float scale = 1f;
	protected float targetWidth, targetHeight;

	public UIFitScaleContainer(UIContainer parent, float targetWidth, float targetHeight) {
		super(parent);
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
	}
	
	@Override
	public void layout() {
		float scalew = getWidth() / targetWidth;
		float scaleh = getHeight() / targetHeight;
		scale = Math.min(scalew, scaleh);
		
		float w = getWidth() / scale;
		float h = getHeight() / scale;
		for(UIElement c : children) {
			c.setLocation(0, 0);
			c.setSize(w, h);
			c.layout();
		}
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
	protected float localToParentX(float x) {
		return super.localToParentX(x*scale);
	}

	@Override
	protected float localToParentY(float y) {
		return super.localToParentY(y*scale);
	}

	@Override
	protected void paintChildren(GraphAssist g) {
		g.graph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if(g.pushClip(0, 0, getWidth(), getHeight())) {
			g.pushTx();
			g.scale(scale);
			super.paintChildren(g);
			g.popTx();
			g.popClip();
		}
	}
}
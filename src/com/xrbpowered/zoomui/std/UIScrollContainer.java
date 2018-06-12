package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIPanView;

public abstract class UIScrollContainer extends UIContainer {

	public static float scrollStep = 3f*GraphAssist.ptToPixels(9f);
	
	private UIPanView view;
	
	public UIScrollContainer(UIContainer parent) {
		super(parent);
		view = new UIPanView(this);
	}
	
	public UIPanView getView() {
		return view;
	}
	
	@Override
	public
	final void layout() {
		view.setLocation(0, 0);
		view.setSize(getWidth(), getHeight());
		int pan = (int)(layoutView()-getHeight());
		if(pan<0) pan = 0;
		view.setPanRange(0, pan);
	}
	
	protected abstract float layoutView();
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		// TODO scroll bars
		if(view.getMaxPanY()>0) {
			g.fillRect(getWidth()-4f, 0, 4, getHeight(), new Color(0xdddddd));
			float s = getHeight()/(view.getMaxPanY()+getHeight());
			float top = view.getPanY() * s;
			float h = getHeight() * s;
			g.fillRect(getWidth()-4f, top, 4, h, new Color(0x777777));
		}
	}
	
	@Override
	protected boolean onMouseScroll(float x, float y, float delta, int modifiers) {
		if(modifiers==modNone) {
			view.pan(0, -delta*scrollStep);
			requestRepaint();
			return true;
		}
		else
			return false;
	}

}

package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIPanView;

public abstract class UIScrollContainer extends UIContainer {

	private UIPanView view;
	
	public UIScrollContainer(UIContainer parent) {
		super(parent);
		view = new UIPanView(this);
	}
	
	public UIPanView getView() {
		return view;
	}
	
	@Override
	protected void layout() {
		view.setLocation(0, 0);
		view.setSize(getWidth(), getHeight());
		int pan = (int)(layoutView()-getHeight());
		if(pan<0) pan = 0;
		view.setPanRange(0, pan);
	}
	
	protected abstract float layoutView();
	
	@Override
	protected void paintChildren(Graphics2D g2) {
		super.paintChildren(g2);
		// TODO scroll bars
		if(view.getMaxPanY()>0) {
			g2.setColor(new Color(0xdddddd));
			g2.fillRect((int)(getWidth()-4f), 0, 4, (int)getHeight());
			float s = getHeight()/(view.getMaxPanY()+getHeight());
			float top = view.getPanY() * s;
			float h = getHeight() * s;
			g2.setColor(new Color(0x777777));
			g2.fillRect((int)(getWidth()-4f), (int)top, 4, (int)h);
		}
	}
	
	@Override
	protected boolean onMouseScroll(float x, float y, float delta) {
		view.pan(0, -delta*3f*StdPainter.instance.fontSize);
		requestRepaint();
		return true;
	}

}

package com.xrbpowered.zoomui.std;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIPanView;

public abstract class UIScrollContainer extends UIContainer {

	public static int scrollStep = GraphAssist.ptToPixels(9f);
	public static int wheelStep = 3*scrollStep;
	
	private UIPanView view;
	private UIScrollBar scroll;
	
	public UIScrollContainer(UIContainer parent) {
		super(parent);
		view = new UIPanView(this);
		scroll = new UIScrollBar(this, true) {
			@Override
			public void onChanged() {
				view.setPan(0, getValue());
			}
			@Override
			protected void paintSelf(GraphAssist g) {
				if(view.getMaxPanY()>0) {
					scroll.setThumbSpan(Math.round(getHeight()));
					scroll.setRange(0, view.getMaxPanY(), scrollStep);
					scroll.setValue(Math.round(view.getPanY()));
				}
				else
					scroll.setRange(0, 0, 0);
				super.paintSelf(g);
			}
		};
	}
	
	public UIPanView getView() {
		return view;
	}
	
	@Override
	public
	final void layout() {
		scroll.setSize(getHeight());
		scroll.setLocation(getWidth()-scroll.getWidth(), 0);
		scroll.layout();
		
		view.setLocation(0, 0);
		view.setSize(getWidth()-scroll.getWidth(), getHeight());
		view.setPanRangeForClient(0, layoutView());
	}
	
	protected abstract float layoutView();
	
	@Override
	public boolean onMouseScroll(float x, float y, float delta, int modifiers) {
		if(modifiers==modNone) {
			view.pan(0, -delta*wheelStep);
			repaint();
			return true;
		}
		else
			return false;
	}

}

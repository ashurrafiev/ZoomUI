package com.xrbpowered.zoomui;

import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class UIContainer extends UIElement {

	protected ArrayList<UIElement> children = new ArrayList<>();
	
	public UIContainer(UIContainer parent) {
		super(parent);
	}
	
	protected UIContainer(BasePanel basePanel) {
		super(null, basePanel);
	}
	
	protected void addChild(UIElement c) {
		children.add(c);
		invalidateLayout();
	}
	
	protected void removeChild(UIElement c) {
		if(children.remove(c))
			c.onRemove();
		invalidateLayout();
	}
	
	public void removeAllChildren() {
		for(UIElement c : children)
			c.onRemove();
		children.clear();
		invalidateLayout();
	}
	
	@Override
	public void layout() {
		for(UIElement c : children) {
			c.layout();
		}
	}
	
	protected void paintSelf(GraphAssist g) {
	}
	
	protected void paintChildren(GraphAssist g) {
		Rectangle clip = g.graph.getClipBounds();
		for(UIElement c : children) {
			if(c.isVisible(clip)) {
				g.pushTx();
				g.translate(c.getX(), c.getY());
				c.paint(g);
				g.popTx();
			}
		}
	}
	
	@Override
	public void paint(GraphAssist g) {
		paintSelf(g);
		paintChildren(g);
	}
	
	protected UIElement getElementUnderMouse(float x, float y) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).getElementUnderMouse(cx, cy);
			if(e!=null)
				return e;
		}
		return super.getElementUnderMouse(x, y);
	}
	
	@Override
	protected UIElement notifyMouseDown(float x, float y, int buttons) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseDown(cx, cy, buttons);
			if(e!=null)
				return e;
		}
		return super.notifyMouseDown(x, y, buttons);
	}
	
	@Override
	protected UIElement notifyMouseUp(float x, float y, int buttons, UIElement initiator) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseUp(cx, cy, buttons, initiator);
			if(e!=null)
				return e;
		}
		return super.notifyMouseUp(x, y, buttons, initiator);
	}
	
	@Override
	protected UIElement notifyMouseScroll(float x, float y, float delta, int modifiers) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseScroll(cx, cy, delta, modifiers);
			if(e!=null)
				return e;
		}
		return super.notifyMouseScroll(x, y, delta, modifiers);
	}
	
}

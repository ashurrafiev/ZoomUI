package com.xrbpowered.zoomui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
	
	protected void paintSelf(Graphics2D g2) {
	}
	
	protected void paintChildren(Graphics2D g2) {
		for(UIElement c : children) {
			if(c.isVisible()) {
				AffineTransform tx = g2.getTransform();
				g2.translate(c.getX(), c.getY());
				c.paint(g2);
				g2.setTransform(tx);
			}
		}
	}
	
	@Override
	public void paint(Graphics2D g2) {
		paintSelf(g2);
		paintChildren(g2);
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

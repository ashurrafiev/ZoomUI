package com.xrbpowered.zoomui;

import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class UIContainer extends UIElement {

	protected ArrayList<UIElement> children = new ArrayList<>();
	
	public UIContainer(UIContainer parent) {
		super(parent);
	}
	
	protected void addChild(UIElement c) {
		children.add(c);
		invalidateLayout();
	}
	
	public void removeChild(UIElement c) {
		if(children.remove(c))
			invalidateLayout();
	}
	
	public void removeAllChildren() {
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
	
	public UIElement getElementAt(float x, float y) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).getElementAt(cx, cy);
			if(e!=null)
				return e;
		}
		return super.getElementAt(x, y);
	}
	
	@Override
	public UIElement notifyMouseDown(float x, float y, Button button, int mods) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseDown(cx, cy, button, mods);
			if(e!=null)
				return e;
		}
		return super.notifyMouseDown(x, y, button, mods);
	}
	
	@Override
	public UIElement notifyMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseUp(cx, cy, button, mods, initiator);
			if(e!=null)
				return e;
		}
		return super.notifyMouseUp(x, y, button, mods, initiator);
	}
	
	@Override
	public UIElement notifyMouseScroll(float x, float y, float delta, int mods) {
		if(!isVisible())
			return null;
		float cx = parentToLocalX(x);
		float cy = parentToLocalY(y);
		for(int i=children.size()-1; i>=0; i--) {
			UIElement e = children.get(i).notifyMouseScroll(cx, cy, delta, mods);
			if(e!=null)
				return e;
		}
		return super.notifyMouseScroll(x, y, delta, mods);
	}
	
}

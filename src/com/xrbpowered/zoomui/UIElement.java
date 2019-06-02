package com.xrbpowered.zoomui;

import java.awt.Rectangle;

public abstract class UIElement {

	public enum Button {
		left, right, middle, unknown
	}
	public static final int modNone = 0;
	public static final int modCtrlMask = 1;
	public static final int modAltMask = 2;
	public static final int modShiftMask = 4;
	
	private final UIContainer parent;
	private final BaseContainer base;

	private boolean visible = true;
	private float x, y;
	private float width, height;
	
	public UIElement(UIContainer parent) {
		this.parent = parent;
		this.base = (parent!=null) ? parent.getBase() : null;
		if(parent!=null)
			parent.addChild(this);
	}
	
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public BaseContainer getBase() {
		return base;
	}
	
	protected float parentToLocalX(float x) {
		return x - this.x;
	}

	protected float parentToLocalY(float y) {
		return y - this.y;
	}
	
	public float baseToLocalX(float x) {
		return parentToLocalX(parent==null ? x : parent.baseToLocalX(x));
	}

	public float baseToLocalY(float y) {
		return parentToLocalY(parent==null ? y : parent.baseToLocalY(y));
	}

	public float getPixelScale() {
		if(parent!=null)
			return parent.getPixelScale();
		else
			return 1f;
	}
	
	public void repaint() {
		if(parent!=null)
			parent.repaint();
	}
	
	public void invalidateLayout() {
		getBase().invalidateLayout();
	}
	
	public void layout() {
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible(Rectangle clip) {
		return visible &&
			!(clip.x-x>getWidth() || clip.x-x+clip.width<0 ||
			clip.y-y>getHeight() || clip.y-y+clip.height<0);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public boolean isInside(float x, float y) {
		return isVisible() && x>=getX() && y>=getY() && x<=getX()+getWidth() && y<=getY()+getHeight();
	}
	
	public UIContainer getParent() {
		return parent;
	}
	
	public abstract void paint(GraphAssist g);
	
	public DragActor acceptDrag(float x, float y, Button button, int mods) {
		return null;
	}

	public UIElement getElementAt(float x, float y) {
		if(isInside(x, y))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseDown(float x, float y, Button button, int mods) {
		if(isInside(x, y) && onMouseDown(x, y, button, mods))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		if(isInside(x, y) && onMouseUp(x, y, button, mods, initiator))
			return this;
		else
			return null;
	}
	
	public UIElement notifyMouseScroll(float x, float y, float delta, int mods) {
		if(isInside(x, y) && onMouseScroll(x, y, delta, mods))
			return this;
		else
			return null;
	}

	public void onMouseIn() {
	}
	
	public void onMouseOut() {
	}
	
	public void onMouseReleased() {
	}
	
	public void onMouseMoved(float x, float y, int mods) {
	}
	
	public boolean onMouseDown(float x, float y, Button button, int mods) {
		return false;
	}
	
	public boolean onMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		return false;
	}
	
	public boolean onMouseScroll(float x, float y, float delta, int mods) {
		return false;
	}
	
}

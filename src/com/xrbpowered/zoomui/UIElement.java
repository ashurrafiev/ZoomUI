package com.xrbpowered.zoomui;

import java.awt.Graphics2D;

public abstract class UIElement {

	public static final int mouseLeftMask = 1;
	public static final int mouseRightMask = 2;
	public static final int mouseMiddleMask = 4;
	public static final int modNone = 0;
	public static final int modCtrlMask = 8;
	public static final int modAltMask = 16;
	public static final int modShiftMask = 32;
	
	private UIContainer parent;

	private boolean visible = true;
	private float x, y;
	private float width, height;
	
	public UIElement(UIContainer parent) {
		this.parent = parent;
		if(parent!=null)
			parent.addChild(this);
	}

	public void destroy() {
		getParent().removeChild(this);
	}
	
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	protected BasePanel getBasePanel() {
		if(parent!=null)
			return parent.getBasePanel();
		else
			return null;
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
	
	protected void requestRepaint() {
		getBasePanel().repaint();
	}
	
	public void invalidateLayout() {
		if(parent!=null)
			parent.invalidateLayout();
	}
	
	protected void layout() {
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
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
	
	public abstract void paint(Graphics2D g2);
	
	public DragActor acceptDrag(int x, int y, int buttons) {
		return null;
	}

	protected UIElement getElementUnderMouse(float x, float y) {
		if(isInside(x, y))
			return this;
		else
			return null;
	}
	
	protected UIElement notifyMouseDown(float x, float y, int buttons) {
		if(isInside(x, y) && onMouseDown(x, y, buttons))
			return this;
		else
			return null;
	}
	
	protected UIElement notifyMouseUp(float x, float y, int buttons, UIElement initiator) {
		if(isInside(x, y) && onMouseUp(x, y, buttons, initiator))
			return this;
		else
			return null;
	}
	
	protected UIElement notifyMouseScroll(float x, float y, float delta) {
		if(isInside(x, y) && onMouseScroll(x, y, delta))
			return this;
		else
			return null;
	}

	protected void onMouseIn() {
	}
	
	protected void onMouseOut() {
	}
	
	protected void onMouseReleased() {
	}
	
	protected boolean onMouseDown(float x, float y, int buttons) {
		return false;
	}
	
	protected boolean onMouseUp(float x, float y, int buttons, UIElement initiator) {
		return false;
	}
	
	protected boolean onMouseScroll(float x, float y, float delta) {
		return false;
	}
	
	protected void onRemove() {
	}
	
}

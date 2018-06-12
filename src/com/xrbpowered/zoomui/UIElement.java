package com.xrbpowered.zoomui;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

public abstract class UIElement {

	public static final int mouseLeftMask = 1;
	public static final int mouseRightMask = 2;
	public static final int mouseMiddleMask = 4;
	public static final int modNone = 0;
	public static final int modCtrlMask = 8;
	public static final int modAltMask = 16;
	public static final int modShiftMask = 32;
	
	private final UIContainer parent;
	private final BasePanel basePanel;

	private boolean visible = true;
	private float x, y;
	private float width, height;
	
	public UIElement(UIContainer parent, BasePanel basePanel) {
		this.parent = parent;
		this.basePanel = basePanel;
		if(parent!=null)
			parent.addChild(this);
	}
	
	protected UIElement(UIContainer parent) {
		this(parent, (parent!=null) ? parent.getBasePanel() : null);
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
	
	public BasePanel getBasePanel() {
		return basePanel;
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
	
	public Point2D getMousePosition() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(p, getBasePanel());
		return new Point2D.Float(baseToLocalX(p.x), baseToLocalY(p.y));
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
		getBasePanel().invalidateLayout();
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
	
	protected UIElement notifyMouseScroll(float x, float y, float delta, int modifiers) {
		if(isInside(x, y) && onMouseScroll(x, y, delta, modifiers))
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
	
	protected boolean onMouseScroll(float x, float y, float delta, int modifiers) {
		return false;
	}
	
	protected void onRemove() {
	}
	
}

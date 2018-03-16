package com.xrbpowered.zoomui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class UIPanView extends UIContainer {

	public static final int UNLIMITED = -1;
	public static final int DISABLED = 0;
	
	private DragActor panActor = new DragActor() {
		@Override
		public boolean notifyMouseDown(int x, int y, int buttons) {
			if(buttons==mouseRightMask) {
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(int dx, int dy) {
			pan(dx, dy);
			requestRepaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(int x, int y, int buttons, UIElement target) {
			return true;
		}
	};
	
	protected float panX = 0;
	protected float panY = 0;
	protected int maxPanX = -1;
	protected int maxPanY = -1;

	public UIPanView(UIContainer parent) {
		super(parent);
	}
	
	private void checkPanRange() {
		if(maxPanX!=DISABLED) {
			if(maxPanX>0) {
				if(panX<0) panX = 0f;
				if(panX>maxPanX) panX = maxPanX;
			}
		}
		else
			panX = 0;
		if(maxPanY!=DISABLED) {
			if(maxPanY>0) {
				if(panY<0) panY = 0f;
				if(panY>maxPanY) panY = maxPanY;
			}
		}
		else
			panY = 0;
	}
	
	public void pan(float dx, float dy) {
		float scale = getPixelScale();
		panX -= dx * scale;
		panY -= dy * scale;
		checkPanRange();
	}
	
	public void resetPan() {
		panX = 0f;
		panY = 0f;
	}
	
	public float getPanX() {
		return panX;
	}
	
	public float getPanY() {
		return panY;
	}
	
	public int getMaxPanX() {
		return maxPanX;
	}
	
	public int getMaxPanY() {
		return maxPanY;
	}
	
	public void setPanRange(int h, int v) {
		this.maxPanX = h;
		this.maxPanY = v;
		checkPanRange();
	}
	
	@Override
	protected float parentToLocalX(float x) {
		return super.parentToLocalX(x)+panX;
	}

	@Override
	protected float parentToLocalY(float y) {
		return super.parentToLocalY(y)+panY;
	}

	protected void applyTransform(Graphics2D g2) {
		g2.translate(-panX, -panY);
	}
	
	@Override
	protected void paintChildren(Graphics2D g2) {
		g2.setClip(0, 0, (int)getWidth(), (int)getHeight());
		AffineTransform tx = g2.getTransform();
		applyTransform(g2);
		super.paintChildren(g2);
		g2.setTransform(tx);
		g2.setClip(null);
	}
	
	@Override
	protected UIElement getElementUnderMouse(float x, float y) {
		if(isInside(x, y))
			return super.getElementUnderMouse(x, y);
		else
			return null;
	}
	
	@Override
	protected UIElement notifyMouseDown(float x, float y, int buttons) {
		if(isInside(x, y))
			return super.notifyMouseDown(x, y, buttons);
		else
			return null;
	}
	
	@Override
	protected UIElement notifyMouseUp(float x, float y, int buttons, UIElement initiator) {
		if(isInside(x, y))
			return super.notifyMouseUp(x, y, buttons, initiator);
		else
			return null;
	}

	@Override
	protected UIElement notifyMouseScroll(float x, float y, float delta) {
		if(isInside(x, y))
			return super.notifyMouseScroll(x, y, delta);
		else
			return null;
	}

	@Override
	public DragActor acceptDrag(int x, int y, int buttons) {
		if(panActor.notifyMouseDown(x, y, buttons))
			return panActor;
		else
			return null;
	}
	
	@Override
	protected boolean onMouseDown(float x, float y, int buttons) {
		if(buttons==mouseRightMask)
			return true;
		return false;
	}
	
}

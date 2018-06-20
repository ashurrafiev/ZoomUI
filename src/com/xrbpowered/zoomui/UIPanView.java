package com.xrbpowered.zoomui;

public class UIPanView extends UIContainer {

	public static final int UNLIMITED = -1;
	public static final int DISABLED = 0;
	
	private DragActor panActor = new DragActor() {
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.right) {
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			float pix = getPixelScale();
			pan(dx / pix, dy / pix);
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
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

	protected void applyTransform(GraphAssist g) {
		g.translate(-panX, -panY);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		if(g.pushClip(0, 0, getWidth(), getHeight())) {
			g.pushTx();
			applyTransform(g);
			super.paintChildren(g);
			g.popTx();
			g.popClip();
		}
	}
	
	@Override
	public UIElement getElementAt(float x, float y) {
		if(isInside(x, y))
			return super.getElementAt(x, y);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseDown(float x, float y, Button button, int mods) {
		if(isInside(x, y))
			return super.notifyMouseDown(x, y, button, mods);
		else
			return null;
	}
	
	@Override
	public UIElement notifyMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		if(isInside(x, y))
			return super.notifyMouseUp(x, y, button, mods, initiator);
		else
			return null;
	}

	@Override
	public UIElement notifyMouseScroll(float x, float y, float delta, int mods) {
		if(isInside(x, y))
			return super.notifyMouseScroll(x, y, delta, mods);
		else
			return null;
	}

	@Override
	public DragActor acceptDrag(float x, float y, Button button, int mods) {
		if(panActor.notifyMouseDown(x, y, button, mods))
			return panActor;
		else
			return null;
	}
	
	@Override
	public boolean onMouseDown(float x, float y, Button button, int mods) {
		if(button==Button.right)
			return true;
		return false;
	}
	
}

package com.xrbpowered.zoomui;

import java.awt.RenderingHints;

public class BaseContainer extends UIContainer implements KeyInputHandler {

	public static class ModalBaseContainer<A> extends BaseContainer {
		protected ModalBaseContainer(UIModalWindow<A> window, float scale) {
			super(window, scale);
		}
		@SuppressWarnings("unchecked")
		@Override
		public UIModalWindow<A> getWindow() {
			return (UIModalWindow<A>) super.getWindow();
		}
	}
	
	private float baseScale;
	private UIWindow window;
	
	protected BaseContainer(UIWindow window, float scale) {
		super(null);
		this.baseScale = scale;
		this.window = window;
	}
	
	@Override
	public BaseContainer getBase() {
		return this;
	}
	
	public UIWindow getWindow() {
		return window;
	}
	
	private UIElement uiUnderMouse = null;
	private KeyInputHandler uiFocused = null;
	
	private DragActor drag = null;
	private UIElement uiInitiator = null;
	private Button initiatorButton = Button.left;
	private int initiatorMods = 0;
	private int prevMouseX = 0;
	private int prevMouseY = 0;
	
	private boolean invalidLayout = true;

	public void invalidateLayout() {
		invalidLayout = true;
	}

	@Override
	public void onFocusGained() {
	}
	
	@Override
	public void onFocusLost() {
		resetFocus();
	}
	
	@Override
	public boolean onKeyPressed(char c, int code, int mods) {
		if(uiFocused!=null)
			return uiFocused.onKeyPressed(c, code, mods);
		else
			return false;
	}
	
	@Override
	public UIElement notifyMouseDown(float x, float y, Button button, int mods) {
		if(drag==null) {
			prevMouseX = getWindow().baseToScreenX(x);
			prevMouseY = getWindow().baseToScreenY(y);
			initiatorButton = button;
			initiatorMods = mods;
			UIElement ui = super.notifyMouseDown(x, y, button, mods);
			if(ui!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased();
			uiInitiator = ui;
			//if(uiFocused!=null && uiFocused!=uiInitiator) // FIXME better strategy for losing focus?
			//	resetFocus();
		}
		return this;
	}
	
	@Override
	public UIElement notifyMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
		if(drag!=null) {
			UIElement ui = getElementAt(x, y);
			if(drag.notifyMouseUp(x, y, button, mods, ui))
				drag = null;
		}
		else {
			if(super.notifyMouseUp(x, y, button, mods, uiInitiator)!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased(); // FIXME release for multi-button scenarios
		}
		return this;
	}
	
	@Override
	public void onMouseOut() {
		if(drag==null && uiUnderMouse!=null) {
			if(uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = null;
		}
	}
	
	private void updateMouseMove(float x, float y) {
		UIElement ui = getElementAt(x, y);
		if(ui!=uiUnderMouse) {
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = ui;
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseIn();
		}
	}
	
	@Override
	public void onMouseMoved(float x, float y, int mods) {
		if(drag==null) {
			updateMouseMove(x, y);
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseMoved(x, y, mods);
		}
	}
	
	public void onMouseDragged(float x, float y) {
		int sx = getWindow().baseToScreenX(x);
		int sy = getWindow().baseToScreenY(y);
		if(drag==null && uiInitiator!=null) {
			drag = uiInitiator.acceptDrag(getWindow().screenToBaseX(prevMouseX), getWindow().screenToBaseY(prevMouseY), initiatorButton, initiatorMods);
		}
		if(drag!=null) {
			if(!drag.notifyMouseMove(sx-prevMouseX, sy-prevMouseY))
				drag = null;
			prevMouseX = sx;
			prevMouseY = sy;
		}
		updateMouseMove(x, y);
	}
	
	public void resetFocus() {
		if(uiFocused!=null)
			uiFocused.onFocusLost();
		KeyInputHandler e = null;
		for(UIElement c : children)
			if(c instanceof KeyInputHandler)
				e = (KeyInputHandler) c;
		uiFocused = e;
	}

	public void setFocus(KeyInputHandler handler) {
		if(uiFocused!=null && uiFocused!=handler)
			resetFocus();
		uiFocused = handler;
		if(uiFocused!=null)
			uiFocused.onFocusGained();
	}
	
	public KeyInputHandler getFocus() {
		return uiFocused;
	}

	@Override
	protected void addChild(UIElement c) {
		super.addChild(c);
		resetFocus();
	}

	public float getBaseScale() {
		return baseScale;
	}
	
	public void setBaseScale(float scale) {
		this.baseScale = (scale>0f) ? scale :getWindow().getFactory().getBaseScale();
		invalidateLayout();
	}
	
	@Override
	public float getPixelScale() {
		return 1f / baseScale;
	}
	
	@Override
	protected float parentToLocalX(float x) {
		return x / baseScale;
	}
	
	@Override
	protected float parentToLocalY(float y) {
		return y / baseScale;
	}
	
	@Override
	public void layout() {
		for(UIElement c : children) {
			c.setLocation(0, 0);
			c.setSize(getWidth(), getHeight());
			c.layout();
		}
		invalidLayout = false;
	}
	
	@Override
	public float getWidth() {
		return getWindow().getClientWidth() / baseScale;
	}
	
	@Override
	public float getHeight() {
		return getWindow().getClientHeight() / baseScale;
	}
	
	@Override
	public float getX() {
		return 0;
	}
	
	@Override
	public float getY() {
		return 0;
	}
	
	@Override
	public boolean isInside(float x, float y) {
		return true;
	}
	
	@Override
	public void paint(GraphAssist g) {
		if(invalidLayout)
			layout();
		g.graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		super.paint(g);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		g.pushTx();
		g.scale(baseScale);
		super.paintChildren(g);
		g.popTx();
	}
	
}

package com.xrbpowered.zoomui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

public class BasePanel extends JPanel {

	private final BaseContainer container;
	
	private UIElement uiUnderMouse = null;
	private KeyInputHandler uiFocused = null;
	
	private DragActor drag = null;
	private boolean dragCancelled = false;
	private UIElement uiInitiator = null;
	private int initiatorButtons = 0;
	private Point prevMousePoint = null;
	
	public BasePanel() {
		setFocusable(true);
		container = new BaseContainer(this);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				container.invalidateLayout();
			}
		});
			
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				resetFocus();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(uiFocused!=null) {
					if(uiFocused.onKey(e.getKeyChar(), e.getKeyCode(), getKeyModifiers(e)))
						e.consume();
				}
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(container!=null && container.notifyMouseScroll(e.getX(), e.getY(), (float) e.getPreciseWheelRotation())!=null)
					return;
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(drag!=null)
					return;
				if(container!=null) {
					prevMousePoint = e.getPoint();
					initiatorButtons = getMouseButtons(e);
					UIElement ui = container.notifyMouseDown(e.getX(), e.getY(), initiatorButtons);
					if(ui!=uiInitiator && uiInitiator!=null)
						uiInitiator.onMouseReleased();
					uiInitiator = ui;
					if(uiFocused!=null && uiFocused!=uiInitiator)
						resetFocus();
					dragCancelled = false;
					if(uiInitiator!=null)
						return;
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(drag!=null && container!=null) {
					UIElement ui = container.getElementUnderMouse(e.getX(), e.getY());
					if(drag.notifyMouseUp(e.getX(), e.getY(), getMouseButtons(e), ui)) {
						drag = null;
					}
					return;
				}
				if(container!=null && container.notifyMouseUp(e.getX(), e.getY(), getMouseButtons(e), uiInitiator)==uiInitiator)
					return;
				if(uiInitiator!=null)
					uiInitiator.onMouseReleased(); // FIXME release for multi-button scenarios
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if(drag!=null || container==null)
					return;
				if(uiUnderMouse!=null) {
					uiUnderMouse.onMouseOut();
					uiUnderMouse = null;
				}
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(drag==null && !dragCancelled && uiInitiator!=null) {
					drag = uiInitiator.acceptDrag(prevMousePoint.x, prevMousePoint.y, initiatorButtons);
					if(drag==null)
						dragCancelled = false;
				}
				if(drag!=null) {
					Point p = e.getPoint();
					if(!drag.notifyMouseMove(p.x-prevMousePoint.x, p.y-prevMousePoint.y)) {
						drag = null;
						return;
					}
					prevMousePoint = p;
				}
				UIElement ui = container.getElementUnderMouse(e.getX(), e.getY());
				if(ui!=uiUnderMouse) {
					if(uiUnderMouse!=null)
						uiUnderMouse.onMouseOut();
					if(ui!=null)
						ui.onMouseIn();
					uiUnderMouse = ui;
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if(drag!=null || container==null)
					return;
				UIElement ui = container.getElementUnderMouse(e.getX(), e.getY());
				if(ui!=uiUnderMouse) {
					if(uiUnderMouse!=null)
						uiUnderMouse.onMouseOut();
					if(ui!=null)
						ui.onMouseIn();
					uiUnderMouse = ui;
				}
			}
		});
	}
	
	public void resetFocus() {
		if(uiFocused!=null) {
			uiFocused.onFocusLost();
			uiFocused = null;
		}
		container.resetFocus();
	}

	public void setFocus(KeyInputHandler handler) {
		if(uiFocused!=null && uiFocused!=handler)
			resetFocus();
		if(handler!=null) {
			uiFocused = handler;
			uiFocused.onFocus();
		}
	}
	
	public KeyInputHandler getFocus() {
		return uiFocused;
	}
	
	private static int getMouseButtons(MouseEvent e) {
		int mb = 0;
		switch(e.getButton()) {
			case MouseEvent.BUTTON1:
				mb |= UIElement.mouseLeftMask;
				break;
			case MouseEvent.BUTTON2:
				mb |= UIElement.mouseMiddleMask;
				break;
			case MouseEvent.BUTTON3:
				mb |= UIElement.mouseRightMask;
				break;
		}
		if(e.isControlDown())
			mb |= UIElement.modCtrlMask;
		if(e.isAltDown())
			mb |= UIElement.modAltMask;
		if(e.isShiftDown())
			mb |= UIElement.modShiftMask;
		return mb;
	}
	
	private static int getKeyModifiers(KeyEvent e) {
		int mods = 0;
		if(e.isControlDown())
			mods |= UIElement.modCtrlMask;
		if(e.isAltDown())
			mods |= UIElement.modAltMask;
		if(e.isShiftDown())
			mods |= UIElement.modShiftMask;
		return mods;
	}
	
	public BaseContainer getBaseContainer() {
		return container;
	}
	
	@Override
	public void paint(Graphics g) {
		if(container!=null)
			container.paint((Graphics2D) g);
	}
	
}

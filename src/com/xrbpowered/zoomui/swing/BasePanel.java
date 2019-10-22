package com.xrbpowered.zoomui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIElement.Button;
import com.xrbpowered.zoomui.UIWindow;

public class BasePanel extends JPanel {

	public final UIWindow window;
	
	public BasePanel(final UIWindow window) {
		this.window = window;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				window.notifyResized();
			}
		});
			
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				window.getContainer().resetFocus();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(window.getContainer().onKeyPressed(e.getKeyChar(), e.getKeyCode(), getModifiers(e)))
					e.consume();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				window.getContainer().notifyMouseScroll(e.getX(), e.getY(),
						(float)e.getPreciseWheelRotation(), getModifiers(e));
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				window.getContainer().notifyMouseDown(e.getX(), e.getY(),
						getMouseButton(e), getModifiers(e));
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				window.getContainer().notifyMouseUp(e.getX(), e.getY(),
						getMouseButton(e), getModifiers(e), null);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				window.getContainer().onMouseIn();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				window.getContainer().onMouseOut();
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				window.getContainer().onMouseDragged(e.getX(), e.getY());
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				window.getContainer().onMouseMoved(e.getX(), e.getY(), getModifiers(e));
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		window.getContainer().paint(new GraphAssist((Graphics2D) g));
	}
	
	@Override
	public void resize(int width, int height) {
		float scale = window.getContainer().getBaseScale();
		setPreferredSize(new Dimension((int)(width*scale), (int)(height*scale)));
		Window javaWindow = SwingUtilities.getWindowAncestor(this);
		if(javaWindow!=null)
			javaWindow.pack();
		window.notifyResized();
	}
	
	private Point pt = new Point();
	
	public int baseToScreenX(float x) {
		pt.setLocation(x, 0);
		SwingUtilities.convertPointToScreen(pt, this);
		return pt.x;
	}
	
	public int baseToScreenY(float y) {
		pt.setLocation(0, y);
		SwingUtilities.convertPointToScreen(pt, this);
		return pt.y;
	}
	
	public float screenToBaseX(int x) {
		pt.setLocation(x, 0);
		SwingUtilities.convertPointFromScreen(pt, this);
		return pt.x;
	}
	
	public float screenToBaseY(int y) {
		pt.setLocation(0, y);
		SwingUtilities.convertPointFromScreen(pt, this);
		return pt.y;
	}

	public void setBorder(int width, Color color) {
		setBorder(BorderFactory.createLineBorder(color, width));
	}
	
	public void removeBorder() {
		setBorder(BorderFactory.createEmptyBorder());
	}
	
	private static Button getMouseButton(MouseEvent e) {
		switch(e.getButton()) {
			case MouseEvent.BUTTON1:
				return Button.left;
			case MouseEvent.BUTTON2:
				return Button.middle;
			case MouseEvent.BUTTON3:
				return Button.right;
			default:
				return Button.unknown;
		}
	}
	
	private static int getModifiers(InputEvent e) {
		int mods = 0;
		if(e.isControlDown())
			mods |= UIElement.modCtrlMask;
		if(e.isAltDown())
			mods |= UIElement.modAltMask;
		if(e.isShiftDown())
			mods |= UIElement.modShiftMask;
		return mods;
	}
}

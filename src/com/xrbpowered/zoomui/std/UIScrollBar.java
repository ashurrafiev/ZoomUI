package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.GradientPaint;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIHoverElement;

public class UIScrollBar extends UIContainer {

	public static int defaultWidth = 16;
	public static int arrowSpan = 4;
	
	public static Color colorBg = new Color(0xf2f2f2);
	public static Color colorBorder = new Color(0xcccccc);
	public static Color colorArrow = UIButton.colorText;
	public static Color colorArrowDisabled = UIButton.colorBorder;
	
	private class Thumb extends UIHoverElement {
		public int span = 0;
		public float top, bottom;
		public boolean down = false;
		
		public Thumb() {
			super(UIScrollBar.this);
		}
		
		public void updateLocation() {
			top = vertical ? decButton.getHeight() : decButton.getWidth();
			bottom = vertical ? incButton.getY() : incButton.getX();
			if(isEnabled()) {
				float s = vertical ? getParent().getWidth() : getParent().getHeight();
				float h = span*(bottom-top)/(max-min+span);
				if(h<s) {
					span = (int)Math.ceil(s*(max-min)/(bottom-top-s));
					h = s;
				}
				float pos = value*(bottom-top)/(max-min+span)+top;
				
				if(vertical) {
					setSize(s, h);
					setLocation(0, pos);
				}
				else {
					setSize(h, s);
					setLocation(pos, 0);
				}
			}
		}
		
		@Override
		public DragActor acceptDrag(float x, float y, Button button, int mods) {
			if(dragThumbActor.notifyMouseDown(x, y, button, mods))
				return dragThumbActor;
			else
				return null;
		}
		
		@Override
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				down = true;
				repaint();
				return true;
			}
			else
				return false;
		}
		
		@Override
		public boolean onMouseUp(float x, float y, Button button, int mods, UIElement initiator) {
			if(initiator==this) {
				down = false;
				repaint();
				return true;
			}
			else
				return false;
		}
		
		@Override
		public void paint(GraphAssist g) {
			g.setPaint(down ? UIButton.colorDown : vertical ?
				new GradientPaint(0, 0, UIButton.colorGradTop, getWidth(), 0, UIButton.colorGradBottom) :
				new GradientPaint(0, 0, UIButton.colorGradTop, 0, getHeight(), UIButton.colorGradBottom));
			g.fill(this);
			g.border(this, hover ? UIButton.colorText : UIButton.colorBorder);
		}
	}
	
	private DragActor dragThumbActor = new DragActor() {
		private float pos;
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				pos = vertical ? thumb.getY() : thumb.getX();
				thumb.down = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			pos += (vertical ? dy : dx) * getPixelScale();
			float s = (pos-thumb.top) / (thumb.bottom-thumb.top);
			if(setValue(Math.round(min + s*(max-min+thumb.span))))
				onChanged();
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
			thumb.down = false;
			repaint();
			return true;
		}
	};
	
	public final boolean vertical;
	public final UIButtonBase decButton, incButton;
	private final Thumb thumb;
	
	private int min = 0;
	private int max = 100;
	private int step = 1;
	private int value = 0;
	
	public UIScrollBar(UIContainer parent, final boolean vertical) {
		super(parent);
		this.vertical = vertical;
		decButton = new UIButtonBase(this) {
			@Override
			public void onAction() {
				if(setValue(value-step))
					onChanged();
			}
			@Override
			public void paint(GraphAssist g) {
				if(down || hover)
					g.fill(this, down ? UIButton.colorDown : colorBorder);
				g.setColor(isEnabled() ? colorArrow : colorArrowDisabled);
				if(vertical)
					drawUpArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
				else
					drawLeftArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
			}
		};
		incButton = new UIButtonBase(this) {
			@Override
			public void onAction() {
				if(setValue(value+step))
					onChanged();
			}
			@Override
			public void paint(GraphAssist g) {
				if(down || hover)
					g.fill(this, down ? UIButton.colorDown : colorBorder);
				g.setColor(isEnabled() ? colorArrow : colorArrowDisabled);
				if(vertical)
					drawDownArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
				else
					drawRightArrow(g, (int)(getWidth()/2f), (int)(getHeight()/2f));
			}
		};
		thumb = new Thumb();
	}
	
	public void onChanged() {
	}
	
	public void setRange(int min, int max, int step) {
		this.min = min;
		this.max = max;
		this.step = step;
		checkRange();
	}

	private void checkRange() {
		if(max<min)
			max = min;
		if(value<min)
			value = min;
		if(value>max)
			value = max;
		decButton.setEnabled(isEnabled());
		incButton.setEnabled(isEnabled());
		thumb.setVisible(isEnabled());
	}
	
	public int getValue() {
		return value;
	}
	
	public boolean setValue(int v) {
		int old = this.value;
		this.value = v;
		checkRange();
		return old!=this.value;
	}
	
	public boolean isEnabled() {
		return max>min && step>0;
	}
	
	public void setThumbSpan(int v) {
		thumb.span = v;
	}
	
	public void setLength(float length) {
		if(vertical)
			setSize(defaultWidth, length);
		else
			setSize(length, defaultWidth);
	}
	
	@Override
	public void layout() {
		float s = vertical ? getWidth() : getHeight();
		decButton.setLocation(0, 0);
		decButton.setSize(s, s);
		if(vertical)
			incButton.setLocation(0, getHeight()-s);
		else
			incButton.setLocation(getWidth()-s, 0);
		incButton.setSize(s, s);
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, colorBg);
		g.border(this, colorBorder);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		thumb.updateLocation();
		super.paintChildren(g);
	}
	
	public static void drawUpArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span, x, x+span}, new int[] {y+span/2, y-span/2, y+span/2}, 3);
	}

	public static void drawDownArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span, x, x+span}, new int[] {y-span/2, y+span/2, y-span/2}, 3);
	}

	public static void drawLeftArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x+span/2, x-span/2, x+span/2}, new int[] {y-span, y, y+span}, 3);
	}

	public static void drawRightArrow(GraphAssist g, int x, int y, int span) {
		g.graph.fillPolygon(new int[] {x-span/2, x+span/2, x-span/2}, new int[] {y-span, y, y+span}, 3);
	}

	public static void drawUpArrow(GraphAssist g, int x, int y) {
		drawUpArrow(g, x, y, arrowSpan);
	}

	public static void drawDownArrow(GraphAssist g, int x, int y) {
		drawDownArrow(g, x, y, arrowSpan);
	}

	public static void drawLeftArrow(GraphAssist g, int x, int y) {
		drawLeftArrow(g, x, y, arrowSpan);
	}

	public static void drawRightArrow(GraphAssist g, int x, int y) {
		drawRightArrow(g, x, y, arrowSpan);
	}


}

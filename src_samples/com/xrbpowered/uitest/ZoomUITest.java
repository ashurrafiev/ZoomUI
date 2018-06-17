package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.WindowUtils;

public class ZoomUITest {

	private static final int MAX_LEVEL = 2;
	private static final Font font = new Font("Tahoma", Font.BOLD, GraphAssist.ptToPixels(14f));
	
	private static int nextId = 0;
	
	private static String buttonsToString(int buttons) {
		String s = "";
		if((buttons&UIElement.modCtrlMask)!=0) { if(!s.isEmpty()) s += "+"; s += "Ctrl"; }
		if((buttons&UIElement.modAltMask)!=0) { if(!s.isEmpty()) s += "+"; s += "Alt"; }
		if((buttons&UIElement.modShiftMask)!=0) { if(!s.isEmpty()) s += "+"; s += "Shift"; }
		if((buttons&UIElement.mouseLeftMask)!=0) { if(!s.isEmpty()) s += "+"; s += "LMB"; }
		if((buttons&UIElement.mouseMiddleMask)!=0) { if(!s.isEmpty()) s += "+"; s += "MMB"; }
		if((buttons&UIElement.mouseRightMask)!=0) { if(!s.isEmpty()) s += "+"; s += "RMB"; }
		return s;
	}
	
	private static class UIRect extends UIElement {
		private final int id;
		private int clicks = 0;
		private boolean hover = false;
		private boolean down = false;
		public UIRect(UIContainer parent) {
			super(parent);
			this.id = nextId++; 
		}
		@Override
		public void paint(GraphAssist g) {
			g.setColor(down ? new Color(0xcccccc) : hover ? new Color(0xdddddd) : new Color(0xeeeeee));
			g.fillRect(0, 0, (int)getWidth(), (int)getHeight());
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, (int)getWidth(), (int)getHeight());
			g.setFont(font);
			g.drawString(String.format("%d:%d", id, clicks), 10, 25);
		}
		@Override
		protected void onMouseIn() {
			System.out.printf("%d: in\n", id);
			hover = true;
			requestRepaint();
		}
		@Override
		protected void onMouseOut() {
			System.out.printf("%d: out\n", id);
			hover = false;
			requestRepaint();
		}
		@Override
		protected void onMouseReleased() {
			System.out.printf("%d: released\n", id);
			down = false;
			requestRepaint();
		}
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			System.out.printf("%d: down[%s]\n", id, buttonsToString(buttons));
			if(buttons==mouseLeftMask) {
				down = true;
				requestRepaint();
			}
			return true;
		}
		@Override
		protected boolean onMouseUp(float x, float y, int buttons, UIElement initiator) {
			if(initiator!=this)
				return false;
			System.out.printf("%d: up[%s]\n", id, buttonsToString(buttons));
			if(buttons==mouseLeftMask) {
				down = false;
				clicks++;
				requestRepaint();
			}
			return true;
		}
	}
	
	private static class TestContainer extends UIContainer {
		private int level;
		private UIElement left, right;
		public TestContainer(UIContainer parent, int level) {
			super(parent);
			this.level = level;
			if(level==0) {
				left = new UIRect(this);
				right = new UIRect(this);
			}
			else {
				left = new TestContainer(this, level-1);
				right = new TestContainer(this, level-1);
			}
		}
		@Override
		public void layout() {
			float w = getWidth()/2;
			float h = getHeight();
			left.setLocation(5, 5);
			left.setSize(w-10, h-10);
			right.setLocation(w+5, 5);
			right.setSize(w-10, h-10);
			super.layout();
		}
		@Override
		protected void paintSelf(GraphAssist g) {
			if(level<MAX_LEVEL) {
				g.setStroke(getPixelScale());
				g.setColor(Color.RED);
				g.drawRect(0, 0, (int)getWidth(), (int)getHeight());
				g.setColor(new Color(0x22ff0000, true));
				g.fillRect(0, 0, (int)getWidth(), (int)getHeight());
			}
			else {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, (int)getWidth(), (int)getHeight());
			}
		}
		public void start() {
			getBasePanel().showWindow();
		}
	}
	
	public static void main(String[] args) {
		new TestContainer(WindowUtils.createFrame("ZoomUI", 1200, 600), MAX_LEVEL).start();
	}
	
}

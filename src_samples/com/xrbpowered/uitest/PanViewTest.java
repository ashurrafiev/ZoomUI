package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIPanView;
import com.xrbpowered.zoomui.WindowUtils;

public class PanViewTest extends UIPanView {

	private static final Font font = new Font("Tahoma", Font.PLAIN, TextUtils.ptToPixels(9f));

	private static TestButton selected = null;
	
	private static class TestButton extends UIElement {
		private boolean hover = false;
		private String label;
		public TestButton(UIContainer parent, String label) {
			super(parent);
			this.label = label;
		}
		@Override
		public void paint(Graphics2D g2) {
			g2.setColor(selected==this ? new Color(0x0077dd) : hover ? new Color(0xe8f0ff) : Color.WHITE);
			g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
			g2.setColor(selected==this ? Color.WHITE : Color.BLACK);
			g2.setFont(font);
			TextUtils.drawString(g2, label, 10, (int)(getHeight()/2f), TextUtils.LEFT, TextUtils.CENTER);
		}
		@Override
		protected void onMouseIn() {
			hover = true;
			requestRepaint();
		}
		@Override
		protected void onMouseOut() {
			hover = false;
			requestRepaint();
		}
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			if(buttons==mouseLeftMask) {
				selected = this;
				requestRepaint();
				return true;
			}
			else
				return false;
		}
	}
	
	private TestButton[] btn = new TestButton[20];
	
	public PanViewTest(UIContainer parent) {
		super(parent);
		for(int i=0; i<btn.length; i++)
			btn[i] = new TestButton(this, "List item "+i);
	}
	
	@Override
	public void layout() {
		float y = 0f;
		float w = getWidth();
		for(int i=0; i<btn.length; i++) {
			btn[i].setSize(w, 20f);
			btn[i].setLocation(0, y);
			y+=20f;
		}
		int pan = (int)(y-getHeight());
		if(pan<0) pan = 0;
		setPanRange(0, pan);
	}
	
	@Override
	protected void paintSelf(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
	}
	
	@Override
	protected void paintChildren(Graphics2D g2) {
		super.paintChildren(g2);
		if(maxPanY>0) {
			g2.setColor(new Color(0xdddddd));
			g2.fillRect((int)(getWidth()-5f), 0, 5, (int)getHeight());
			float s = getHeight()/(maxPanY+getHeight());
			float top = panY * s;
			float h = getHeight() * s;
			g2.setColor(new Color(0x777777));
			g2.fillRect((int)(getWidth()-5f), (int)top, 5, (int)h);
		}
	}
	
	public static void main(String[] args) {
		new PanViewTest(WindowUtils.createFrame("PanViewTest", 400, 300)).getBasePanel().showWindow();
	}
	
	
}

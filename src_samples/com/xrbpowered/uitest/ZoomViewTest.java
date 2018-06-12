package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.WindowUtils;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListBox;
import com.xrbpowered.zoomui.std.UITextBox;

public class ZoomViewTest extends UIZoomView {

	private UIButton btn1, btn2, btn3;
	private UIListBox list;
	private UITextBox text;
	
	public ZoomViewTest(UIContainer parent) {
		super(parent);
		
		btn1 = new UIButton(this, "Browse...");
		btn2 = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				System.out.println("OK");
			}
		};
		btn3 = new UIButton(this, "Cancel");
		String[] items = new String[20];
		for(int i=0; i<20; i++)
			items[i] = "List item "+i;
		list = new UIListBox(this, items);
		text = new UITextBox(this);
		text.text = "Hello world";
	}
	
	@Override
	public void layout() {
		btn1.setLocation(16, 16);
		btn2.setLocation(16, 16+24);
		btn3.setLocation(16+88+4, 16+24);
		list.setLocation(16, 16+48);
		list.setSize(88*2+4, 120);
		list.layout();
		text.setLocation(16, 32+48+120);
		text.setSize(list.getWidth(), text.getHeight());
	}
	
	@Override
	protected void paintSelf(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
	}

	public static void main(String[] args) {
		UIContainer root = new UIContainer(WindowUtils.createFrame("ZoomViewTest", 800, 600)) {
			private ZoomViewTest top = new ZoomViewTest(this);
			private UIElement bottom = new UIElement(this) {
				@Override
				public void paint(Graphics2D g2) {
					g2.setColor(new Color(0xeeeeee));
					g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
					g2.setColor(new Color(0x999999));
					g2.drawLine(0, 0, (int)getWidth(), 0);
					g2.setColor(Color.BLACK);
					g2.setFont(UIButton.font);
					TextUtils.drawString(g2, "Test UIZoomView and std controls", 16, (int)(getHeight()/2), TextUtils.LEFT, TextUtils.CENTER);
				}
			};
			
			@Override
			public void layout() {
				bottom.setSize(getWidth(), 100);
				bottom.setLocation(0, getHeight() - bottom.getHeight());
				top.setSize(getWidth(), bottom.getY());
				top.setLocation(0, 0);
				top.layout();
			}
		};
		root.getBasePanel().showWindow();
	}
}

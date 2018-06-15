package com.xrbpowered.uitest;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.WindowUtils;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListBox;
import com.xrbpowered.zoomui.std.UITextBox;
import com.xrbpowered.zoomui.std.UIToolButton;

public class ZoomViewTest extends UIZoomView {

	private static final SvgIcon fileIcon = new SvgIcon("svg/file.svg", 160, FileBrowser.iconPalette);
	
	private UIButton btn1, btn2, btn3;
	private UIListBox list;
	private UITextBox text;
	private UIToolButton toolBtn;
	
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
		toolBtn = new UIToolButton(this, fileIcon, 32, 8);
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
		toolBtn.setLocation(-40, list.getY());
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int)getWidth(), (int)getHeight());
	}

	public static void main(String[] args) {
		UIContainer root = new UIContainer(WindowUtils.createFrame("ZoomViewTest", 800, 600)) {
			private ZoomViewTest top = new ZoomViewTest(this);
			private UIElement bottom = new UIElement(this) {
				@Override
				public void paint(GraphAssist g) {
					g.fill(this, new Color(0xeeeeee));
					g.hborder(this, GraphAssist.TOP, new Color(0x999999));
					g.setColor(Color.BLACK);
					g.setFont(UIButton.font);
					g.drawString("Test UIZoomView and std controls", 16, getHeight()/2f,
							GraphAssist.LEFT, GraphAssist.CENTER);
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

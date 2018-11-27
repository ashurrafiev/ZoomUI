package com.xrbpowered.uitest;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIFormattedLabel;
import com.xrbpowered.zoomui.std.UIListBox;
import com.xrbpowered.zoomui.std.UIMessageBox;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResult;
import com.xrbpowered.zoomui.std.UIToolButton;
import com.xrbpowered.zoomui.std.text.UITextBox;
import com.xrbpowered.zoomui.swing.SwingFrame;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class ZoomViewTest extends UIZoomView {

	private static final SvgIcon fileIcon = new SvgIcon(UIToolButton.iconPath+"file.svg", 160, UIToolButton.palette);
	
	private UIButton btn1, btn2, btn3;
	private UIListBox list;
	private UITextBox text;
	private UIToolButton toolBtn;
	private UIFormattedLabel html;
	
	public ZoomViewTest(UIContainer parent) {
		super(parent);
		
		btn1 = new UIButton(this, "Browse...") {
			@Override
			public void onAction() {
				UIMessageBox.show("Error", "This function is not supported.",
						UIMessageBox.iconError, new MessageResult[] {MessageResult.ok}, null);
			}
		};
		btn2 = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				UIMessageBox.show("Done", "<b>OK</b> button has been clicked.",
						UIMessageBox.iconOk, new MessageResult[] {MessageResult.ok}, null);
			}
		};
		btn3 = new UIButton(this, "Cancel") {
			@Override
			public void onAction() {
				UIMessageBox.show("Exit", "Save file before closing the application?",
						UIMessageBox.iconQuestion, new MessageResult[] {MessageResult.yes, MessageResult.no, MessageResult.cancel}, null);
			}
		};
		String[] items = new String[20];
		for(int i=0; i<20; i++)
			items[i] = "List item "+i;
		list = new UIListBox(this, items);
		text = new UITextBox(this);
		text.editor.setText("Hello world");
		toolBtn = new UIToolButton(this, fileIcon, 32, 8) {
			@Override
			public void onAction() {
				UIMessageBox.show("Alert", "An instance of <b>UIToolButton</b> has been clicked "
						+ "invoking <b>UIMessageBox</b> via <b>onAction</b> handler.",
						UIMessageBox.iconAlert, new MessageResult[] {MessageResult.ok}, null);
			}
		};
		
		html = new UIFormattedLabel(this, "This is an example of a <b>formatted label</b>. Click <a href=\"link\">here</a> to test if the link works or not.") {
			@Override
			public void setupHtmlKit() {
				htmlKit.defaultHoverColor = new Color(0x0099ff);
				htmlKit.defaultColor = UIButton.colorBorder;
				htmlKit.getStyleSheet().addRule("a { text-decoration: none; color: #0077dd }");
			}
			@Override
			public void onHrefClicked(String href) {
				System.out.printf("[%s] clicked\n", href);
			}
		};
		
		setPan(-64, 0);
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
		
		html.setLocation(16, 64+48+120);
		html.setSize(list.getWidth(), 0);
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}

	private static class ZoomViewTop extends UIContainer {
		public ZoomViewTop(UIContainer parent) {
			super(parent);
		}
		
		private ZoomViewTest top = new ZoomViewTest(this);
		private UIElement bottom = new UIElement(this) {
			@Override
			public void paint(GraphAssist g) {
				g.fill(this, new Color(0xf2f2f2));
				g.hborder(this, GraphAssist.TOP, new Color(0xcccccc));
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
	}
	
	public static void main(String[] args) {
		UIWindow frame = new SwingFrame(SwingWindowFactory.use(), "ZoomViewTest", 800, 600, true, false) {
			@Override
			public boolean onClosing() {
				confirmClosing();
				return false;
			}
		};
		new ZoomViewTop(frame.getContainer());
		frame.show();
	}
}

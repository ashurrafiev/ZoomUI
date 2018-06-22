package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIFormattedLabel;
import com.xrbpowered.zoomui.std.UIToolButton;
import com.xrbpowered.zoomui.swing.SwingFrame;

public class SvgHtmlTest extends UIContainer {

	private static final Font font = new Font("Verdana", Font.PLAIN, GraphAssist.ptToPixels(9f));
	private static final SvgIcon testIcon = new SvgIcon("svg/folder.svg", 160, UIToolButton.palette);

	private static final String html = "<html>Hello <a href=\"world\" hover=\"#0099ff\" style=\"font-weight:bold\">world</a>"
			+ " and <img size=\"16\" dy=\"-3\" src=\"test\"> <a href=\"people\" style=\"font-weight:bold\">all people</a>!";
	private static final String css = "a { text-decoration: none; color: #0077dd }";
	
	private UIFormattedLabel label;
	
	public SvgHtmlTest(UIContainer parent) {
		super(parent);
		label = new UIFormattedLabel(this, html) {
			@Override
			public void setupHtmlKit() {
				htmlKit.defaultHoverColor = Color.RED;
				htmlKit.getStyleSheet().addRule(css);
				htmlKit.icons.put("test", testIcon);
			}
			@Override
			public void onHrefClicked(String href) {
				System.out.printf("#%s clicked\n", href);
			}
			@Override
			public void paintSelf(GraphAssist g) {
				g.setFont(font);
				g.setColor(Color.BLACK);
				super.paintSelf(g);
			}
		};
	}
	
	@Override
	public void layout() {
		label.setLocation(10, 10);
		label.setSize(getWidth()-20, getHeight()-20);
		label.layout();
	}

	@Override
	public void paintSelf(GraphAssist g) {
		g.fill(this, new Color(0xfff6e6));
	}

	public static void main(String[] args) {
		UIWindow.setBaseScale(2f);
		UIWindow frame = new SwingFrame("SvgHtmlTest", 400, 300);
		new SvgHtmlTest(frame.getContainer());
		frame.show();
	}

}

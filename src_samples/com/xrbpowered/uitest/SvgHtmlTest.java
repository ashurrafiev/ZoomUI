package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIFormattedLabel;
import com.xrbpowered.zoomui.std.UIToolButton;
import com.xrbpowered.zoomui.swing.SwingFrame;

public class SvgHtmlTest extends UIZoomView {

	private static final Font font = new Font("Verdana", Font.PLAIN, GraphAssist.ptToPixels(11f));
	private static final SvgIcon testIcon = new SvgIcon("svg/folder.svg", 160, UIToolButton.palette);

	private static final String html = "Hello <a href=\"world\" hover=\"#0099ff\" style=\"font-weight:bold\">world</a>"
			+ " and <img size=\"16\" dy=\"-3\" src=\"test\"> <a href=\"people\" style=\"font-weight:bold\">all people</a>!"
			+ "<table id=\"tab1\"cellspacing=\"0\"><tr><td valign=\"top\" class=\"col1\">table</td><td valign=\"top\">Multiline contents within a table cell</td></tr>"
			+ "<tr><td valign=\"top\" class=\"col1\">another row</td><td valign=\"top\">Another table cell</td></tr></table>";
	
	private static final String css = "a { text-decoration: none; color: #0077dd } table#tab1 {width: 100%} td {background-color: #ffffff} td.col1 {text-align: right; font-weight: bold}";
	
	private UIFormattedLabel label;
	
	public SvgHtmlTest(UIContainer parent) {
		super(parent);
		label = new UIFormattedLabel(this, html) {
			@Override
			public void setupHtmlKit() {
				htmlKit.defaultHoverColor = Color.RED;
				htmlKit.defaultFont = font;
				ZoomableCss zcss = new ZoomableCss(css);
				zcss.addZoomRule("table#tab1", "margin-top", 10);
				zcss.addPtZoomRule("table#tab1", "font-size", 8.5f);
				zcss.addZoomRule("td", "padding-top", 4);
				zcss.addZoomRule("td", "padding-bottom", 4);
				zcss.addZoomRule("td", "padding-left", 10);
				zcss.addZoomRule("td", "padding-right", 10);
				zcss.addZoomRule("td.col1", "width", 100);
				htmlKit.zoomableCss = zcss;
				htmlKit.icons.put("test", testIcon);
			}
			@Override
			public void onHrefClicked(String href) {
				System.out.printf("#%s clicked\n", href);
			}
		};
		setPanRange(0, 0);
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
		UIWindow frame = new SwingFrame("SvgHtmlTest", 300, 200);
		new SvgHtmlTest(frame.getContainer());
		frame.show();
	}

}

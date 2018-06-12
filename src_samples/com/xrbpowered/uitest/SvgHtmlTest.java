package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.WindowUtils;
import com.xrbpowered.zoomui.icons.SvgIcon;

public class SvgHtmlTest extends UIElement {

	private static final Font font = new Font("Tahoma", Font.PLAIN, TextUtils.ptToPixels(9f));
	private static final SvgIcon testIcon = new SvgIcon("svg/folder.svg", 160, FileBrowser.iconPalette);
	private static final int iconSize = 16;

	public static class SvgImageView extends ImageView {
		public SvgImageView(Element elem) {
			super(elem);
		}

		@Override
		public float getPreferredSpan(int axis) {
			if(axis==View.X_AXIS)
				return iconSize;
			else
				return iconSize-4;
		}
		
		@Override
		public void paint(Graphics g, Shape a) {
			Rectangle rect = (Rectangle) a;
			testIcon.paint((Graphics2D) g, 0, rect.x, rect.y, iconSize, htmlBaseScale, false);
		}
		
	}
	
	public static HTMLEditorKit htmlKit = new HTMLEditorKit() {
		@Override
		public ViewFactory getViewFactory() {
			return new HTMLEditorKit.HTMLFactory() {
				@Override
				public View create(Element elem) {
					AttributeSet attrs = elem.getAttributes();
					Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
					Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
					if(o instanceof HTML.Tag) {
						HTML.Tag kind = (HTML.Tag) o;
						if(kind == HTML.Tag.IMG) {
							return new SvgImageView(elem);
						}
					}
					return super.create(elem);
				}
			};
		}
	};
	
	private static float htmlBaseScale = -1f;
	private String html;
	
	public SvgHtmlTest(UIContainer parent) {
		super(parent);
	}
	
	@Override
	protected boolean onMouseDown(float x, float y, int buttons) {
		requestRepaint();
		return true;
	}

	@Override
	public void paint(Graphics2D g2) {
		float baseScale = getPixelScale();
		if(baseScale!=htmlBaseScale) {
			html = "<html>Hello <img> <a style=\"font-weight:bold;color:#0077ff;text-decoration:underline\">world</a>!";
			//html = "<html>Hello <img src=\"data:image/png;base64,"+diskIcon.createBase64ImageData(0, 16, getPixelScale())+"\"> <b>world</b>!";
			htmlBaseScale = baseScale;
			
			TextUtils.htmlKit = htmlKit;
		}
		
		g2.setColor(new Color(0xfff6e6));
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setFont(font);
		g2.setColor(Color.BLACK);
		TextUtils.drawFormattedString(g2, html, 10, 10, (int)(getWidth()-20), (int)(getHeight()-20));
	}

	public static void main(String[] args) {
		new SvgHtmlTest(WindowUtils.createFrame("SvgHtmlTest", 400, 300)).getBasePanel().showWindow();
	}
	
}

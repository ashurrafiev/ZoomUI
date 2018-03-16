/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Ashur Rafiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package com.xrbpowered.zoomui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

public class TextUtils {

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;
	
	public static int ptToPixels(float pt) {
		return Math.round(96f * pt / 72f);
	}
	
	public static int drawString(Graphics2D g2, String str, int x, int y, int halign, int valign) {
		FontMetrics fm = g2.getFontMetrics();
		float w = fm.stringWidth(str);
		float h = fm.getAscent() - fm.getDescent();
		float tx = x-w*(float)halign/2f;
		float ty = y+h-h*(float)valign/2f;
		g2.drawString(str, tx, ty);
		return y + fm.getHeight();
	}
	
	public static HTMLEditorKit htmlKit = new HTMLEditorKit() {
		@Override
		public ViewFactory getViewFactory() {
			return new HTMLEditorKit.HTMLFactory() {
				@Override
				public View create(Element elem) {
					View view = super.create(elem);
		            if (view instanceof ImageView) {
		                ((ImageView)view).setLoadsSynchronously(true);
		            }
		            return view;
				}
			};
		}
	};
	private static JEditorPane htmlAssist = null;
	
	public static void drawFormattedString(Graphics2D g2, String htmlStr, int x, int y, int w, int h) {
		if(htmlAssist==null) {
			htmlAssist = new JEditorPane(); // new JLabel();
			htmlAssist.setOpaque(false);
			htmlAssist.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		}

		htmlAssist.setEditorKit(htmlKit);

		g2.translate(x, y);
		htmlAssist.setFont(g2.getFont());
		htmlAssist.setForeground(g2.getColor());
		htmlAssist.setBounds(0, 0, w, h);
		//htmlAssist.setVerticalAlignment(JLabel.TOP);
		htmlAssist.invalidate();
		htmlAssist.setText(htmlStr);
		htmlAssist.paint(g2);
		g2.translate(-x, -y);
	}
	
	public static String htmlString(String str) {
		str = str.replaceAll("\\&", "&amp;");
		str = str.replaceAll("\\<", "&lt;");
		str = str.replaceAll("\\>", "&gt;");
		return "<html>"+str;
	}
	
}

package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIArrowButtonBase;
import com.xrbpowered.zoomui.base.UIOptionBoxBase;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIOptionBox<T> extends UIOptionBoxBase<T> {

	public static int defaultWidth = 160;
	public static int defaultArrowWidth = 16;
	public static int defaultHeight = 20;

	public static Font font = UIButton.font;

	public static Color colorBackground = UIScrollBar.colorBackground;
	public static Color colorText = UITextBox.colorText;
	public static Color colorBorder = UITextBox.colorBorder;
	
	public boolean hover = false;
	
	public UIOptionBox(UIContainer parent, T[] options) {
		super(parent, options);
		left.setSize(defaultArrowWidth, 0);
		right.setSize(defaultArrowWidth, 0);
		setSize(defaultWidth, defaultHeight);
	}

	protected UIArrowButtonBase createArrowButton(final int delta) {
		return new UIArrowButton(this, false, delta) {
			@Override
			public void onAction() {
				flip(delta);
			}
			@Override
			public void onMouseIn() {
				((UIOptionBox<?>) getParent()).hover = true;
				super.onMouseIn();
			}
			@Override
			public void onMouseOut() {
				((UIOptionBox<?>) getParent()).hover = false;
				super.onMouseOut();
			}
		};
	}
	
	@Override
	public void paintSelf(GraphAssist g) {
		g.fill(this, colorBackground);
		
		g.setColor(colorText);
		g.setFont(font);
		g.drawString(formatOption(getSelectedOption()), getWidth()/2f, getHeight()/2f, GraphAssist.CENTER, GraphAssist.CENTER);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		g.border(this, hover ? colorText : colorBorder);
	}
	
	@Override
	public void onMouseIn() {
		hover = true;
		repaint();
	}
	
	@Override
	public void onMouseOut() {
		hover = false;
		repaint();
	}

}

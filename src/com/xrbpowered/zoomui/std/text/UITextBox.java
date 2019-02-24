package com.xrbpowered.zoomui.std.text;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIPanView;
import com.xrbpowered.zoomui.base.UITextEditBase;
import com.xrbpowered.zoomui.std.UIButton;

public class UITextBox extends UIPanView {

	public static Font font = UIButton.font;

	public static Color colorBackground = Color.WHITE;
	public static Color colorText = Color.BLACK;
	public static Color colorSelection = new Color(0x0077dd);
	public static Color colorSelectedText = Color.WHITE;
	public static Color colorBorder = new Color(0x888888);

	public static int defaultWidth = 120;
	public static int defaultHeight = 20;

	public final UITextEditBase editor;
	
	public UITextBox(UIContainer parent) {
		super(parent);
		editor = createEditor();
		setSize(defaultWidth, defaultHeight);
	}
	
	protected UITextEditBase createEditor() {
		return new UITextEditBase(this, true);
	}

	@Override
	public void layout() {
		editor.setLocation(0, 0);
		editor.updateSize();
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		g.border(this, editor.isFocused() ? colorSelection : editor.hover ? colorText : colorBorder);
	}
	
}

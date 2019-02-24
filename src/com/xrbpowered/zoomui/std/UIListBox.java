package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIListBoxBase;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIListBox extends UIListBoxBase<UIListItem> {

	public static Color colorBorder = UITextBox.colorBorder;
	public static Color colorBackground = UITextBox.colorBackground;

	public UIListBox(UIContainer parent, Object[] objects) {
		super(parent, objects);
	}

	@Override
	protected UIScrollBar createScroll() {
		return UIScrollContainer.createScroll(this);
	}
	
	@Override
	protected void paintBorder(GraphAssist g) {
		g.border(this, colorBorder);
	}
	
	protected UIListItem createItem(int index, Object object) {
		return new UIListItem(this, index, object);
	}

	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, UIListBox.colorBackground);
	}
}

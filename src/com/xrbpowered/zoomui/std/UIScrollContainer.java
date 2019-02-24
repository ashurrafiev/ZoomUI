package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIScrollContainerBase;
import com.xrbpowered.zoomui.std.text.UITextBox;

public abstract class UIScrollContainer extends UIScrollContainerBase {

	public static Color colorBorder = UITextBox.colorBorder;

	public UIScrollContainer(UIContainer parent) {
		super(parent);
	}
	
	protected UIScrollBar createScroll() {
		return createScroll(this);
	}
	
	protected void paintBorder(GraphAssist g) {
		g.border(this, colorBorder);
	}
	
	public static UIScrollBar createScroll(final UIScrollContainerBase base) {
		return new UIScrollBar(base, true) {
			@Override
			public void onChanged() {
				base.getView().setPan(0, getValue());
			}
			@Override
			protected void updateRange() {
				base.updateScrollRange(this);
			}
		};
	}
	
}

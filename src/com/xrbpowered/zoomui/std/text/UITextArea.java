package com.xrbpowered.zoomui.std.text;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UITextEditBase;
import com.xrbpowered.zoomui.std.UIScrollContainer;

public class UITextArea extends UIScrollContainer {

	public final UITextEditBase editor;
	
	public UITextArea(UIContainer parent) {
		super(parent);
		editor = createEditor();
	}
	
	protected UITextEditBase createEditor() {
		return new UITextEditBase(getView(), false);
	}
	
	@Override
	protected float layoutView() {
		editor.setLocation(0, 0);
		editor.updateSize();
		return editor.getHeight();
	}
	
}

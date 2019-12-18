package com.xrbpowered.zoomui.std.text;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UITextEdit;
import com.xrbpowered.zoomui.std.UIScrollContainer;

public class UITextArea extends UIScrollContainer {

	public final UITextEdit editor;
	
	public UITextArea(UIContainer parent) {
		super(parent);
		editor = createEditor();
	}
	
	protected UITextEdit createEditor() {
		return new UITextEdit(getView(), false);
	}
	
	@Override
	protected float layoutView() {
		editor.setLocation(0, 0);
		editor.updateSize();
		return editor.getHeight();
	}
	
}

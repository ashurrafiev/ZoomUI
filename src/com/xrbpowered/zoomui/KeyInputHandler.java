package com.xrbpowered.zoomui;

public interface KeyInputHandler {

	public boolean onKey(char c, int code, int modifiers);
	public void onFocus();
	public void onFocusLost();
	
}

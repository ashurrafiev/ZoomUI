package com.xrbpowered.zoomui;

public interface KeyInputHandler {

	public boolean onKeyPressed(char c, int code, int mods);
	public void onFocusGained();
	public void onFocusLost();
	
}

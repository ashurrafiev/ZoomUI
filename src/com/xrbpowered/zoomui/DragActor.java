package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.UIElement.Button;

public interface DragActor {

	public boolean notifyMouseDown(float x, float y, Button button, int mods);
	public boolean notifyMouseMove(float dx, float dy);
	public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target);

}

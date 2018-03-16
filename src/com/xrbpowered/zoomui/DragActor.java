package com.xrbpowered.zoomui;

public interface DragActor {

	public boolean notifyMouseDown(int x, int y, int buttons);
	public boolean notifyMouseMove(int dx, int dy);
	public boolean notifyMouseUp(int x, int y, int buttons, UIElement target);

}

package com.xrbpowered.zoomui;

import java.awt.Cursor;

import com.xrbpowered.zoomui.std.UIMessageBox;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResult;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResultHandler;

public abstract class UIWindow {

	protected final UIWindowFactory factory;
	protected final BaseContainer container;

	protected boolean exitOnClose = false;
	
	public UIWindow(UIWindowFactory factory) {
		this.factory = factory;
		this.container = createContainer();
	}
	
	public UIWindowFactory getFactory() {
		return factory;
	}
	
	protected BaseContainer createContainer() {
		return new BaseContainer(this, factory.getBaseScale());
	}
	
	public BaseContainer getContainer() {
		return this.container;
	}

	public abstract int getClientWidth();
	public abstract int getClientHeight();
	public abstract void setClientSize(int width, int height);
	
	public abstract int getX();
	public abstract int getY();
	public abstract void moveTo(int x, int y);

	public abstract void center();
	
	public void move(int dx, int dy) {
		moveTo(getX()+dx, getY()+dy);
	}
	
	public void notifyResized() {
		getContainer().invalidateLayout();
		repaint();
	}
	
	public abstract void show();
	public abstract void repaint();
	
	public abstract int baseToScreenX(float x);
	public abstract int baseToScreenY(float y);
	public abstract float screenToBaseX(int x);
	public abstract float screenToBaseY(int y);
	
	public abstract void setCursor(Cursor cursor);
	
	public UIWindow exitOnClose(boolean exit) {
		this.exitOnClose = exit;
		return this;
	}
	
	public boolean onClosing() {
		return true;
	}
	
	public void onClose() {
		if(exitOnClose)
			System.exit(0);
	}
	
	public boolean requestClosing() {
		if(onClosing()) {
			close();
			return true;
		}
		else
			return false;
	}
	
	public void close() {
		onClose();
	}
	
	public void confirmClosing() {
		UIMessageBox.show(factory, "Exit", "Do you want to close the application?",
			UIMessageBox.iconQuestion, new MessageResult[] {MessageResult.ok, MessageResult.cancel},
			new MessageResultHandler() {
				@Override
				public void onResult(MessageResult result) {
					if(result==MessageResult.ok)
						close();
				}
			});
	}
	
}

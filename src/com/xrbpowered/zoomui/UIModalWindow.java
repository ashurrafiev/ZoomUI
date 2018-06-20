package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;

public abstract class UIModalWindow<A> extends UIWindow {

	private final A defaultResult;
	
	public UIModalWindow(A defaultResult) {
		this.defaultResult = defaultResult;
	}
	
	@Override
	protected BaseContainer createContainer() {
		return new ModalBaseContainer<A>(this, UIWindow.getBaseScale());
	}
	
	@SuppressWarnings("unchecked")
	public ModalBaseContainer<A> getContainer() {
		return (ModalBaseContainer<A>) this.container;
	}
	
	public void onResult(A result) {
	}
	
	@Override
	public void close() {
		closeWithResult(defaultResult);
	}
	
	public void closeWithResult(A result) {
		onClose();
		onResult(result);
	}
}

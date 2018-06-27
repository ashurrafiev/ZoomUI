package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;

public abstract class UIModalWindow<A> extends UIWindow {

	public static interface ResultHandler<A> {
		public void onResult(UIModalWindow<A> dlg, A result);
	}
	
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
	
	public ResultHandler<A> onResult = null;	
	@Override
	public void close() {
		closeWithResult(defaultResult);
	}
	
	public void closeWithResult(A result) {
		onClose();
		if(onResult!=null)
			onResult.onResult(this, result);
	}
}

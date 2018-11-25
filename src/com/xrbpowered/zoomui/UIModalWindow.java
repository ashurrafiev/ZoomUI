package com.xrbpowered.zoomui;

import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;

public abstract class UIModalWindow<A> extends UIWindow {

	public static interface ResultHandler<A> {
		public void onResult(A result);
		public void onCancel();
	}

	public static abstract class ResultHandlerWithDefault<A> implements ResultHandler<A> {
		public final A defaultResult;
		public ResultHandlerWithDefault(A defaultResult) {
			this.defaultResult = defaultResult;
		}
		@Override
		public void onCancel() {
			onResult(defaultResult);
		}
	}
	
	public ResultHandler<A> onResult = null;	
	
	public UIModalWindow(UIWindowFactory factory, ResultHandler<A> onResult) {
		super(factory);
		this.onResult = onResult;
	}

	@Override
	protected BaseContainer createContainer() {
		return new ModalBaseContainer<A>(this, factory.getBaseScale());
	}
	
	@SuppressWarnings("unchecked")
	public ModalBaseContainer<A> getContainer() {
		return (ModalBaseContainer<A>) this.container;
	}
	
	@Override
	public void close() {
		onClose();
		if(onResult!=null)
			onResult.onCancel();
	}
	
	public void closeWithResult(A result) {
		onClose();
		if(onResult!=null)
			onResult.onResult(result);
	}
	
	public ResultHandler<A> wrapInResultHandler() {
		return new ResultHandler<A>() {
			@Override
			public void onResult(A result) {
				closeWithResult(result);
			}
			@Override
			public void onCancel() {
				close();
			}
		};
	}
}

package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.UIContainer;

public abstract class UIOptionBoxBase<T> extends UIContainer {
	
	protected final T[] options;
	
	private int selectedIndex = 0;
	
	protected final UIArrowButtonBase left, right;

	public UIOptionBoxBase(UIContainer parent, T[] options) {
		super(parent);
		this.options = options;
		
		left = createArrowButton(-1);
		right = createArrowButton(1);
	}

	protected abstract UIArrowButtonBase createArrowButton(final int delta);
	
	public void flip(int delta) {
		selectedIndex = (selectedIndex + delta + options.length) % options.length;
		onOptionSelected(options[selectedIndex]);
		repaint();
	}
	
	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		left.setSize(left.getWidth(), getHeight());
		left.setLocation(0, 0);
		right.setSize(right.getWidth(), getHeight());
		right.setLocation(getWidth()-right.getWidth(), 0);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public T getSelectedOption() {
		return options[selectedIndex];
	}
	
	public int getNumItems() {
		return options.length;
	}
	
	public T getOption(int index) {
		return options[index];
	}
	
	public T selectOption(T value) {
		selectedIndex = 0;
		for(int i=0; i<options.length; i++) {
			if(options[i]==value) {
				selectedIndex = i;
				return value;
			}
		}
		return options[0];
	}
	
	protected void onOptionSelected(T value) {
	}
	
	protected String formatOption(T value) {
		return value.toString();
	}
	
	public static Integer[] createRange(int rangeSize) {
		Integer[] opts = new Integer[rangeSize];
		for(int i=0; i<opts.length; i++)
			opts[i] = i;
		return opts;
	}
}

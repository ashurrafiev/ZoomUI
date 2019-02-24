package com.xrbpowered.zoomui.base;

import java.util.LinkedList;

public abstract class History<T> {

	public final int maxSize;
	
	private final LinkedList<T> history = new LinkedList<>();
	private int size = 0;
	private int index = -1;

	public History(int maxSize) {
		this.maxSize = maxSize;
	}
	
	protected abstract void apply(T item);
	public abstract void push();
	
	public boolean canUndo() {
		return index>0;
	}

	public boolean undo() {
		if(canUndo()) {
			jumpTo(index-1);
			return true;
		}
		else
			return false;
	}
	
	public boolean canRedo() {
		return index<size-1;
	}
	
	public boolean redo() {
		if(canRedo()) {
			jumpTo(index+1);
			return true;
		}
		else
			return false;
	}

	public void jumpTo(int index) {
		this.index = index;
		apply(history.get(index));
		onUpdate();
	}

	public void clear() {
		history.clear();
		size = 0;
		index = -1;
	}
	
	protected void push(T item) {
		index++;
		while(size>index) {
			history.removeLast();
			size--;
		}
		history.add(item);
		size++;
		while(maxSize>0 && size>maxSize) {
			history.removeFirst();
			size--;
		}
		onUpdate();
	}
	
	protected void onUpdate() {
	}
	
}

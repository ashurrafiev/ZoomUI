package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public abstract class UIListBoxBase<T extends UIElement> extends UIScrollContainerBase {

	protected UIElement[] listItems;
	
	private int selectedIndex = -1;
	
	public UIListBoxBase(UIContainer parent, Object[] objects) {
		super(parent);
		setItems(objects);
	}
	
	public void setItems(Object[] objects) {
		getView().removeAllChildren();
		int num = objects==null ? 0 : objects.length;
		listItems = new UIElement[num];
		for(int i=0; i<num; i++) {
			listItems[i] = createItem(i, objects[i]);
		}
		deselect();
	}
	
	protected abstract T createItem(int index, Object object);
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	@SuppressWarnings("unchecked")
	public T getSelectedItem() {
		return selectedIndex<0 ? null : (T)listItems[selectedIndex];
	}
	
	public void deselect() {
		this.selectedIndex = -1;
		onNothingSelected();
	}
	
	@SuppressWarnings("unchecked")
	public void select(int index) {
		if(index>=0 && index<listItems.length) {
			this.selectedIndex = index;
			onItemSelected((T)listItems[index]);
		}
	}
	
	public int getNumItems() {
		return listItems.length;
	}
	
	@SuppressWarnings("unchecked")
	public T getItem(int index) {
		return (T)listItems[index];
	}
	
	public void onItemSelected(T item) {
	}

	public void onNothingSelected() {
	}

	public void onClickSelected() {
	}
	
	@Override
	protected float layoutView() {
		float w = getView().getWidth();
		float y = 0;
		for(int i=0; i<listItems.length; i++) {
			listItems[i].setLocation(0, y);
			float h = listItems[i].getHeight();
			listItems[i].setSize(w, h);
			y += h;
		}
		return y;
	}
	
}

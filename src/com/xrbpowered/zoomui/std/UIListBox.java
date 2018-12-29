package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIListBox extends UIScrollContainer {

	public static Color colorBackground = UITextBox.colorBackground;

	protected UIListItem[] listItems;
	
	private int selectedIndex = -1;
	
	public UIListBox(UIContainer parent, Object[] objects) {
		super(parent);
		setItems(objects);
	}
	
	public void setItems(Object[] objects) {
		getView().removeAllChildren();
		int num = objects==null ? 0 : objects.length;
		listItems = new UIListItem[num];
		for(int i=0; i<num; i++) {
			listItems[i] = createItem(i, objects[i]);
		}
		deselect();
	}
	
	protected UIListItem createItem(int index, Object object) {
		return new UIListItem(this, index, object);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public UIListItem getSelectedItem() {
		return selectedIndex<0 ? null : listItems[selectedIndex];
	}
	
	public void deselect() {
		this.selectedIndex = -1;
		onNothingSelected();
	}
	
	public void select(int index) {
		if(index>=0 && index<listItems.length) {
			this.selectedIndex = index;
			onItemSelected(listItems[index]);
		}
	}
	
	public int getNumItems() {
		return listItems.length;
	}
	
	public UIListItem getItem(int index) {
		return listItems[index];
	}
	
	public void onItemSelected(UIListItem item) {
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
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, colorBackground);
	}
	
}

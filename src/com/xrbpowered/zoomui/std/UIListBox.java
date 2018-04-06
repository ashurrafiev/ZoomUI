package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.UIContainer;

public class UIListBox extends UIScrollContainer {

	public static Color colorBackground = UITextBox.colorBackground;
	public static Color colorBorder = UITextBox.colorBorder;

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
		selectedIndex = -1;
	}
	
	protected UIListItem createItem(int index, Object object) {
		return new UIListItem(this, index, object);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}
	
	public void onSelect(int index) {
	}

	public void onClickSelected() {
	}
	
	@Override
	protected float layoutView() {
		float w = getWidth();
		float y = 0;
		for(int i=0; i<listItems.length; i++) {
			listItems[i].setLocation(0, y);
			listItems[i].setSize(w, 20f);
			y += 20f;
		}
		return y;
	}
	
	@Override
	protected void paintSelf(Graphics2D g2) {
		g2.setColor(colorBackground);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
	}
	
	@Override
	protected void paintChildren(Graphics2D g2) {
		super.paintChildren(g2);
		g2.setColor(colorBorder);
		g2.drawRect(0, 0, (int)getWidth(), (int)getHeight());
	}
	
}

package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIElement;

public class UIListItem extends UIElement {
	
	public static Font font = UIButton.font;

	public static Color colorText = UITextBox.colorText;
	public static Color colorHighlight = new Color(0xe8f0ff);
	public static Color colorSelection = UITextBox.colorSelection;
	public static Color colorSelectedText = UITextBox.colorSelectedText;

	public final int index;
	protected Object object;
	
	public final UIListBox list;
	protected boolean hover = false;
	
	public UIListItem(UIListBox list, int index, Object object) {
		super(list.getView());
		this.index = index;
		this.object = object;
		this.list = list;
	}
	
	@Override
	public void paint(Graphics2D g2) {
		boolean sel = (index==list.getSelectedIndex());
		g2.setColor(sel ? colorSelection : hover ? colorHighlight : Color.WHITE);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setColor(sel ? colorSelectedText : colorText);
		g2.setFont(font);
		TextUtils.drawString(g2, object.toString(), 8, (int)(getHeight()/2f), TextUtils.LEFT, TextUtils.CENTER);
	}
	
	@Override
	protected void onMouseIn() {
		hover = true;
		requestRepaint();
	}
	
	@Override
	protected void onMouseOut() {
		hover = false;
		requestRepaint();
	}
	
	@Override
	protected boolean onMouseDown(float x, float y, int buttons) {
		if(buttons==mouseLeftMask) {
			if(list.getSelectedIndex()==index)
				list.onClickSelected();
			else {
				list.setSelectedIndex(index);
				list.onSelect(index);
			}
			requestRepaint();
			return true;
		}
		else
			return false;
	}
}
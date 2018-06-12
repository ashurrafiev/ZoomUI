package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.icons.SvgIcon;

public class UIToolButton extends UIButtonBase {

	public static Color colorDown = UIButton.colorDown;
	public static Color colorHover = new Color(0xe8e8e8);
	public static Color colorBorder = UIButton.colorBorder;

	public static final int STYLE_NORMAL = 0;
	public static final int STYLE_SELECTED = 1;
	public static final int STYLE_DISABLED = 2;
	public static final int STYLE_DISABLED_SELECTED = 3;
	
	protected SvgIcon icon;
	private int iconSize;
	
	public UIToolButton(UIContainer parent, SvgIcon icon, int iconSize, int padding) {
		super(parent);
		this.icon = icon;
		setIconSize(iconSize, padding);
	}
	
	public void setIconSize(int iconSize, int padding) {
		this.iconSize = iconSize;
		float size = iconSize+padding*2;
		setSize(size, size);
	}
	
	@Override
	public void paint(GraphAssist g) {
		int w = (int)getWidth();
		int h = (int)getHeight();
		
		Color bgColor = down ? colorDown : hover ? colorHover : null;
		if(bgColor!=null) {
			g.setColor(bgColor);
			g.fillRect(0, 0, w, h);
		}
		if(hover) {
			g.setColor(colorBorder);
			g.drawRect(0, 0, w, h);
		}
		icon.paint(g.graph, isDisabled() ? STYLE_DISABLED : STYLE_NORMAL, (w-iconSize)/2f, (h-iconSize)/2f, iconSize, getPixelScale(), true);
	}

}

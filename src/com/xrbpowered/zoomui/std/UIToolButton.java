package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.icons.SvgIcon;

public class UIToolButton extends UIButtonBase {

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
	public void paint(Graphics2D g2) {
		int w = (int)getWidth();
		int h = (int)getHeight();
		
		StdPainter painter = StdPainter.instance;
		Color bgColor = down ? painter.colorDown : hover ? painter.colorHover : null;
		if(bgColor!=null) {
			g2.setColor(bgColor);
			g2.fillRect(0, 0, w, h);
		}
		if(hover) {
			g2.setColor(painter.colorBorder);
			g2.drawRect(0, 0, w, h);
		}
		icon.paint(g2, isDisabled() ? STYLE_DISABLED : STYLE_NORMAL, (w-iconSize)/2f, (h-iconSize)/2f, iconSize, getPixelScale(), true);
	}

}

package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIButtonBase;
import com.xrbpowered.zoomui.icons.IconPalette;
import com.xrbpowered.zoomui.icons.SvgIcon;

public class UIToolButton extends UIButtonBase {

	public static final IconPalette palette = new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xf8f8f8), new Color(0x888888), Color.BLACK},
		{new Color(0x66aaff), new Color(0x4499ee), new Color(0xddeeff), Color.WHITE},
		{new Color(0xf9f9f9), new Color(0xfdfdfd), new Color(0xd8d8d8), new Color(0xababab)}, 
		{new Color(0x4298f3), new Color(0x2c8de8), new Color(0x90c4f3), new Color(0xa6d0f3)},
	});

	public static String iconPath = "com/xrbpowered/zoomui/std/icons/";
	
	public static Color colorDown = UIButton.colorDown;
	public static Color colorHover = new Color(0xe8e8e8);
	public static Color colorBorder = UIButton.colorBorder;
	
	public static int defaultIconSize = 160;

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
	
	public UIToolButton(UIContainer parent, String iconUri, int iconSize, int padding) {
		this(parent, new SvgIcon(iconUri, defaultIconSize, palette), iconSize, padding);
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

package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.icons.IconPalette;

public class StdPainter {

	public static StdPainter instance = new StdPainter();

	public int fontSize = TextUtils.ptToPixels(9f);
	public Font font = new Font("Tahoma", Font.PLAIN, fontSize);
	
	public int buttonWidth = 88;
	public int buttonHeight = 20;

	/*public Color colorBgDark = new Color(0x222222);
	public Color colorBg = new Color(0x666666);
	public Color colorBgLight = new Color(0x333333);

	public Color colorDown = new Color(0xd4d4d4);
	public Color colorHover = new Color(0x555555);
	public Color colorGradTop = new Color(0xeeeeee);
	public Color colorGradBottom = new Color(0xcccccc);
	public Color colorHighlight = new Color(0x666666);
	
	public Color colorBorder = new Color(0xcccccc);
	public Color colorBorderLight = new Color(0x444444);
	public Color colorSelection = new Color(0xff9900);
	public Color colorTextBg = new Color(0x444444);
	
	public Color colorSelectionFgDisabled = new Color(0xffdd99);
	public Color colorFgDisabled = new Color(0x888888);
	public Color colorSelectionFg = Color.BLACK;
	public Color colorFg = new Color(0xdddddd);
	
	public IconPalette iconPalette = new IconPalette(new Color[][] {
		{new Color(0x333333), new Color(0x666666), Color.WHITE, colorBorder},
		{new Color(0xeeeeee), new Color(0xf8f8f8), new Color(0x666666), Color.BLACK},
		{new Color(0x333333), new Color(0x666666), Color.WHITE, colorBorder}, 
		{new Color(0xeeeeee), new Color(0xf8f8f8), new Color(0x666666), Color.BLACK},
	});*/

	public Color colorBgDark = new Color(0xe4e4e4);
	public Color colorBg = new Color(0xf2f2f2);
	public Color colorBgLight = Color.WHITE;

	public Color colorDown = new Color(0xd4d4d4);
	public Color colorHover = new Color(0xe8e8e8);
	public Color colorGradTop = new Color(0xeeeeee);
	public Color colorGradBottom = new Color(0xcccccc);
	public Color colorHighlight = new Color(0xe8f0ff);
	
	public Color colorBorder = new Color(0x888888);
	public Color colorBorderLight = new Color(0xcccccc);
	public Color colorSelection = new Color(0x0077dd);
	public Color colorTextBg = Color.WHITE;
	
	public Color colorSelectionFgDisabled = new Color(0x99ccff);
	public Color colorFgDisabled = new Color(0x888888);
	public Color colorSelectionFg = Color.WHITE;
	public Color colorFg = Color.BLACK;
	
	public IconPalette iconPalette = new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xf8f8f8), colorBorder, Color.BLACK},
		{new Color(0x66aaff), new Color(0x4499ee), new Color(0xddeeff), Color.WHITE},
		{new Color(0xf9f9f9), new Color(0xfdfdfd), new Color(0xd8d8d8), new Color(0xababab)}, 
		{new Color(0x4298f3), new Color(0x2c8de8), new Color(0x90c4f3), new Color(0xa6d0f3)},
	});
	
}

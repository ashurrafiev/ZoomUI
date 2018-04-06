package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;

public class UIButton extends UIButtonBase {

	public static Font font = new Font("Tahoma", Font.PLAIN, TextUtils.ptToPixels(9f));
	
	public static Color colorDown = new Color(0xd4d4d4);
	public static Color colorBorder = new Color(0x888888);
	public static Color colorText = Color.BLACK;
	public static Color colorGradTop = new Color(0xeeeeee);
	public static Color colorGradBottom = new Color(0xcccccc);

	public static int defaultWidth = 88;
	public static int defaultHeight = 20;

	protected String label;
	
	public UIButton(UIContainer parent, String label) {
		super(parent);
		this.label = label;
		setSize(defaultWidth, defaultHeight);
	}
	
	@Override
	public void paint(Graphics2D g2) {
		if(down)
			g2.setColor(colorDown);
		else
			g2.setPaint(new GradientPaint(0, 0, colorGradTop, 0, getHeight(), colorGradBottom));
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setColor(hover ? colorText : colorBorder);
		g2.drawRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setColor(colorText);
		g2.setFont(font);
		TextUtils.drawString(g2, label, (int)(getWidth()/2f), (int)(getHeight()/2f), TextUtils.CENTER, TextUtils.CENTER);
	}
	
}

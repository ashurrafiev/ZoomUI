package com.xrbpowered.zoomui.std;

import java.awt.GradientPaint;
import java.awt.Graphics2D;

import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;

public class UIButton extends UIButtonBase {

	protected String label;
	
	public UIButton(UIContainer parent, String label) {
		super(parent);
		this.label = label;
		setSize(StdPainter.instance.buttonWidth, StdPainter.instance.buttonHeight);
	}
	
	@Override
	public void paint(Graphics2D g2) {
		StdPainter painter = StdPainter.instance;
		if(down)
			g2.setColor(painter.colorDown);
		else
			g2.setPaint(new GradientPaint(0, 0, painter.colorGradTop, 0, getHeight(), painter.colorGradBottom));
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setColor(hover ? painter.colorFg : painter.colorBorder);
		g2.drawRect(0, 0, (int)getWidth(), (int)getHeight());
		g2.setColor(painter.colorFg);
		g2.setFont(painter.font);
		TextUtils.drawString(g2, label, (int)(getWidth()/2f), (int)(getHeight()/2f), TextUtils.CENTER, TextUtils.CENTER);
	}
	
}

package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.GradientPaint;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIArrowButtonBase;
import com.xrbpowered.zoomui.base.UIScrollBarBase;

public class UIScrollBar extends UIScrollBarBase {

	public static int defaultWidth = 16;
	
	public static Color colorBackground = new Color(0xf2f2f2);
	public static Color colorBorder = new Color(0xcccccc);
	
	public UIScrollBar(UIContainer parent, boolean vertical, int thumbWidth) {
		super(parent, vertical, thumbWidth);
	}

	public UIScrollBar(UIContainer parent, boolean vertical) {
		super(parent, vertical, defaultWidth);
	}

	protected UIArrowButtonBase createArrowButton(final int delta) {
		return new UIArrowButton(this, vertical, delta) {
			@Override
			public void onAction() {
				if(setValue(value+step*delta))
					onChanged();
			}
		};
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		super.paintSelf(g);
		g.fill(this, colorBackground);
		g.border(this, colorBorder);
	}
	
	@Override
	protected void paintThumb(GraphAssist g, Thumb thumb) {
		g.setPaint(thumb.down ? UIButton.colorDown : vertical ?
				new GradientPaint(0, 0, UIButton.colorGradTop, thumb.getWidth(), 0, UIButton.colorGradBottom) :
				new GradientPaint(0, 0, UIButton.colorGradTop, 0, thumb.getHeight(), UIButton.colorGradBottom));
		g.fill(thumb);
		g.border(thumb, thumb.hover ? UIButton.colorText : UIButton.colorBorder);
	}

}

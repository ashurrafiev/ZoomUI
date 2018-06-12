package com.xrbpowered.zoomui;

import java.awt.Dimension;

import javax.swing.JFrame;

public abstract class WindowUtils {

	public static BaseContainer createFrame(String title, int width, int height, float baseScale, boolean canResize) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BasePanel base = new BasePanel(frame);
		if(baseScale<=0f)
			baseScale = BaseContainer.getAutoScale();
		base.setPreferredSize(new Dimension((int)(width*baseScale), (int)(height*baseScale)));
		base.getBaseContainer().setBaseScale(baseScale);
		
		frame.setResizable(canResize);
		frame.setContentPane(base);
		frame.pack();
		return base.getBaseContainer();
	}
	
	public static BaseContainer createFrame(String title, int width, int height) {
		return createFrame(title, width, height, 0f, true);
	}

}

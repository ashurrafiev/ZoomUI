package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import com.xrbpowered.zoomui.BasePanel;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.std.StdPainter;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListBox;

public class ZoomViewTest extends UIZoomView {

	private UIButton btn1, btn2, btn3;
	private UIListBox list;
	
	public ZoomViewTest(UIContainer parent) {
		super(parent);
		btn1 = new UIButton(this, "Browse...");
		btn2 = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				System.out.println("OK");
			}
		};
		btn3 = new UIButton(this, "Cancel");
		String[] items = new String[20];
		for(int i=0; i<20; i++)
			items[i] = "List item "+i;
		list = new UIListBox(this, items);
	}
	
	@Override
	protected void layout() {
		btn1.setLocation(16, 16);
		btn2.setLocation(16, 16+24);
		btn3.setLocation(16+88+4, 16+24);
		list.setLocation(16, 16+48);
		list.setSize(88*2+4, 120);
	}
	
	@Override
	protected void paintSelf(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		StdPainter painter = StdPainter.instance;
		int y = 200+painter.fontSize;
		g2.setColor(painter.colorFg);
		g2.drawString("sample.txt", 16, y);
		y += painter.fontSize;
		g2.setColor(painter.colorBorder);
		g2.drawString("10 Jan 2018, 18:23, 932KB", 16, y);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("ZoomUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BasePanel base = new BasePanel();
		base.setPreferredSize(new Dimension(800, 600));
		// base.getBaseContainer().setBaseScale(1f);
		
		new ZoomViewTest(base.getBaseContainer());
		
		frame.setContentPane(base);
		frame.pack();
		frame.setVisible(true);

	}
}

package com.xrbpowered.zoomui.swing;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.xrbpowered.zoomui.UIWindow;

public class SwingFrame extends UIWindow {

	public final JFrame frame;
	public final BasePanel panel;
	
	protected SwingFrame(SwingWindowFactory factory, String title, int w, int h, boolean canResize, boolean undecorated) {
		super(factory);
		exitOnClose(true);
		
		frame = new JFrame();
		frame.setTitle(title);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setResizable(canResize && !undecorated);
		frame.setUndecorated(undecorated);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				requestClosing();
			}
		});
		
		panel = new BasePanel(this);
		frame.setContentPane(panel);
		if(w>0 && h>0)
			setClientSize(w, h);
		center();
	}
	
	public SwingFrame maximize() {
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		return this;
	}
	
	@Override
	public int getClientWidth() {
		return panel.getWidth();
	}

	@Override
	public int getClientHeight() {
		return panel.getHeight();
	}

	@Override
	public void setClientSize(int width, int height) {
		panel.resize(width, height);
	}
	
	@Override
	public int getX() {
		return frame.getX();
	}
	
	@Override
	public int getY() {
		return frame.getY();
	}
	
	@Override
	public void moveTo(int x, int y) {
		frame.setLocation(x, y);
	}

	@Override
	public void center() {
		frame.setLocationRelativeTo(null);
	}

	@Override
	public void show() {
		frame.setVisible(true);
	}

	@Override
	public void repaint() {
		panel.repaint();
	}

	@Override
	public void setCursor(Cursor cursor) {
		panel.setCursor(cursor);
	}
	
	@Override
	public void close() {
		frame.dispose();
		super.close();
	}

	@Override
	public int baseToScreenX(float x) {
		return panel.baseToScreenX(x);
	}

	@Override
	public int baseToScreenY(float y) {
		return panel.baseToScreenY(y);
	}

	@Override
	public float screenToBaseX(int x) {
		return panel.screenToBaseX(x);
	}

	@Override
	public float screenToBaseY(int y) {
		return panel.screenToBaseY(y);
	}
	
	@Override
	public FontMetrics getFontMetrics(Font font) {
		return panel.getFontMetrics(font);
	}

}

package com.xrbpowered.zoomui.swing;

import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.xrbpowered.zoomui.UIWindow;

public class SwingFrame extends UIWindow {

	public final JFrame frame;
	public final BasePanel panel;
	
	public SwingFrame(String title, int w, int h) {
		this(title, w, h, true);
	}
	
	public SwingFrame(String title, int w, int h, boolean canResize) {
		frame = new JFrame();
		frame.setTitle(title);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setResizable(canResize);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				requestClosing();
			}
		});
		
		panel = new BasePanel(this);
		frame.setContentPane(panel);
		setClientSize(w, h);
		center();
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
		System.exit(0);
	}

}

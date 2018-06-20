package com.xrbpowered.zoomui.swing;

import java.awt.Cursor;

import javax.swing.JFrame;

import com.xrbpowered.zoomui.BaseContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;

public class SwingFrame extends UIWindow {

	public final JFrame frame;
	public final BasePanel panel;
	
	public SwingFrame(String title, int w, int h, boolean canResize) {
		frame = new JFrame();
		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO close listener
		frame.setResizable(canResize);
		
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
		frame.setVisible(false);
		super.close();
	}
	
	public static <A extends UIElement> A show(String title, int w, int h,
			boolean canResize, UIFactory<A, BaseContainer> ui) {
		UIWindow wnd = new SwingFrame(title, w, h, canResize);
		A e = ui.create(wnd.getContainer());
		wnd.show();
		return e;
	}

}

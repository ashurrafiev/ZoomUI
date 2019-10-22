package com.xrbpowered.zoomui.swing;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import com.xrbpowered.zoomui.UIModalWindow;

public class SwingModalDialog<A> extends UIModalWindow<A> {

	public final JDialog dialog;
	public final BasePanel panel;

	protected SwingModalDialog(SwingWindowFactory factory, String title, int w, int h, boolean canResize) {
		super(factory);
		
		dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(" "+title);
		dialog.setResizable(canResize);
		dialog.setModal(true);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				requestClosing();
			}
		});
		
		panel = new BasePanel(this);
		dialog.setContentPane(panel);
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
	public int getX() {
		return dialog.getX();
	}
	
	@Override
	public int getY() {
		return dialog.getY();
	}
	
	@Override
	public void moveTo(int x, int y) {
		dialog.setLocation(x, y);
	}

	@Override
	public void center() {
		dialog.setLocationRelativeTo(null);
	}

	@Override
	public void show() {
		dialog.setVisible(true);
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
		dialog.dispose();
		super.close();
	}
	
	@Override
	public void closeWithResult(A result) {
		dialog.dispose();
		super.closeWithResult(result);
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

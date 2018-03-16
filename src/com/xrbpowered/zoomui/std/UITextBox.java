package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public class UITextBox extends UIElement implements KeyInputHandler {

	protected boolean hover = false;

	public String text = "";
	private int cursor = 0;
	private int selStart = -1;
	private int selEnd = -1;
	private int selMin = -1;
	private int selMax = -1;
	
	private float cursorX;
	private boolean updateCursor = false;
	private boolean dragSelecting = false;
	
	private DragActor dragSelectActor = new DragActor() {
		private float x;
		@Override
		public boolean notifyMouseDown(int x, int y, int buttons) {
			if(buttons==mouseLeftMask) {
				dragSelecting = true;
				this.x = parentToLocalX(x*getPixelScale());
				cursorX = this.x;
				updateCursor = true;
				startSelection();
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(int dx, int dy) {
			x += dx*getPixelScale();
			cursorX = x;
			updateCursor = true;
			requestRepaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(int x, int y, int buttons, UIElement target) {
			dragSelecting = false;
			return true;
		}
	};
	
	public UITextBox(UIContainer parent) {
		super(parent);
		setSize(StdPainter.instance.buttonWidth, StdPainter.instance.buttonHeight);
	}
	
	@Override
	public void paint(Graphics2D g2) {
		StdPainter painter = StdPainter.instance;
		g2.setColor(painter.colorTextBg);
		g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		
		boolean focused = isFocused();
		
		AffineTransform tx = g2.getTransform(); // TODO manual scale as function
		g2.setTransform(new AffineTransform());
		g2.translate(tx.getTranslateX(), tx.getTranslateY());
		float pix = getPixelScale();
		
		g2.setFont(painter.font.deriveFont(painter.fontSize/pix));
		FontMetrics fm = g2.getFontMetrics();
		if(updateCursor) {
			cursor = searchCol(fm, (cursorX-4f)/pix);
			updateCursor = false;
			if(dragSelecting)
				modifySelection(selStart);
		}
		
		// FontMetrics fm = new JLabel().getFontMetrics(g2.getFont());
		
		// TODO clip
		// TODO scroll text in textbox to cursor
		
		float h = fm.getAscent() - fm.getDescent();
		float x0 = 4/pix;
		float y = getHeight()/pix/2f+h/2f;
		float lh = fm.getHeight();
		float descent = fm.getDescent();
		
		if(selStart<0) {
			g2.setColor(painter.colorFg);
			g2.drawString(text, x0, y);
		}
		else {
			float x = x0;
			String s;
			if(selMin>0) {
				g2.setColor(painter.colorFg);
				s = text.substring(0, selMin);
				g2.drawString(s, x, y);
				x += (float)fm.getStringBounds(s, g2).getWidth();
			}
			s = text.substring(selMin, selMax);
			float w = (float)fm.getStringBounds(s, g2).getWidth();
			g2.setColor(painter.colorSelection);
			g2.fillRect((int)x, (int)(y-lh+descent), (int)(x+w)-(int)x, (int)lh);
			g2.setColor(painter.colorSelectionFg);
			g2.drawString(s, x, y);
			x += w;
			if(selMax<text.length()) {
				g2.setColor(painter.colorFg);
				s = text.substring(selMax);
				g2.drawString(s, x, y);
			}
		}
		
		if(focused) {
			int cx = fm.stringWidth(text.substring(0, cursor));
			g2.setXORMode(Color.BLACK);
			g2.setColor(Color.WHITE);
			g2.fillRect((int)(x0+cx), (int)(y-lh+descent), (int)(2f/pix), (int)lh);
			g2.setPaintMode();
		}
		
		g2.setTransform(tx);
		
		g2.setColor(focused ? painter.colorSelection : hover ? painter.colorFg : painter.colorBorder);
		g2.drawRect(0, 0, (int)getWidth(), (int)getHeight());
	}

	public boolean isFocused() {
		return getBasePanel().getFocus()==this;
	}
	
	public void deselect() {
		selStart = -1;
		selEnd = -1;
		updateSelRange();
	}

	public void selectAll() {
		selStart = 0;
		selEnd = text.length();
		cursor = selEnd;
		updateSelRange();
	}
	
	private void startSelection() {
		if(selStart<0) {
			selStart = cursor;
			selEnd = cursor;
			updateSelRange();
		}
	}
	
	private void modifySelection(int keepStart) {
		if(selStart>=0) {
			selEnd = cursor;
			if(selStart==selEnd && keepStart<0)
				deselect();
			updateSelRange();
		}
	}
	
	private void modifySelection() {
		modifySelection(-1);
	}
	
	private void updateSelRange() {
		if(selStart<0) {
			selMin = -1;
			selMax = -1;
		}
		else {
			if(selStart>selEnd) {
				selMin = selEnd;
				selMax = selStart;
			}
			else {
				selMin = selStart;
				selMax = selEnd;
			}
		}
	}

	public void deleteSelection() {
		if(selStart>=0) {
			cursor = selMin;
			modify(selMin, '\0', selMax);
			deselect();
		}
	}
	
	public void modify(int before, char add, int after) {
		if(add!='\0')
			text = text.substring(0, before) + add + text.substring(after);
		else
			text = text.substring(0, before) + text.substring(after);
	}
	
	private int searchCol(FontMetrics fm, float tx) {
		return bsearchCol(fm, tx, 0, 0, text.length(), fm.stringWidth(text));
	}
	
	private int bsearchCol(FontMetrics fm, float tx, int c0, int w0, int c1, int w1) {
		if(tx<=w0)
			return c0;
		if(tx>=w1)
			return c1;
		if(c1-c0==1) {
			if((tx-w0)*3f < (w1-tx))
				return c0;
			else
				return c1;
		}
		else {
			float s = (tx-w0) / (float)(w1-w0);
			int c = c0+(int)(s*(c1-c0));
			if(c==c0) c = c0+1;
			if(c==c1) c = c1-1;
			int w = fm.stringWidth(text.substring(0, c));
			if(tx<=w)
				return bsearchCol(fm, tx, c0, w0, c, w);
			else
				return bsearchCol(fm, tx, c, w, c1, w1);
		}
	}
	
	@Override
	protected void onMouseIn() {
		hover = true;
		getBasePanel().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		requestRepaint();
	}
	
	@Override
	protected void onMouseOut() {
		hover = false;
		getBasePanel().setCursor(Cursor.getDefaultCursor());
		requestRepaint();
	}
	
	@Override
	protected boolean onMouseDown(float x, float y, int buttons) {
		if(buttons==mouseLeftMask) {
			if(isFocused()) {
				cursorX = parentToLocalX(x);
				updateCursor = true;
				deselect();
				requestRepaint();
			}
			else {
				getBasePanel().setFocus(this);
			}
			return true;
		}
		else
			return false;
	}
	
	@Override
	public DragActor acceptDrag(int x, int y, int buttons) {
		// FIXME initial drag
		if(dragSelectActor.notifyMouseDown(x, y, buttons))
			return dragSelectActor;
		else
			return null;
	}

	@Override
	public boolean onKey(char c, int code, int modifiers) {
		switch(code) {
			case KeyEvent.VK_LEFT:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor>0)
					cursor--;
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_RIGHT:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor<text.length())
					cursor++;
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_HOME:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				cursor = 0;
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_END:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				cursor = text.length();
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_BACK_SPACE:
				if(selStart>=0) {
					deleteSelection();
				}
				else if(cursor>0) {
					modify(cursor-1, '\0', cursor);
					cursor--;
				}
				break;
			case KeyEvent.VK_DELETE:
				if(selStart>=0) {
					deleteSelection();
				}
				else if(cursor<text.length()) {
					modify(cursor, '\0', cursor+1);
				}
				break;
			case KeyEvent.VK_ENTER:
				getBasePanel().resetFocus();
				break;
			default: {
				if(!Character.isISOControl(c) && c!=KeyEvent.CHAR_UNDEFINED) {
					if(selStart>=0)
						deleteSelection();
					modify(cursor, c, cursor);
					cursor++;
				}
			}
		}
		requestRepaint();
		return true;
	}

	@Override
	public void onFocus() {
		selectAll();
		cursor = text.length();
		requestRepaint();
	}

	@Override
	public void onFocusLost() {
		deselect();
		requestRepaint();
	}
}

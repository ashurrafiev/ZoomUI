package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.std.UIListItem;
import com.xrbpowered.zoomui.std.UIScrollContainer;
import com.xrbpowered.zoomui.std.UITextBox;
import com.xrbpowered.zoomui.swing.SwingFrame;

public class UITextEdit extends UIElement implements KeyInputHandler {

	private static final String TEST_INPUT = "src_samples/com/xrbpowered/uitest/UITextEdit.java";
	
	public static Font font = new Font("Verdana", Font.PLAIN, GraphAssist.ptToPixels(10f));

	public static Color colorBackground = UITextBox.colorBackground;
	public static Color colorHighlight = UIListItem.colorHighlight;
	public static Color colorText = UITextBox.colorText;
	public static Color colorSelection = UITextBox.colorSelection;
	public static Color colorSelectedText = UITextBox.colorSelectedText;
	public static Color colorBorder = UITextBox.colorBorder;

	public static class Position {
		public int line;
		public int col;
		
		public Position(Position pos) {
			set(pos);
		}
		
		public Position(int line, int col) {
			this.line = line;
			this.col = col;
		}
		
		public void set(Position pos) {
			this.line = pos.line;
			this.col = pos.col;
		}
		
		public boolean equals(Position pos) {
			return this.line==pos.line && this.col==pos.col;
		}
	}
	
	private DragActor dragSelectActor = new DragActor() {
		private float x, y;
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				dragSelecting = true;
				this.x = baseToLocalX(x);
				this.y = baseToLocalX(y);
				cursorX = this.x;
				cursor.line = (int)(this.y / pixelScale / lineHeight)+displayLine; // FIXME displayLine rounding error?
				updateCursor = true;
				startSelection();
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			x += dx * getPixelScale();
			y += dy * getPixelScale();
			cursorX = x;
			cursor.line = (int)(y / pixelScale / lineHeight)+displayLine;
			updateCursor = true;
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
			dragSelecting = false;
			return true;
		}
	};
	
	private float pixelScale = 0;
	private int lineHeight = 0;
	private int tabWidth = 0;
	
	private int displayLine = 0;
	
	private ArrayList<String> lines = new ArrayList<>();
	
	private Position cursor = new Position(0, 0);
	private Position selStart = null;
	private Position selEnd = null;
	private Position selMin = null;
	private Position selMax = null;
	
	private float cursorX;
	private boolean updateCursor = false;
	private boolean dragSelecting = false;
	
	public UITextEdit(UIContainer parent) {
		super(parent);
	}
	
	public void setText(String text) {
		String[] ls = text.split("\\r?\\n");
		lines.clear();
		for(String l: ls)
			lines.add(l);
	}
	
	@Override
	public boolean isVisible(Rectangle clip) {
		return isVisible();
	}

	@Override
	public void paint(GraphAssist g) {
		boolean focused = isFocused();
		
		g.pushTx();
		g.clearTransform();
		g.translate(g.getTx().getTranslateX(), g.getTx().getTranslateY());
		float pix = getPixelScale();
		
		g.setFont(font.deriveFont(font.getSize()/pix));
		FontMetrics fm = g.getFontMetrics();
		//float h = fm.getAscent() - fm.getDescent();
		int lh = fm.getHeight();
		int descent = fm.getDescent();
		int y = lh*(1+displayLine); //-descent;
		float height = y+getParent().getHeight()/pix;
		float width = getWidth()/pix;
		tabWidth = fm.stringWidth("    ");
		int leftWidth = fm.stringWidth(Integer.toString(lines.size()))+(int)(8/pix);
		int x0 = leftWidth+(int)(4/pix);
		g.fillRect(0, y-lh, width, descent, colorBackground);
		g.fillRect(0, y-lh, leftWidth, height, new Color(0xf2f2f2));
		g.fillRect(leftWidth, y-lh, x0-leftWidth, height, colorBackground);

		for(int lineIndex = displayLine; lineIndex<lines.size() && y-lh<height;) {
			String line = lines.get(lineIndex);

			if(updateCursor && cursor.line==lineIndex) {
				cursor.col = searchCol(fm, line, cursorX/pix-x0);
				updateCursor = false;
				if(dragSelecting)
					modifySelection(selStart);
			}
			
			Color bg = lineIndex==cursor.line ? colorHighlight : colorBackground;
			
			if(selMin==null || lineIndex<selMin.line || lineIndex>selMax.line) {
				int x = drawText(g, fm, line, x0, y, 0, line.length(), bg, colorText, lh, descent);
				g.fillRect(x, y-lh+descent, width-x, lh, bg);
			}
			else {
				int x = x0;
				int col = 0;
				int len = line.length();
				if(lineIndex==selMin.line && selMin.col>0) {
					col = Math.min(selMin.col, len);
					x = drawText(g, fm, line, x, y, 0, col, bg, colorText, lh, descent);
				}
				if(lineIndex==selMax.line && selMax.col<len) {
					if(col<selMax.col)
						x = drawText(g, fm, line, x, y, col, selMax.col, colorSelection, colorSelectedText, lh, descent);
					x = drawText(g, fm, line, x, y, selMax.col, len, bg, colorText, lh, descent);
					g.fillRect(x, y-lh+descent, width-x, lh, bg);
				}
				else {
					x = drawText(g, fm, line, x, y, col, len, colorSelection, colorSelectedText, lh, descent);
					g.fillRect(x, y-lh+descent, width-x, lh, lineIndex<selMax.line ? colorSelection : bg);
				}
			}
			
			if(focused && cursor.line==lineIndex) {
				int cx = stringWidth(fm, line, 0, cursor.col);
				g.graph.setXORMode(Color.BLACK);
				g.fillRect(x0+cx, y-lh+descent, 2f/pix, lh, Color.WHITE);
				g.graph.setPaintMode();
				if(cursorX<0)
					cursorX = (x0+cx)*pix;
			}
			
			y += lh;
			lineIndex++;
		}
		y = lh*(1+displayLine); //-descent;
		g.line(leftWidth, y-lh, leftWidth, y-lh+height, new Color(0xdddddd));
		g.setColor(new Color(0xbbbbbb));
		for(int lineIndex = displayLine; lineIndex<lines.size() && y-lh<height;) {
			g.drawString(Integer.toString(lineIndex+1), leftWidth-4/pix, y, GraphAssist.RIGHT, GraphAssist.BOTTOM);
			y += lh;
			lineIndex++;
		}
		
		if(lh!=lineHeight) {
			lineHeight = lh;
			invalidateLayout();
		}
		if(pix!=pixelScale) {
			pixelScale = pix;
			invalidateLayout();
		}
		
		g.popTx();
	}
	
	private int drawString(GraphAssist g, FontMetrics fm, String s, int x, int y, Color bg, Color fg, int lh, int descent) {
		int w = fm.stringWidth(s);
		g.fillRect(x, y-lh+descent, (int)(x+w)-(int)x, lh, bg);
		g.setColor(fg);
		g.drawString(s, x, y);
		return x + w;
	}
	
	private int drawText(GraphAssist g, FontMetrics fm, String text, int x0, int y, int c0, int c1, Color bg, Color fg, int lh, int descent) {
		int x = x0;
		int col = c0;
		for(;;) {
			int t = text.indexOf('\t', col);
			if(t<0 || t>=c1) {
				if(col<c1) {
					String s = text.substring(col, c1);
					x = drawString(g, fm, s, x, y, bg, fg, lh, descent);
				}
				return x;
			}
			else {
				if(t>col) {
					String s = text.substring(col, t);
					x = drawString(g, fm, s, x, y, bg, fg, lh, descent);
				}
				g.fillRect(x, y-lh+descent, (int)(x+tabWidth)-(int)x, lh, bg);
				x += tabWidth;
				col = t+1;
			}
		}
	}
	
	private int stringWidth(FontMetrics fm, String text, int c0, int c1) {
		int x = 0;
		int col = c0;
		for(;;) {
			int t = text.indexOf('\t', col);
			if(t<0 || t>=c1) {
				if(col<c1) {
					String s = text.substring(col, c1);
					x += fm.stringWidth(s);
				}
				return x;
			}
			else {
				if(t>col) {
					String s = text.substring(col, t);
					x += fm.stringWidth(s);
				}
				x += tabWidth;
				col = t+1;
			}
		}
	}
	
	private int stringWidth(FontMetrics fm, String text) {
		return stringWidth(fm, text, 0, text.length());
	}
	
	private int searchCol(FontMetrics fm, String text, float tx) {
		return bsearchCol(fm, text, tx, 0, 0, text.length(), stringWidth(fm, text));
	}
	
	private int bsearchCol(FontMetrics fm, String text, float tx, int c0, int w0, int c1, int w1) {
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
			int w = stringWidth(fm, text, 0, c);
			if(tx<=w)
				return bsearchCol(fm, text, tx, c0, w0, c, w);
			else
				return bsearchCol(fm, text, tx, c, w, c1, w1);
		}
	}
	
	public void deselect() {
		selStart = null;
		selEnd = null;
		updateSelRange();
	}
	
	public void selectAll() {
		selStart = new Position(0, 0);
		selEnd = new Position(lines.size(), lines.get(lines.size()-1).length());
		cursor.set(selEnd);
		updateSelRange();
	}
	
	private void startSelection() {
		if(selStart==null) {
			selStart = new Position(cursor);
			selEnd = new Position(cursor);
			updateSelRange();
		}
	}
	
	private void modifySelection(Position keepStart) {
		if(selStart!=null) {
			selEnd.set(cursor);
			if(selStart.equals(selEnd) && keepStart==null)
				deselect();
			updateSelRange();
		}
	}
	
	private void modifySelection() {
		modifySelection(null);
	}
	
	private void updateSelRange() {
		if(selStart==null) {
			selMin = null;
			selMax = null;
		}
		else {
			if(selStart.line>selEnd.line || selStart.line==selEnd.line && selStart.col>selEnd.col) {
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
		if(selStart!=null) {
			cursor.set(selMin);
			if(selMin.line==selMax.line) {
				String line = lines.get(selMin.line);
				lines.set(selMin.line, modify(line, selMin.col, '\0', selMax.col));
			}
			else {
				String line = lines.get(selMin.line);
				String s = line.substring(0, selMin.col);
				selMin.line++;
				while(selMax.line>selMin.line) {
					lines.remove(selMin.line);
					selMax.line--;
				}
				s += lines.get(selMax.line).substring(selMax.col);
				lines.remove(selMax.line);
				lines.set(selMin.line-1, s);
			}
			deselect();
		}
	}
	
	private void joinLineWithNext(int index) {
		String line = lines.get(index);
		String next = lines.get(index+1);
		lines.set(index, line+next);
		lines.remove(index+1);
	}
	
	public String modify(String text, int before, char add, int after) {
		if(add!='\0')
			return text.substring(0, before) + add + text.substring(after);
		else
			return text.substring(0, before) + text.substring(after);
	}

	public void modify(int line, int before, char add, int after) {
		lines.set(line, modify(lines.get(line), before, add, after));
	}

	@Override
	public boolean onKeyPressed(char c, int code, int modifiers) {
		switch(code) {
			case KeyEvent.VK_LEFT:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else {
					if(selMin!=null)
						cursor.set(selMin);
					deselect();
				}
				if(cursor.col>0) {
					if(cursor.col>lines.get(cursor.line).length())
						cursor.col = lines.get(cursor.line).length();
					cursor.col--;
					cursorX = -1;
				}
				else if(cursor.line>0) {
					cursor.line--;
					//if(cursor.line<displayLine)
					//	displayLine--; // TODO scroll to cursor
					cursor.col = lines.get(cursor.line).length();
				}
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_RIGHT:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else {
					if(selMax!=null)
						cursor.set(selMax);
					deselect();
				}
				if(cursor.col<lines.get(cursor.line).length()) {
					cursor.col++;
					cursorX = -1;
					if(modifiers==UIElement.modShiftMask)
						modifySelection();
				}
				else if(cursor.line<lines.size()) {
					cursor.line++;
					//if(cursor.line>=displayLine+numDisplayedLines-1)
					//	displayLine++; // TODO scroll to cursor
					cursor.col = 0;
				}
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_UP:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line>0) {
					cursor.line--;
					//if(cursor.line<displayLine)
					//	displayLine--; // TODO scroll to cursor
					//cursor.col = getColForX(lines.get(cursor.line), cursorX);
					updateCursor = true; // FIXME instant update cursor
				}
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_DOWN:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line<lines.size()) {
					cursor.line++;
					//if(cursor.line>=displayLine+numDisplayedLines-1)
					//	displayLine++; // TODO scroll to cursor
					//cursor.col = getColForX(lines.get(cursor.line), cursorX);
					updateCursor = true; // FIXME instant update cursor
				}
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_HOME:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				cursor.col = 0;
				cursorX = -1;
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_END:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				cursor.col = lines.get(cursor.line).length();
				cursorX = -1;
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
			case KeyEvent.VK_BACK_SPACE:
				if(selStart!=null) {
					deleteSelection();
				}
				else {
					if(cursor.col>0) {
						modify(cursor.line, cursor.col-1, '\0', cursor.col);
						cursor.col--;
					}
					else if(cursor.line>0) {
						cursor.col = lines.get(cursor.line-1).length();
						joinLineWithNext(cursor.line-1);
						cursor.line--;
					}
				}
				break;
			case KeyEvent.VK_DELETE:
				if(selStart!=null) {
					deleteSelection();
				}
				else {
					if(cursor.col<lines.get(cursor.line).length()) {
						modify(cursor.line, cursor.col, '\0', cursor.col+1);
					}
					else if(cursor.line<lines.size()-1) {
						joinLineWithNext(cursor.line);
					}
				}
				break;
			case KeyEvent.VK_ENTER:
				if(selStart!=null)
					deleteSelection();
				String line = lines.get(cursor.line);
				lines.set(cursor.line, line.substring(0, cursor.col));
				String next = line.substring(cursor.col);
				cursor.line++;
				lines.add(cursor.line, next);
				cursor.col = 0;
				break;
			case KeyEvent.VK_TAB:
				modify(cursor.line, cursor.col, '\t', cursor.col);
				cursor.col++;
				break;
			default: {
				if(!Character.isISOControl(c) && c!=KeyEvent.CHAR_UNDEFINED) {
					if(selStart!=null)
						deleteSelection();
					modify(cursor.line, cursor.col, c, cursor.col);
					cursor.col++;
				}
			}	
		}
		repaint();
		return true;
	}

	@Override
	public void onMouseIn() {
		getBase().getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		super.onMouseIn();
	}
	
	@Override
	public void onMouseOut() {
		getBase().getWindow().setCursor(Cursor.getDefaultCursor());
		super.onMouseOut();
	}
	
	@Override
	public boolean onMouseDown(float x, float y, Button button, int mods) {
		if(button==Button.left) {
			if(!isFocused())
				getBase().setFocus(this);
			deselect();
			cursorX = parentToLocalX(x);
			cursor.line = (int)(parentToLocalY(y) / pixelScale / lineHeight);
			updateCursor = true;
			repaint();
			return true;
		}
		else
			return false;
	}
	
	@Override
	public DragActor acceptDrag(float x, float y, Button button, int mods) {
		if(dragSelectActor.notifyMouseDown(x, y, button, mods))
			return dragSelectActor;
		else
			return null;
	}
	
	public boolean isFocused() {
		return getBase().getFocus()==this;
	}
	
	@Override
	public void onFocusGained() {
		repaint();
	}

	@Override
	public void onFocusLost() {
		deselect();
		repaint();
	}

	private static class UITextEditScrollPane extends UIScrollContainer {
		public final UITextEdit edit;
		public UITextEditScrollPane(UIContainer parent) {
			super(parent);
			edit = new UITextEdit(getView());
		}
		@Override
		protected float layoutView() {
			edit.setLocation(0, 0);
			float h = edit.lines.size()*edit.lineHeight*edit.pixelScale;
			edit.setSize(getWidth(), h);
			return h;
		}
		@Override
		public void paint(GraphAssist g) {
			super.paint(g);
		}
		@Override
		protected void paintChildren(GraphAssist g) {
			if(edit.lineHeight>0)
				edit.displayLine = (int)(getView().getPanY() / edit.pixelScale / edit.lineHeight);
			super.paintChildren(g);
			g.hborder(this, GraphAssist.TOP, colorBorder);
		}
	}
	
	public static byte[] loadBytes(InputStream s) throws IOException {
		DataInputStream in = new DataInputStream(s);
		byte bytes[] = new byte[in.available()];
		in.readFully(bytes);
		in.close();
		return bytes;
	}
	
	public static String loadString(String path) {
		try {
			return new String(loadBytes(new FileInputStream(path)));
		} catch(IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void main(String[] args) {
		UIWindow frame = new SwingFrame("UITextEdit", 800, 600) {
			@Override
			public boolean onClosing() {
				confirmClosing();
				return false;
			}
		};
		new UITextEditScrollPane(frame.getContainer()).edit.setText(loadString(TEST_INPUT));
		frame.show();
	}

}
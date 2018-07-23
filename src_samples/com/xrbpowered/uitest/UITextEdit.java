package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIPanView;
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

	public static final Pattern newlineRegex = Pattern.compile("\\r?\\n");
	public static final Pattern indentRegex = Pattern.compile("\\s*");
	public static String newline = System.lineSeparator();
	
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
	
	public class Line {
		public int offs, length;
		public int width = -1;
		
		public int calcStart() {
			int pos = 0;
			for(Line line: lines) {
				if(line==this)
					return pos+offs;
				pos += line.offs+line.length;
			}
			return 0;
		}
	}
	
	private DragActor dragSelectActor = new DragActor() {
		private float x, y;
		@Override
		public boolean notifyMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				this.x = baseToLocalX(x);
				this.y = baseToLocalY(y);
				cursorToMouse(this.x, this.y);
				startSelection();
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			x += dx * getPixelScale();
			y += dy * getPixelScale();
			cursorToMouse(this.x, this.y);
			scrollToCursor();
			modifySelection(selStart);
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
			return true;
		}
	};
	
	public boolean autoIndent = true;
	
	private String text = "";
	private ArrayList<Line> lines = new ArrayList<>();
	
	private Position cursor = new Position(0, 0);
	private Position selStart = null;
	private Position selEnd = null;
	private Position selMin = null;
	private Position selMax = null;
	
	private float cursorX;
	private Line cursorLine = null;
	private int cursorLineStart = -1;
	private int cursorLineIndex = -1;

	private int displayLine = 0;
	private float pixelScale = 0;
	private int lineHeight = 0;
	private int descent = 0;
	private int page = 0;
	private int tabWidth = 0;
	private int x0, y0;
	private Rectangle clipBounds = new Rectangle();
	private int minx, maxx, maxy;
	private boolean updateSize = false;
	
	private FontMetrics fm = null;
	
	public UITextEdit(UIPanView parent) {
		super(parent);
	}
	
	public UIPanView panView() {
		return (UIPanView) getParent();
	}
	
	public void scrollToCursor() {
		float panx = panView().getPanX();
		float pany = panView().getPanY();
		
		checkCursorLineCache();
		int cx = x0+stringWidth(cursorLineStart, cursorLineStart+cursor.col);
		if(cx<minx)
			panx = cx*pixelScale;
		else if(cx+4+x0>maxx) // FIXME progress bar width
			panx += (cx+4+x0-maxx)*pixelScale;
		
		int topLine = (int)(pany / pixelScale / lineHeight);
		if(topLine>=cursor.line)
			pany = (cursor.line*lineHeight+descent)*pixelScale;
		else if(topLine+page<=cursor.line) {
			float dy = getParent().getHeight()/pixelScale-descent-page*lineHeight;
			pany = ((cursor.line-page)*lineHeight+dy)*pixelScale;  // FIXME error in this branch
		}
		
		panView().setPan(panx, pany);
		repaint();
	}
	
	public void setText(String text) {
		this.text = text;
		lines.clear();
		Matcher m = newlineRegex.matcher(text);
		Line line = new Line();
		lines.add(line);
		line.offs = 0;
		int pos = 0;
		while(m.find()) {
			line.length = m.start()-pos;
			line = new Line();
			lines.add(line);
			pos = m.end();
			line.offs = pos-m.start();
		}
		line.length = text.length()-pos;
		cursorLine = null;
	}
	
	@Override
	public boolean isVisible(Rectangle clip) {
		return isVisible();
	}

	private void updateMetrics(GraphAssist g) {
		fm = g.getFontMetrics();
		lineHeight = fm.getHeight();
		
		descent = fm.getDescent();
		tabWidth = fm.stringWidth("    ");
		y0 = lineHeight*(1+displayLine); //-descent;
		x0 = (int)(4/pixelScale);
		g.graph.getClipBounds(clipBounds);
		minx = (int)Math.floor(clipBounds.getMinX());
		maxx = (int)Math.ceil(clipBounds.getMaxX());
		maxy = (int)Math.ceil(clipBounds.getMaxY());
		page = (maxy-y0)/lineHeight;
	}
	
	@Override
	public void paint(GraphAssist g) {
		boolean focused = isFocused();
		if(lineHeight>0)
			displayLine = (int)(panView().getPanY() / pixelScale / lineHeight);
		
		Object aa = g.graph.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
		g.pushTx();
		g.clearTransform();
		g.translate(g.getTx().getTranslateX(), g.getTx().getTranslateY());
		pixelScale = getPixelScale();
		
		g.setFont(font.deriveFont(font.getSize()/pixelScale));
		updateMetrics(g);
		
		g.fillRect(minx, y0-lineHeight, maxx, descent, colorBackground);
		if(x0>minx)
			g.fillRect(minx, y0-lineHeight, x0-minx, maxy-y0+lineHeight, colorBackground);

		int y = y0;
		int pos = 0;
		float w = 0;
		int lineIndex = 0;
		for(Line line : lines) {
			int lineStart = pos+line.offs;
			int lineEnd = lineStart+line.length;
			if(line.width<0)
				line.width = stringWidth(lineStart, lineEnd);
			if(line.width>w)
				w = line.width;
			
			if(lineIndex>=displayLine && y-lineHeight<maxy) {
				Color bg = (lineIndex==cursor.line && focused) ? colorHighlight : colorBackground;
				
				int x = x0;
				if(selMin==null || lineIndex<selMin.line || lineIndex>selMax.line) {
					x = drawText(g, x, y, lineStart, lineEnd, bg, colorText);
					drawRemainder(g, x, y, bg);
				}
				else {
					int col = lineStart;
					if(lineIndex==selMin.line && selMin.col>0) {
						col = Math.min(lineStart+selMin.col, lineEnd);
						x = drawText(g, x, y, lineStart, col, bg, colorText);
					}
					if(lineIndex==selMax.line && selMax.col<line.length) {
						int cmax = lineStart+selMax.col;
						if(col<cmax)
							x = drawText(g, x, y, col, cmax, colorSelection, colorSelectedText);
						x = drawText(g, x, y, cmax, lineEnd, bg, colorText);
						drawRemainder(g, x, y, bg);
					}
					else {
						x = drawText(g, x, y, col, lineEnd, colorSelection, colorSelectedText);
						drawRemainder(g, x, y, lineIndex<selMax.line ? colorSelection : bg);
					}
				}
				
				if(focused && cursor.line==lineIndex) {
					int cx = stringWidth(lineStart, lineStart+cursor.col);
					g.graph.setXORMode(Color.BLACK);
					g.fillRect(x0+cx, y-lineHeight+descent, 2f/pixelScale, lineHeight, Color.WHITE);
					g.graph.setPaintMode();
					if(cursorX<0)
						cursorX = (x0+cx)*pixelScale;
				}
				
				y += lineHeight;
			}
			
			pos = lineEnd;
			lineIndex++;
		}
		if(y-lineHeight<maxy)
			g.fillRect(minx, y-lineHeight+descent, maxx, maxy-y+lineHeight-descent, colorBackground);
		
		w = (w+x0*2)*pixelScale+4; // FIXME progress bar width
		float h = (lineHeight*lines.size()+descent)*pixelScale;
		if(updateSize || getWidth()!=w || getHeight()!=h) {
			panView().setPanRangeForClient(w, h);
			if(w<getParent().getWidth())
				w = getParent().getWidth();
			if(h<getParent().getHeight())
				h = getParent().getHeight();
			setSize(w, h);
			updateSize = false;
		}
		
		g.popTx();
		g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
	}
	
	private void drawRemainder(GraphAssist g, int x, int y, Color bg) {
		if(x<maxx)
			g.fillRect(x, y-lineHeight+descent, maxx-x, lineHeight, bg);
	}
	
	private int drawString(GraphAssist g, String s, int x, int y, Color bg, Color fg) {
		int w = fm.stringWidth(s);
		if(x<maxx && x+w>minx) {
			g.fillRect(x, y-lineHeight+descent, w, lineHeight, bg);
			g.setColor(fg);
			g.drawString(s, x, y);
		}
		return x + w;
	}
	
	private int drawText(GraphAssist g, int x, int y, int c0, int c1, Color bg, Color fg) {
		int col = c0;
		for(;;) {
			int t = text.indexOf('\t', col);
			if(t<0 || t>=c1) {
				if(col<c1) {
					String s = text.substring(col, c1);
					x = drawString(g, s, x, y, bg, fg);
				}
				return x;
			}
			else {
				if(t>col) {
					String s = text.substring(col, t);
					x = drawString(g, s, x, y, bg, fg);
				}
				int w = ((x-x0)+tabWidth)/tabWidth*tabWidth-(x-x0);
				g.fillRect(x, y-lineHeight+descent, w, lineHeight, bg);
				x += w;
				col = t+1;
			}
		}
	}
	
	private int stringWidth(int c0, int c1) {
		int x = 0;
		int col = c0;
		c1 = Math.min(c1, text.length());
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
				int w = (x+tabWidth)/tabWidth*tabWidth-x;
				x += w;
				col = t+1;
			}
		}
	}
	
	private int searchCol(float tx) {
		checkCursorLineCache();
		Line line = cursorLine;
		int lineStart = cursorLineStart;
		int lineEnd = lineStart+line.length;
		return bsearchCol(tx, lineStart, lineStart, 0, lineEnd, stringWidth(lineStart, lineEnd))-lineStart;
	}
	
	private int bsearchCol(float tx, int cstart, int c0, int x0, int c1, int x1) {
		if(tx<=x0)
			return c0;
		if(tx>=x1)
			return c1;
		if(c1-c0==1) {
			if((tx-x0)*3f < (x1-tx))
				return c0;
			else
				return c1;
		}
		else {
			float s = (tx-x0) / (float)(x1-x0);
			int c = c0+(int)(s*(c1-c0));
			if(c==c0) c = c0+1;
			if(c==c1) c = c1-1;
			int w = stringWidth(cstart, c);
			if(tx<=w)
				return bsearchCol(tx, cstart, c0, x0, c, w);
			else
				return bsearchCol(tx, cstart, c, w, c1, x1);
		}
	}
	
	private void checkCursorLineCache() {
		if(cursor.line!=cursorLineIndex || cursorLine==null) {
			cursorLine = lines.get(cursor.line);
			cursorLineIndex = cursor.line;
			cursorLineStart = cursorLine.calcStart();
		}
	}
	
	private void cursorToMouse(float x, float y) {
		cursor.line = (int)(y / pixelScale / lineHeight);
		if(cursor.line<0)
			cursor.line = 0;
		if(cursor.line>=lines.size())
			cursor.line = lines.size()-1;
		cursorX = x;
		updateCursor();
	}
	
	private void updateCursor() {
		cursor.col = searchCol(cursorX/pixelScale-x0);
	}
	
	public void deselect() {
		selStart = null;
		selEnd = null;
		updateSelRange();
	}
	
	public void selectAll() {
		selStart = new Position(0, 0);
		selEnd = new Position(lines.size()-1, lines.get(lines.size()-1).length);
		cursor.set(selEnd);
		updateSelRange();
	}
	
	public String getSelectedText() {
		if(selStart!=null) {
			int start = lines.get(selMin.line).calcStart()+selMin.col;
			int end = lines.get(selMax.line).calcStart()+selMax.col;
			return text.substring(start, end);
		}
		else
			return null;
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

	public void setCursor(int textPos) {
		int pos = 0;
		int lineIndex = 0;
		for(Line line : lines) {
			int nextPos = pos+line.offs+line.length;
			if(textPos<=nextPos) {
				cursor.line = lineIndex;
				cursor.col = textPos-pos-line.offs;
				return;
			}
			lineIndex++;
			pos = nextPos;
		}
	}
	
	public void copySelection() {
		String s = getSelectedText();
		if(s!=null) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection con = new StringSelection(s);
			clipboard.setContents(con, con);
		}
	}
	
	public void cutSelection() {
		copySelection();
		deleteSelection();
	}
	
	public void pasteAtCursor() {
		deleteSelection();
		checkCursorLineCache();
		int pos = cursorLineStart+cursor.col;

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				String add = (String) clipboard.getData(DataFlavor.stringFlavor);
				modify(pos, add, pos);
				setText(text);
				setCursor(pos+add.length());
				scrollToCursor();
			} catch(UnsupportedFlavorException | IOException e) {
			}
		}
	}
	
	public void deleteSelection() {
		if(selStart!=null) {
			cursor.set(selMin);
			if(selMin.line==selMax.line) {
				modify(selMin.line, selMin.col, "", selMax.col);
			}
			else {
				int start = lines.get(selMin.line).calcStart()+selMin.col;
				int end = lines.get(selMax.line).calcStart()+selMax.col;
				modify(start, "", end);
				setText(text);
			}
			deselect();
		}
	}
	
	public void indentSelection(String indent) {
		if(selStart!=null) {
			int indentLen = indent.length();
			Line startLine = lines.get(selMin.line);
			int pos = startLine.calcStart()-startLine.offs;
			for(int i=selMin.line; i<=selMax.line; i++) {
				Line line = lines.get(i);
				pos += line.offs;
				if(line.length>0 && !(i==selMax.line && selMax.col==0)) {
					modify(pos, indent, pos);
					line.length += indentLen;
				}
				pos += line.length;
			}
			if(selMin.col>0) selMin.col += indentLen;
			if(selMax.col>0) selMax.col += indentLen;
			if(cursor.col>0) cursor.col += indentLen;
		}
	}
	
	public void unindentSelection() {
		if(selStart!=null) {
			Line startLine = lines.get(selMin.line);
			int pos = startLine.calcStart()-startLine.offs;
			for(int i=selMin.line; i<=selMax.line; i++) {
				Line line = lines.get(i);
				pos += line.offs;
				if(line.length>0 && !(i==selMax.line && selMax.col==0) &&
						Character.isWhitespace(text.charAt(pos))) {
					modify(pos, "", pos+1);
					line.length--;
					if(i==selMin.line && selMin.col>0) selMin.col--;
					if(i==selMax.line && selMax.col>0) selMax.col--;
					if(i==cursor.line && cursor.col>0) cursor.col--;
				}
				pos += line.length;
			}
		}
	}
	
	private void joinLineWithNext() {
		checkCursorLineCache();
		Line line = cursorLine;
		int lineStart = cursorLineStart;
		Line next = lines.get(cursor.line+1);
		modify(lineStart+line.length, "", lineStart+line.length+next.offs);
		line.length += next.length;
		line.width = -1;
		lines.remove(cursor.line+1);
	}

	private int splitLineAtCursor() {
		checkCursorLineCache();
		Line line = cursorLine;
		int lineStart = cursorLineStart;
		int len = line.length;
		line.length = cursor.col;
		line.width = -1;
		
		String indent = "";
		int indentLen = 0;
		if(autoIndent) {
			Matcher m = indentRegex.matcher(text);
			if(m.find(lineStart)) {
				indent = m.group(0);
				indentLen = indent.length();
				if(cursor.col<indentLen) {
					indent = indent.substring(0, cursor.col);
					indentLen = cursor.col;
				}
			}
		}
		
		modify(lineStart+cursor.col, newline+indent, lineStart+cursor.col);
		Line next = new Line();
		next.offs = newline.length();
		next.length = len-cursor.col+indentLen;
		lines.add(cursor.line+1, next);
		
		return indentLen;
	}

	public int modify(int before, String add, int after) {
		text = text.substring(0, before) + add + text.substring(after);
		return before-after+add.length();
	}

	public void modify(int lineIndex, int before, String add, int after) {
		checkCursorLineCache();
		Line line = cursorLine;
		int lineStart = cursorLineStart;
		line.length += modify(lineStart+before, add, lineStart+after);
		line.width = -1;
	}

	protected boolean isCursorAtWordBoundary() {
		checkCursorLineCache();
		if(cursor.col==0 || cursor.col==cursorLine.length)
			return true;
		else {
			char ch = cursorLineStart+cursor.col==0 ? ' ' : text.charAt(cursorLineStart+cursor.col-1);
			char ch1 = text.charAt(cursorLineStart+cursor.col);
			return Character.isWhitespace(ch) && !Character.isWhitespace(ch1) ||
					(Character.isAlphabetic(ch) || Character.isDigit(ch))!=(Character.isAlphabetic(ch1) || Character.isDigit(ch1)) ||
					Character.isLowerCase(ch) && Character.isUpperCase(ch1);
		}
	}
	
	@Override
	public boolean onKeyPressed(char c, int code, int modifiers) {
		switch(code) {
			case KeyEvent.VK_LEFT:
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else {
					if(selMin!=null)
						cursor.set(selMin);
					deselect();
				}
				do {
					if(cursor.col>0) {
						if(cursor.col>lines.get(cursor.line).length)
							cursor.col = lines.get(cursor.line).length;
						cursor.col--;
						cursorX = -1;
					}
					else if(cursor.line>0) {
						cursor.line--;
						cursor.col = lines.get(cursor.line).length;
					}
				} while((modifiers&UIElement.modCtrlMask)>0 && !isCursorAtWordBoundary());
				scrollToCursor();
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_RIGHT:
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else {
					if(selMax!=null)
						cursor.set(selMax);
					deselect();
				}
				do {
					if(cursor.col<lines.get(cursor.line).length) {
						cursor.col++;
						cursorX = -1;
					}
					else if(cursor.line<lines.size()-1) {
						cursor.line++;
						cursor.col = 0;
					}
					scrollToCursor();
				} while((modifiers&UIElement.modCtrlMask)>0 && !isCursorAtWordBoundary());
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_UP:
				if(modifiers==UIElement.modCtrlMask) {
					panView().pan(0, lineHeight);
				}
				else {
					if(modifiers==UIElement.modShiftMask)
						startSelection();
					else
						deselect();
					if(cursor.line>0) {
						cursor.line--;
						updateCursor();
					}
					scrollToCursor();
					if(modifiers==UIElement.modShiftMask)
						modifySelection();
				}
				break;
				
			case KeyEvent.VK_DOWN:
				if(modifiers==UIElement.modCtrlMask) {
					panView().pan(0, -lineHeight);
				}
				else {
					if(modifiers==UIElement.modShiftMask)
						startSelection();
					else
						deselect();
					if(cursor.line<lines.size()-1) {
						cursor.line++;
						updateCursor();
					}
					scrollToCursor();
					if(modifiers==UIElement.modShiftMask)
						modifySelection();
				}
				break;
				
			case KeyEvent.VK_PAGE_UP:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line>page)
					cursor.line -= page;
				else
					cursor.line = 0;
				scrollToCursor();
				updateCursor();
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
				
			case KeyEvent.VK_PAGE_DOWN:
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line+page<=lines.size()-1)
					cursor.line += page;
				else
					cursor.line = lines.size()-1;
				scrollToCursor();
				updateCursor();
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
				
			case KeyEvent.VK_HOME:
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else
					deselect();
				if((modifiers&UIElement.modCtrlMask)>0) {
					cursor.line = 0;
					scrollToCursor();
				}
				cursor.col = 0;
				cursorX = -1;
				scrollToCursor();
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_END:
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else
					deselect();
				if((modifiers&UIElement.modCtrlMask)>0) {
					cursor.line = lines.size()-1;
					scrollToCursor();
				}
				cursor.col = lines.get(cursor.line).length;
				cursorX = -1;
				scrollToCursor();
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_BACK_SPACE:
				if(selStart!=null) {
					deleteSelection();
				}
				else {
					if(cursor.col>0) {
						modify(cursor.line, cursor.col-1, "", cursor.col);
						cursor.col--;
						scrollToCursor();
					}
					else if(cursor.line>0) {
						cursor.col = lines.get(cursor.line-1).length;
						cursor.line--;
						joinLineWithNext();
						scrollToCursor();
					}
				}
				break;
				
			case KeyEvent.VK_DELETE:
				if(selStart!=null) {
					deleteSelection();
				}
				else {
					if(cursor.col<lines.get(cursor.line).length) {
						modify(cursor.line, cursor.col, "", cursor.col+1);
					}
					else if(cursor.line<lines.size()-1) {
						joinLineWithNext();
					}
				}
				break;
				
			case KeyEvent.VK_ENTER:
				if(selStart!=null)
					deleteSelection();
				cursor.col = splitLineAtCursor();
				cursor.line++;
				scrollToCursor();
				break;
				
			case KeyEvent.VK_TAB:
				if(selStart==null) {
					if(modifiers==UIElement.modNone) {
						modify(cursor.line, cursor.col, "\t", cursor.col);
						cursor.col++;
						scrollToCursor();
					}
				}
				else {
					if(modifiers==UIElement.modShiftMask)
						unindentSelection();
					else
						indentSelection("\t");
				}
				break;
				
			default: {
				if(modifiers==UIElement.modCtrlMask) {
					switch(code) {
						case KeyEvent.VK_A:
							selectAll();
							break;
						case KeyEvent.VK_X:
							cutSelection();
							break;
						case KeyEvent.VK_C:
							copySelection();
							break;
						case KeyEvent.VK_V:
							pasteAtCursor();
							break;
					}
				}
				else {
					if(!Character.isISOControl(c) && c!=KeyEvent.CHAR_UNDEFINED) {
						if(selStart!=null)
							deleteSelection();
						modify(cursor.line, cursor.col, Character.toString(c), cursor.col);
						cursor.col++;
						scrollToCursor();
					}
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
			cursorToMouse(x, y);
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
			edit.updateSize = true;
			return edit.getHeight();
		}
		@Override
		public void paint(GraphAssist g) {
			super.paint(g);
		}
		@Override
		protected void paintChildren(GraphAssist g) {
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
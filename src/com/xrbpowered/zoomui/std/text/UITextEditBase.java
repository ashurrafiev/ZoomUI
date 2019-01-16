package com.xrbpowered.zoomui.std.text;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIHoverElement;
import com.xrbpowered.zoomui.UIPanView;
import com.xrbpowered.zoomui.std.History;
import com.xrbpowered.zoomui.std.UIListItem;

public class UITextEditBase extends UIHoverElement implements KeyInputHandler {
	
	public static final Pattern newlineRegex = Pattern.compile("\\r?\\n");
	public static final Pattern indentRegex = Pattern.compile("\\s*");
	public static String newline = System.lineSeparator();

	public Font font = UITextBox.font;

	public Color colorBackground = UITextBox.colorBackground;
	public Color colorHighlight = UIListItem.colorHighlight;
	public Color colorText = UITextBox.colorText;
	public Color colorSelection = UITextBox.colorSelection;
	public Color colorSelectedText = UITextBox.colorSelectedText;
	
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
	
	public static Position copyPosition(Position pos) {
		return pos==null ? null : new Position(pos);
	}
	
	public class Line {
		public int offs, length;
		public int width = -1;
		
		public int calcStart() {
			int pos = 0;
			for(Line line: lines) {
				if(line==this)
					return pos+line.offs;
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
				checkPushHistory(HistoryAction.unspecified);
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
			modifySelection(true);
			repaint();
			return true;
		}

		@Override
		public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
			return true;
		}
	};

	private class HistoryState {
		public final Position cursor;
		public final Position selStart;
		public final Position selEnd;
		public final String text;
		
		public HistoryState() {
			text = UITextEditBase.this.text;
			cursor = new Position(UITextEditBase.this.cursor);
			selStart = copyPosition(UITextEditBase.this.selStart);
			selEnd = copyPosition(UITextEditBase.this.selEnd);
		}
		
		public void restore() {
			setText(text, false);
			UITextEditBase.this.cursor.set(cursor);
			UITextEditBase.this.selStart = copyPosition(selStart);
			UITextEditBase.this.selEnd = copyPosition(selEnd);
			updateSelRange();
			scrollToCursor();
		}
	}
	
	private enum HistoryAction {
		unspecified, typing, deleting
	}
	
	private HistoryAction historyAction = HistoryAction.unspecified;
	public History<HistoryState> history = new History<HistoryState>(32) {
		@Override
		protected void apply(HistoryState item) {
			item.restore();
		}
		@Override
		public void push() {
			push(new HistoryState());
		}
	};

	public boolean autoIndent = true;
	public final boolean singleLine;
	
	private String text;
	private ArrayList<Line> lines = new ArrayList<>();
	
	private final Position cursor = new Position(0, 0);
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
	
	public UITextEditBase(UIPanView parent, boolean singleLine) {
		super(parent);
		this.singleLine = singleLine;
		setText("");
	}
	
	public void updateSize() {
		this.updateSize = true;
	}
	
	public UIPanView panView() {
		return (UIPanView) getParent();
	}
	
	public void scrollToCursor() {
		float panx = panView().getPanX();
		float pany = panView().getPanY();
		
		checkCursorLineCache();
		int cx = x0+stringWidth(cursorLineStart, cursorLineStart+cursor.col);
		if(cx-x0<minx)
			panx = (cx-x0)*pixelScale;
		else if(cx+x0>maxx)
			panx += (cx+x0-maxx)*pixelScale; // FIXME error in this branch
		
		if(singleLine) {
			pany = 0;
		}
		else {
			if(displayLine>cursor.line)
				pany = cursor.line*lineHeight*pixelScale;
			else if(displayLine+page<=cursor.line) {
				pany = lineHeight*(cursor.line+1)*pixelScale - getParent().getHeight();
			}
		}
		
		panView().setPan(panx, pany);
	}

	public void setText(String text) {
		setText(text, true);
	}
	
	public String getText() {
		return text;
	}

	private void setText(String text, boolean resetHistory) {
		lines.clear();
		Matcher m = newlineRegex.matcher(text);
		if(singleLine) {
			this.text = m.replaceAll("").replaceAll("\\t", "");
			Line line = new Line();
			lines.add(line);
			line.offs = 0;
			line.length = this.text.length();
		}
		else {
			this.text = text;
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
		}
		cursorLine = null;
		
		if(resetHistory) {
			history.clear();
			history.push();
		}
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
		y0 = lineHeight*(1+displayLine)-descent;
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
		if(lineHeight>0 && !singleLine)
			displayLine = (int)(panView().getPanY() / pixelScale / lineHeight);
		
		pixelScale = g.startPixelMode(this);
		
		g.setFont(font.deriveFont(font.getSize()/pixelScale));
		updateMetrics(g);
		
		if(singleLine) {
			g.fillRect(minx, y0-lineHeight, maxx-minx, maxy-y0+lineHeight, colorBackground);
		}
		else {
			g.fillRect(minx, y0-lineHeight, maxx, descent, colorBackground);
			if(x0>minx)
				g.fillRect(minx, y0-lineHeight, x0-minx, maxy-y0+lineHeight, colorBackground);
		}

		int y = singleLine ? (int)(getParent().getHeight()/pixelScale/2f+(fm.getAscent()-fm.getDescent())/2f) : y0;
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
			
			if(singleLine || lineIndex>=displayLine && y-lineHeight<maxy) {
				Color bg = (lineIndex==cursor.line && focused && !singleLine) ? colorHighlight : colorBackground;
				drawLine(g, lineIndex, lineStart, lineEnd, y, bg, focused);
				y += lineHeight;
			}
			
			pos = lineEnd;
			lineIndex++;
		}
		if(!singleLine && y-lineHeight<maxy)
			g.fillRect(minx, y-lineHeight+descent, maxx, maxy-y+lineHeight-descent, colorBackground);
		
		w = (w+x0*2)*pixelScale;
		float h = singleLine ? 0 : lineHeight*lines.size()*pixelScale;
		if(updateSize || getWidth()!=w || getHeight()!=h) {
			panView().setPanRangeForClient(w, h);
			if(w<getParent().getWidth())
				w = getParent().getWidth();
			if(h<getParent().getHeight())
				h = getParent().getHeight();
			setSize(w, h);
			updateSize = false;
		}
		
		g.finishPixelMode();
	}
	
	protected void drawLine(GraphAssist g, int lineIndex, int lineStart, int lineEnd, int y, Color bg, boolean drawCursor) {
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
			if(lineIndex==selMax.line && selMax.col<lineEnd-lineStart) {
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
		
		if(drawCursor && cursor.line==lineIndex) {
			int cx = stringWidth(lineStart, lineStart+cursor.col);
			g.graph.setXORMode(Color.BLACK);
			g.fillRect(x0+cx, y-lineHeight+descent, 2f/pixelScale, lineHeight, Color.WHITE);
			g.graph.setPaintMode();
			if(cursorX<0)
				cursorX = (x0+cx)*pixelScale;
		}
	}
	
	protected void drawRemainder(GraphAssist g, int x, int y, Color bg) {
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
	
	protected int drawText(GraphAssist g, int x, int y, int c0, int c1, Color bg, Color fg) {
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
	
	protected int stringWidth(int c0, int c1) {
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
	
	protected int searchCol(float tx) {
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
		cursor.line = singleLine ? 0 : (int)(y / pixelScale / lineHeight);
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
	
	private void modifySelection(boolean keepStart) {
		if(selStart!=null) {
			selEnd.set(cursor);
			if(selStart.equals(selEnd) && !keepStart)
				deselect();
			updateSelRange();
		}
	}
	
	private void modifySelection() {
		modifySelection(false);
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
		scrollToCursor();
	}
	
	public void pasteAtCursor() {
		history.push();
		
		boolean changed = deleteSelection(false);
		checkCursorLineCache();
		int pos = cursorLineStart+cursor.col;

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				String add = (String) clipboard.getData(DataFlavor.stringFlavor);
				modify(pos, add, pos);
				// setText may strip newlines and tabs, setCursor must consider the difference
				int len = text.length();
				setText(text, false);
				setCursor(pos+add.length()-(len-text.length()));
				changed = true;
			} catch(UnsupportedFlavorException | IOException e) {
			}
		}
		
		if(changed) {
			history.push();
			historyAction = HistoryAction.unspecified;
		}
		scrollToCursor();
	}
	
	private boolean deleteSelection(boolean pushHistory) {
		if(selStart!=null) {
			if(pushHistory)
				history.push();
			
			cursor.set(selMin);
			if(selMin.line==selMax.line) {
				modify(selMin.line, selMin.col, "", selMax.col);
			}
			else {
				int start = lines.get(selMin.line).calcStart()+selMin.col;
				int end = lines.get(selMax.line).calcStart()+selMax.col;
				modify(start, "", end);
				setText(text, false);
			}
			deselect();
			
			if(pushHistory) {
				history.push();
				historyAction = HistoryAction.unspecified;
			}
			return true;
		}
		else
			return false;
	}
	
	public void deleteSelection() {
		deleteSelection(true);
	}
	
	public void indentSelection(String indent) {
		boolean changed = false;
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
					changed = true;
				}
				pos += line.length;
			}
			if(selMin.col>0) selMin.col += indentLen;
			if(selMax.col>0) selMax.col += indentLen;
			if(cursor.col>0) cursor.col += indentLen;
		}
		if(changed)
			history.push();
	}
	
	public void unindentSelection() {
		boolean changed = false;
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
					changed = true;
				}
				pos += line.length;
			}
		}
		if(changed)
			history.push();
	}
	
	private void joinLineWithNext() {
		if(singleLine)
			return;
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
		if(singleLine)
			return cursor.col;
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

	private void checkPushHistory(HistoryAction action) {
		if(historyAction!=action) {
			if(historyAction!=HistoryAction.unspecified)
				history.push();
			historyAction = action;
		}
		else {
			// TODO push after timer
		}
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
				checkPushHistory(HistoryAction.unspecified);
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
				checkPushHistory(HistoryAction.unspecified);
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
				} while((modifiers&UIElement.modCtrlMask)>0 && !isCursorAtWordBoundary());
				scrollToCursor();
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_UP:
				checkPushHistory(HistoryAction.unspecified);
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
				checkPushHistory(HistoryAction.unspecified);
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
				checkPushHistory(HistoryAction.unspecified);
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line>page)
					cursor.line -= page;
				else
					cursor.line = 0;
				updateCursor();
				scrollToCursor();
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
				
			case KeyEvent.VK_PAGE_DOWN:
				checkPushHistory(HistoryAction.unspecified);
				if(modifiers==UIElement.modShiftMask)
					startSelection();
				else
					deselect();
				if(cursor.line+page<=lines.size()-1)
					cursor.line += page;
				else
					cursor.line = lines.size()-1;
				updateCursor();
				scrollToCursor();
				if(modifiers==UIElement.modShiftMask)
					modifySelection();
				break;
				
			case KeyEvent.VK_HOME:
				checkPushHistory(HistoryAction.unspecified);
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else
					deselect();
				if((modifiers&UIElement.modCtrlMask)>0) {
					cursor.line = 0;
				}
				cursor.col = 0;
				cursorX = -1;
				scrollToCursor();
				if((modifiers&UIElement.modShiftMask)>0)
					modifySelection();
				break;
				
			case KeyEvent.VK_END:
				checkPushHistory(HistoryAction.unspecified);
				if((modifiers&UIElement.modShiftMask)>0)
					startSelection();
				else
					deselect();
				if((modifiers&UIElement.modCtrlMask)>0) {
					cursor.line = lines.size()-1;
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
					scrollToCursor();
				}
				else {
					checkPushHistory(HistoryAction.deleting);
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
					scrollToCursor();
				}
				else {
					checkPushHistory(HistoryAction.deleting);
					if(cursor.col<lines.get(cursor.line).length) {
						modify(cursor.line, cursor.col, "", cursor.col+1);
					}
					else if(cursor.line<lines.size()-1) {
						joinLineWithNext();
					}
				}
				break;
				
			case KeyEvent.VK_ENTER:
				if(!singleLine) {
					deleteSelection();
					checkPushHistory(HistoryAction.typing);
					cursor.col = splitLineAtCursor();
					cursor.line++;
					scrollToCursor();
				}
				else {
					checkPushHistory(HistoryAction.unspecified);
					getBase().resetFocus();
				}
				break;

			case KeyEvent.VK_ESCAPE:
				checkPushHistory(HistoryAction.unspecified);
				getBase().resetFocus();
				break;
				
			case KeyEvent.VK_TAB:
				if(!singleLine) {
					if(selStart==null) {
						if(modifiers==UIElement.modNone) {
							checkPushHistory(HistoryAction.typing);
							modify(cursor.line, cursor.col, "\t", cursor.col);
							cursor.col++;
							scrollToCursor();
						}
					}
					else {
						checkPushHistory(HistoryAction.unspecified);
						if(modifiers==UIElement.modShiftMask)
							unindentSelection();
						else
							indentSelection("\t");
					}
				}
				break;
				
			default: {
				if(modifiers==UIElement.modCtrlMask) {
					switch(code) {
						case KeyEvent.VK_A:
							checkPushHistory(HistoryAction.unspecified);
							selectAll();
							break;
						case KeyEvent.VK_X:
							checkPushHistory(HistoryAction.unspecified);
							cutSelection();
							break;
						case KeyEvent.VK_C:
							checkPushHistory(HistoryAction.unspecified);
							copySelection();
							break;
						case KeyEvent.VK_V:
							checkPushHistory(HistoryAction.unspecified);
							pasteAtCursor();
							break;
						case KeyEvent.VK_Z:
							checkPushHistory(HistoryAction.unspecified);
							history.undo();
							break;
						case KeyEvent.VK_Y:
							checkPushHistory(HistoryAction.unspecified);
							history.redo();
							break;
					}
				}
				else {
					if(!Character.isISOControl(c) && c!=KeyEvent.CHAR_UNDEFINED) {
						deleteSelection();
						checkPushHistory(HistoryAction.typing);
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
			else 
				checkPushHistory(HistoryAction.unspecified);
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

}
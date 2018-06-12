package com.xrbpowered.uitest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Stroke;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.WindowUtils;
import com.xrbpowered.zoomui.icons.IconPalette;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIButtonBase;
import com.xrbpowered.zoomui.std.UIListBox;
import com.xrbpowered.zoomui.std.UIListItem;
import com.xrbpowered.zoomui.std.UIScrollContainer;
import com.xrbpowered.zoomui.std.UITextBox;
import com.xrbpowered.zoomui.std.UIToolButton;

public class FileBrowser extends UIContainer {

	public static Font font = UIButton.font;

	public static Color colorBackground = Color.WHITE;
	public static Color colorBorder = UIListBox.colorBorder;
	public static Color colorBorderLight = new Color(0xcccccc);
	public static Color colorText = UIListItem.colorText;
	public static Color colorHighlight = UIListItem.colorHighlight;
	public static Color colorSelection = UIListItem.colorSelection;
	public static Color colorSelectedText = UIListItem.colorSelectedText;
	public static Color colorDisabledSelectedText = new Color(0x99ccff);
	public static Color colorDisabledText = new Color(0x888888);

	public static final IconPalette iconPalette = new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xf8f8f8), new Color(0x888888), Color.BLACK},
		{new Color(0x66aaff), new Color(0x4499ee), new Color(0xddeeff), Color.WHITE},
		{new Color(0xf9f9f9), new Color(0xfdfdfd), new Color(0xd8d8d8), new Color(0xababab)}, 
		{new Color(0x4298f3), new Color(0x2c8de8), new Color(0x90c4f3), new Color(0xa6d0f3)},
	});

	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy, HH:mm");
	
	private static final SvgIcon fileIcon = new SvgIcon("svg/file.svg", 160, iconPalette);
	private static final SvgIcon folderIcon = new SvgIcon("svg/folder.svg", 160, iconPalette);
	private static final SvgIcon diskIcon = new SvgIcon("svg/disk.svg", 160, iconPalette);
	
	private static final int LIST_ITEM_WIDTH = 256;
	private static final int LIST_ITEM_HEIGHT = 48;
	
	private static String formatFileSize(long size) {
		String[] prefs = {"bytes", "KB", "MB", "GB", "TB"};
		double s = size;
		for(int d=0; d<prefs.length; d++) {
			if(d>0 && s<10.0)
				return String.format("%.2f %s", s, prefs[d]);
			else if(d>0 && s<100.0)
				return String.format("%.1f %s", s, prefs[d]);
			else if(s<1000.0)
				return String.format("%.0f %s", s, prefs[d]);
			else
				s = s/1024.0;
		}
		return "";
	}
	
	private static boolean startsWithSymbol(String s) {
		if(s.isEmpty())
			return false;
		char ch = s.charAt(0);
		return !(Character.isLetter(ch) || Character.isDigit(ch)) && ch!='_';
	}
	
	public static class FileListItem extends UIElement {
		protected File file;
		public final FileBrowser fileBrowser;
		protected boolean hover = false;
		
		private String info;
		private boolean isSystem;
		private int textWidth = -1;
		private int textHeight = -1;
		
		public FileListItem(UIContainer parent, FileBrowser fileBrowser, File file) {
			super(parent);
			this.fileBrowser = fileBrowser;
			this.file = file;
			if(file.isFile())
				info = dateFmt.format(file.lastModified()) + ", "+formatFileSize(file.length());
			else if(file.getName().isEmpty())
				info = formatFileSize(file.getFreeSpace()) + " free, " + formatFileSize(file.getTotalSpace()) + " total";
			else
				info = null;
			isSystem = startsWithSymbol(file.getName()) || file.isHidden() && !file.getName().isEmpty();
		}
		
		@Override
		public void paint(GraphAssist g) {
			int w = (int)getWidth();
			int h = (int)getHeight();
			
			boolean sel = (file==fileBrowser.view.getSelectedFile());
			Color bgColor = sel ? colorSelection : hover ? colorHighlight : colorBackground;
			g.fill(this, bgColor);

			String fileName = file.getName();
			boolean disk = false;
			if(fileName.isEmpty()) {
				fileName = file.getAbsolutePath();
				disk = true;
			}

			int style = sel ? 1 : 0;
			if(isSystem) style += 2;
			(disk ? diskIcon : file.isFile() ? fileIcon : folderIcon).paint(g.graph, style, 20, 8, 32, getPixelScale(), true);

			g.setFont(font);
			g.setColor(sel ? colorSelectedText : colorText);
			if(textWidth<0) {
				FontMetrics fm = g.graph.getFontMetrics();
				textWidth = fm.stringWidth(fileName);
				textHeight = fm.getAscent() - fm.getDescent();
			}
			float y = info==null ? (h/2f + textHeight/2f) : (h/2f-3f);
			if(textWidth+60>=w-8 && g.pushClip(0, 0, w-8, h)) {
				g.drawString(fileName, 60, y);
				g.popClip();
				g.setPaint(new GradientPaint(w-32, 0, new Color(bgColor.getRGB()&0xffffff, true), w-8, 0, bgColor));
				g.fillRect(w-32, 0, 24, h);
			}
			else {
				g.drawString(fileName, 60, y);
			}
			if(info!=null) {
				g.setColor(sel ? colorDisabledSelectedText : colorDisabledText);
				g.drawString(info, 60, (int)(h/2f+3f+textHeight));
			}
		}
		
		@Override
		protected void onMouseIn() {
			hover = true;
			requestRepaint();
		}
		
		@Override
		protected void onMouseOut() {
			hover = false;
			requestRepaint();
		}
		
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			if(buttons==mouseLeftMask) {
				FileViewPane fileView = fileBrowser.view;
				if(fileView.getSelectedFile()==file)
					fileView.onClickSelected();
				else {
					fileView.setSelectedFile(file);
					fileView.onSelect(file);
				}
				requestRepaint();
				return true;
			}
			else
				return false;
		}
	}

	public static class FileGroupBoxHeader extends UIElement {
		protected boolean hover = false;

		public FileGroupBoxHeader(FileGroupBox parent) {
			super(parent);
		}
		
		@Override
		public void paint(GraphAssist g) {
			Color bgColor = hover ? colorHighlight : colorBackground;
			g.fill(this, bgColor);
			
			FileGroupBox grp = (FileGroupBox) getParent();
			boolean open = grp.isViewOpen();
			
			g.setColor(open ? colorSelection : colorDisabledText);
			String str = String.format("%s (%d)", grp.title, grp.getNumFiles());
			FontMetrics fm = g.graph.getFontMetrics();
			int textWidth = fm.stringWidth(str);
			g.drawString(str, 20, 2+font.getSize());
			
			Stroke stroke = g.graph.getStroke();
			g.graph.setStroke(new BasicStroke(2f));
			g.setColor(colorText);
			int w = (int)(getHeight()/2f);
			if(open)
				g.graph.drawPolyline(new int[] {6, 10, 14}, new int[] {w-2, w+2, w-2}, 3);
			else
				g.graph.drawPolyline(new int[] {8, 12, 8}, new int[] {w-4, w, w+4}, 3);
			
			g.graph.setStroke(stroke);
			g.setColor(colorBorderLight);
			g.line(textWidth+28, w, getWidth()-8, w);
		}
	
		@Override
		protected void onMouseIn() {
			hover = true;
			requestRepaint();
		}
		
		@Override
		protected void onMouseOut() {
			hover = false;
			requestRepaint();
		}
		
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			if(buttons==mouseLeftMask) {
				FileGroupBox grp = (FileGroupBox) getParent();
				grp.toggleView();
				requestRepaint();
				return true;
			}
			else
				return false;
		}
	}

	public static class FileGroupBoxBody extends UIContainer {
		
		public FileGroupBoxBody(FileGroupBox parent) {
			super(parent);
		}
		
		@Override
		public void layout() {
			float w = LIST_ITEM_WIDTH;
			float h = LIST_ITEM_HEIGHT;
			float maxw = getWidth();
			float y = 0f;
			float x = 0f; 
			for(UIElement e : children) {
				if(x+w>maxw) {
					x = 0f;
					y += h;
				}
				e.setLocation(x, y);
				e.setSize(w, h);
				x += w;
			}
			setSize(getWidth(), y+h);
		}
		
	}

	public static class FileGroupBox extends UIContainer implements Comparable<FileGroupBox> {
		public final FileBrowser fileBrowser;
		public int order;
		public String title;
		private final FileGroupBoxHeader header;
		private final FileGroupBoxBody body;
		private int numFiles = 0;
		
		public FileGroupBox(FileBrowser fileBrowser, int order, String title) {
			super(fileBrowser.view.getView());
			this.fileBrowser = fileBrowser;
			this.title = title;
			this.order = order;
			this.header = new FileGroupBoxHeader(this);
			this.body = new FileGroupBoxBody(this);
		}
		
		public void addFile(File file) {
			numFiles++;
			new FileListItem(body, fileBrowser, file);
		}
		
		public int getNumFiles() {
			return numFiles;
		}
		
		public boolean isViewOpen() {
			return body.isVisible();
		}
		
		public void toggleView() {
			body.setVisible(!body.isVisible());
			invalidateLayout();
		}
		
		@Override
		public void layout() {
			float w = getWidth();
			header.setLocation(0, 0);
			header.setSize(w, font.getSize()+8);
			if(body.isVisible()) {
				body.setLocation(0, header.getHeight());
				body.setSize(w, 0);
				body.layout();
				setSize(w, body.getHeight()+header.getHeight()+8);
			}
			else {
				setSize(w, header.getHeight()+8);
			}
		}
		
		@Override
		public int compareTo(FileGroupBox o) {
			int res = Integer.compare(order, o.order);
			if(res==0) {
				res = title.compareToIgnoreCase(o.title);
			}
			return res;
		}
	}
	
	public static class FileViewPane extends UIScrollContainer {
		private File directory;
		private File selectedFile = null;
		private List<FileGroupBox> groups = new ArrayList<>();
		private String[] groupTypes = null;
		public boolean autoTypes = false;
		
		public FileViewPane(FileBrowser fileBrowser, String[] groupTypes, boolean autoTypes) {
			super(fileBrowser);
			this.groupTypes = autoTypes ? null : groupTypes;
			this.autoTypes = autoTypes;
		}
		
		public boolean setDirectory(File directory) {
			FileBrowser fileBrowser = (FileBrowser) getParent();
			File[] files = null;
			if(directory==null) {
				fileBrowser.btnUp.disable();
				files = File.listRoots();
			}
			else {
				fileBrowser.btnUp.enable();
				directory = Paths.get(directory.toURI()).normalize().toFile();
				files = directory.listFiles();
			}
			if(files==null)
				return false;
			this.directory = directory;
			fileBrowser.txtPath.text = directory==null ? "This computer" : directory.getAbsolutePath();
			
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int res = Boolean.compare(!o1.isDirectory(), !o2.isDirectory());
					if(res==0) {
						res = o1.getName().compareToIgnoreCase(o2.getName());
					}
					return res;
				}
			});
			
			getView().removeAllChildren();
			groups.clear();
			FileGroupBox dirGroup = null;
			FileGroupBox rootGroup = null;
			FileGroupBox allGroup = null;
			HashMap<String, FileGroupBox> groupMap = new HashMap<>();
			for(File file : files) {
				if(file.isDirectory()) {
					if(file.getName().isEmpty()) {
						if(rootGroup==null) {
							rootGroup = new FileGroupBox(fileBrowser, -1, "File systems");
							groups.add(rootGroup);
						}
						rootGroup.addFile(file);
					}
					else {
						if(dirGroup==null) {
							dirGroup = new FileGroupBox(fileBrowser, 0, "Folders");
							groups.add(dirGroup);
						}
						dirGroup.addFile(file);
					}
				}
				else {
					String type = null;
					if(autoTypes || groupTypes!=null) {
						String fileName = file.getName();
						int dotIndex = fileName.lastIndexOf('.');
						if(!startsWithSymbol(fileName) && dotIndex>0) {
							String ext = fileName.substring(dotIndex+1);
							if(autoTypes) {
								type = ext.toLowerCase();
							}
							else {
								for(String t : groupTypes) {
									if(t.equalsIgnoreCase(ext)) {
										type = t;
										break;
									}
								}
							}
						}
					}
					if(type==null) {
						if(allGroup==null) {
							allGroup = new FileGroupBox(fileBrowser, 2, groupTypes==null && !autoTypes ? "All files" : "All other files");
							groups.add(allGroup);
						}
						allGroup.addFile(file);
					}
					else {
						FileGroupBox grp = groupMap.get(type);
						if(grp==null) {
							grp = new FileGroupBox(fileBrowser, 1, type.toUpperCase()+" files");
							groupMap.put(type, grp);
							groups.add(grp);
						}
						grp.addFile(file);
					}
				}
			}
			Collections.sort(groups);
			
			selectedFile = null;
			onSelect(selectedFile);
			return true;
		}
		
		@Override
		protected float layoutView() {
			float w = getWidth();
			float y = 0f;
			for(FileGroupBox grp : groups) {
				grp.setLocation(0, y);
				grp.setSize(w, 0);
				grp.layout();
				y += grp.getHeight();
			}
			return y;
		}
		
		public File getDirectory() {
			return directory;
		}
		
		public void refresh() {
			setDirectory(directory);
		}
		
		public boolean upDirectory() {
			if(directory==null)
				return false;
			Path path = Paths.get(directory.toURI());
			Path parent = path.getParent();
			if(parent!=null)
				return setDirectory(parent.toFile());
			else
				return setDirectory(null);
		}
		
		public File getSelectedFile() {
			return selectedFile;
		}
		
		public void setSelectedFile(File file) {
			this.selectedFile = file;
		}
		
		public void onSelect(File file) {
			((FileBrowser) getParent()).txtFileName.text = file==null ? "" : file.getName();
			requestRepaint();
		}
		
		public void onClickSelected() {
			if(selectedFile!=null) {
				if(selectedFile.isDirectory()) {
					if(setDirectory(selectedFile)) {
						FileBrowser fileBrowser = (FileBrowser) getParent();
						fileBrowser.pushHistory();
					}
				}
			}
		}
		
		@Override
		protected void paintSelf(GraphAssist g) {
			g.fill(this, colorBackground);
		}
		
		@Override
		protected void paintChildren(GraphAssist g) {
			super.paintChildren(g);
			g.border(this, colorBorder);
		}
	}

	public final FileViewPane view;
	public final UITextBox txtFileName, txtPath;
	private UIButtonBase btnBack, btnFwd, btnRefresh, btnUp, btnHome, btnRoots, btnOk, btnCancel;
	public LinkedList<File> history = new LinkedList<>();
	public int historyIndex = -1;
	
	public FileBrowser(UIContainer parent) {
		super(parent);
		view = new FileViewPane(this, null, true);
		txtPath = new UITextBox(this);
		txtFileName = new UITextBox(this);
		btnBack = new UIToolButton(this, new SvgIcon("svg/back.svg", 160, iconPalette), 16, 2) {
			public void onAction() {
				if(historyIndex>0) {
					historyIndex--;
					view.setDirectory(history.get(historyIndex));
					if(historyIndex==0)
						btnBack.disable();
					if(historyIndex<history.size()-1)
						btnFwd.enable();
				}
				requestRepaint();
			}
		}.disable();
		btnFwd = new UIToolButton(this, new SvgIcon("svg/forward.svg", 160, iconPalette), 16, 2) {
			public void onAction() {
				if(historyIndex<history.size()-1) {
					historyIndex++;
					view.setDirectory(history.get(historyIndex));
					if(historyIndex>=history.size()-1)
						btnFwd.disable();
					if(historyIndex>0)
						btnBack.enable();
				}
				requestRepaint();
			}
		}.disable();
		btnRefresh = new UIToolButton(this, new SvgIcon("svg/refresh.svg", 160, iconPalette), 16, 2) {
			public void onAction() {
				view.refresh();				
				requestRepaint();
			}
		};
		btnUp = new UIToolButton(this, new SvgIcon("svg/up.svg", 160, iconPalette), 32, 8) {
			public void onAction() {
				if(view.upDirectory())
					pushHistory();
				requestRepaint();
			}
		};
		btnHome = new UIToolButton(this, new SvgIcon("svg/home.svg", 160, iconPalette), 32, 8) {
			public void onAction() {
				if(view.setDirectory(new File(System.getProperty("user.home"))))
					pushHistory();
				requestRepaint();
			}
		};
		btnRoots = new UIToolButton(this, new SvgIcon("svg/roots.svg", 160, iconPalette), 32, 8) {
			public void onAction() {
				if(view.setDirectory(null))
					pushHistory();
				requestRepaint();
			}
		};
		btnOk = new UIButton(this, "OK");
		btnCancel = new UIButton(this, "Cancel") {
			@Override
			public void onAction() {
				System.exit(0);
			}
		};
		view.setDirectory(new File("."));
		pushHistory();
	}
	
	public void pushHistory() {
		historyIndex++;
		while(history.size()>historyIndex)
			history.removeLast();
		history.add(view.getDirectory());
		if(historyIndex>0)
			btnBack.enable();
		btnFwd.disable();
	}
	
	@Override
	public void layout() {
		float w = getWidth();
		float h = getHeight();
		float top = txtFileName.getHeight()+16;
		float viewh = h-24-UIButton.defaultHeight*2-top;
		view.setLocation(56, top);
		view.setSize(w-56, viewh);
		txtFileName.setLocation(56, h-UIButton.defaultHeight*2-16);
		txtFileName.setSize(w-56-8, txtFileName.getHeight());
		txtPath.setLocation(56, 8);
		txtPath.setSize(w-56-4-28, txtFileName.getHeight());
		btnBack.setLocation(28-22, 8);
		btnFwd.setLocation(28+2, 8);
		btnRefresh.setLocation(w-28, 8);
		btnUp.setLocation(4, top+4);
		btnHome.setLocation(4, top+viewh-48*2-4);
		btnRoots.setLocation(4, top+viewh-48-4);
		btnOk.setLocation(w-UIButton.defaultWidth*2-12, h-UIButton.defaultHeight-8);
		btnCancel.setLocation(w-UIButton.defaultWidth-8, h-UIButton.defaultHeight-8);
		super.layout();
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		int w = (int)getWidth();
		int h = (int)getHeight();
		int top = (int)(txtFileName.getHeight()+16);
		int viewh = h-24-UIButton.defaultHeight*2-top;
		
		g.fillRect(0, 0, w, top, Color.WHITE);
		g.fillRect(0, top, w, h-top, new Color(0xf2f2f2));
		
		g.fillRect(0, top, 56, viewh, new Color(0xe4e4e4));
		g.setColor(new Color(0xcccccc));
		g.line(0, top, w, top);
		g.line(0, top+viewh, 56, top+viewh);
		
		g.setFont(font);
		g.setColor(colorText);
		g.drawString("File:", 52, txtFileName.getY()+txtFileName.getHeight()/2f,
				GraphAssist.RIGHT, GraphAssist.CENTER);
	}
	
	public static void main(String[] args) {
		new FileBrowser(WindowUtils.createFrame("Open file", 840, 480)).getBasePanel().showWindow();
	}
}

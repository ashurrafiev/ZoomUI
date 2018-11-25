package com.xrbpowered.zoomui.std.file;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIHoverElement;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListItem;
import com.xrbpowered.zoomui.std.UIMessageBox;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResult;
import com.xrbpowered.zoomui.std.UIScrollBar;
import com.xrbpowered.zoomui.std.UIScrollContainer;
import com.xrbpowered.zoomui.std.UIToolButton;

public class UIFileView extends UIScrollContainer {

	public static Font font = UIButton.font;

	public static Color colorBackground = Color.WHITE;
	public static Color colorBorderLight = new Color(0xcccccc);
	public static Color colorText = UIListItem.colorText;
	public static Color colorHighlight = UIListItem.colorHighlight;
	public static Color colorSelection = UIListItem.colorSelection;
	public static Color colorSelectedText = UIListItem.colorSelectedText;
	public static Color colorDisabledSelectedText = new Color(0x99ccff);
	public static Color colorDisabledText = new Color(0x888888);

	private static final SvgIcon fileIcon = new SvgIcon(UIToolButton.iconPath+"file.svg", 160, UIToolButton.palette);
	private static final SvgIcon folderIcon = new SvgIcon(UIToolButton.iconPath+"folder.svg", 160, UIToolButton.palette);
	private static final SvgIcon diskIcon = new SvgIcon(UIToolButton.iconPath+"disk.svg", 160, UIToolButton.palette);
	
	private static final int LIST_ITEM_WIDTH = 256;
	private static final int LIST_ITEM_HEIGHT = 48;

	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy, HH:mm");

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

	private class FileListItem extends UIHoverElement {
		public final File file;
		
		public final String info;
		public final boolean isSystem;
		
		private int textWidth = -1;
		private int textHeight = -1;
		
		public FileListItem(UIContainer parent, File file) {
			super(parent);
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
			
			boolean sel = file==selectedFile;
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
				FontMetrics fm = g.getFontMetrics();
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
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				if(selectedFile==file)
					onSelectionClicked();
				else {
					selectedFile = file;
					onFileSelected(file);
				}
				repaint();
				return true;
			}
			else
				return false;
		}
	}
	
	private class FileGroupBox extends UIContainer implements Comparable<FileGroupBox> {
		public final int order;
		public final String title;
		public final UIElement header;
		public final UIContainer body;
		public int numFiles = 0;
		
		public FileGroupBox(final int order, final String title) {
			super(getView());
			this.title = title;
			this.order = order;
			
			this.header = new UIHoverElement(this) {
				@Override
				public void paint(GraphAssist g) {
					Color bgColor = hover ? colorHighlight : colorBackground;
					g.fill(this, bgColor);
					
					boolean open = body.isVisible();
					g.setColor(open ? colorSelection : colorDisabledText);
					String str = String.format("%s (%d)", title, numFiles);
					FontMetrics fm = g.getFontMetrics();
					int textWidth = fm.stringWidth(str);
					g.drawString(str, 20, 2+font.getSize());
					
					g.setColor(colorText);
					int y = (int)(getHeight()/2f);
					if(open)
						UIScrollBar.drawDownArrow(g, 10, y);
					else
						UIScrollBar.drawRightArrow(g, 10, y);
					
					g.setColor(colorBorderLight);
					g.line(textWidth+28, y, getWidth()-8, y);
				}
				
				@Override
				public boolean onMouseDown(float x, float y, Button button, int mods) {
					if(button==Button.left) {
						toggleView();
						repaint();
						return true;
					}
					else
						return false;
				}
			};
			
			this.body = new UIContainer(this) {
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
			};
		}
		
		public void addFile(File file) {
			numFiles++;
			new FileListItem(body, file);
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
			if(res==0)
				res = title.compareToIgnoreCase(o.title);
			return res;
		}
	}

	public final String[] groupTypes;
	public final boolean autoTypes;

	private File directory = null;
	public File selectedFile = null;
	private final ArrayList<FileGroupBox> groups = new ArrayList<>();

	public UIFileView(UIContainer parent, String[] groupTypes, boolean autoTypes) {
		super(parent);
		this.groupTypes = groupTypes;
		this.autoTypes = autoTypes;
	}
	
	public boolean setDirectory(File directory) {
		File[] files;
		if(directory==null) {
			directory = null;
			files = File.listRoots();
		}
		else {
			directory = Paths.get(directory.toURI()).normalize().toFile();
			files = directory.listFiles();
		}
		if(files==null)
			return false;
		
		this.directory = directory;
		
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int res = Boolean.compare(!o1.isDirectory(), !o2.isDirectory());
				if(res==0)
					res = o1.getName().compareToIgnoreCase(o2.getName());
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
						rootGroup = new FileGroupBox(-1, "File systems");
						groups.add(rootGroup);
					}
					rootGroup.addFile(file);
				}
				else {
					if(dirGroup==null) {
						dirGroup = new FileGroupBox(0, "Folders");
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
						allGroup = new FileGroupBox(2, groupTypes==null && !autoTypes ? "All files" : "All other files");
						groups.add(allGroup);
					}
					allGroup.addFile(file);
				}
				else {
					FileGroupBox grp = groupMap.get(type);
					if(grp==null) {
						grp = new FileGroupBox(1, type.toUpperCase()+" files");
						groupMap.put(type, grp);
						groups.add(grp);
					}
					grp.addFile(file);
				}
			}
		}
		Collections.sort(groups);
		
		selectedFile = null;
		onNothingSelected();
		onDirectorySet();
		return true;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public void refresh() {
		setDirectory(directory);
	}
	
	public boolean upDirectory() {
		if(directory!=null) {
			Path parent = Paths.get(directory.toURI()).getParent();
			return setDirectory(parent!=null ? parent.toFile() : null);
		}
		else
			return false;
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

	public void onFileSelected(File file) {
	}
	
	public void onNothingSelected() {
	}

	public void onDirectorySet() {
	}
	
	public void onBrowse() {
	}
	
	public void onSelectedFileClicked() {
	}
	
	public void onSelectionClicked() {
		if(selectedFile!=null) {
			if(selectedFile.isDirectory()) {
				if(setDirectory(selectedFile))
					onBrowse();
				else
					UIMessageBox.show(getBase().getWindow().getFactory(), "Error", "Access denied.", UIMessageBox.iconError, new MessageResult[] {MessageResult.ok}, null);
			}
			else
				onSelectedFileClicked();
		}
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, colorBackground);
	}
	
}

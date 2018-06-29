package com.xrbpowered.zoomui.std.file;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.LinkedList;

import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIButtonBase;
import com.xrbpowered.zoomui.std.UITextBox;
import com.xrbpowered.zoomui.std.UIToolButton;

public class UIFileBrowser extends UIContainer {

	public static Font font = UIButton.font;
	public static Color colorText = UIButton.colorText;

	public final UIFileView view;
	public final UITextBox txtFileName, txtPath;
	public final UIButtonBase btnBack, btnFwd, btnRefresh, btnUp, btnHome, btnRoots, btnOk, btnCancel;
	
	public final LinkedList<File> history = new LinkedList<>();
	public int historyIndex = -1;
	
	public UIFileBrowser(final ModalBaseContainer<File> parent) {
		super(parent);
		
		view = new UIFileView(this, null, true) {
			@Override
			public void onDirectorySet() {
				File dir = view.getDirectory();
				btnUp.setEnabled(dir!=null);
				txtPath.text = dir!=null ? dir.getAbsolutePath() : "This computer";
			}
			@Override
			public void onBrowse() {
				pushHistory();
			}
			@Override
			public void onFileSelected(File file) {
				txtFileName.text = file.getName();
			}
		};
		
		// top pane
		txtPath = new UITextBox(this);
		btnBack = new UIToolButton(this, UIToolButton.iconPath+"back.svg", 16, 2) {
			public void onAction() {
				if(historyIndex>0) {
					setHistory(historyIndex-1);
					repaint();
				}
			}
		}.disable();
		btnFwd = new UIToolButton(this, UIToolButton.iconPath+"forward.svg", 16, 2) {
			public void onAction() {
				if(historyIndex<history.size()-1) {
					setHistory(historyIndex+1);
					repaint();
				}
			}
		}.disable();
		btnRefresh = new UIToolButton(this, UIToolButton.iconPath+"refresh.svg", 16, 2) {
			public void onAction() {
				view.refresh();				
				repaint();
			}
		};
		
		// left pane
		btnUp = new UIToolButton(this, UIToolButton.iconPath+"up.svg", 32, 8) {
			public void onAction() {
				if(view.upDirectory())
					pushHistory();
				repaint();
			}
		};
		btnHome = new UIToolButton(this, UIToolButton.iconPath+"home.svg", 32, 8) {
			public void onAction() {
				if(view.setDirectory(new File(System.getProperty("user.home"))))
					pushHistory();
				repaint();
			}
		};
		btnRoots = new UIToolButton(this, UIToolButton.iconPath+"roots.svg", 32, 8) {
			public void onAction() {
				if(view.setDirectory(null))
					pushHistory();
				repaint();
			}
		};
		
		// bottom pane
		txtFileName = new UITextBox(this);
		btnOk = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				parent.getWindow().closeWithResult(view.selectedFile);
			};
		};
		btnCancel = new UIButton(this, "Cancel") {
			@Override
			public void onAction() {
				parent.getWindow().close();
			}
		};
		
		view.setDirectory(new File("."));
		pushHistory();
	}
	
	private void setHistory(int index) {
		historyIndex = index;
		view.setDirectory(history.get(index));
		btnBack.setEnabled(index > 0);
		btnFwd.setEnabled(index < history.size()-1);
	}

	private void pushHistory() {
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
}

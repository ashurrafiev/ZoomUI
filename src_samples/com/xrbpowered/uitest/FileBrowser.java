package com.xrbpowered.uitest;

import java.io.File;

import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.std.file.UIFileBrowser;
import com.xrbpowered.zoomui.swing.SwingModalDialog;

public class FileBrowser {
	public static void main(String[] args) {
		UIModalWindow<File> frame = new SwingModalDialog<File>("Open file", 840, 480, true, null) {
			public void onResult(File file) {
				System.out.println(file);
				System.exit(0);
			}
		};
		new UIFileBrowser(frame.getContainer());
		frame.show();
	}
}

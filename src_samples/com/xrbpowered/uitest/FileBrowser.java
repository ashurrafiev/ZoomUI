package com.xrbpowered.uitest;

import java.io.File;

import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.std.file.UIFileBrowser;
import com.xrbpowered.zoomui.swing.SwingModalDialog;

public class FileBrowser {
	public static void main(String[] args) {
		UIModalWindow<File> frame = new SwingModalDialog<File>("Open file", 840, 480, true, null);
		frame.onResult = new ResultHandler<File>() {
			@Override
			public void onResult(UIModalWindow<File> dlg, File result) {
				System.out.println(result);
				System.exit(0);
			}
		};
		new UIFileBrowser(frame.getContainer());
		frame.show();
	}
}

package com.xrbpowered.uitest;

import java.io.File;

import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIWindowFactory;
import com.xrbpowered.zoomui.std.file.UIFileBrowser;

public class FileBrowser {
	public static void main(String[] args) {
		UIModalWindow<File> frame = UIWindowFactory.getInstance().createModal("Open file", 840, 480, true, null);
		frame.onResult = new ResultHandler<File>() {
			@Override
			public void onResult(File result) {
				System.out.println(result);
				System.exit(0);
			}
			@Override
			public void onCancel() {
				System.out.println("Cancelled");
				System.exit(1);
			}
		};
		new UIFileBrowser(frame.getContainer(), frame.wrapInResultHandler());
		frame.show();
	}
}

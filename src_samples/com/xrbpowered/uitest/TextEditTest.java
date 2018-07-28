package com.xrbpowered.uitest;

import java.awt.Font;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.std.text.UITextArea;
import com.xrbpowered.zoomui.std.text.UITextEditBase;
import com.xrbpowered.zoomui.swing.SwingFrame;

public class TextEditTest {
	
	private static final String TEST_INPUT = "src_samples/com/xrbpowered/uitest/TextEditTest.java";

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
		
		new UITextArea(frame.getContainer()) {
			@Override
			protected UITextEditBase createEditor() {
				return new UITextEditBase(getView(), false) {
					@Override
					protected void setupStyle() {
						font = new Font("Verdana", Font.PLAIN, GraphAssist.ptToPixels(10f));
					}
				};
			}
			
			@Override
			protected void paintBorder(GraphAssist g) {
				g.hborder(this, GraphAssist.TOP, colorBorder);
			}
		}.editor.setText(loadString(TEST_INPUT));
		
		frame.show();
	}

}

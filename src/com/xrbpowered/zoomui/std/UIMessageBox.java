package com.xrbpowered.zoomui.std;

import java.awt.Color;

import com.sun.glass.events.KeyEvent;
import com.xrbpowered.zoomui.BaseContainer.ModalBaseContainer;
import com.xrbpowered.zoomui.icons.IconPalette;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.swing.SwingModalDialog;

public class UIMessageBox extends UIContainer implements KeyInputHandler {

	public enum MessageResult {
		ok("OK", KeyEvent.VK_ENTER),
		yes("Yes", KeyEvent.VK_Y),
		no("No", KeyEvent.VK_N),
		cancel("Cancel", KeyEvent.VK_ESCAPE);
		
		public final String label;
		public final int keyCode;
		private MessageResult(String label, int keyCode) {
			this.label = label;
			this.keyCode = keyCode;
		}
	}
	
	public static final SvgIcon iconError = new SvgIcon("svg/error.svg", 160, new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xeecccc), new Color(0xaa0000), Color.RED}
	}));
	public static final SvgIcon iconAlert = new SvgIcon("svg/alert.svg", 160, new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xeeddbb), new Color(0xee9900), new Color(0xffdd55)}
	}));
	public static final SvgIcon iconQuestion = new SvgIcon("svg/question.svg", 160, new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xccddee), new Color(0x0077dd), new Color(0x00bbff)}
	}));
	public static final SvgIcon iconOk = new SvgIcon("svg/ok.svg", 160, new IconPalette(new Color[][] {
		{new Color(0xeeeeee), new Color(0xcceecc), new Color(0x007700), new Color(0x00ee00)}
	}));
	
	public static final int iconSize = 32;
	
	public final UIFormattedLabel label;
	public final SvgIcon icon;
	public final MessageResult[] options;
	public final UIButton[] buttons;
	
	public UIMessageBox(ModalBaseContainer<MessageResult> parent, String message, SvgIcon icon, MessageResult[] options) {
		super(parent);
		this.options = options;
		this.icon = icon;
		
		this.buttons = new UIButton[options.length];
		for(int i=0; i<buttons.length; i++) {
			final MessageResult res = options[i];
			buttons[i] = new UIButton(this, options[i].label) {
				@Override
				public void onAction() {
					notifyResult(res);
				}
			};
		}
			
		this.label = new UIFormattedLabel(this, message);
	}
	
	@SuppressWarnings("unchecked")
	public void notifyResult(MessageResult res) {
		((ModalBaseContainer<MessageResult>) getBase()).getWindow().closeWithResult(res);
	}
	
	@Override
	public void onFocusGained() {
	}
	
	@Override
	public void onFocusLost() {
	}
	
	@Override
	public boolean onKeyPressed(char c, int code, int mods) {
		for(int i=0; i<options.length; i++)
			if(code==options[i].keyCode) {
				notifyResult(options[i]);
				return true;
			}
		if(code==MessageResult.cancel.keyCode) {
			notifyResult(MessageResult.cancel);
			return true;
		}
		return false;
	}
	
	@Override
	public void layout() {
		float x = (icon==null) ? 0 : iconSize+16; 
		float h = Math.max(label.getHeight(), (icon==null) ? 0 : iconSize+8);
		label.setLocation(x+16, (h-label.getHeight())/2f+8);
		label.setSize(getWidth()-x-32, 0);
		label.layout();
		
		float y = getHeight()-8-UIButton.defaultHeight;
		for(int i=0; i<buttons.length; i++)
			buttons[i].setLocation(getWidth()-4-(UIButton.defaultWidth+4)*(buttons.length-i), y);
	}
	
	@Override
	public void paint(GraphAssist g) {
		float hlabel = label.getHeight();
		super.paint(g);
		
		float h = Math.max(label.getHeight(), (icon==null) ? 0 : iconSize+8);
		if(icon!=null)
			icon.paint(g.graph, 0, 16, 12, iconSize, getPixelScale(), true);
		
		if(hlabel!=label.getHeight())
			getBase().getWindow().setClientSize((int)getWidth(), (int)(h+UIButton.defaultHeight+40));
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		float w = getWidth();
		float y = getHeight()-16-UIButton.defaultHeight;
		g.fillRect(0, 0, w, y, Color.WHITE);
		g.fillRect(0, y, w, getHeight()-y, new Color(0xf2f2f2));
		g.setColor(new Color(0xcccccc));
		g.line(0, y, w, y);
	}
	
	public static void show(String title, String message, SvgIcon icon, MessageResult[] options) {
		int width = Math.max(options.length*2+1, 6) * (UIButton.defaultWidth+4) / 2 + 32;
		UIModalWindow<MessageResult> dlg = new SwingModalDialog<MessageResult>(title, width, UIButton.defaultHeight+40, false, MessageResult.cancel);
		new UIMessageBox(dlg.getContainer(), message, icon, options);
		dlg.show();
	}

}

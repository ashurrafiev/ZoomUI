package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Rectangle;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIWindowFactory;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.icons.SvgFile;

public class RenderSvg extends UIZoomView {

	public final SvgFile svg = new SvgFile("drawing.svg"); 
	
	public RenderSvg(UIContainer parent) {
		super(parent);
		new UIElement(this) {
			@Override
			public boolean isVisible(Rectangle clip) {
				return isVisible();
			}
			@Override
			public void paint(GraphAssist g) {
				g.pushPureStroke(true);
				svg.render(g.graph, 10);
				g.popPureStroke();
			};
		};
		setScaleRange(0.1f, 10);
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}

	public static void main(String[] args) {
		UIWindowFactory factory = UIWindowFactory.getInstance();
		factory.setBaseScale(1f);
		UIWindow frame = factory.create("RenderSvg", 1800, 960, true);
		new RenderSvg(frame.getContainer());
		frame.show();
	}

}

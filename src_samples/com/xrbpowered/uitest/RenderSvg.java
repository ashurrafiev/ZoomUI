package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.UIZoomView;
import com.xrbpowered.zoomui.icons.SvgFile;
import com.xrbpowered.zoomui.swing.SwingFrame;

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
				g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				svg.render(g.graph, 10);
			};
		};
		setScaleRange(0.1f, 10);
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}

	public static void main(String[] args) {
		UIWindow.setBaseScale(1f);
		UIWindow frame = new SwingFrame("RenderSvg", 1800, 960);
		new RenderSvg(frame.getContainer());
		frame.show();
	}

}

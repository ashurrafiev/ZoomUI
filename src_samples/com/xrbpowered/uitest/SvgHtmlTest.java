package com.xrbpowered.uitest;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.StyleSheet;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.UIToolButton;
import com.xrbpowered.zoomui.swing.SwingFrame;

public class SvgHtmlTest extends UIContainer {

	private static final Font font = new Font("Verdana", Font.PLAIN, GraphAssist.ptToPixels(9f));
	private static final SvgIcon testIcon = new SvgIcon("svg/folder.svg", 160, UIToolButton.palette);

	private static final String html = "<html>Hello <img size=\"16\" src=\"test\"> "
			+ "<a href=\"world\" hover=\"#0099ff\" style=\"font-weight:bold\">world</a>"
			+ " and <a href=\"people\" style=\"font-weight:bold\">all people</a>!";
	private static final String css = "a { text-decoration: none; color: #0077dd }";

	// Known issues (unable to resolve):
	// * Always rendered at 1x zoom. All sizes in HTML/CSS ignore scaling
	// * Hover color has to be done via custom "hover" attribute. CSS ":hover" selector will not work.
	
	public static class ZoomUIHtmlEditorKit extends HTMLEditorKit {
		
		public class SvgImageView extends ImageView {
			private SvgIcon icon;
			private int iconSize;
			public SvgImageView(Element elem) {
				super(elem);
				icon = icons.get((String) elem.getAttributes().getAttribute(HTML.Attribute.SRC));
				iconSize = Integer.parseInt((String) elem.getAttributes().getAttribute(HTML.Attribute.SIZE));
			}

			@Override
			public float getPreferredSpan(int axis) {
				if(axis == View.X_AXIS)
					return iconSize * scale;
				else
					return (iconSize - 4) * scale; // TODO proper align to text baseline?
			}

			@Override
			public void paint(Graphics g, Shape a) {
				Rectangle rect = (Rectangle) a;
				icon.paint((Graphics2D) g, 0, rect.x, rect.y, iconSize * scale, 1f, false);
			}

		}

		public class HrefView extends InlineView {
			private String href;
			private Color hoverColor = null;
			public HrefView(Element elem) {
				super(elem);
				href = (String) elem.getAttributes().getAttribute(HTML.Attribute.HREF);
				String hover = (String) elem.getAttributes().getAttribute("hover");
				if(hover!=null)
					hoverColor = cssStringToColor(hover);
			}

			@Override
			public Color getForeground() {
				if(hoverHref==null || !hoverHref.equals(href))
					return super.getForeground();
				else if(hoverColor!=null)
					return hoverColor;
				else if(defaultHoverColor!=null)
					return defaultHoverColor;
				else
					return super.getForeground();
			}
			
			@Override
			public void paint(Graphics g, Shape a) {
				Rectangle rect = (Rectangle) a;
				if(container!=null && rebuildUI) {
					UIElement ui = new UIElement(container) {
						@Override
						public void onMouseIn() {
							hoverHref = href;
							getBase().getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							repaint();
						}
						@Override
						public void onMouseOut() {
							hoverHref = null;
							getBase().getWindow().setCursor(Cursor.getDefaultCursor());
							repaint();
						}
						@Override
						public boolean onMouseDown(float x, float y, Button button, int mods) {
							System.out.println("#" + href + " clicked"); // TODO onHrefClicked event
							return true;
						}
						@Override
						public void paint(GraphAssist g) {
						}
					};
					ui.setLocation(rect.x / scale + 10, rect.y / scale + 10);
					ui.setSize(rect.width / scale, rect.height / scale);
				}
				super.paint(g, a);
			}
		}

		class CustomHtmlDocument extends HTMLDocument {
			public CustomHtmlDocument(StyleSheet styles) {
				super(styles);
			}

			public HTMLEditorKit.ParserCallback getReader(int pos) {
				Object desc = getProperty(Document.StreamDescriptionProperty);
				if(desc instanceof URL) {
					setBase((URL) desc);
				}
				return new HTMLDocument.HTMLReader(pos) {
					public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
						if(t.toString().equals("a")) {
							registerTag(t, new CharacterAction() {
								public void start(HTML.Tag t, MutableAttributeSet attr) {
									attr.addAttribute(StyleConstants.NameAttribute, t);
									ElementSpec es = new ElementSpec(attr.copyAttributes(), ElementSpec.StartTagType);
									parseBuffer.addElement(es);
									super.start(t, attr);
								}
								public void end(HTML.Tag t) {
									ElementSpec es = new ElementSpec(null, ElementSpec.EndTagType);
									parseBuffer.addElement(es);
									super.end(t);
								}
							});
						}
						super.handleStartTag(t, a, pos);
					}
				};
			}
		}

		public final UIContainer container;
		public float scale = 1f;
		public Color defaultHoverColor = null;
		public HashMap<String, SvgIcon> icons = new HashMap<>();

		private boolean rebuildUI = true;
		private String hoverHref = null;

		public ZoomUIHtmlEditorKit(UIContainer container) {
			this.container = container;
		}
		
		public Document createDefaultDocument() {
			StyleSheet styles = getStyleSheet();
			StyleSheet ss = new StyleSheet();
			ss.addStyleSheet(styles);
			CustomHtmlDocument doc = new CustomHtmlDocument(ss);
			doc.setParser(getParser());
			return doc;
		}

		@Override
		public ViewFactory getViewFactory() {
			return new HTMLEditorKit.HTMLFactory() {
				@Override
				public View create(Element elem) {
					AttributeSet attrs = elem.getAttributes();
					Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
					Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
					View res = null;
					if(o instanceof HTML.Tag) {
						HTML.Tag kind = (HTML.Tag) o;
						if(kind == HTML.Tag.IMG) {
							res = new SvgImageView(elem);
						} else if(kind == HTML.Tag.A) {
							res = new HrefView(elem);
						}
					}
					if(res == null)
						res = super.create(elem);
					return res;
				}
			};
		}
	}
	
	public static void printAttr(AttributeSet attr) {
		System.out.println("[");
		Enumeration<?> an = attr.getAttributeNames();
		while(an.hasMoreElements()) {
			Object key = an.nextElement();
			Object val = attr.getAttribute(key);
			System.out.printf("\t%s %s : %s %s\n", key.getClass().getName(), key.toString(), val.getClass().getName(), val.toString());
		}
		System.out.println("]");
	}
	
	public static Color cssStringToColor(String s) {
		// *facepalm*
		try {
			Method m = javax.swing.text.html.CSS.class.getDeclaredMethod("stringToColor", String.class);
			m.setAccessible(true);
			return (Color) m.invoke(null, s);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ZoomUIHtmlEditorKit htmlKit;
	private JEditorPane htmlAssist = null;

	public void drawFormattedString(Graphics2D g2, float pixelScale, String htmlStr, int x, int y, int w, int h) {
		if(htmlAssist == null) {
			htmlAssist = new JEditorPane();
			htmlAssist.setOpaque(false);
			htmlAssist.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		}

		htmlAssist.setEditorKit(htmlKit);
		
		AffineTransform tx = g2.getTransform();
		g2.setTransform(new AffineTransform());
		g2.translate(x/pixelScale, y/pixelScale);
		Font font = g2.getFont();
		htmlAssist.setFont(font.deriveFont(font.getSize()/pixelScale));
		htmlAssist.setForeground(g2.getColor());
		htmlAssist.setBounds(0, 0, (int)(w/pixelScale), (int)(h/pixelScale));
		htmlAssist.invalidate();

		htmlAssist.setText(html);

		htmlKit.scale = 1/pixelScale;
		if(htmlKit.rebuildUI && htmlKit.container!=null)
			htmlKit.container.removeAllChildren();
		htmlAssist.paint(g2);
		htmlKit.rebuildUI = false;
		
		g2.setTransform(tx);
	}

	public SvgHtmlTest(UIContainer parent) {
		super(parent);
		htmlKit = new ZoomUIHtmlEditorKit(this);
		htmlKit.defaultHoverColor = Color.RED;
		htmlKit.getStyleSheet().addRule(css);
		htmlKit.icons.put("test", testIcon);
	}

	@Override
	public boolean onMouseDown(float x, float y, Button button, int mods) {
		repaint();
		return true;
	}
	
	@Override
	public void layout() {
		htmlKit.rebuildUI = true;
		super.layout();
	}

	@Override
	public void paintSelf(GraphAssist g) {
		g.fill(this, new Color(0xfff6e6));
		g.setFont(font);
		g.setColor(Color.BLACK);
		drawFormattedString(g.graph, getPixelScale(), html, 10, 10, (int) (getWidth() - 20), (int) (getHeight() - 20));
	}

	public static void main(String[] args) {
		UIWindow.setBaseScale(2f);
		UIWindow frame = new SwingFrame("SvgHtmlTest", 400, 300);
		new SvgHtmlTest(frame.getContainer());
		frame.show();
	}

}

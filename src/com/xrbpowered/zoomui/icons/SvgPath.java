package com.xrbpowered.zoomui.icons;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SvgPath {

	private static void arcTo(Path2D.Double path, double rx, double ry, double theta, boolean largeArcFlag, boolean sweepFlag, double x, double y) {
		// Ensure radii are valid
		if(rx == 0 || ry == 0) {
			path.lineTo(x, y);
			return;
		}
		// Get the current (x, y) coordinates of the path
		Point2D p2d = path.getCurrentPoint();
		double x0 = p2d.getX();
		double y0 = p2d.getY();
		// Compute the half distance between the current and the final point
		double dx2 = (x0 - x) / 2.0f;
		double dy2 = (y0 - y) / 2.0f;
		// Convert theta from degrees to radians
		theta = Math.toRadians(theta % 360f);

		//
		// Step 1 : Compute (x1, y1)
		//
		double x1 = Math.cos(theta) * dx2 + Math.sin(theta) * dy2;
		double y1 = -Math.sin(theta) * dx2 + Math.cos(theta) * dy2;
		// Ensure radii are large enough
		rx = Math.abs(rx);
		ry = Math.abs(ry);
		double Prx = rx * rx;
		double Pry = ry * ry;
		double Px1 = x1 * x1;
		double Py1 = y1 * y1;
		double d = Px1 / Prx + Py1 / Pry;
		if(d > 1) {
			rx = Math.abs(Math.sqrt(d) * rx);
			ry = Math.abs(Math.sqrt(d) * ry);
			Prx = rx * rx;
			Pry = ry * ry;
		}

		//
		// Step 2 : Compute (cx1, cy1)
		//
		double sign = (largeArcFlag == sweepFlag) ? -1d : 1d;
		double coef = sign * Math.sqrt(((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1)));
		double cx1 = coef * ((rx * y1) / ry);
		double cy1 = coef * -((ry * x1) / rx);

		//
		// Step 3 : Compute (cx, cy) from (cx1, cy1)
		//
		double sx2 = (x0 + x) / 2.0f;
		double sy2 = (y0 + y) / 2.0f;
		double cx = sx2 + (Math.cos(theta) * cx1 - Math.sin(theta) * cy1);
		double cy = sy2 + (Math.sin(theta) * cx1 + Math.cos(theta) * cy1);

		//
		// Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
		//
		double ux = (x1 - cx1) / rx;
		double uy = (y1 - cy1) / ry;
		double vx = (-x1 - cx1) / rx;
		double vy = (-y1 - cy1) / ry;
		double p, n;
		// Compute the angle start
		n = Math.sqrt((ux * ux) + (uy * uy));
		p = ux; // (1 * ux) + (0 * uy)
		sign = (uy < 0) ? -1d : 1d;
		double angleStart = Math.toDegrees(sign * Math.acos(p / n));
		// Compute the angle extent
		n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
		p = ux * vx + uy * vy;
		sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
		double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
		if(!sweepFlag && angleExtent > 0) {
			angleExtent -= 360f;
		} else if(sweepFlag && angleExtent < 0) {
			angleExtent += 360f;
		}
		angleExtent %= 360f;
		angleStart %= 360f;

		Arc2D.Double arc = new Arc2D.Double();
		arc.x = cx - rx;
		arc.y = cy - ry;
		arc.width = rx * 2.0f;
		arc.height = ry * 2.0f;
		arc.start = -angleStart;
		arc.extent = -angleExtent;
		path.append(arc, true);
	}
	
	private static Path2D createPath(String d, double scale) {
		Path2D.Double path = new Path2D.Double();
		String[] s = d.split("[\\,\\s]\\s*");
		char cmd = '\0';
		double dx =0.0;
		double dy = 0.0;
		for(int i=0; i<s.length; i++) {
			char c = s[i].charAt(0);
			if(c>='A' && c<='Z' || c>='a' && c<='z') {
				cmd= c;
				i++;
			}
			Point2D.Double cur = (Point2D.Double) path.getCurrentPoint();
			if(cur!=null) {
				dx = cur.x;
				dy = cur.y;
			}
			switch(cmd) {
				case 'M':
					path.moveTo(Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale);
					i+=1;
					cmd = 'L';
					break;
				case 'm':
					path.moveTo(Double.parseDouble(s[i]) * scale + dx, Double.parseDouble(s[i+1]) * scale + dy);
					i+=1;
					cmd = 'l';
					break;
				case 'L':
					path.lineTo(Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale);
					i+=1;
					break;
				case 'l':
					path.lineTo(Double.parseDouble(s[i]) * scale + dx, Double.parseDouble(s[i+1]) * scale + dy);
					i+=1;
					break;
				case 'V':
					path.lineTo(cur.x, Double.parseDouble(s[i]) * scale);
					break;
				case 'v':
					path.lineTo(cur.x, Double.parseDouble(s[i]) * scale + dy);
					break;
				case 'H':
					path.lineTo(Double.parseDouble(s[i]) * scale, cur.y);
					break;
				case 'h':
					path.lineTo(Double.parseDouble(s[i]) * scale + dx, cur.y);
					break;
				case 'C':
					path.curveTo(
							Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale,
							Double.parseDouble(s[i+2]) * scale, Double.parseDouble(s[i+3]) * scale,
							Double.parseDouble(s[i+4]) * scale, Double.parseDouble(s[i+5]) * scale
						);
					i+=5;
					break;
				case 'c':
					path.curveTo(
							Double.parseDouble(s[i]) * scale + dx, Double.parseDouble(s[i+1]) * scale + dy,
							Double.parseDouble(s[i+2]) * scale + dx, Double.parseDouble(s[i+3]) * scale + dy,
							Double.parseDouble(s[i+4]) * scale + dx, Double.parseDouble(s[i+5]) * scale + dy
						);
					i+=5;
					break;
				case 'Q':
					path.quadTo(
							Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale,
							Double.parseDouble(s[i+2]) * scale, Double.parseDouble(s[i+3]) * scale
						);
					i+=3;
					break;
				case 'q':
					path.quadTo(
							Double.parseDouble(s[i]) * scale + dx, Double.parseDouble(s[i+1]) * scale + dy,
							Double.parseDouble(s[i+2]) * scale + dx, Double.parseDouble(s[i+3]) * scale + dy
						);
					i+=3;
					break;
				case 'A':
					arcTo(path,
							Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale,
							Double.parseDouble(s[i+2]), Integer.parseInt(s[i+3])!=0, Integer.parseInt(s[i+4])!=0,
							Double.parseDouble(s[i+5]) * scale, Double.parseDouble(s[i+6]) * scale
						);
					i+=6;
					break;
				case 'a':
					arcTo(path,
							Double.parseDouble(s[i]) * scale, Double.parseDouble(s[i+1]) * scale,
							Double.parseDouble(s[i+2]), Integer.parseInt(s[i+3])!=0, Integer.parseInt(s[i+4])!=0,
							Double.parseDouble(s[i+5]) * scale + dx, Double.parseDouble(s[i+6]) * scale + dy
						);
					i+=6;
					break;
				case 'Z':
				case 'z':
					path.closePath();
					i-=1;
					break;
				default:
					if(cmd!='\0')
						System.err.printf("Unknown path command: '%c'\n", cmd);
					cmd = '\0';
			}
		}
		return path;
	}

	private static void transform(AffineTransform transform, String tr, double scale) {
		if(tr==null || tr.isEmpty())
			return;
		
		Matcher m = Pattern.compile("([a-z]+)\\((.*?)\\)").matcher(tr);
		int offs = 0;
		
		while(m.find(offs)) {
			String t = m.group(1);
			String[] s = m.group(2).split("[\\,\\s]\\s*");
			
			if(t.equals("translate")) {
				double x = Double.parseDouble(s[0]) * scale;
				double y = s.length<2 ? 0.0 : Double.parseDouble(s[1]) * scale;
				transform.translate(x, y);
			}
			else if(t.equals("scale")) {
				double x = Double.parseDouble(s[0]);
				double y = s.length<2 ? x : Double.parseDouble(s[1]);
				transform.scale(x, y);
			}
			else if(t.equals("matrix")) {
				AffineTransform tx = new AffineTransform(new double[] {
					Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]), Double.parseDouble(s[3]),
					Double.parseDouble(s[4]) * scale, Double.parseDouble(s[5]) * scale
				});
				transform.concatenate(tx);
			}
			
			offs = m.end();
		}
	}
	
	private static Path2D searchGroup(String pathId, AffineTransform transform, Element g, double scale) {
		Node n = g.getFirstChild();
		
		while(n!=null) {
			if(n.getNodeType()==Node.ELEMENT_NODE) {
				Element e = (Element) n;
				
				AffineTransform t = new AffineTransform(transform);
				transform(t, e.getAttribute("transform"), scale);
				
				if(e.getNodeName().equals("g")) {
					Path2D path = searchGroup(pathId, t, e, scale);
					if(path!=null) {
						path.transform(t);
						return path;
					}
				}
				else if(e.getNodeName().equals("rect")) {
					// not supported, convert everything to paths
				}
				else if(e.getNodeName().equals("circle")) {
					// not supported, convert everything to paths
				}
				else if(e.getNodeName().equals("path")) {
					if(e.getAttribute("id").equals(pathId)) {
						Path2D path = createPath(e.getAttribute("d"), scale);
						path.transform(t);
						return path;
					}
				}
			}
			
			n = n.getNextSibling();
		}
		return null;
	}
	
	public static Path2D getSvgPath(String uri, String pathId, double scale) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(uri);
			doc.getDocumentElement().normalize();
			Element root = doc.getDocumentElement();
			
			return searchGroup(pathId, new AffineTransform(), root, scale);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}

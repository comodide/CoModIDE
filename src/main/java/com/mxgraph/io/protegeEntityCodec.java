//package com.mxgraph.io;
//
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//
//import com.mxgraph.util.mxUtils;
//import com.mxgraph.view.mxStylesheet;
//
//import edu.wright.dase.util.CustomEntityType;
//
//public class protegeEntityCodec extends mxObjectCodec {
//
//	public protegeEntityCodec() {
//		// TODO Auto-generated constructor stub
//		this(new CustomEntityType());
//	}
//
//	public protegeEntityCodec(Object template) {
//		// TODO Auto-generated constructor stub
//		this(template, null, null, null);
//	}
//
//	public protegeEntityCodec(Object template, String[] exclude, String[] idrefs, Map<String, String> mapping) {
//		super(template, exclude, idrefs, mapping);
//
//		System.out.println("sarker.3 called 2 protegeEntityCodec");
//		// TODO Auto-generated constructor stub
//	}
//
//	public Node encode(mxCodec enc, Object obj) {
//		Element node = enc.document.createElement(getName());
//		System.out.println("sarker.3 inside" + node.getNodeType());
//		
//
//		//System.out.println("nodexml: " + _toString((Node) obj) +"\t"+ obj.toString());
//		
//		if (obj instanceof CustomEntityType) {
//			CustomEntityType entityType =  (CustomEntityType) obj;
//			String value= entityType.getName();
//			
//			//Element entityNode = enc.document.createElement("add");
//			node.setAttribute("entityType", value);
//			//node.appendChild(entityNode);
//			
//			System.out.println("nodexml: " + _toString(node) +"\t"+ entityType.getName());
//			/* Map<String, Object> it = stylesheet.getStyles().entrySet().iterator();
//
//			while (it.hasNext()) {
//				Map.Entry<String, Map<String, Object>> entry = it.next();
//
//				Element styleNode = enc.document.createElement("add");
//				String stylename = entry.getKey();
//				styleNode.setAttribute("as", stylename);
//
//				Map<String, Object> style = entry.getValue();
//				Iterator<Map.Entry<String, Object>> it2 = style.entrySet().iterator();
//
//				while (it2.hasNext()) {
//					Map.Entry<String, Object> entry2 = it2.next();
//					Element entryNode = enc.document.createElement("add");
//					entryNode.setAttribute("as", String.valueOf(entry2.getKey()));
//					entryNode.setAttribute("value", getStringValue(entry2));
//					styleNode.appendChild(entryNode);
//				}
//
//				if (styleNode.getChildNodes().getLength() > 0) {
//					node.appendChild(styleNode);
//				}
//			}*/
//		}
//
//		return node;
//	}
//
//	/**
//	 * Returns the string for encoding the given value.
//	 */
//	protected String getStringValue(Map.Entry<String, Object> entry) {
//		if (entry.getValue() instanceof Boolean) {
//			return ((Boolean) entry.getValue()) ? "1" : "0";
//		}
//
//		return entry.getValue().toString();
//	}
//
//	/**
//	 * Decodes the given mxStylesheet.
//	 */
//	/*public Object decode(mxCodec dec, Node node, Object into) {
//		Object obj = null;
//
//		if (node instanceof Element) {
//			String id = ((Element) node).getAttribute("id");
//			obj = dec.objects.get(id);
//
//			if (obj == null) {
//				obj = into;
//
//				if (obj == null) {
//					obj = cloneTemplate(node);
//				}
//
//				if (id != null && id.length() > 0) {
//					dec.putObject(id, obj);
//				}
//			}
//
//			node = node.getFirstChild();
//
//			while (node != null) {
//				if (!processInclude(dec, node, obj) && node.getNodeName().equals("add") && node instanceof Element) {
//					String as = ((Element) node).getAttribute("as");
//
//					if (as != null && as.length() > 0) {
//						String extend = ((Element) node).getAttribute("extend");
//						Map<String, Object> style = (extend != null) ? ((mxStylesheet) obj).getStyles().get(extend)
//								: null;
//
//						if (style == null) {
//							style = new Hashtable<String, Object>();
//						} else {
//							style = new Hashtable<String, Object>(style);
//						}
//
//						Node entry = node.getFirstChild();
//
//						while (entry != null) {
//							if (entry instanceof Element) {
//								Element entryElement = (Element) entry;
//								String key = entryElement.getAttribute("as");
//
//								if (entry.getNodeName().equals("add")) {
//									String text = entry.getTextContent();
//									Object value = null;
//
//									if (text != null && text.length() > 0) {
//										value = mxUtils.eval(text);
//									} else {
//										value = entryElement.getAttribute("value");
//
//									}
//
//									if (value != null) {
//										style.put(key, value);
//									}
//								} else if (entry.getNodeName().equals("remove")) {
//									style.remove(key);
//								}
//							}
//
//							entry = entry.getNextSibling();
//						}
//
//						((mxStylesheet) obj).putCellStyle(as, style);
//					}
//				}
//
//				node = node.getNextSibling();
//			}
//		}
//
//		return obj;
//	}
//*/
//}

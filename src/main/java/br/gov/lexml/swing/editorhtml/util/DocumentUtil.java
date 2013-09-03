package br.gov.lexml.swing.editorhtml.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.SizeRequirements;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hexidec.ekit.component.EkitTableView;
import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.ExtendedHTMLEditorKit;
import com.hexidec.ekit.component.HTMLUtilities;

public class DocumentUtil {
	
	private static final Log log = LogFactory.getLog(DocumentUtil.class);
	
	public enum Marker {TO_REMOVE};  
	
	public static void debug(AttributeSet attr) {
		debug(attr, 0);
	}
	
	private static void debug(AttributeSet attr, int level) {
		String tab = StringUtils.repeat("  ", level) + "- ";
		if(log.isDebugEnabled()) {
			Enumeration<?> eNames = attr.getAttributeNames();
			while(eNames.hasMoreElements()) {
				Object aName = eNames.nextElement();
				Object aValue = attr.getAttribute(aName);
				log.debug(tab + aName.getClass().getSimpleName() + " " + aName + " = "
						+ aValue.getClass().getSimpleName() + " " + aValue);
			}
					
		}
	}
	
	public static void debug(StyledDocument doc) {
		debug(doc.getRootElements(), 0);
	}
	
	private static void debug(Element[] elements, int level) {
		for(Element e: elements) {
			debug(e, level);
		}
	}

	public static void debug(Element e) {
		debug(e, 0);
	}
	
	public static void debug(Element e, int level) {
		String tab = StringUtils.repeat("  ", level);
		log.debug(tab + "+ " + e.getName() + " (" + e.getStartOffset() + " to " + e.getEndOffset() + ") " + e.getClass().getSimpleName());
		debug(e.getAttributes(), level);
		int eCount = e.getElementCount();
		for(int i = 0; i < eCount; i++) {
			debug(e.getElement(i), level + 1);
		}
	}

	public static boolean hasClass(AttributeSet attr, String className) {
		
		String classValue = (String) attr.getAttribute(Attribute.CLASS);
		
		if(classValue == null) {
			return false;
		}
		
		String[] classNames = classValue.split(" ");
		for(String c: classNames) {
			if(c.equals(className)) {
				return true;
			}
		}
		
		return false;
	}

	public static void addClass(MutableAttributeSet attr, String className) {
		
		String classValue = (String) attr.getAttribute(Attribute.CLASS);
		
		if(classValue == null || classValue.trim().equals("")) {
			attr.addAttribute(Attribute.CLASS, className);
		}
		else {
			attr.addAttribute(Attribute.CLASS, classValue + " " + className);
		}
		
	}
	
	public static void removeClass(MutableAttributeSet attr, String className) {
		
		String classValue = (String) attr.getAttribute(Attribute.CLASS);
		
		if(classValue == null || classValue.trim().equals("")) {
			attr.addAttribute(Attribute.CLASS, "");
			return;
		}
		
		List<String> classNames = new ArrayList<String>(Arrays.asList(classValue.split(" ")));
		classNames.remove(className);
		attr.addAttribute(Attribute.CLASS, StringUtils.join(classNames, " "));
	}

	public static void copyAttributes(MutableAttributeSet to,
			AttributeSet from, Object... names) {
		for(Object name: names) {
			Object value = from.getAttribute(name);
			if(value != null) {
				to.addAttribute(name, value);
			}
		}
	}
	
	public static void copyAllAttributesRemovingMarked(
			MutableAttributeSet to, AttributeSet from) {
		Enumeration<?> eNames = from.getAttributeNames();
		while(eNames.hasMoreElements()) {
			Object aName = eNames.nextElement();
			Object aValue = from.getAttribute(aName);
			if(aValue == Marker.TO_REMOVE) {
				to.removeAttribute(aName);
			}
			else {
				to.addAttribute(aName, aValue);
			}
		}
	}

	

	public static String ensureAcceptedCssProperties(String style, List<String> acceptedProperties) {
		StringBuilder sb = new StringBuilder();
		String propName;
		int i;
		for(String prop: style.split(";")) {
			i = prop.indexOf(":");
			if(i != -1) {
				propName = prop.substring(0, i).trim();
				if(acceptedProperties.contains(propName)) {
					sb.append(prop);
					sb.append(";");
				}
			}
		}
		return sb.toString();
	}

	public static Map<String, String> styleToMap(String style) {
		Map<String, String> map = new HashMap<String, String>();
		String[] properties = style.split(";");
		for(String p: properties) {
			String[] tokens = p.split(":");
			map.put(tokens[0].trim(), tokens[1].trim());
		}
 		return map;
	}

	public static Element getElementByTag(HTMLDocument htmlDoc,
			int offset, Tag tag) {
		
		Element e = htmlDoc.getCharacterElement(offset);
		while(e != null && !e.getAttributes().containsAttribute(StyleConstants.NameAttribute, tag)) {
			e = e.getParentElement();
		}
		
		return e;
	}

	public static int getIndexInParent(Element e) {
		Element parent = e.getParentElement();
		int count = parent.getElementCount();
		for(int i = 0; i < count; i++) {
			if(e == parent.getElement(i)) {
				return i;
			}
		}
		return -1;
	}

	public static void corrigeTabelas(ExtendedHTMLDocument doc, ExtendedHTMLEditorKit editorKit) {
		List<Element> tabelas = findElementByTag(doc, Tag.TABLE);
		for(Element tabela: tabelas) {
			corrigeTabela(doc, editorKit, tabela);
		}
	}

	private static void corrigeTabela(ExtendedHTMLDocument doc, ExtendedHTMLEditorKit editorKit, Element tabela) {
		
		int numLinhas = tabela.getElementCount();
		int qtColunas = -1;
		for(int i = 0; i < numLinhas; i++) {
			qtColunas = Math.max(qtColunas, tabela.getElement(i).getElementCount());
		}
		
		Element linha;
		String html;
		for(int i = 0; i < numLinhas; i++) {
			linha = tabela.getElement(i);
			if(linha.getElementCount() < qtColunas) {
				html = StringUtils.repeat("<td></td>", qtColunas - linha.getElementCount());
				doc.setEdicaoControlada(true);
				try {
					doc.insertBeforeEnd(linha, html);
				} catch (Exception e) {
					log.error("Falha ao corrigir tabela.", e);
				}
			}
		}
		
		// Retira tabela de parágrafo
		Element parent = tabela.getParentElement();
		if(parent.getAttributes().containsAttribute(StyleConstants.NameAttribute, Tag.P)) {
			StringWriter sw = new StringWriter();
			int startOffset = tabela.getStartOffset();
			int length = tabela.getEndOffset() - startOffset;
			try {
				editorKit.write(sw, doc, startOffset, length);
				doc.setEdicaoControlada(true);

				html = HTMLUtilities.getConteudoTag(sw.toString(), Tag.BODY);
				
				doc.setEdicaoControlada(true);
				doc.remove(startOffset, length);
				
				doc.setEdicaoControlada(true);
				editorKit.read(new StringReader(html), doc, startOffset);
				
			} catch (Exception e) {
				log.error("Falha ao corrigir tabela.", e);
			}
		}
		
	}

	private static List<Element> findElementByTag(ExtendedHTMLDocument doc,
			Tag tag) {
		return findElementByTag(doc.getDefaultRootElement(), tag);
	}

	private static List<Element> findElementByTag(Element e, Tag tag) {
		List<Element> list = new ArrayList<Element>();
		findElementByTagRec(list, e, tag);
		return list;
	}
	
	private static void findElementByTagRec(List<Element> list, 
			Element element, Tag tag) {
		if(element.getAttributes().containsAttribute(StyleConstants.NameAttribute, tag)) {
			list.add(element);
		}
		int count = element.getElementCount();
		for(int i = 0; i < count; i++) {
			findElementByTagRec(list, element.getElement(i), tag);
		}
	}

	public static void corrigePImplied(ExtendedHTMLDocument doc) {
		List<Element> tds = findElementByTag(doc, Tag.TD);
		for(Element td: tds) {
			if(td.getElementCount() > 0) {
				Element p = td.getElement(0);
				AttributeSet attrs = p.getAttributes();
				if(attrs.containsAttribute(StyleConstants.NameAttribute, Tag.IMPLIED) &&
						attrs.isDefined(CSS.Attribute.TEXT_ALIGN)) {
					SimpleAttributeSet s = new SimpleAttributeSet();
					s.addAttribute(StyleConstants.NameAttribute, Tag.P);
					doc.setParagraphAttributes(p.getStartOffset(), p.getEndOffset(), 
							s, false);
				}
			}
		}
	}

	public static String preparaSave(JTextPane jtpMain) {

		ExtendedHTMLEditorKit editorKit = (ExtendedHTMLEditorKit) jtpMain.getEditorKit();
		ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) jtpMain.getDocument();
		
		// Transforma p-implied dentro de table em p
		DocumentUtil.corrigePImplied(htmlDoc);
		
		List<String> colgroups = createColgroups(editorKit, htmlDoc);
		
		String html = jtpMain.getText();
		
		return HTMLUtilities.preparaSave(html, colgroups);
	}

	private static List<String> createColgroups(
			ExtendedHTMLEditorKit editorKit, ExtendedHTMLDocument htmlDoc) {
		
		List<String> colgroups = new ArrayList<String>();
		
		Map<Element, EkitTableView> views = editorKit.getTableViews(); 
		
		for(Element e: findElementByTag(htmlDoc.getDefaultRootElement(), Tag.TABLE)) {
			StringBuilder sb = new StringBuilder();
			EkitTableView view = views.get(e);
			if(view != null) {
				int[] columnSpans = view.getColumnSpans();
				
				if(columnSpans != null) {
					
					sb.append("<colgroup>");
					int tableWidth = 0;
					for(int span: columnSpans) {
						tableWidth += span;
					}
					for(int span: columnSpans) {
						float percent = (float)span / tableWidth * 100;
						sb.append("<col width=\"" + Math.round(percent) + "%\"/>");
					}
					sb.append("</colgroup>");
				}
			}
			colgroups.add(sb.toString());
		}
		
		
		return colgroups;
	}

}

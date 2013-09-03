/*
GNU Lesser General Public License

ExtendedHTMLEditorKit
Copyright (C) 2001  Frits Jalvingh, Jerry Pommer & Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.hexidec.ekit.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil;

/**
 * This class extends HTMLEditorKit so that it can provide other renderer
 * classes instead of the defaults. Most important is the part which renders
 * relative image paths.
 */

@SuppressWarnings("serial")
public class ExtendedHTMLEditorKit extends HTMLEditorKit {

	private static final Log log = LogFactory
			.getLog(ExtendedHTMLEditorKit.class);
	
	private Map<Element, EkitTableView> tableViews = new HashMap<Element, EkitTableView>();

	/**
	 * Constructor
	 */
	public ExtendedHTMLEditorKit() {
	}

	/**
	 * Method for returning a ViewFactory which handles the image rendering.
	 */
	public ViewFactory getViewFactory() {
		return new HTMLFactoryExtended();
	}
	
	public Map<Element, EkitTableView> getTableViews() {
		return tableViews;
	}

	public Document createDefaultDocument() {
		StyleSheet styles = getStyleSheet();
		StyleSheet ss = new StyleSheet();
		ss.addStyleSheet(styles);
		ExtendedHTMLDocument doc = new ExtendedHTMLDocument(ss);
		doc.setParser(getParser());
		doc.setAsynchronousLoadPriority(4);
		doc.setTokenThreshold(100);
		return doc;
	}

	@Override
	public void read(Reader in, Document doc, int pos) throws IOException,
			BadLocationException {
		String html = IOUtils.toString(in);
		
//		log.debug(str);

		html = HTMLUtilities.preparaPaste(html);

		ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) doc;
		
		html = htmlDoc.filterRead(html, pos);
		
		htmlDoc.setEdicaoControlada(false);
		
		if (StringUtils.isEmpty(html)) {
			return;
		}
		
		if (HTMLUtilities.isInline(html)) {
			html = "<span>" + html + "</span>";
			insertHTML((HTMLDocument) doc, pos, html, 0, 0, Tag.SPAN);
		} else if(DocumentUtil.getElementByTag((HTMLDocument) doc, pos, Tag.TD) != null) {
			// Retira parágrafos ao colar dentro de célula para evitar quebra da tabela.
			html = "<span>" + HTMLUtilities.removeTag(html, Tag.P) + "<span>";
			insertHTML((HTMLDocument) doc, pos, html, 0, 0, Tag.SPAN);
		} else {
			
			if(pos == doc.getLength() && HTMLUtilities.terminaComTabela(html)) {
				// Evita que o texto termine com tabela,
				// caso em que não se consegue apagar a tabela devido a bug do swing.
				html += "<p></p>";
			}
			
			super.read(new StringReader(html), doc, pos);
		}
		
		// Verifica se leu alguma célula de tabela.
		if(HTMLUtilities.contemTag(html, Tag.TD)) {
			DocumentUtil.corrigeTabelas(htmlDoc, this);
		}
		
	}
	
	public StyleSheet getStyleSheet() {
		try {
			StyleSheet defaultStyles = new StyleSheet();
			InputStream is = getClass().getResourceAsStream("/ekit.css");
			Reader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
			defaultStyles.loadRules(r, null);
			r.close();
			
			return defaultStyles;
		} catch (Throwable e) {
			log.error("Falha ao ler ekit.css. Utilizando estilos padrão.", e);
			return super.getStyleSheet();
		}
	}

	/* Inner Classes --------------------------------------------- */

	/**
	 * Class that replaces the default ViewFactory and supports the proper
	 * rendering of both URL-based and local images.
	 */
	public class HTMLFactoryExtended extends HTMLFactory implements
			ViewFactory {

		public View create(Element elem) {
			
			Object obj = elem.getAttributes().getAttribute(
					StyleConstants.NameAttribute);
			
			if (obj instanceof HTML.Tag) {
				HTML.Tag tagType = (HTML.Tag) obj;
				
				if (tagType == HTML.Tag.HTML) {
					tableViews.clear();
				}
				else if (tagType == HTML.Tag.IMG) {
					return new RelativeImageView(elem);
				}
				else if (tagType == HTML.Tag.TABLE) {
					EkitTableView view = new EkitTableView(elem);
					tableViews.put(elem, view);
					return view;
				}
			}
			
			return super.create(elem);
		}
	}

}

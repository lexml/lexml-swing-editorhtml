/*
GNU Lesser General Public License

PropertiesDialog
Copyright (C) 2003 Frits Jalvingh, Jerry Pommer & Howard Kistler

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil;

@SuppressWarnings("serial")
public class ExtendedHTMLDocument extends HTMLDocument {

	private static final Log log = LogFactory.getLog(ExtendedHTMLDocument.class);
	
	private boolean inlineEdit;
	private List<HTMLDocumentBehavior> behaviors = new ArrayList<HTMLDocumentBehavior>();
	
	private static final List<Tag> acceptedCommonTags = Arrays.asList(
			new Tag[] { Tag.HTML, Tag.HEAD, Tag.STYLE, Tag.BODY });
	
	private static final List<Tag> tableTags = new ArrayList<Tag>();
	
	private static final List<Tag> acceptedInlineTags = new ArrayList<Tag>();

	private static final List<Tag> acceptedTags = new ArrayList<Tag>();
	
	private static final Map<Tag, List<Attribute>> acceptedAttributes = new HashMap<Tag, List<Attribute>>();
	
	private static final List<String> acceptedBlockCssProperties = Arrays.asList(
			new String[] {"text-indent", "margin-left"});
	
	private static final List<String> acceptedInlineCssProperties = Arrays.asList(
			new String[] {"font-weight", "font-style"});
	
	private static final List<String> acceptedInlineTagsAsString = new ArrayList<String>();
	
	private boolean edicaoControlada;
	
	private int tableDepth;
	
	private boolean dirty;

	static {
		// Table Tags
		tableTags.addAll(Arrays.asList(
				new Tag[] {Tag.TABLE, Tag.TR, Tag.TD}));
		
		// Inline tags
		acceptedInlineTags.addAll(acceptedCommonTags);
		acceptedInlineTags.addAll(Arrays.asList(
				new Tag[] {Tag.B, Tag.I, Tag.U, Tag.FONT, Tag.SUB, Tag.SUP, Tag.SPAN}));
		
		// All tags
		acceptedTags.addAll(acceptedInlineTags);
		acceptedTags.addAll(Arrays.asList(
				new Tag[] {Tag.P, Tag.BR, Tag.UL, Tag.LI}));
		acceptedTags.addAll(tableTags);
		
		// Accepted attributes
		acceptedAttributes.put(Tag.STYLE, Arrays.asList(new Attribute[] {Attribute.TYPE}));
		acceptedAttributes.put(Tag.FONT, Arrays.asList(new Attribute[] {Attribute.STYLE}));
		acceptedAttributes.put(Tag.SPAN, Arrays.asList(new Attribute[] {Attribute.CLASS, Attribute.STYLE}));
		acceptedAttributes.put(Tag.P, Arrays.asList(new Attribute[] {Attribute.CLASS, Attribute.STYLE, Attribute.ALIGN}));
		acceptedAttributes.put(Tag.TD, Arrays.asList(new Attribute[] {Attribute.ALIGN}));
		
		// Inline tags as String
		for(Tag t: acceptedInlineTags) {
			acceptedInlineTagsAsString.add(t.toString());
		}
	}
	
	private DocumentFilterChainManager filters = new DocumentFilterChainManager();
	
	public ExtendedHTMLDocument(AbstractDocument.Content c, StyleSheet styles) {
		super(c, styles);
		setup();
	}

	public ExtendedHTMLDocument(StyleSheet styles) {
		super(styles);
		setup();
	}

	public ExtendedHTMLDocument() {
		setup();
	}
	
	private void setup() {
		setDocumentFilter(filters);
	}
	
	public boolean isEdicaoControlada() {
		return edicaoControlada;
	}
	
	public void setEdicaoControlada(boolean edicaoControlada) {
		this.edicaoControlada = edicaoControlada;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void resetDirty() {
		dirty = false;
	}
	
	public void addFilter(ExtendedHTMLDocumentFilter filter) {
		filters.addFilter(filter);
	}

	public void replaceAttributes(Element e, AttributeSet a, Tag tag) {
		if ((e != null) && (a != null)) {
			try {
				writeLock();
				int start = e.getStartOffset();
				DefaultDocumentEvent changes = new DefaultDocumentEvent(start,
						e.getEndOffset() - start,
						DocumentEvent.EventType.CHANGE);
				AttributeSet sCopy = a.copyAttributes();
				changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
				MutableAttributeSet attr = (MutableAttributeSet) e
						.getAttributes();
				Enumeration aNames = attr.getAttributeNames();
				Object value;
				Object aName;
				while (aNames.hasMoreElements()) {
					aName = aNames.nextElement();
					value = attr.getAttribute(aName);
					if (value != null
							&& !value.toString().equalsIgnoreCase(
									tag.toString())) {
						attr.removeAttribute(aName);
					}
				}
				attr.addAttributes(a);
				changes.end();
				fireChangedUpdate(changes);
				fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
			} finally {
				writeUnlock();
			}
		}
	}

	@Override
	public void setParagraphAttributes(int offset, int length, AttributeSet s,
			boolean replace) {
		for (HTMLDocumentBehavior b : behaviors) {
			s = b.beforeSetParagraphAttributes(this, offset, length, s, replace);
		}
		
		try {
			writeLock();
			// Make sure we send out a change for the length of the paragraph.
			int end = Math.min(offset + length, getLength());
			Element e = getParagraphElement(offset);
			offset = e.getStartOffset();
			e = getParagraphElement(end);
			length = Math.max(0, e.getEndOffset() - offset);
			DefaultDocumentEvent changes = new DefaultDocumentEvent(offset,
					length, DocumentEvent.EventType.CHANGE);
			AttributeSet sCopy = s.copyAttributes();
			int lastEnd = Integer.MAX_VALUE;
			for (int pos = offset; pos <= end; pos = lastEnd) {
				Element paragraph = getParagraphElement(pos);
				if (lastEnd == paragraph.getEndOffset()) {
					lastEnd++;
				} else {
					lastEnd = paragraph.getEndOffset();
				}
				MutableAttributeSet attr = (MutableAttributeSet) paragraph
						.getAttributes();
				changes.addEdit(new AttributeUndoableEdit(paragraph, sCopy,
						replace));
				if (replace) {
					attr.removeAttributes(attr);
				}
				
				DocumentUtil.copyAllAttributesRemovingMarked(attr, s);
			}
			changes.end();
			fireChangedUpdate(changes);
			fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
		} finally {
			writeUnlock();
		}
	}

	@Override
	public ParserCallback getReader(int pos) {
		return new ParserCallbackWrapper(super.getReader(pos));
	}

	@Override
	public ParserCallback getReader(int pos, int popDepth, int pushDepth,
			Tag insertTag) {
		return new ParserCallbackWrapper(super.getReader(pos, popDepth,
				pushDepth, insertTag));
	}

	public boolean isInlineEdit() {
		return inlineEdit;
	}

	public void setInlineEdit(boolean inlineEdit) {
		this.inlineEdit = inlineEdit;
	}

	public List<HTMLDocumentBehavior> getBehaviors() {
		return behaviors;
	}

	public void addBehavior(HTMLDocumentBehavior b) {
		behaviors.add(b);
	}

	public String filterRead(String str, int pos) {
		
		beforeRead(pos);
		
		for(HTMLDocumentBehavior b: behaviors) {
			str = b.filterRead(str, this, pos);
		}
		return str;
	}
	
	private void beforeRead(int pos) {
		dirty = true;
		tableDepth = DocumentUtil.getElementByTag(this, pos, Tag.TABLE) == null? 0: 1;
	}
	
	// ------------------------------------------------------------------------------
	private class ParserCallbackWrapper extends ParserCallback {
		
		private ParserCallback reader;
		
		public ParserCallbackWrapper(ParserCallback reader) {
			this.reader = reader;
		}

		public void flush() throws BadLocationException {
			reader.flush();
		}

		public void handleText(char[] data, int pos) {
//			log.debug("handleText: " + new String(data));
			// Corrige problema de colar caractere 0 no final
			if(!(data.length == 1 && data[0] == 0)) {
				reader.handleText(data, pos);
			}
		}

		public void handleComment(char[] data, int pos) {
			reader.handleComment(data, pos);
		}

		public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
			
			if(t == Tag.TABLE) {
				tableDepth++;
			}
			
			if(isAccepted(t)) {
				a = filterAttributeSet(t, a);
				if(t == Tag.TABLE) {
					a.addAttribute(Attribute.WIDTH, "100%");
				}
				else if(t == Tag.TD) {
					a.addAttribute(Attribute.VALIGN, "top");
				}
				reader.handleStartTag(t, a, pos);
			}
		}

		public void handleEndTag(Tag t, int pos) {
			
			if(t == Tag.TABLE) {
				tableDepth--;
			}
			
			if(isAccepted(t)) {
				reader.handleEndTag(t, pos);
			}
		}

		public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
			if(isAccepted(t)) {
				a = filterAttributeSet(t, a);
				reader.handleSimpleTag(t, a, pos);
			}
		}

		public void handleError(String errorMsg, int pos) {
			reader.handleError(errorMsg, pos);
		}

		public void handleEndOfLineString(String eol) {
			reader.handleEndOfLineString(eol);
		}

		private boolean isAccepted(Tag t) {
			
			// Não permite tabela aninhada
			if(tableTags.contains(t) && tableDepth > 1) {
				return false;
			}
			
			return inlineEdit? acceptedInlineTags.contains(t): acceptedTags.contains(t);
		}

		private MutableAttributeSet filterAttributeSet(Tag t, MutableAttributeSet a) {
			for(Object aName: Collections.list((Enumeration<Object>)a.getAttributeNames())) {
				List<Attribute> lAttr = acceptedAttributes.get(t);
				if(lAttr == null || !lAttr.contains(aName)) {
					a.removeAttribute(aName);
				}
				else {
					if(aName == HTML.Attribute.STYLE) {
						//System.out.println(">> " + aName + ": " + a.getAttribute(aName));
						if(t.isBlock()) {
							a.addAttribute(aName, DocumentUtil.ensureAcceptedCssProperties(
									(String) a.getAttribute(aName), acceptedBlockCssProperties));
						}
						else {
							a.addAttribute(aName, DocumentUtil.ensureAcceptedCssProperties(
									(String) a.getAttribute(aName), acceptedInlineCssProperties));
						}
					}
				}
			}
			return a;
		}

	}
	
	private class DocumentFilterChainManager extends DocumentFilter {
		
		private List<ExtendedHTMLDocumentFilter> filters = 
				new ArrayList<ExtendedHTMLDocumentFilter>();
		
		public void addFilter(ExtendedHTMLDocumentFilter filter) {
			if(!filters.contains(filter)) {
				filters.add(filter);
			}
		}
		
		@Override
		public void insertString(FilterBypass fb, int offset, String string,
				AttributeSet attr) throws BadLocationException {
			replace(fb, offset, 0, string, attr);
		}
		
		@Override
		public void remove(FilterBypass fb, int offset, int length)
				throws BadLocationException {
			replace(fb, offset, length, "", null);
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs) throws BadLocationException {
			dirty = true;
			if (inlineEdit) text = text.replaceAll("(\\r|\\n)", " ");
			new DocumentFilterChain(filters).replace(fb, offset, length, text, attrs);
			edicaoControlada = false;
		}
		
	}
	
	public static class DocumentFilterChain {
		
		private Queue<ExtendedHTMLDocumentFilter> filters;
		
		private DocumentFilterChain(List<ExtendedHTMLDocumentFilter> filters) {
			this.filters = new LinkedList<ExtendedHTMLDocumentFilter>(filters);
		}

		public void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs) throws BadLocationException {
			
			ExtendedHTMLDocumentFilter filter = filters.poll();
			if(filter != null) {
				filter.replace(fb, offset, length, text, attrs, this);
			}
			else {
//				log.info("fb.replace(" + offset + ", " + length + ", \"" + text + "\", " + attrs + ")");
				fb.replace(offset, length, text, attrs);
			}
		}
		
	}
	
	public static abstract class ExtendedHTMLDocumentFilter {
		
		public abstract void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs, DocumentFilterChain chain) 
				throws BadLocationException;
		
	}

	public static List<String> getAcceptedInlineTags() {
		return acceptedInlineTagsAsString;
	}

}

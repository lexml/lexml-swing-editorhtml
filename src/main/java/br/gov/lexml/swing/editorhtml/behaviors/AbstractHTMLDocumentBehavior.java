package br.gov.lexml.swing.editorhtml.behaviors;

import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;

import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.HTMLDocumentBehavior;

public class AbstractHTMLDocumentBehavior implements HTMLDocumentBehavior {

	@Override
	public Action getAction(String keyTool) {
		return null;
	}
	
	@Override
	public void initializeDocument(JTextPane textPane) {
	}

	@Override
	public AttributeSet beforeSetParagraphAttributes(ExtendedHTMLDocument doc,
			int offset, int length, AttributeSet s, boolean replace) {
		return s;
	}

	@Override
	public String filterRead(String html, ExtendedHTMLDocument doc, int offset) {
		return html;
	}
	
	@Override
	public void afterClearFormat(JTextPane textPane) {
	}
	
}

package com.hexidec.ekit.component;

import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;

public interface HTMLDocumentBehavior {
	
	Action getAction(String keyTool);
	
	void initializeDocument(JTextPane textPane);
	
	AttributeSet beforeSetParagraphAttributes(ExtendedHTMLDocument doc, int offset, int length,
			AttributeSet s, boolean replace);
	
	String filterRead(String html, ExtendedHTMLDocument doc, int offset);

	void afterClearFormat(JTextPane textPane);
	
}

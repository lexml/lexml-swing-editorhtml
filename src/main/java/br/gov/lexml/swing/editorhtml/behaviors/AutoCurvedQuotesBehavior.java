package br.gov.lexml.swing.editorhtml.behaviors;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.ExtendedHTMLDocument.DocumentFilterChain;
import com.hexidec.ekit.component.ExtendedHTMLDocument.ExtendedHTMLDocumentFilter;

public class AutoCurvedQuotesBehavior extends AbstractHTMLDocumentBehavior {
	
	private static final Log log = LogFactory.getLog(AutoCurvedQuotesBehavior.class);
	
	private ExtendedHTMLDocument doc;
	
	public AutoCurvedQuotesBehavior() {
	}
	
	@Override
	public void initializeDocument(JTextPane textPane) {
		doc = (ExtendedHTMLDocument) textPane.getDocument();
		doc.addFilter(new MyDocumentFilter());
	}

	private class MyDocumentFilter extends ExtendedHTMLDocumentFilter {
		
		@Override
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs, DocumentFilterChain chain)
				throws BadLocationException {
			
			if(text.equals("\"")) {
				fb.replace(offset, length, text, attrs);
				boolean open = shouldOpenQuote(doc, offset);
				String curvedQuote = open? "“": "”";
				if(open) {
					attrs = doc.getCharacterElement(offset + 1).getAttributes();
				}
				chain.replace(fb, offset, 1, curvedQuote, attrs);
				return;
			}
			
			chain.replace(fb, offset, length, text, attrs);
		}

		private boolean shouldOpenQuote(HTMLDocument doc, int offset) {
			try {
				return offset <= 1 || doc.getText(offset - 1, 1).equals(" ") ||
					!doc.getParagraphElement(offset).equals(doc.getParagraphElement(offset - 1));
			} catch (BadLocationException e) {
				log.error(e.getMessage(), e);
				return false;
			}
		}
		
	}

}

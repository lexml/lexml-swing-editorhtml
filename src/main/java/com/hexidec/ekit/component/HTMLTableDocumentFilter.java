package com.hexidec.ekit.component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.Element;
import javax.swing.text.html.HTML.Tag;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil;

import com.hexidec.ekit.component.ExtendedHTMLDocument.DocumentFilterChain;
import com.hexidec.ekit.component.ExtendedHTMLDocument.ExtendedHTMLDocumentFilter;

public class HTMLTableDocumentFilter extends ExtendedHTMLDocumentFilter {

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text,
			AttributeSet attrs, DocumentFilterChain chain)
			throws BadLocationException {
		
		ExtendedHTMLDocument htmlDoc = (ExtendedHTMLDocument) fb.getDocument();
		
		if (HTMLTableDocumentFilter.bloquearEdicaoDeTabela(htmlDoc, offset, length, text)) {
			return;
		}

		chain.replace(fb, offset, length, text, attrs);
	}

	public static boolean bloquearEdicaoDeTabela(ExtendedHTMLDocument htmlDoc,
			int offset, int length, String text) throws BadLocationException {
		
		// Não bloqueia edição controlada pelo editor.
		if(htmlDoc.isEdicaoControlada()) {
			return false;
		}
		
		Element eTable = DocumentUtil.getElementByTag(htmlDoc, offset, Tag.TABLE);
		if(eTable != null && eTable.getStartOffset() == offset && 
				eTable.getEndOffset() == (offset + length)) {
			// Se a edição englobar toda a tabela tudo bem
			return false;
		}
		
		// Verifica se a edição passa por mais de uma célula
		Element eStart = DocumentUtil.getElementByTag(htmlDoc, offset, Tag.TD);
		Element eEnd = DocumentUtil.getElementByTag(htmlDoc, offset + length,
				Tag.TD);
		
		return eStart != eEnd;
	}
}

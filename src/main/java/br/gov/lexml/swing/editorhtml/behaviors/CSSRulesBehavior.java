package br.gov.lexml.swing.editorhtml.behaviors;

import javax.swing.JTextPane;
import javax.swing.text.html.StyleSheet;

import com.hexidec.ekit.component.ExtendedHTMLDocument;

public class CSSRulesBehavior extends AbstractHTMLDocumentBehavior {
	
	private String[] rules;

	public CSSRulesBehavior(String... rules) {
		this.rules = rules;
	}

	@Override
	public void initializeDocument(JTextPane textPane) {
		StyleSheet ss = ((ExtendedHTMLDocument)textPane.getDocument()).getStyleSheet();
		for(String rule: rules) {
			ss.addRule(rule);
		}
	}
	
}

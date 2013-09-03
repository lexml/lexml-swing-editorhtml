package br.gov.lexml.swing.editorhtml;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import br.gov.lexml.swing.editorhtml.behaviors.AutoFirstLineIndentBehavior;
import br.gov.lexml.swing.editorhtml.behaviors.CSSRulesBehavior;
import br.gov.lexml.swing.editorhtml.handlers.CardLayoutMaximizeHandler;

public class MaximizarEditor {

	public static void main(String[] args) {

		boolean inlineEdit = false;
		
		EditorHtmlFactory factory = new EditorHtmlFactory();
		factory.setInlineEdit(inlineEdit);
		factory.addToToolbar(EditorHtmlFactory.TOOLBAR_DEFAULT);
		if(!inlineEdit) {
			factory.addToToolbar("SP");
			factory.addToToolbar(EditorHtmlFactory.TOOLBAR_PARAGRAPH);
		}
		factory.addToToolbar("SP", "MX");
		
		factory.addHTMLDocumentBehavior(new CSSRulesBehavior("p { text-align: justify; }"));
		factory.addHTMLDocumentBehavior(new AutoFirstLineIndentBehavior());

		final EditorHtml e = factory.createEditorHtml();
//		e.setDocumentText("<p style='text-indent: 2cm;'>Testando 123 Testando <b>123</b></p>");
		e.setDocumentText("<p>Testando 123 Testando <b>123</b></p>");

		JFrame f = new JFrame("HtmlEditor");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setBounds(0, 0, 800, 800);
		f.getContentPane().setLayout(new BorderLayout());
		
		final CardLayout cl = new CardLayout();
		final JPanel pCards = new JPanel(cl);
		f.getContentPane().add(pCards, BorderLayout.CENTER);
		
		final JPanel pCentro = new JPanel(new BorderLayout());
		pCentro.add(Box.createVerticalStrut(50), BorderLayout.NORTH);
		pCentro.add(Box.createVerticalStrut(150), BorderLayout.SOUTH);
		pCentro.add(Box.createHorizontalStrut(100), BorderLayout.WEST);
		
		pCards.add(pCentro, CardLayoutMaximizeHandler.DEFAULT_MINIMIZED_CARD_KEY);
		
		CardLayoutMaximizeHandler mh = new CardLayoutMaximizeHandler(e, pCards) {
			
			@Override
			public void addEditorToDefaultContainer() {
				pCentro.add(e, BorderLayout.CENTER);
			}
		};
		
		mh.addEditorToDefaultContainer();
		
		e.setMaximizeHandler(mh);
		
		// f.pack();
		
		f.setVisible(true);
		
	}
	
}

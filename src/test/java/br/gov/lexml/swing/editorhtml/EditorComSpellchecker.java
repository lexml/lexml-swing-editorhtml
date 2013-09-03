package br.gov.lexml.swing.editorhtml;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import br.gov.lexml.swing.editorhtml.behaviors.SpellcheckBehavior;

public class EditorComSpellchecker {

	public static void main(String[] args) throws Exception {

		boolean inlineEdit = false;
		
		EditorHtmlFactory factory = new EditorHtmlFactory();
		factory.setInlineEdit(inlineEdit);
		factory.addToToolbar(EditorHtmlFactory.TOOLBAR_DEFAULT);
		if(!inlineEdit) {
			factory.addToToolbar("SP");
			factory.addToToolbar(EditorHtmlFactory.TOOLBAR_PARAGRAPH);
		}
		factory.addToToolbar("SP", SpellcheckBehavior.KEY_TOOL_SPELLCHECK_DIALOG);
		
		File baseDir = new File("target/spellchecker");
		if(!baseDir.isDirectory()) {
			baseDir.mkdirs();
		}
		SpellcheckBehavior sb = new SpellcheckBehavior(baseDir);
		factory.addHTMLDocumentBehavior(sb);
		
		final EditorHtml e = factory.createEditorHtml();
		
		e.setDocumentText("");
//		e.setDocumentText("<p>Testanxdo 123 Árvore ação asim Testanxdo Lauro fillo art. 29<b>123</b></p>");
//		e.setDocumentText("<p>Testando 123 Árvore ação Testando Lauro filho art. 29<b>123</b></p>");

		final JFrame f = new JFrame("HtmlEditor");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 600, 600);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(e, BorderLayout.CENTER);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				f.setVisible(true);
			}
			
		});
		
	}
	
}

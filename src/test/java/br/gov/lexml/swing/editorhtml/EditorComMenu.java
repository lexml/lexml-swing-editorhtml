package br.gov.lexml.swing.editorhtml;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import br.gov.lexml.swing.editorhtml.behaviors.AutoCurvedQuotesBehavior;
import br.gov.lexml.swing.editorhtml.behaviors.AutoFirstLineIndentBehavior;
import br.gov.lexml.swing.editorhtml.behaviors.CSSRulesBehavior;
import br.gov.lexml.swing.editorhtml.behaviors.OmissisBehavior;
import br.gov.lexml.swing.editorhtml.behaviors.ToggleParagraphIndentBehavior;

import com.hexidec.ekit.EkitCore;

public class EditorComMenu {

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {

		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				init();
			}
			
		});
		
	}
	
	private static void init() {
		boolean inlineEdit = false;
		
		EditorHtmlFactory factory = new EditorHtmlFactory();
		factory.setInlineEdit(inlineEdit);
		factory.addToToolbar(EditorHtmlFactory.TOOLBAR_DEFAULT);
		if(!inlineEdit) {
			factory.addToToolbar("SP");
			factory.addToToolbar(EditorHtmlFactory.TOOLBAR_PARAGRAPH);
			factory.addToToolbar("SP");
			factory.addToToolbarAtPosition(EkitCore.KEY_TOOL_ALIGNJ, "SP", ToggleParagraphIndentBehavior.KEY_TOOL_TOGGLE_PARAGRAPH_INDENT);
			factory.addToToolbar("SP");
			factory.addToToolbarAtPosition(EkitCore.KEY_TOOL_SPECIAL_CHAR, OmissisBehavior.KEY_TOOL_OMISSIS);
			
			factory.addHTMLDocumentBehavior(new OmissisBehavior());
			factory.addHTMLDocumentBehavior(new CSSRulesBehavior("p { text-align: justify; }"));
			factory.addHTMLDocumentBehavior(new AutoFirstLineIndentBehavior());
			factory.addHTMLDocumentBehavior(new ToggleParagraphIndentBehavior());
		}
		
		factory.addHTMLDocumentBehavior(new AutoCurvedQuotesBehavior());
		
		final EditorHtml e = factory.createEditorHtml();
//		e.setDocumentText("<p>Testando</p>");
		
//		e.setDocumentText("<p>Testando 123 Testando <b>123</b>" +
//		e.setDocumentText("<p style=\"text-indent: 0cm; margin-left: 4.5cm; \">Testando 123 Testando <b>123</b>" +
//		e.setDocumentText("<p style=\"margin-left: 3cm; text-indent: 0cm; \">Testando 123 Testando <b>123</b>" +
//				" Testando 123 Testando <b>123</b> Testando 123 Testando <b>123</b>" +
//				" Testando 123 Testando <b>123</b> Testando 123 Testando <b>123</b>" +
//				" Testando 123 Testando <b>123</b> Testando 123 Testando <b>123</b>" +
//				" Testando 123 Testando <b>123</b> Testando 123 Testando <b>123</b>" +
//				" Testando 123 Testando <b>123</b> ............" +
//				"</p>");

//		e.setDocumentText("<p>Testando 123\n Testando <b>123</b></p>");
//		e.setDocumentText("<p class=\"no_indent\" align=\"center\">Testando 123 Testando <b>123</b></p>");

		e.setDocumentText("<table>" +
				"<tr><th>Header 1</th><th>Header 2</th></tr>" +
				"<tr><td>Data 1</td><td>Data 2</td></tr>" +
//				"<tr><th><p>Header 1</p></th><th><p>Header 2</p></th></tr>" +
//				"<tr><td><p>Data 1</p></td><td><p>Data 2</p></td></tr>" +
				"</table>");

//		e.setDocumentText("<p>Tabela:</p><p> </p>" +
//				"<p>xxx<table>" +
//				"<tr><th>Header 1</th><th>Header 2</th></tr>" +
//				"<tr><td>Data 1</td><td>Data 2</td></tr>" +
////				"<tr><th><p>Header 1</p></th><th><p>Header 2</p></th></tr>" +
////				"<tr><td><p>Data 1</p></td><td><p>Data 2</p></td></tr>" +
//"</table>mmm</p>" + 
//				"<p> </p>");
//		
		JFrame f = new JFrame("HtmlEditor");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setJMenuBar(e.getMenuBar());
		f.setBounds(100, 100, 700, 600);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(e, BorderLayout.CENTER);
		f.setVisible(true);
		
//		System.out.println(e.getDocumentBody());
	}
	
}

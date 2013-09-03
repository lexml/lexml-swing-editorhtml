package br.gov.lexml.swing.editorhtml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import br.gov.lexml.swing.editorhtml.behaviors.CSSRulesBehavior;

import com.hexidec.ekit.component.HTMLDocumentBehavior;

public class EditorHtmlFactory {

	public static final String[] TOOLBAR_DEFAULT = {"CT", "CP", "PS", "SP", "UN", "RE", "SP", "BL", "IT", "UD", "SP", "SU", "SB", "CH", "SP", "CF"};
	public static final String[] TOOLBAR_PARAGRAPH = {"AL", "AC", "AR", "AJ", "SP", "TM"};

	private boolean inlineEdit;
	private List<HTMLDocumentBehavior> behaviors = new ArrayList<HTMLDocumentBehavior>();
	private List<String> toolbarList = new ArrayList<String>();
	private boolean debugMode;
	
	public EditorHtmlFactory() {
		//
	}
	
	public EditorHtml createEditorHtml() {
		
		// Tem que ser após aplicação dos demais behaviors.
		addHTMLDocumentBehavior(new CSSRulesBehavior("td p, th p { text-indent: 0; }"));
		
		String strToolbar = StringUtils.join(toolbarList, "|");
		
		return new EditorHtml(inlineEdit, behaviors, strToolbar, debugMode);
	}
	
	public void setInlineEdit(boolean inlineEdit) {
		this.inlineEdit = inlineEdit;
	}
	
	public void addHTMLDocumentBehavior(HTMLDocumentBehavior b) {
		behaviors.add(b);
	}
	
	public void addToToolbar(String... botoes) {
		toolbarList.addAll(Arrays.asList(botoes));
	}
	
	public void addToToolbarAtPosition(String botaoAnterior, String... botoes) {
		if(botaoAnterior == null) {
			toolbarList.addAll(0, Arrays.asList(botoes));
		}
		else {
			toolbarList.addAll(toolbarList.indexOf(botaoAnterior) + 1, Arrays.asList(botoes));
		}
	}
	
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

}

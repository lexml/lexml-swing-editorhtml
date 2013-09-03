package br.gov.lexml.swing.editorhtml.behaviors;

import java.io.File;

import javax.swing.Action;
import javax.swing.JTextPane;

import br.gov.lexml.swing.spellchecker.SpellcheckManager;
import br.gov.lexml.swing.spellchecker.SpellcheckerFactory;
import br.gov.lexml.swing.spellchecker.SpellcheckerInitializationException;
import br.gov.lexml.swing.spellchecker.dialog.SpellcheckDialogAction;

/**
 * 	Aplica a funcionalidade de correção ortográfica a um EditorHtml.
 * 
 *  <p>
 *  Não pode ser reutilizado entre vários EditorHtml.
 *  </p>
 */
public class SpellcheckBehavior extends AbstractHTMLDocumentBehavior {

	public static final String KEY_TOOL_SPELLCHECK_DIALOG = "SC";
	
	private File baseDir;
	private SpellcheckManager mgr;
	private SpellcheckDialogAction action;

	public SpellcheckBehavior(File baseDir) throws SpellcheckerInitializationException {
		
		// Apenas para testar se o spellchecker pode ser criado (não onera por causa do cache)
		SpellcheckerFactory.getInstance().createSpellchecker(baseDir);
		
		this.baseDir = baseDir;
		action = new SpellcheckDialogAction();
	}
	
	@Override
	public Action getAction(String keyTool) {
		return KEY_TOOL_SPELLCHECK_DIALOG.equals(keyTool)? action: null;
	}

	@Override
	public void initializeDocument(final JTextPane textPane) {
		if(mgr == null) {
			try {
				mgr = new SpellcheckManager(baseDir, textPane);
			} catch (SpellcheckerInitializationException e) {
				// Já teria ocorrido no construtor
			}
			action.setSpellcheckManager(mgr);
		}
		else {
			mgr.registerDocument(textPane.getDocument());
		}
	}
	
	public Action getSpellcheckDialogAction() {
		return action;
	}

}

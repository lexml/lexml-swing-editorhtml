package br.gov.lexml.swing.editorhtml.behaviors;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.CSS.Attribute;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil.Marker;
import br.gov.lexml.swing.editorhtml.util.LengthUnit;

public class ToggleParagraphIndentBehavior extends AbstractHTMLDocumentBehavior {
	
	private static final Log log = LogFactory.getLog(ToggleParagraphIndentBehavior.class);

	public static final String KEY_TOOL_TOGGLE_PARAGRAPH_INDENT = "TPI";

	private Action action;

	public ToggleParagraphIndentBehavior() {
		action = new ToggleParagraphIndentAction("Recuo de parágrafo");
		action.putValue(Action.SHORT_DESCRIPTION, "Recuo de parágrafo");
		action.putValue(Action.LONG_DESCRIPTION, "Recuo de parágrafo");
		action.putValue(Action.SMALL_ICON, loadIcon());
	}

	@Override
	public Action getAction(String keyTool) {
		return KEY_TOOL_TOGGLE_PARAGRAPH_INDENT.equals(keyTool)? action: null;
	}

	private ImageIcon loadIcon() {
		URL imageURL = getClass().getResource("/icons/ToggleParagraphIndent.png");
		if (imageURL != null) {
			return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageURL));
		}
		return null;
	}

	@SuppressWarnings("serial")
	public static class ToggleParagraphIndentAction extends StyledTextAction {

		public ToggleParagraphIndentAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			JEditorPane editor = getEditor(e);
			if (editor != null) {
				int caretOffset = editor.getSelectionStart();
				
			    HTMLDocument doc = (HTMLDocument) getStyledDocument(editor);
			    
//			    DocumentUtil.debug(doc);
			    
				AttributeSet asParagraph = doc.getParagraphElement(caretOffset).getAttributes();
				
				Object leftMarginValue = LengthUnit.getValue(4, "cm");
				Object noIndentValue = 0f;
				if(asParagraph.isDefined(Attribute.MARGIN_LEFT)) {
					leftMarginValue = Marker.TO_REMOVE;
					noIndentValue = Marker.TO_REMOVE;
				}
				
				MutableAttributeSet attr = new SimpleAttributeSet();
				attr.addAttribute(StyleConstants.LeftIndent, leftMarginValue);
				attr.addAttribute(StyleConstants.FirstLineIndent, noIndentValue);
				setParagraphAttributes(editor, attr, false);
				
//			    log.debug(editor.getText());
			}
		}
		
	}
	
}

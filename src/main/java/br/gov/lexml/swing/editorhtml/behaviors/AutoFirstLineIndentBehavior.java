package br.gov.lexml.swing.editorhtml.behaviors;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil;

import com.hexidec.ekit.component.ExtendedHTMLDocument;

public class AutoFirstLineIndentBehavior extends CSSRulesBehavior {
	
	public static final String NO_INDENT_CLASS = "no_indent";
	
	public AutoFirstLineIndentBehavior() {
		super("p { text-indent: 2.5cm; }", "." + NO_INDENT_CLASS + " { text-indent: 0; }");
	}
	
	@Override
	public AttributeSet beforeSetParagraphAttributes(ExtendedHTMLDocument doc, int offset, int length,
			AttributeSet s, boolean replace) {
		
		Integer alignment = (Integer) s.getAttribute(StyleConstants.Alignment);
		
		if(alignment == null) {
			return s;
		}
		
		MutableAttributeSet attr = new SimpleAttributeSet(s);

		if(alignment == StyleConstants.ALIGN_LEFT || alignment == StyleConstants.ALIGN_JUSTIFIED) {
			attr.addAttribute(Attribute.CLASS, "");
		}
		else if(alignment == StyleConstants.ALIGN_CENTER || alignment == StyleConstants.ALIGN_RIGHT) {
			attr.addAttribute(Attribute.CLASS, NO_INDENT_CLASS);
		}
		
		return attr;
	}

}

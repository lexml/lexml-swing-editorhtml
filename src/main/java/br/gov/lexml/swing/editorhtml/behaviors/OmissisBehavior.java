package br.gov.lexml.swing.editorhtml.behaviors;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.editorhtml.util.EditContext;

import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.ExtendedHTMLDocument.DocumentFilterChain;
import com.hexidec.ekit.component.ExtendedHTMLDocument.ExtendedHTMLDocumentFilter;

public class OmissisBehavior extends CSSRulesBehavior {
	
	private static final Log log = LogFactory.getLog(OmissisBehavior.class);

	public static final String KEY_TOOL_OMISSIS = "OMI";
	public static final String CLASS_OMISSIS = "omissis";
	
	private static final int 	QTD_PONTOS = 50;
	private static final String PONTOS = StringUtils.repeat(".", QTD_PONTOS);
	private static final String SPAN_OMISSIS = "<span class='" + CLASS_OMISSIS + "'>" + PONTOS + "</omissis>";

	private Action action;
	
	private boolean enabled = true;
	
	private JTextPane editor;
	private ExtendedHTMLDocument doc;

	public OmissisBehavior() {
		super(".omissis {background: #EEEEEE;}");
		
		action = new OmissisAction("Inserir omissis");
		action.putValue(Action.SHORT_DESCRIPTION, "Inserir omissis");
		action.putValue(Action.LONG_DESCRIPTION, "Inserir omissis");
		action.putValue(Action.SMALL_ICON, loadIcon());
	}
	
	@Override
	public void initializeDocument(JTextPane textPane) {
		super.initializeDocument(textPane);
		editor = textPane;
		doc = (ExtendedHTMLDocument) textPane.getDocument();
		doc.addFilter(new MyDocumentFilter());
	}
	
	@Override
	public Action getAction(String keyTool) {
		return KEY_TOOL_OMISSIS.equals(keyTool)? action: null;
	}
	
	@Override
	public String filterRead(String html, ExtendedHTMLDocument doc, int offset) {
		if(!enabled) {
			return html;
		}
		if(inOmissis(offset)) {
			return null;
		}
		try {
			if(podeGerarOmissis(offset, offset, html)) {
				agendaIdentificacaoAutomatica();
			}
		}
		catch(BadLocationException e) {
			log.error(e.getMessage(), e);
		}
		return html;
	}

	private ImageIcon loadIcon() {
		URL imageURL = getClass().getResource("/icons/Omissis.png");
		if (imageURL != null) {
			return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageURL));
		}
		return null;
	}
	
	private boolean inOmissis(int offset) {
		Element e = doc.getCharacterElement(offset);
		AttributeSet attr = (AttributeSet) e.getAttributes().getAttribute(Tag.SPAN);
		if(attr == null) {
			return false;
		}
		return CLASS_OMISSIS.equals(attr.getAttribute(Attribute.CLASS));
	}
	
	@SuppressWarnings("serial")
	public class OmissisAction extends StyledTextAction {

		public OmissisAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			if (editor != null) {
			    HTMLDocument doc = (HTMLDocument) getStyledDocument(editor);
			    HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			    
			    int offset = editor.getSelectionStart();
			    Element p = doc.getParagraphElement(offset);
			    if(offset == p.getStartOffset()) {
			    	editor.replaceSelection("\n");
			    	offset++;
			    }
			    try {
			    	kit.insertHTML(doc, offset, SPAN_OMISSIS, 0, 0, Tag.SPAN);
			    	editor.moveCaretPosition(offset + PONTOS.length());
			    	editor.replaceSelection("\n");
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
//			    DocumentUtil.debug(doc);
			}
		}
		
	}
	
	public class MyDocumentFilter extends ExtendedHTMLDocumentFilter {

		@Override
		public void replace(FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs, DocumentFilterChain chain)
				throws BadLocationException {
			
			EditContext ctx = new EditContext(offset, length, text, attrs);
			
			if(!enabled) {
				replace(fb, ctx, chain);
				return;
			}
			
			if(bloquearEdicao(ctx)) {
				return;
			}

			// Não permite escrever no início ou no final do omissis.
			ajustaEdicaoForaDoOmissis(ctx);

			if(podeGerarOmissis(ctx.getOffset(), ctx.getEndOffset(), ctx.getText())) {
				agendaIdentificacaoAutomatica();
			}
			
			replace(fb, ctx, chain);
		}
		
		private void replace(FilterBypass fb, EditContext ctx, DocumentFilterChain chain) throws BadLocationException {
			chain.replace(fb, ctx.getOffset(), ctx.getLength(), ctx.getText(), ctx.getAttrs());
		}

		/**
		 * Ajusta o contexto para edição antes ou após o tag span da omissis caso
		 * a edição seja no início ou final da mesma.
		 */
		private void ajustaEdicaoForaDoOmissis(EditContext ctx) {
			if(ctx.getAttrs() != null) {
				AttributeSet attrSpan = (AttributeSet) ctx.getAttrs().getAttribute(Tag.SPAN);
				if(attrSpan != null && attrSpan.containsAttribute(Attribute.CLASS, "omissis")) {
					MutableAttributeSet newAttrs = new SimpleAttributeSet(ctx.getAttrs());
					newAttrs.removeAttribute(Tag.SPAN);
					ctx.setAttrs(newAttrs);
				}
			}
		}

		/**
		 * Retorna true se o usuário tentar editar a omissis.
		 * Ajusta o contexto para exclusão da omissis se o usuário teclar backspace ao final
		 * da mesma ou delete no início (apagar o primeiro ou último ponto). 
		 */
		private boolean bloquearEdicao(EditContext ctx) {
			
			boolean bloquear = false;

			int endOffset = ctx.getEndOffset();
			
			// Verifica se início ou fim está dentro de omissis
			boolean inicioOmissis = inOmissis(ctx.getOffset());
			boolean fimOmissis = ctx.getLength() == 0? inicioOmissis : inOmissis(endOffset); 
			
			if(inicioOmissis || fimOmissis) {
				bloquear = true;
				if(fimOmissis && endOffset == doc.getCharacterElement(endOffset).getStartOffset() &&
						(!inicioOmissis || ctx.getLength() == 0)) {
					bloquear = false;
				}
				else if(ctx.getLength() == 1 && ctx.getText().equals("")) {
					if(inicioOmissis && ctx.getOffset() == doc.getCharacterElement(ctx.getOffset()).getStartOffset()) {
						bloquear = false;
						ctx.setLength(doc.getCharacterElement(ctx.getOffset()).getEndOffset() - ctx.getOffset());
					}
					else if(inicioOmissis && ctx.getOffset() + 1 == doc.getCharacterElement(ctx.getOffset()).getEndOffset()) {
						bloquear = false;
						ctx.setOffset(doc.getCharacterElement(ctx.getOffset()).getStartOffset()); 
						ctx.setLength(doc.getCharacterElement(ctx.getOffset()).getEndOffset() - ctx.getOffset());
					}
				}
			}
			
			return bloquear;
		}

	}
	
	private void agendaIdentificacaoAutomatica() {
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				enabled = false;
				try {
					identificacaoAutomatica();
				} catch (BadLocationException e) {
					log.error(e.getMessage(), e);
				}
				enabled = true;
			}
			
		});
	}
	
	/**
	 * Identifica se existe a possibilidade de surgir um omissis ao se inserir ou remover um texto.
	 */
	private boolean podeGerarOmissis(int offset, int endOffset, String text) throws BadLocationException {
		if(text.contains(".")) {
			return true;
		}
		// Verifica remoção de texto e conseguinte junção de pontos
		Element pStart = doc.getParagraphElement(offset);
		Element pEnd = doc.getParagraphElement(endOffset);
		return text.equals("") && 
				offset > pStart.getStartOffset() && doc.getText(offset - 1, 1).equals(".") &&
				endOffset < pEnd.getEndOffset() && doc.getText(endOffset, 1).equals(".");
	}

	/**
	 * Identifica omissis automaticamente
	 */
	private void identificacaoAutomatica() throws BadLocationException {
		
		String text = doc.getText(0, doc.getLength());
		
		List<int[]> todosOmissis = identificaOmissis(text);
		
		if(todosOmissis.isEmpty()) {
			return;
		}
		
		Collections.reverse(todosOmissis);
		for(int[] bounds: todosOmissis) {
			
			int start = bounds[0];
			int end = bounds[1];
			
			AttributeSet attrs = getAttrsParaOmissis(start);
			
			MutableAttributeSet attrsSemOmissis = new SimpleAttributeSet(attrs);
			attrsSemOmissis.removeAttribute(Tag.SPAN);
			
			if(end - start != QTD_PONTOS) {
				doc.replace(start, end - start, PONTOS, attrs);
			}
			else {
				doc.setCharacterAttributes(start, QTD_PONTOS, attrs, true);
			}
			end = start + QTD_PONTOS;
		}
		
	}

	private AttributeSet getAttrsParaOmissis(int offset) {
		
		MutableAttributeSet attrSpan = new SimpleAttributeSet();
		attrSpan.addAttribute(Attribute.CLASS, "omissis");

		AttributeSet attrsOrig = doc.getCharacterElement(offset).getAttributes();
		MutableAttributeSet attrs = new SimpleAttributeSet(attrsOrig);
		attrs.addAttribute(Tag.SPAN, attrSpan);
		
		return attrs;
	}

	// Retorna lista de intervalos int[2] {start, end} para novos omissis identificados
	// no texto indicado
	private List<int[]> identificaOmissis(String text) {
		
		List<int[]> ret = new ArrayList<int[]>();
		
		Pattern p = Pattern.compile("\\.{4,}", Pattern.DOTALL);
		Matcher m = p.matcher(text);
		
		while(m.find()) {
			ret.add(new int[] {m.start(), m.end()});
		}
		
		return ret;
	}
	
	@Override
	public void afterClearFormat(JTextPane textPane) {
		agendaIdentificacaoAutomatica();
	}

}

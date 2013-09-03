package br.gov.lexml.swing.editorhtml.handlers;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;

import javax.swing.JPanel;

import br.gov.lexml.swing.editorhtml.EditorHtml;

import com.hexidec.ekit.component.MaximizeHandler;

public abstract class CardLayoutMaximizeHandler implements MaximizeHandler {

	public static final String DEFAULT_MINIMIZED_CARD_KEY = "form";
	
	private static final String MAXIMIZED_CARD_KEY = CardLayoutMaximizeHandler.class.getName() + "_maximizedCard";
	
	private EditorHtml editorHtml; 
	private Container cardLayoutContainer;
	private CardLayout cardLayout;
	private String minimizedCardKey = DEFAULT_MINIMIZED_CARD_KEY;
	
	public CardLayoutMaximizeHandler(EditorHtml editorHtml,
			Container cardLayoutContainer) {
		this.editorHtml = editorHtml;
		this.cardLayoutContainer = cardLayoutContainer;
		cardLayout = (CardLayout) cardLayoutContainer.getLayout();
	}
	
	public void setMinimizedCardKey(String minimizedCardKey) {
		this.minimizedCardKey = minimizedCardKey;
	}

	@Override
	public void maximize() {
		if(editorHtml.getParent() != null) {
			editorHtml.getParent().remove(editorHtml);
		}
		JPanel p = new JPanel(new BorderLayout());
		p.add(editorHtml, BorderLayout.CENTER);
		cardLayoutContainer.add(p, MAXIMIZED_CARD_KEY);
		cardLayout.show(cardLayoutContainer, MAXIMIZED_CARD_KEY);
	}

	@Override
	public void minimize() {
		cardLayoutContainer.remove(editorHtml.getParent());
		editorHtml.getParent().remove(editorHtml);
		addEditorToDefaultContainer();
		cardLayout.show(cardLayoutContainer, minimizedCardKey);
	}
	
	public abstract void addEditorToDefaultContainer();

}

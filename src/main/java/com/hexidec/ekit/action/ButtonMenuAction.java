package com.hexidec.ekit.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Action que abre um menu popup em baixo de um botão.
 * 
 * @author fragomeni
 */
@SuppressWarnings("serial")
public class ButtonMenuAction extends AbstractAction {

	private JPopupMenu menu;
	
	/**
	 * Construtor
	 * 
	 * @param actionName 	Nome da Action utilizado também como descrição curta e longa
	 * @param icon 			Ícone da ação
	 * @param actions 		Actions para criação do menu popup. Utilize null para adicionar separadores.
	 */
	public ButtonMenuAction(String actionName, Icon icon, Action... actions) {
		super(actionName, icon);
		putValue(Action.LONG_DESCRIPTION, actionName);
		putValue(Action.SHORT_DESCRIPTION, actionName);
		
		menu = new JPopupMenu();
		for(Action a: actions) {
			if(a == null) {
				menu.addSeparator();
			}
			else {
				JMenuItem jmi = new JMenuItem(a);
				jmi.setToolTipText(null);
				menu.add(jmi);
			}
		}
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() instanceof JButton) {
			JButton btn = (JButton)ae.getSource();
			menu.show(btn, 0, btn.getHeight());
		}
	}
}

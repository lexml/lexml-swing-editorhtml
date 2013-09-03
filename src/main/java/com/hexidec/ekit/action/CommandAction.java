package com.hexidec.ekit.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class CommandAction extends AbstractAction {

	private ActionListener listener;
	
	public CommandAction(String actionName, Icon icon, String actionCommand, ActionListener listener) {
		super(actionName, icon);
		putValue(Action.LONG_DESCRIPTION, actionName);
		putValue(Action.SHORT_DESCRIPTION, actionName);
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);

		this.listener = listener;
	}
	
	public void actionPerformed(ActionEvent ae) {
		listener.actionPerformed(ae);
	}
}

/*
GNU Lesser General Public License

PropertiesDialog
Copyright (C) 2003 Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.hexidec.ekit.component;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.apache.commons.lang.StringUtils;

import com.hexidec.util.Translatrix;

/** Class for providing a dialog that lets the user specify values for tag attributes
  */
public class PropertiesDialog extends JDialog
{
	private JOptionPane jOptionPane;
	private Hashtable<String, JComponent> htInputFields;

	public PropertiesDialog(Window parent, String[] fields, String[] types, String[] values, String title, boolean bModal)
	{
		super(parent, title);
		setModal(bModal);
		htInputFields = new Hashtable<String, JComponent>();
		final Object[] buttonLabels = { Translatrix.getTranslationString("DialogAccept"), Translatrix.getTranslationString("DialogCancel") };
		List<Object> panelContents = new ArrayList<Object>();
		for(int iter = 0; iter < fields.length; iter++)
		{
			String fieldName = fields[iter];
			String fieldType = types[iter];
			JComponent fieldComponent;
			JComponent panelComponent = null;
			if(fieldType.equals("text") || fieldType.equals("integer"))
			{
				fieldComponent = new JTextField(3);
				if(values[iter] != null && values[iter].length() > 0)
				{
					((JTextField)(fieldComponent)).setText(values[iter]);
				}
				
				if(fieldType.equals("integer")) {
					((AbstractDocument)((JTextField)(fieldComponent)).getDocument()).setDocumentFilter(
							new DocumentFilter() {
								
								@Override
								public void insertString(FilterBypass fb,
										int offset, String text,
										AttributeSet attrs)
										throws BadLocationException {
									replace(fb, offset, 0, text, attrs);
								}
								
								@Override
								public void replace(FilterBypass fb,
										int offset, int length, String text,
										AttributeSet attrs)
										throws BadLocationException {
									
									if(StringUtils.isNumeric(text)) {
										super.replace(fb, offset, length, text, attrs);
									}
									
								}
								
							}
					);
				}
			}
			else if(fieldType.equals("bool"))
			{
				fieldComponent = new JCheckBox(fieldName);
				if(values[iter] != null)
				{
					((JCheckBox)(fieldComponent)).setSelected(values[iter] == "true");
				}
				panelComponent = fieldComponent;
			}
			else if(fieldType.equals("combo"))
			{
				fieldComponent = new JComboBox();
				if(values[iter] != null)
				{
					StringTokenizer stParse = new StringTokenizer(values[iter], ",", false);
					while(stParse.hasMoreTokens())
					{
						((JComboBox)(fieldComponent)).addItem(stParse.nextToken());
					}
				}
			}
			else
			{
				fieldComponent = new JTextField(3);
			}
			htInputFields.put(fieldName, fieldComponent);
			if(panelComponent == null) {
				panelContents.add(fieldName);
				panelContents.add(fieldComponent);
			}
			else {
				panelContents.add(panelComponent);
			}
		}
		jOptionPane = new JOptionPane(panelContents.toArray(), 
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, 
				null, buttonLabels, buttonLabels[0]);

		setContentPane(jOptionPane);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		jOptionPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();
				if(isVisible() && (e.getSource() == jOptionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY) || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
				{
					Object value = jOptionPane.getValue();
					if(value == JOptionPane.UNINITIALIZED_VALUE)
					{
						return;
					}
					if(value.equals(buttonLabels[0]))
					{
						setVisible(false);
					}
					else
					{
						setVisible(false);
					}
				}
			}
		});
		this.pack();
		setLocation(SwingUtilities.getPointForCentering(this, parent));
	}

	public PropertiesDialog(Frame parent, String[] fields, String[] types, String title, boolean bModal)
	{
		this(parent, fields, types, new String[fields.length], title, bModal);
	}

	public String getFieldValue(String fieldName)
	{
		Object dataField = htInputFields.get(fieldName);
		if(dataField instanceof JTextField)
		{
			return ((JTextField)dataField).getText();
		}
		else if(dataField instanceof JCheckBox)
		{
			if(((JCheckBox)dataField).isSelected())
			{
				return "true";
			}
			else
			{
				return "false";
			}
		}
		else if(dataField instanceof JComboBox)
		{
			return (String)(((JComboBox)dataField).getSelectedItem());
		}
		else
		{
			return (String)null;
		}
	}

	public String getDecisionValue()
	{
		return jOptionPane.getValue().toString();
	}
}


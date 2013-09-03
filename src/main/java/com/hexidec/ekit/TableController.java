package com.hexidec.ekit;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit.AlignmentAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.editorhtml.util.DocumentUtil;

import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.ExtendedHTMLEditorKit;
import com.hexidec.ekit.component.PropertiesDialog;
import com.hexidec.ekit.component.SimpleInfoDialog;
import com.hexidec.util.Translatrix;

class TableController {

	private static final Log log = LogFactory.getLog(TableController.class);

	private EkitCore ekit;
	private JTextPane jtpMain;
	private ExtendedHTMLDocument htmlDoc;
	private ExtendedHTMLEditorKit htmlKit;

	TableController(EkitCore ekit) {
		this.ekit = ekit;
		jtpMain = ekit.getTextPane();
		htmlDoc = (ExtendedHTMLDocument) jtpMain.getDocument();
		htmlKit = (ExtendedHTMLEditorKit) jtpMain.getEditorKit();
	}

	/**
	 * Method for inserting an HTML Table
	 */
	void insertTable() throws IOException, BadLocationException,
			RuntimeException, NumberFormatException {

		int caretPos = jtpMain.getCaretPosition();

		if (DocumentUtil.getElementByTag(htmlDoc, caretPos, Tag.TABLE) != null) {
			new SimpleInfoDialog(ekit.getWindow(),
					Translatrix.getTranslationString("Table"), true,
					"Não é permitido inserir uma tabela dentro de outra.");
			return;
		}

		String[] fieldNames = { "Linhas", "Colunas" };
		String[] fieldTypes = { "integer", "integer" };
		String[] fieldValues = { "3", "3" };

		StringBuffer compositeElement = new StringBuffer("<TABLE");
		int rows = 0;
		int cols = 0;
		if (fieldNames != null && fieldNames.length > 0) {
			PropertiesDialog propertiesDialog = new PropertiesDialog(
					ekit.getWindow(), fieldNames, fieldTypes, fieldValues,
					Translatrix.getTranslationString("TableDialogTitle"), true);
			propertiesDialog.setVisible(true);
			String decision = propertiesDialog.getDecisionValue();
			if (decision.equals(Translatrix
					.getTranslationString("DialogCancel"))) {
				propertiesDialog.dispose();
				propertiesDialog = null;
				return;
			} else {
				for (String fieldName : fieldNames) {
					String propValue = propertiesDialog
							.getFieldValue(fieldName);
					if (propValue != null && propValue != ""
							&& propValue.length() > 0) {
						if (fieldName.equals("Linhas")) {
							rows = Integer.parseInt(propValue);
						} else if (fieldName.equals("Colunas")) {
							cols = Integer.parseInt(propValue);
						} else {
							compositeElement.append(" " + fieldName + "=" + '"'
									+ propValue + '"');
						}
					}
				}
			}
			propertiesDialog.dispose();
			propertiesDialog = null;
		}
		compositeElement.append(">");
		for (int i = 0; i < rows; i++) {
			compositeElement.append("<TR>");
			for (int j = 0; j < cols; j++) {
				compositeElement.append("<TD></TD>");
			}
			compositeElement.append("</TR>");
		}
		compositeElement.append("</TABLE>");

		htmlKit.read(new StringReader(compositeElement.toString()), htmlDoc,
				caretPos);

		jtpMain.setCaretPosition(caretPos == 1 ? 1 : caretPos + 1);
		ekit.refreshOnUpdate();
	}

	/**
	 * Method for deleting an HTML Table
	 */
	void deleteTable() {

		Element tableElement = checkCaretInTable();
		if (tableElement == null) {
			return;
		}

		try {
			htmlDoc.setEdicaoControlada(true);
			htmlDoc.remove(tableElement.getStartOffset(),
					tableElement.getEndOffset() - tableElement.getStartOffset());
		} catch (BadLocationException e) {
			log.error("Falha ao remover tabela.", e);
		}

	}

	/**
	 * Checks if the caret is inside a table and shows a dialog if it isn't.
	 * 
	 * @return The table element or null
	 */
	private Element checkCaretInTable() {
		Element tableElement = DocumentUtil.getElementByTag(htmlDoc,
				jtpMain.getCaretPosition(), Tag.TABLE);
		if (tableElement == null) {
			new SimpleInfoDialog(ekit.getWindow(),
					Translatrix.getTranslationString("Table"), true,
					Translatrix.getTranslationString("CursorNotInTable"));
		}
		return tableElement;
	}

	/**
	 * Method for editing an HTML Table
	 */
	void editTable() {
		int caretPos = jtpMain.getCaretPosition();
		Element element = htmlDoc.getCharacterElement(caretPos);
		Element elementParent = element.getParentElement();
		while (elementParent != null
				&& !elementParent.getName().equals("table")) {
			elementParent = elementParent.getParentElement();
		}
		if (elementParent != null) {
			HTML.Attribute[] fieldKeys = { HTML.Attribute.BORDER,
					HTML.Attribute.CELLSPACING, HTML.Attribute.CELLPADDING,
					HTML.Attribute.WIDTH, HTML.Attribute.VALIGN };
			String[] fieldNames = { "border", "cellspacing", "cellpadding",
					"width", "valign" };
			String[] fieldTypes = { "text", "text", "text", "text", "combo" };
			String[] fieldValues = { "", "", "", "", "top,middle,bottom," };
			MutableAttributeSet myatr = (MutableAttributeSet) elementParent
					.getAttributes();
			for (int i = 0; i < fieldNames.length; i++) {
				if (myatr.isDefined(fieldKeys[i])) {
					if (fieldTypes[i].equals("combo")) {
						fieldValues[i] = myatr.getAttribute(fieldKeys[i])
								.toString() + "," + fieldValues[i];
					} else {
						fieldValues[i] = myatr.getAttribute(fieldKeys[i])
								.toString();
					}
				}
			}
			PropertiesDialog propertiesDialog = new PropertiesDialog(
					ekit.getWindow(), fieldNames, fieldTypes, fieldValues,
					Translatrix.getTranslationString("TableEdit"), true);
			propertiesDialog.setVisible(true);
			if (!propertiesDialog.getDecisionValue().equals(
					Translatrix.getTranslationString("DialogCancel"))) {
				String myAtributes = "";
				SimpleAttributeSet mynew = new SimpleAttributeSet();
				for (int i = 0; i < fieldNames.length; i++) {
					String propValue = propertiesDialog
							.getFieldValue(fieldNames[i]);
					if (propValue != null && propValue.length() > 0) {
						myAtributes = myAtributes + fieldNames[i] + "=\""
								+ propValue + "\" ";
						mynew.addAttribute(fieldKeys[i], propValue);
					}
				}
				htmlDoc.replaceAttributes(elementParent, mynew, HTML.Tag.TABLE);
				ekit.refreshOnUpdate();
			}
			propertiesDialog.dispose();
		} else {
			new SimpleInfoDialog(ekit.getWindow(),
					Translatrix.getTranslationString("Table"), true,
					Translatrix.getTranslationString("CursorNotInTable"));
		}
	}

	/**
	 * Method for editing HTML Table cells
	 */
	void editCell() {
		int caretPos = jtpMain.getCaretPosition();
		Element element = htmlDoc.getCharacterElement(caretPos);
		Element elementParent = element.getParentElement();
		while (elementParent != null && !elementParent.getName().equals("td")) {
			elementParent = elementParent.getParentElement();
		}
		if (elementParent != null) {
			HTML.Attribute[] fieldKeys = { HTML.Attribute.WIDTH,
					HTML.Attribute.HEIGHT, HTML.Attribute.ALIGN,
					HTML.Attribute.VALIGN, HTML.Attribute.BGCOLOR };
			String[] fieldNames = { "width", "height", "align", "valign",
					"bgcolor" };
			String[] fieldTypes = { "text", "text", "combo", "combo", "combo" };
			String[] fieldValues = {
					"",
					"",
					"left,right,center",
					"top,middle,bottom",
					"none,aqua,black,fuchsia,gray,green,lime,maroon,navy,olive,purple,red,silver,teal,white,yellow" };
			MutableAttributeSet myatr = (MutableAttributeSet) elementParent
					.getAttributes();
			for (int i = 0; i < fieldNames.length; i++) {
				if (myatr.isDefined(fieldKeys[i])) {
					if (fieldTypes[i].equals("combo")) {
						fieldValues[i] = myatr.getAttribute(fieldKeys[i])
								.toString() + "," + fieldValues[i];
					} else {
						fieldValues[i] = myatr.getAttribute(fieldKeys[i])
								.toString();
					}
				}
			}
			PropertiesDialog propertiesDialog = new PropertiesDialog(
					ekit.getWindow(), fieldNames, fieldTypes, fieldValues,
					Translatrix.getTranslationString("TableCellEdit"), true);
			propertiesDialog.setVisible(true);
			if (!propertiesDialog.getDecisionValue().equals(
					Translatrix.getTranslationString("DialogCancel"))) {
				String myAtributes = "";
				SimpleAttributeSet mynew = new SimpleAttributeSet();
				for (int i = 0; i < fieldNames.length; i++) {
					String propValue = propertiesDialog
							.getFieldValue(fieldNames[i]);
					if (propValue != null && propValue.length() > 0) {
						myAtributes = myAtributes + fieldNames[i] + "=\""
								+ propValue + "\" ";
						mynew.addAttribute(fieldKeys[i], propValue);
					}
				}
				htmlDoc.replaceAttributes(elementParent, mynew, HTML.Tag.TD);
				ekit.refreshOnUpdate();
			}
			propertiesDialog.dispose();
		} else {
			new SimpleInfoDialog(ekit.getWindow(),
					Translatrix.getTranslationString("Cell"), true,
					Translatrix.getTranslationString("CursorNotInCell"));
		}
	}

	/**
	 * Method for inserting a row into an HTML Table
	 */
	void insertTableRow(boolean after) {
		int caretPos = jtpMain.getCaretPosition();

		if (checkCaretInTable() == null) {
			return;
		}

		Element rowElement = DocumentUtil.getElementByTag(htmlDoc, caretPos,
				Tag.TR);

		int startPoint = -1;
		if (after) {
			startPoint = rowElement.getEndOffset();
		} else {
			startPoint = rowElement.getStartOffset();
		}
		int columnCount = rowElement.getElementCount();

		jtpMain.setCaretPosition(startPoint);

		StringBuffer sRow = new StringBuffer();
		sRow.append("<TR>");
		for (int i = 0; i < columnCount; i++) {
			sRow.append("<TD></TD>");
		}
		sRow.append("</TR>");

		ActionEvent actionEvent = new ActionEvent(jtpMain, 0, "insertTableRow");
		new HTMLEditorKit.InsertHTMLTextAction("insertTableRow",
				sRow.toString(), HTML.Tag.TABLE, HTML.Tag.TR)
				.actionPerformed(actionEvent);
		ekit.refreshOnUpdate();
		jtpMain.setCaretPosition(caretPos);

	}

	/**
	 * Method for inserting a column into an HTML Table
	 */
	void insertTableColumn(boolean after) {
		int caretPos = jtpMain.getCaretPosition();
		int startPoint = -1;
		int rowCount = -1;
		int cellOffset = 0;
		boolean lastColumn = false;

		Element tableElement = checkCaretInTable();
		if (tableElement == null) {
			return;
		}

		startPoint = tableElement.getStartOffset();
		rowCount = tableElement.getElementCount();
		Element tdElement = DocumentUtil.getElementByTag(htmlDoc, caretPos,
				Tag.TD);
		cellOffset = DocumentUtil.getIndexInParent(tdElement);
		Element trElement = tdElement.getParentElement();
		lastColumn = (cellOffset == (trElement.getElementCount() - 1));

		String sCell = "<TD></TD>";
		ActionEvent actionEvent = new ActionEvent(jtpMain, 0, "insertTableCell");
		for (int i = 0; i < rowCount; i++) {
			Element row = tableElement.getElement(i);
			if (lastColumn) {
				try {
					htmlDoc.insertBeforeEnd(row, sCell);
				} catch (Exception e) {
					log.error("Falha ao incluir célula.", e);
				}
			} else {
				Element whichCell = row.getElement(cellOffset);
				if (after) {
					jtpMain.setCaretPosition(whichCell.getEndOffset());
				} else {
					jtpMain.setCaretPosition(whichCell.getStartOffset());
				}
				new HTMLEditorKit.InsertHTMLTextAction("insertTableCell",
						sCell, HTML.Tag.TR, HTML.Tag.TD)
						.actionPerformed(actionEvent);
			}
		}

		ekit.refreshOnUpdate();
		jtpMain.setCaretPosition(caretPos);

	}

	/**
	 * Method for deleting a row from an HTML Table
	 */
	void deleteTableRow() throws IOException, BadLocationException {

		if (checkCaretInTable() == null) {
			return;
		}

		int caretPos = jtpMain.getCaretPosition();
		int startPoint = -1;
		int endPoint = -1;
		Element eLinha = DocumentUtil
				.getElementByTag(htmlDoc, caretPos, Tag.TR);
		if (eLinha != null) {
			startPoint = eLinha.getStartOffset();
			endPoint = eLinha.getEndOffset();
		}
		if (startPoint > -1 && endPoint > startPoint) {

			// log.debug("endPoint: " + endPoint + ", docLength: " +
			// htmlDoc.getLength());

			// Element element = htmlDoc.getParagraphElement(endPoint);
			// DocumentUtil.debug(element);

			htmlDoc.setEdicaoControlada(true);
			htmlDoc.remove(startPoint, endPoint - startPoint);
			ekit.refreshOnUpdate();
			if (caretPos >= htmlDoc.getLength()) {
				caretPos = htmlDoc.getLength() - 1;
			}
			jtpMain.setCaretPosition(caretPos);
		}
	}

	/**
	 * Method for deleting a column from an HTML Table
	 */
	void deleteTableColumn() throws BadLocationException {

		// Locate the table, row, and cell location of the cursor
		Element elementTable = checkCaretInTable();
		if (elementTable == null) {
			return;
		}

		int caretPos = jtpMain.getCaretPosition();
		Element elementCell = DocumentUtil.getElementByTag(htmlDoc, caretPos,
				Tag.TD);
		Element elementRow = DocumentUtil.getElementByTag(htmlDoc,
				elementCell.getStartOffset(), Tag.TR);

		int whichColumn = -1;
		// Find the column the cursor is in
		int myOffset = 0;
		for (int i = 0; i < elementRow.getElementCount(); i++) {
			if (elementCell == elementRow.getElement(i)) {
				whichColumn = i;
				myOffset = elementCell.getEndOffset();
			}
		}
		if (whichColumn > -1) {
			// Iterate through the table rows, deleting cells from the
			// indicated column
			int mycaretPos = caretPos;
			for (int i = 0; i < elementTable.getElementCount(); i++) {
				elementRow = elementTable.getElement(i);
				elementCell = (elementRow.getElementCount() > whichColumn ? elementRow
						.getElement(whichColumn) : elementRow
						.getElement(elementRow.getElementCount() - 1));
				int columnCellStart = elementCell.getStartOffset();
				int columnCellEnd = elementCell.getEndOffset();
				int dif = columnCellEnd - columnCellStart;
				if (columnCellStart < myOffset) {
					mycaretPos = mycaretPos - dif;
					myOffset = myOffset - dif;
				}
				htmlDoc.setEdicaoControlada(true);
				if (whichColumn == 0) {
					htmlDoc.remove(columnCellStart, dif);
				} else {
					htmlDoc.remove(columnCellStart - 1, dif);
				}
			}
			ekit.refreshOnUpdate();
			if (mycaretPos >= htmlDoc.getLength()) {
				mycaretPos = htmlDoc.getLength() - 1;
			}
			if (mycaretPos < 1) {
				mycaretPos = 1;
			}
			jtpMain.setCaretPosition(mycaretPos);
		}
	}

	/**
	 * Method for formating an HTML Table column
	 */
	void formatTableColumn() throws BadLocationException {

		int caretPos = jtpMain.getCaretPosition();

		Element tableElement = checkCaretInTable();
		if (tableElement == null) {
			return;
		}
		
		Element elementParent = DocumentUtil.getElementByTag(htmlDoc, caretPos, Tag.TD);		

		HTML.Attribute[] fieldKeys = { HTML.Attribute.ALIGN,
				HTML.Attribute.STYLE, HTML.Attribute.STYLE, HTML.Attribute.STYLE };
		String[] fieldNames = { "Alinhamento", "Negrito", "Itálico", "Sublinhado" };
		String[] fieldTypes = { "combo", "bool", "bool", "bool" };
		String[] fieldValues = {
				" ,Esquerda,Centro,Direita,Justificado",
				"false",
				"false",
				"false"
		};
		MutableAttributeSet myatr = (MutableAttributeSet) elementParent
				.getAttributes();
		for (int i = 0; i < fieldNames.length; i++) {
			if (myatr.isDefined(fieldKeys[i])) {
				if (fieldTypes[i].equals("combo")) {
					fieldValues[i] = myatr.getAttribute(fieldKeys[i])
							.toString() + "," + fieldValues[i];
				} else {
					fieldValues[i] = myatr.getAttribute(fieldKeys[i])
							.toString();
				}
			}
		}
		PropertiesDialog propertiesDialog = new PropertiesDialog(
				ekit.getWindow(), fieldNames, fieldTypes, fieldValues,
				Translatrix.getTranslationString("FormatTableColumn"), true);
		propertiesDialog.setVisible(true);

		if (!propertiesDialog.getDecisionValue().equals(
				Translatrix.getTranslationString("DialogCancel"))) {

			List<Element> coluna = getColuna(caretPos);
			
			String alinhamento = propertiesDialog.getFieldValue("Alinhamento");
			int align = -1;
			if(alinhamento.startsWith("E")) {
				align = StyleConstants.ALIGN_LEFT;
			}
			else if(alinhamento.startsWith("C")) {
				align = StyleConstants.ALIGN_CENTER;
			}
			else if(alinhamento.startsWith("D")) {
				align = StyleConstants.ALIGN_RIGHT;
			}
			else if(alinhamento.startsWith("J")) {
				align = StyleConstants.ALIGN_JUSTIFIED;
			}
			
			AlignmentAction alignAction = null;
			if(align != -1) {
				alignAction = new AlignmentAction("Alinhamento de Coluna", align); 
			}
			
			boolean negrito = Boolean.parseBoolean(propertiesDialog.getFieldValue("Negrito"));
			boolean italico = Boolean.parseBoolean(propertiesDialog.getFieldValue("Itálico"));
			boolean sublinhado = Boolean.parseBoolean(propertiesDialog.getFieldValue("Sublinhado"));
			
			SimpleAttributeSet s = new SimpleAttributeSet();
			StyleConstants.setBold(s, negrito);
			StyleConstants.setItalic(s, italico);
			StyleConstants.setUnderline(s, sublinhado);
			
			for(Element celula: coluna) {
				
				if(alignAction != null) {
					jtpMain.setSelectionStart(celula.getStartOffset());
					jtpMain.setSelectionEnd(celula.getEndOffset() - 1);
					alignAction.actionPerformed(new ActionEvent(jtpMain, align, ""));
				}

				htmlDoc.setCharacterAttributes(celula.getStartOffset(), 
						celula.getEndOffset() - celula.getStartOffset() - 1, s, 
						false);
				
			}
			
			ekit.moveCaret(caretPos, false);
		}
		
		propertiesDialog.dispose();
		
		ekit.refreshOnUpdate();
	}

	private List<Element> getColuna(int pos) {
		
		List<Element> cells = new ArrayList<Element>();
		
		Element elementCell = DocumentUtil.getElementByTag(htmlDoc, pos, Tag.TD);
		Element elementRow = DocumentUtil.getElementByTag(htmlDoc, 
				elementCell.getStartOffset(), Tag.TR);
		Element elementTable = DocumentUtil.getElementByTag(htmlDoc, 
				elementRow.getStartOffset(), Tag.TABLE);

		// Find the column the cursor is in
		int whichColumn = -1;
		for (int i = 0; i < elementRow.getElementCount(); i++) {
			if (elementCell == elementRow.getElement(i)) {
				whichColumn = i;
			}
		}
		
		if (whichColumn > -1) {
			for (int i = 0; i < elementTable.getElementCount(); i++) {
				elementRow = elementTable.getElement(i);
				elementCell = (elementRow.getElementCount() > whichColumn ? elementRow
						.getElement(whichColumn) : elementRow
						.getElement(elementRow.getElementCount() - 1));
				cells.add(elementCell);
			}
		}
		
		return cells;
	}

}

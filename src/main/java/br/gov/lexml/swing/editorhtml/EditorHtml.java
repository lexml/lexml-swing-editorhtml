package br.gov.lexml.swing.editorhtml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;

import com.hexidec.ekit.EkitCore;
import com.hexidec.ekit.component.HTMLDocumentBehavior;
import com.hexidec.ekit.component.MaximizeHandler;

@SuppressWarnings("serial")
public class EditorHtml extends JPanel {

	private EkitCore ekit;
	private JToolBar toolBar;

	EditorHtml(boolean inlineEdit, List<HTMLDocumentBehavior> behaviors, String strToolbar, boolean debugMode) {
		
		ekit = new EkitCore(false, null, null, null, null, null, true,
				false, true, "pt", "BR", false, debugMode, false, false,
				strToolbar, false, inlineEdit, behaviors);

		setLayout(new BorderLayout());

		if(!StringUtils.isEmpty(strToolbar)) {
			toolBar = ekit.getToolBar(true);
			toolBar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
			add(toolBar, BorderLayout.NORTH);
		}

		ekit.setBorder(null);
		add(ekit, BorderLayout.CENTER);
		
	}
	
	public void setDocumentText(String documentText) {
		ekit.setDocumentText(documentText);
	}

	public String getDocumentText() {
		return ekit.getDocumentText();
	}
	
	public String getDocumentBody() {
		return ekit.getDocumentBody();
	}
	
	public JToolBar getToolBar() {
		return toolBar;
	}
	
	public JMenuBar getMenuBar() {
		return ekit.getMenuBar();
	}
	
	public void setHeigtInLines(int lines) {
		Font f = ekit.getFont();
		FontMetrics fm = ekit.getFontMetrics(f);
		ekit.setPreferredSize(
			new Dimension(
					ekit.getPreferredSize().width,
					fm.getHeight() * lines)
		);
	}
	
	public JTextPane getTextPane() {
		return ekit.getTextPane();
	}
	
	public void setTextPaneBorder(Border border) {
		ekit.setTextPaneBorder(border);
	}
	
	public void setMaximizeHandler(MaximizeHandler mh) {
		ekit.setMaximizeHandler(mh);
	}
	
	public boolean isDirty() {
		return ekit.isDirty();
	}
	
}

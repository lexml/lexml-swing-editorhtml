package com.hexidec.ekit.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hexidec.ekit.EkitCore;

/**
 * Janela para seleção de caracter especial.
 */
public class SelecaoCaracterEspecialView extends JDialog {
	
	private static final Log log = LogFactory.getLog(SelecaoCaracterEspecialView.class);
	
	private static final String CHARS = "§&ºª“”‘’";
	
    private static final long serialVersionUID = 1L;

    private EkitCore 	editor;
    private JButton		btnSelecaoCaracterEspecial;

    private JPanel jContentPane;
    private JToolBar tb;
    private JButton fechar;

    public SelecaoCaracterEspecialView(EkitCore editor, JButton btnSelecaoCaracterEspecial) {
        super(editor.getWindow());
        this.editor = editor;
        this.btnSelecaoCaracterEspecial = btnSelecaoCaracterEspecial;
        initialize();
    }

    protected void fechaJanela() {
        this.setVisible(false);
    }

    private void initialize() {
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setModal(true);
        this.setContentPane(getJContentPane());
        this.pack();
    }
    
	@Override
    public void setVisible(boolean b) {
        
		if(b) {
			// Posiciona antes de apresentar
			JButton botao = btnSelecaoCaracterEspecial;
			int x = botao.getLocationOnScreen().x;
			int y = botao.getLocationOnScreen().y + botao.getSize().height + 5;
			this.setLocation(x, y);
		}

        super.setVisible(b);
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout(0, 0));
            jContentPane.add(getToolBar(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    private JToolBar getToolBar() {
        if (tb == null) {
            tb = new JToolBar();
            tb.setFloatable(false);
            tb.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            for(int i = 0; i < CHARS.length(); i++) {
            	tb.add(getButton(CHARS.charAt(i)));
            }
            tb.addSeparator();
            tb.add(getFechar());
        }
        return tb;
    }
    
    private JButton getButton(final char ch) {
        JButton button = new JButtonNoFocus();
        button.setText("<html><font style='font-family: Serif; font-size: 1.2em;'>" + ch + "</font></html>");
        setSize(button, 30, (int)btnSelecaoCaracterEspecial.getPreferredSize().getHeight());
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	try {
					editor.insertUnicodeChar(ch + "");
				} catch (Exception e1) {
					log.error("Falha na inclusão de caractere.", e1);
				}
                fechaJanela();
            }
        });
        return button;
    }
    
    private void setSize(JButton button, int width, int height) {
        Dimension d = new Dimension(width, height);
        button.setMaximumSize(d);
        button.setMinimumSize(d);
        button.setPreferredSize(d);
    }

    private JButton getFechar() {
        if (fechar == null) {
            fechar = new JButton();
            fechar.setText("Fechar");
            setSize(fechar, (int)fechar.getPreferredSize().getWidth(), 
            		(int)btnSelecaoCaracterEspecial.getPreferredSize().getHeight());
            fechar.setMnemonic(KeyEvent.VK_F);
            fechar.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    fechaJanela();
                }
                
            });
        }
        return fechar;
    }
} // @jve:decl-index=0:visual-constraint="10,10"

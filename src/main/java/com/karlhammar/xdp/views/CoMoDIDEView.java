package com.karlhammar.xdp.views;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlhammar.xdp.modl.ModlPanel;

public class CoMoDIDEView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(CoMoDIDEView.class);

	private JLabel helloWorldLabel;
	private ModlPanel modlPanel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        
        helloWorldLabel = new JLabel("Hello, world.");
        add(helloWorldLabel, BorderLayout.CENTER);
        
        modlPanel = new ModlPanel();
        modlPanel.setPreferredSize(new Dimension(300, modlPanel.getPreferredSize().height));
        add(modlPanel, BorderLayout.EAST);
        
        log.info("CoMoDIDE view initialized");
    }

	@Override
	protected void disposeOWLView() {
		log.info("CoMoDIDE view disposed");
	}
}

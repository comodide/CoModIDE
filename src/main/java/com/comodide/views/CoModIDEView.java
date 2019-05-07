package com.comodide.views;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternsPanel;

public class CoModIDEView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(CoModIDEView.class);

	private JLabel helloWorldLabel;
	private PatternsPanel patternsPanel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        
        helloWorldLabel = new JLabel("Hello, world.");
        add(helloWorldLabel, BorderLayout.CENTER);
        
        patternsPanel = new PatternsPanel();
        patternsPanel.setPreferredSize(new Dimension(300, patternsPanel.getPreferredSize().height));
        add(patternsPanel, BorderLayout.EAST);
        
        log.info("CoModIDE view initialized");
    }

	@Override
	protected void disposeOWLView() {
		log.info("CoModIDE view disposed");
	}
}

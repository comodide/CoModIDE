package com.karlhammar.xdp.views;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoMoDIDEView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(CoMoDIDEView.class);

	private JLabel helloWorldLabel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        helloWorldLabel = new JLabel("No CODP selected.");
        add(helloWorldLabel, BorderLayout.CENTER);
        log.info("CoMoDIDE View initialized");
    }

	@Override
	protected void disposeOWLView() {
		log.info("ODP Details View disposed");
	}
}

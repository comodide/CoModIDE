package com.karlhammar.xdp.views;

import java.awt.BorderLayout;
import java.net.URI;

import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodpDetailsView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(CodpDetailsView.class);

	private JLabel codpUriLabel;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        codpUriLabel = new JLabel("No CODP selected.");
        add(codpUriLabel, BorderLayout.CENTER);
        log.info("ODP Details View initialized");
    }
	
	public void selectionChanged(URI selectedCodpUri) {
		codpUriLabel.setText(selectedCodpUri.toString());
	}

	@Override
	protected void disposeOWLView() {
		log.info("ODP Details View disposed");
	}
}

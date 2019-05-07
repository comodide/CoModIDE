package com.comodide.views;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoModIDETab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 5629088250627561592L;
	private static final Logger log = LoggerFactory.getLogger(CoModIDETab.class);

	public CoModIDETab() {
	}

    @Override
	public void initialise() {
		super.initialise();
		log.info("CoModIDE tab initialized");
	}

	@Override
	public void dispose() {
		super.dispose();
		log.info("CoModIDE tab disposed");
	}
}
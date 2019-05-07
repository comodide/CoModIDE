package com.karlhammar.xdp.views;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoMoDIDETab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 5629088250627561592L;
	private static final Logger log = LoggerFactory.getLogger(CoMoDIDETab.class);

	public CoMoDIDETab() {
	}

    @Override
	public void initialise() {
		super.initialise();
		log.info("CoMoDIDE tab initialized");
	}

	@Override
	public void dispose() {
		super.dispose();
		log.info("CoMoDIDE tab disposed");
	}
}
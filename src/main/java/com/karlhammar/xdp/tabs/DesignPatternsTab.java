package com.karlhammar.xdp.tabs;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignPatternsTab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 4570911452730905251L;
	private static final Logger log = LoggerFactory.getLogger(DesignPatternsTab.class);

	public DesignPatternsTab() {
		setToolTipText("Custom tooltip text for Design Patterns Tab");
	}

    @Override
	public void initialise() {
		super.initialise();
		log.info("Design Patterns Tab initialized");
	}

	@Override
	public void dispose() {
		super.dispose();
		log.info("Design Patterns Tab disposed");
	}
}

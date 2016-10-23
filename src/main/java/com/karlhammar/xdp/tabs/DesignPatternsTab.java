package com.karlhammar.xdp.tabs;

import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlhammar.xdp.views.OdpDetailsView;
import com.karlhammar.xdp.views.OdpSelectorView;

public class DesignPatternsTab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 4570911452730905251L;
	private static final Logger log = LoggerFactory.getLogger(DesignPatternsTab.class);

	public DesignPatternsTab() {
		setToolTipText("Custom tooltip text for Design Patterns Tab");
	}

    @Override
	public void initialise() {
		super.initialise();
		
		for (View view : getViewsPane().getViews()){
            ViewComponent vc = view.getViewComponent();
            if (vc != null){
            	if (vc instanceof OdpSelectorView) {
            		log.info("Found selector view and it's not null!");
            	}
            	if (vc instanceof OdpDetailsView) {
            		log.info("Found details view and it's not null!");
            	}
            }
		}
		log.info("Design Patterns Tab initialized");
		
	}
    
	@Override
	public void dispose() {
		super.dispose();
		log.info("Design Patterns Tab disposed");
	}
}

package com.karlhammar.xdp.views;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.karlhammar.xdp.tabs.DesignPatternsTab;

public class OdpSelectorView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(OdpSelectorView.class);
	
	private OWLWorkspaceViewsTab parentTab;
	private OdpDetailsView detailsView;

    private String[] categories = {
    		"Academy",
    		"Agriculture",
    		"Biology",
    		"Building and Construction",
    		"Business",
    		"Cartography",
    		"Chemistry",
    		"Context-aware",
    		"Decision-making",
    		"Design",
    		"DocumentManagement",
    		"Earth Science or Geoscience",
    		"Ecology",
    		"Event Processing",
    		"Explanation",
    		"Fishery",
    		"Game",
    		"General",
    		"Geology",
    		"Healthcare",
    		"Identity",
    		"IndustrialProcesses",
    		"Intellectual Property",
    		"Internet of Things",
    		"Internet of Things (IoT)",
    		"Interoperability",
    		"Knowledge engineering",
    		"Law",
    		"Linguistic",
    		"Management",
    		"Manufacturing",
    		"Materials Science",
    		"Media",
    		"Multimedia",
    		"Music",
    		"Ontology",
    		"Ontology Alignment",
    		"Organization",
    		"Participation",
    		"Parts and Collections",
    		"Personalization",
    		"Pharmaceutical",
    		"Philosophical engineering",
    		"Physics",
    		"Planning",
    		"Product development",
    		"Recommendation",
    		"Scheduling",
    		"Semiotics",
    		"Social Science",
    		"Software",
    		"Software Engineering",
    		"Software measurement",
    		"Time",
    		"University",
    		"Upper Ontology",
    		"Vocabulary",
    		"Web Architecture",
    		"Web2.0",
    		"Workflow"
    };

    @Override
    protected void initialiseOWLView() throws Exception {
    	
    	Container parentContainer = this.getParent();
    	if (parentContainer instanceof DesignPatternsTab) {
    		log.info("Found parent tab reference!");
    	}
    	
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        // Category selector
        JLabel categorySelectorLabel = new JLabel("ODP Category Selector");
        add(categorySelectorLabel);
        JComboBox<String> categoryList = new JComboBox<String>(this.categories);
        add(categoryList);
        
        // Search query field
        JLabel searchHeading = new JLabel("ODP Search");
        add(searchHeading);
        JPanel searchPane = new JPanel();
        searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.X_AXIS));
        JLabel queryLabel = new JLabel("Query:");
        searchPane.add(queryLabel);
        JTextField queryField = new JTextField();
        searchPane.add(queryField);
        add(searchPane);
        
        // Search buttons
        JPanel searchButtonsPane = new JPanel();
        searchButtonsPane.setLayout(new BoxLayout(searchButtonsPane, BoxLayout.X_AXIS));
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detailsView = getDetailsView(parentTab);
				log.info("Details view = " + detailsView.getToolTipText());
				detailsView.setText("Click from selector view triggered!");
				// TODO Auto-generated method stub
				
			}
        });
        searchButtonsPane.add(searchButton);
        JButton resetButton = new JButton("Reset");
        searchButtonsPane.add(resetButton);
        add(searchButtonsPane);
        
        // Results list
        JLabel resultsHeading = new JLabel("Results list");
        add(resultsHeading);
        
        String[] resultsTableColumnNames = {"Name","Confidence","IRI"};
        String[][] resultsTablePlaceholderData = {
        		{"Action","1.0","http://www.ontologydesignpatterns.org/cp/owl/action.owl"},
        		{"Description","1.0","http://www.ontologydesignpatterns.org/cp/owl/description.owl"},
        		{"Nary Relation","1.0","http://www.ontologydesignpatterns.org/cp/owl/naryrelation.owl"},
        		{"Participation","0.9","http://www.ontologydesignpatterns.org/cp/owl/participation.owl"},
        		{"Object Record","0.8","http://www.ontologydesignpatterns.org/cp/owl/objectrecord.owl"},
        		{"Communication Event","0.7","http://www.ontologydesignpatterns.org/cp/owl/communicationevent.owl"},
        		{"Event Core","0.7","http://www.ontologydesignpatterns.org/cp/owl/eventcore.owl"},
        		{"Information Realization","0.5","http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl"},
        };
        JTable resultsTable = new JTable(resultsTablePlaceholderData, resultsTableColumnNames);
        JScrollPane resultsTableScrollPane = new JScrollPane(resultsTable);
        resultsTable.setFillsViewportHeight(true);
        add(resultsTableScrollPane);
        
        parentTab = getParentTab(this);
        log.info("ODP Selector View initialized. Parent tab = " + parentTab.getLabel());
    }

    private OdpDetailsView getDetailsView(OWLWorkspaceViewsTab parentTab) {
    	for (View childView: parentTab.getViewsPane().getViews()) {
    		ViewComponent vc = childView.getViewComponent();
    		if (vc != null) {
    			if (vc instanceof OdpDetailsView) {
    				return (OdpDetailsView)vc;
    			}
    		}
    	}
    	return null;
    }
    
    private OWLWorkspaceViewsTab getParentTab(Container childContainer) throws Exception {
    	Container parentContainer = childContainer.getParent();
    	if (parentContainer != null) {
    		if (parentContainer instanceof DesignPatternsTab) {
    			return (OWLWorkspaceViewsTab)parentContainer;
    		}
    		else {
    			return getParentTab(parentContainer);
    		}
    	}
		throw new Exception("No parent tab found!");
    }
    
    
    
	@Override
	protected void disposeOWLView() {
		log.info("ODP Selector View disposed");
	}
	
	
}

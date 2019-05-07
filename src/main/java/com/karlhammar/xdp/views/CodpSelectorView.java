package com.karlhammar.xdp.views;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodpSelectorView extends AbstractOWLViewComponent implements ActionListener {
	
	private DefaultTableModel resultsTableModel = new DefaultTableModel(new String[]{"Name","Confidence","IRI"},0);
	private JTextField queryField = new JTextField();

	public class NoParentTabException extends Exception {
		private static final long serialVersionUID = -8844706421403164734L;
		public NoParentTabException(String message) {
	        super(message);
	    }
	}
	
	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(CodpSelectorView.class);

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
        searchPane.add(this.queryField);
        add(searchPane);
        
        // Search buttons
        JPanel searchButtonsPane = new JPanel();
        searchButtonsPane.setLayout(new BoxLayout(searchButtonsPane, BoxLayout.X_AXIS));
        JButton searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        searchButtonsPane.add(searchButton);
        JButton resetButton = new JButton("Reset");
        searchButtonsPane.add(resetButton);
        add(searchButtonsPane);
        
        // Results list
        JLabel resultsHeading = new JLabel("Results list");
        add(resultsHeading);

        /*String[] resultsTableColumnNames = {"Name","Confidence","IRI"};
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
        JTable resultsTable = new JTable(resultsTablePlaceholderData, resultsTableColumnNames);*/
        JTable resultsTable = new JTable(resultsTableModel);
        JScrollPane resultsTableScrollPane = new JScrollPane(resultsTable);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setColumnSelectionAllowed(false);
        resultsTable.setRowSelectionAllowed(true);
        ListSelectionModel selectionModel = resultsTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String selectedData = null;
                int selectedRow = resultsTable.getSelectedRow();
                selectedData = (String) resultsTable.getValueAt(selectedRow, 2);
                try {
                	// First find the hosting tab
                	URI selectedCodpUri = new URI(selectedData);
                	// Then iterate over all subviews of that tab, notifying only those
                	// that are CodpDetailsView instances.
					OWLWorkspaceViewsTab parentTab = getParentTab(CodpSelectorView.this);
					for (View childView: parentTab.getViewsPane().getViews()) {
			    		ViewComponent vc = childView.getViewComponent();
			    		if (vc != null) {
			    			if (vc instanceof CoMoDIDEView) {
			    				//((CoMoDIDEView)vc).selectionChanged(selectedCodpUri);;
			    			}
			    		}
			    	}
				} 
                catch (NoParentTabException npte) {
					npte.printStackTrace();
				} 
                catch (URISyntaxException use) {
					use.printStackTrace();
				}
              }

            });
        add(resultsTableScrollPane);
       
        log.info("ODP Selector View initialized");
    }
    
    private OWLWorkspaceViewsTab getParentTab(Container childContainer) throws NoParentTabException {
    	Container parentContainer = childContainer.getParent();
    	if (parentContainer != null) {
    		if (parentContainer instanceof OWLWorkspaceViewsTab) {
    			return (OWLWorkspaceViewsTab)parentContainer;
    		}
    		else {
    			return getParentTab(parentContainer);
    		}
    	}
		throw new NoParentTabException("No parent OWLWorkspaceViewsTab was found for this view!");
    }
    
	@Override
	protected void disposeOWLView() {
		log.info("ODP Selector View disposed");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO: Figure out why table isn't being properly reloaded
		// TODO: Handle other clicks than search clicks, based on e.getActionCommand 
		// TODO: Make REST endpoint used configurable
		// Construct search engine query URL
		String query = this.queryField.getText();
		String queryUrl = "";
		try {
			queryUrl = String.format("http://wp.xd-protege.com:7777/search/odpSearch?queryString=%s", URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// Try to get JSON string from search engine query URL
		String jsonText = "[]";
		try {
			InputStream is = new URL(queryUrl).openStream();
			jsonText = IOUtils.toString(is, "UTF-8");
			is.close();
		} 
		catch (IOException e1) {
			// TODO Implement proper exception handling and logging
			JOptionPane.showMessageDialog(null, "Query failed: " + e1.toString());
			e1.printStackTrace();
		}
		
		// Clear out existing results from table model before proceeding
		for (int i = 0; i < this.resultsTableModel.getRowCount(); i++ )
		{
			this.resultsTableModel.removeRow(i);
		}
		
		// Parse JSON results and add to table model
		JSONArray json = new JSONArray(jsonText);
		for (int i = 0; i < json.length(); i++) {
			JSONObject odpSearchResult = json.getJSONObject(i);
			float confidence = odpSearchResult.getFloat("confidence");
			JSONObject odp = odpSearchResult.getJSONObject("odp");
			String name = odp.getString("name");
			String iri = odp.getString("iri");
			this.resultsTableModel.addRow(new String[] {name, Float.toString(confidence), iri});
		}
		
		// Refresh table
		this.resultsTableModel.fireTableDataChanged();
	}
}

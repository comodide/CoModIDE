package com.comodide.patterns;

import java.util.ArrayList;
import java.util.List;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

public class PatternInstantiationConfiguration {
	
	public static enum EdgeCreationAxiom {
		RDFS_RANGE("RDFS Range"),
		RDFS_DOMAIN("RDFS Domain"),
		OTHER_OPTION("Other option");
		
		private final String fieldDescription;
		
		private EdgeCreationAxiom(String value) {
	        fieldDescription = value;
	    }

	    public String toString() {
	        return fieldDescription;
	    }
	}
	
	private static final PreferencesManager PREFERENCES_MANAGER = PreferencesManager.getInstance();
	private static final String PREFERENCES_SET_ID = "com.comodide.CoModIDE";
	private static final String EDGE_CREATION_AXIOMS_KEY = "EDGE_CREATION_AXIOMS";
	
	public static List<EdgeCreationAxiom> getSelectedEdgeCreationAxioms() {
		Preferences prefs = PREFERENCES_MANAGER.getPreferencesForSet(PREFERENCES_SET_ID, EDGE_CREATION_AXIOMS_KEY);
		List<EdgeCreationAxiom> retVal = new ArrayList<EdgeCreationAxiom>();
		for (String s: prefs.getStringList(EDGE_CREATION_AXIOMS_KEY, new ArrayList<String>())) {
			retVal.add(EdgeCreationAxiom.valueOf(s));
		}
		return retVal;
	}
	
	public static void addSelectedEdgeCreationAxiom(EdgeCreationAxiom eca) {
		List<EdgeCreationAxiom> ecasToUpdate = getSelectedEdgeCreationAxioms();
		if (!ecasToUpdate.contains(eca)) {
			ecasToUpdate.add(eca);
			setSelectedEdgeCreationAxioms(ecasToUpdate);
		}
	}
	
	public static void removeSelectedEdgeCreationAxiom(EdgeCreationAxiom eca) {
		List<EdgeCreationAxiom> ecasToUpdate = getSelectedEdgeCreationAxioms();
		if (ecasToUpdate.contains(eca)) {
			ecasToUpdate.remove(eca);
			setSelectedEdgeCreationAxioms(ecasToUpdate);
		}
	}
	
	private static void setSelectedEdgeCreationAxioms(List<EdgeCreationAxiom> creationAxioms) {
		Preferences prefs = PREFERENCES_MANAGER.getPreferencesForSet(PREFERENCES_SET_ID, EDGE_CREATION_AXIOMS_KEY);
		List<String> listToStore = new ArrayList<String>();
		for (EdgeCreationAxiom e: creationAxioms) {
			listToStore.add(e.name());
		}
		prefs.putStringList(EDGE_CREATION_AXIOMS_KEY, listToStore);
	}
}

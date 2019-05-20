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
	
	private static final String PREFERENCES_SET_ID = "com.comodide.comodide";
	private static final String INSTANTIATION_PREFERENCES_KEY = "instantiation_configuration";
	private static final String EDGE_CREATION_AXIOMS_KEY = "edge_creation_axioms";
	private static final String USE_TARGET_NAMESPACE_KEY = "use_target_namespace";
	
	private static final PreferencesManager PREFERENCES_MANAGER = PreferencesManager.getInstance();
	private static final Preferences PREFERENCES = PREFERENCES_MANAGER.getPreferencesForSet(PREFERENCES_SET_ID, INSTANTIATION_PREFERENCES_KEY);
	
	public static List<EdgeCreationAxiom> getSelectedEdgeCreationAxioms() {
		List<EdgeCreationAxiom> retVal = new ArrayList<EdgeCreationAxiom>();
		for (String s: PREFERENCES.getStringList(EDGE_CREATION_AXIOMS_KEY, new ArrayList<String>())) {
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
		List<String> listToStore = new ArrayList<String>();
		for (EdgeCreationAxiom e: creationAxioms) {
			listToStore.add(e.name());
		}
		PREFERENCES.putStringList(EDGE_CREATION_AXIOMS_KEY, listToStore);
	}
	
	public static Boolean getUseTargetNamespace() {
		return PREFERENCES.getBoolean(USE_TARGET_NAMESPACE_KEY, true);
	}
	
	public static void setUseTargetNamespace(Boolean value) {
		PREFERENCES.putBoolean(USE_TARGET_NAMESPACE_KEY, value);
	}
}

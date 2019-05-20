package com.comodide.patterns;

import java.util.ArrayList;
import java.util.List;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 * Configuration for the CoModIDE plugin's pattern instantiation mechanisms.
 * @author Karl Hammar <karl@karlhammar.com>
 */
public class PatternInstantiationConfiguration {
	
	/**
	 * Enumeration of the different types of ontology axioms that the user might want to create when drawing
	 * a new edge between two nodes in the schema diagram. 
	 * @author Karl Hammar <karl@karlhammar.com>
	 */
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
	
	// Keys used for the Java Preferences API
	private static final String PREFERENCES_SET_ID = "com.comodide.comodide";
	private static final String INSTANTIATION_PREFERENCES_KEY = "instantiation_configuration";
	private static final String EDGE_CREATION_AXIOMS_KEY = "edge_creation_axioms";
	private static final String USE_TARGET_NAMESPACE_KEY = "use_target_namespace";
	private static final String MODULE_METADATA_EXTERNAL_KEY = "module_metadata_external";
	
	// Preference manager and set of preferences for the CoModIDE plugin's instantiation configuration
	private static final PreferencesManager PREFERENCES_MANAGER = PreferencesManager.getInstance();
	private static final Preferences PREFERENCES = PREFERENCES_MANAGER.getPreferencesForSet(PREFERENCES_SET_ID, INSTANTIATION_PREFERENCES_KEY);
	
	/**
	 * A list of axioms that the user may want to create in the target ontology when drawing a new edge between 
	 * two nodes in the schema diagram.
	 */
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
	
	/**
	 * Whether to use the target ontology namespace when copying pattern entities. If this is false,
	 * the pattern entities will retain their original IRIs from the pattern namespace even after 
	 * being cloned into the target ontology.
	 */
	public static Boolean getUseTargetNamespace() {
		return PREFERENCES.getBoolean(USE_TARGET_NAMESPACE_KEY, true);
	}
	
	public static void setUseTargetNamespace(Boolean value) {
		PREFERENCES.putBoolean(USE_TARGET_NAMESPACE_KEY, value);
	}
	
	/** 
	 * Whether to keep modularisation annotations external to the target ontology or not. The pattern selector
	 * will provide two OWLOntology objects, one holding the constructs of the instantiated pattern itself, and one
	 * holding the annotations about this instantiation as an OPLa module. This preference indicates whether the
	 * user wants both of these two to be merged into the target ontology, or whether the annotations should be
	 * kept separate. 
	 */
	public static Boolean getModuleMetadataExternal() {
		return PREFERENCES.getBoolean(MODULE_METADATA_EXTERNAL_KEY, true);
	}
	
	public static void setModuleMetadataExternal(Boolean value) {
		PREFERENCES.putBoolean(MODULE_METADATA_EXTERNAL_KEY, value);
	}
}

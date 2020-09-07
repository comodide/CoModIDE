package com.comodide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

/**
 * Configuration for the CoModIDE plugin.
 * @author Karl Hammar <karl@karlhammar.com>
 */
public class ComodideConfiguration {
	
	/**
	 * Enumeration of the different types of ontology axioms that the user might want to create when drawing
	 * a new edge between two nodes in the schema diagram. 
	 * @author Karl Hammar <karl@karlhammar.com>
	 */
	public static enum EdgeCreationAxiom {
		RDFS_DOMAIN_RANGE("RDFS Domain/Range"),
		SCOPED_RANGE("AllValuesFrom constraint"),
		SCOPED_DOMAIN("SomeValuesFrom constraint");
		
		private final String fieldDescription;
		
		private EdgeCreationAxiom(String value) {
	        fieldDescription = value;
	    }

	    public String toString() {
	        return fieldDescription;
	    }
	    
	    public static List<String> getDefault() {
	    	ArrayList<String> defaultSelection = new ArrayList<String>();
	    	defaultSelection.add("RDFS_DOMAIN_RANGE");
	    	return defaultSelection;
	    }
	}
	
	// Keys used for the Java Preferences API
	private static final String PREFERENCES_SET_ID = "com.comodide.comodide";
	private static final String INSTANTIATION_PREFERENCES_KEY = "comodide_configuration";
	private static final String EDGE_CREATION_AXIOMS_KEY = "edge_creation_axioms";
	private static final String USE_TARGET_NAMESPACE_KEY = "use_target_namespace";
	private static final String MODULE_METADATA_EXTERNAL_KEY = "module_metadata_external";
	private static final String DELETE_PROPERTY_DECLARATIONS_KEY = "delete_property_declarations";
	private static final String SEND_TELEMETRY_KEY = "send_telemetry"; 
	private static final String AUTO_COMPOSE_INTERFACES_KEY = "auto_compose_interfaces";
	private static final String TELEMETRY_PREFERENCE_CHECKED_KEY = "telemetry_preference_checked"; 
	
	// Preference manager and set of preferences for the CoModIDE plugin's instantiation configuration
	private static final PreferencesManager PREFERENCES_MANAGER = PreferencesManager.getInstance();
	private static final Preferences PREFERENCES = PREFERENCES_MANAGER.getPreferencesForSet(PREFERENCES_SET_ID, INSTANTIATION_PREFERENCES_KEY);
	
	/**
	 * A list of axioms that the user may want to create in the target ontology when drawing a new edge between 
	 * two nodes in the schema diagram.
	 */
	public static Set<EdgeCreationAxiom> getSelectedEdgeCreationAxioms() {
		Set<EdgeCreationAxiom> retVal = new HashSet<EdgeCreationAxiom>();
		for (String s: PREFERENCES.getStringList(EDGE_CREATION_AXIOMS_KEY, EdgeCreationAxiom.getDefault())) {
			retVal.add(EdgeCreationAxiom.valueOf(s));
		}
		return retVal;
	}
	
	public static void addSelectedEdgeCreationAxiom(EdgeCreationAxiom eca) {
		Set<EdgeCreationAxiom> ecasToUpdate = getSelectedEdgeCreationAxioms();
		if (!ecasToUpdate.contains(eca)) {
			ecasToUpdate.add(eca);
			setSelectedEdgeCreationAxioms(ecasToUpdate);
		}
	}
	
	public static void removeSelectedEdgeCreationAxiom(EdgeCreationAxiom eca) {
		Set<EdgeCreationAxiom> ecasToUpdate = getSelectedEdgeCreationAxioms();
		if (ecasToUpdate.contains(eca)) {
			ecasToUpdate.remove(eca);
			setSelectedEdgeCreationAxioms(ecasToUpdate);
		}
	}
	
	private static void setSelectedEdgeCreationAxioms(Set<EdgeCreationAxiom> creationAxioms) {
		List<String> listToStore = new ArrayList<String>();
		for (EdgeCreationAxiom e: creationAxioms) {
			listToStore.add(e.name());
		}
		PREFERENCES.putStringList(EDGE_CREATION_AXIOMS_KEY, listToStore);
	}
	
	/**
	 * Whether to delete the property declaration when an edge is deleted from the schema diagram.
	 * If false, only domains and ranges (rdfs or scoped) will be deleted but the property itself kept
	 * in the ontology.
	 */
	public static Boolean getDeletePropertyDeclarations() {
		return PREFERENCES.getBoolean(DELETE_PROPERTY_DECLARATIONS_KEY, true);
	}
	
	public static void setDeletePropertyDeclarations(Boolean value) {
		PREFERENCES.putBoolean(DELETE_PROPERTY_DECLARATIONS_KEY, value);
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
	
	/**
	 * Whether to send usage telemetry. 
	 *
	 */
	
	public static Boolean getSendTelemetry() {
		return PREFERENCES.getBoolean(SEND_TELEMETRY_KEY, false);
	}
	
	public static void setSendTelemetry(Boolean value) {
		PREFERENCES.putBoolean(SEND_TELEMETRY_KEY, value);
	}
	
	/**
	 * Whether the user has been queried for telemetry sending
	 */
	public static Boolean getTelemetryPreferenceChecked() {
		return PREFERENCES.getBoolean(TELEMETRY_PREFERENCE_CHECKED_KEY, false);
	}
	
	public static void setTelemetryPreferenceChecked(Boolean value) {
		PREFERENCES.putBoolean(TELEMETRY_PREFERENCE_CHECKED_KEY, value);
	}
	
	
	/**
	 * Whether to auto-compose interfaces. 
	 *
	 */
	public static Boolean getAutoComposeInterfaces() {
		return PREFERENCES.getBoolean(AUTO_COMPOSE_INTERFACES_KEY, true);
	}
	
	public static void setAutoComposeInterfaces(Boolean value) {
		PREFERENCES.putBoolean(AUTO_COMPOSE_INTERFACES_KEY, value);
	}
}

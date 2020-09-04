package com.comodide.axiomatization;

import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a central object for adding OPLa annotations to an ontology.
 * @author cogan
 *
 */
public class OPLaAnnotationManager
{
	private static OPLaAnnotationManager instance = null;
	
	public static OPLaAnnotationManager getInstance(OWLModelManager modelManager)
	{
		if(instance == null)
		{
			instance = new OPLaAnnotationManager(modelManager);
		}
		
		return instance;
	}
	
	/** Bookkeeping */
	private final Logger log = LoggerFactory.getLogger(OPLaAnnotationManager.class);
	private final String pf = "[CoModIDE:OPLaAnnotationManager]";
	
	/** Used for adding annotations to the active ontology */
	
	
	private OPLaAnnotationManager(OWLModelManager modelManager)
	{
		
	}
}

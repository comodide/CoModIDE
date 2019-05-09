package com.comodide.rendering.owlax.util;

/**
 * Represents the different types of OWL 2 Entities.
 * 
 * @author Matthew Horridge, The University of Manchester, Information
 *         Management Group
 * @since 3.0.0
 * @param <E>
 *        entity type
 */
@SuppressWarnings("unused")
public class CustomEntityType  {

    private static final long serialVersionUID = 40000L;
    //@formatter:off
    /** class entity */                 public static final CustomEntityType CLASS = new CustomEntityType( "Class");
    /** object property entity */       public static final CustomEntityType OBJECT_PROPERTY = new CustomEntityType( "Object Property");
    /** data property entity */         public static final CustomEntityType DATA_PROPERTY = new CustomEntityType( "Data Property");
    /** annotation property entity*/    public static final CustomEntityType ANNOTATION_PROPERTY = new CustomEntityType( "Annotation Property");
    /** named individual entity */      public static final CustomEntityType NAMED_INDIVIDUAL = new CustomEntityType("Named Individual");
    /** datatype entity */              public static final CustomEntityType DATATYPE = new CustomEntityType( "Datatype");
    /** RDF_TYPE */         		    public static final CustomEntityType LITERAL = new CustomEntityType( "Literal");
    /** LITERAL */          		    public static final CustomEntityType RDFTYPE = new CustomEntityType( "rdf:type");
    /** RDFS_SUBCLASS_OF */             public static final CustomEntityType RDFSSUBCLASS_OF = new CustomEntityType( "rdfs:subClassOf");
  //  private static final List<CustomEntityType<?>> VALUES = Collections.<CustomEntityType<?>> unmodifiableList(Arrays.asList(CLASS, OBJECT_PROPERTY, DATA_PROPERTY, ANNOTATION_PROPERTY, NAMED_INDIVIDUAL, DATATYPE));
  //@formatter:on
    
    private  String Name;
    
    public CustomEntityType(){
    	//this(null);
    }

    public CustomEntityType(String name) {
    	
    		this.Name = name;
    }

    /** @return toe vocabulary enum corresponding to this entity */
    /*public OWLRDFVocabulary getVocabulary() {
        return vocabulary;
    }*/

    /** @return this entity tipe name */
    public String getName() {
        return Name;
    }
    
    /** @return this entity tipe name */
    public void setName(String Name) {
        this.Name = Name;
    }
    
    @Override
    public String toString(){
    	return Name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
    	 if (this == obj)
             return true;
         if (getClass() != obj.getClass())
             return false;
         if(this.toString() == obj.toString())
        	 return true;
         return false;
    }


}


import sys
import os
from rdflib import URIRef, Graph, Namespace, Literal
from rdflib import OWL, RDF, RDFS, XSD, TIME

#  Directories
pattern_path = "../schema/"

# Prefix Configurations
name_space = "https://kastle-lab.org/easyai/"
modl_namespace = "https://archive.org/services/purl/purl/modular_ontology_design_library#"
opla_namespace = "https://ontologydesignpatterns.org/"

pfs = {
"": Namespace(f"{name_space}"),
"modl": Namespace(f"{modl_namespace}"),
"kl-res": Namespace(f"{name_space}lod/resource/"),
"kl-ont": Namespace(f"{name_space}lod/ontology/"),
"rdf": RDF,
"rdfs": RDFS,
"xsd": XSD,
"owl": OWL,
"time": TIME,
"dc": Namespace("http://purl.org/dc/elements/1.1/"),
"opla": Namespace("http://ontologydesignpatterns.org/opla#"),
"opla-core": Namespace("http://ontologydesignpatterns.org/opla-core#"),
"opla-sd": Namespace("http://ontologydesignpatterns.org/opla-sd#"),
"opla-cp": Namespace("http://ontologydesignpatterns.org/opla-cp#")
}

# Property Shortcuts
a = pfs["rdf"]["type"]


def init_kg(prefixes=pfs):
    kg = Graph()
    for prefix in pfs:
        kg.bind(prefix, pfs[prefix])
    return kg

def generate_index():
    graph = init_kg()
    result_files = os.listdir(pattern_path)
    result_files.sort()

    # EASY-AI Ontology 
    ## Minting URIs
    easyai_ontology = f"{name_space}"
    easyai_onto_uri = Namespace(easyai_ontology)[""]
    
    pattern_uri = pfs["opla"]["Pattern"]
    category_uri = pfs["opla"]["Category"]
    graph.add( (category_uri, a, pfs["owl"]["Class"]) )

    ## Bind
    graph.add( (easyai_onto_uri, a, OWL.Ontology) )
    graph.add( (easyai_onto_uri, pfs["opla-core"]["hasPatternName"], Literal("Easy-AI Modular Ontology Design Library")) )

    ###  Add specific OPLA annotation+property type
    graph.add( (pfs["opla"]["owlRepresentation"], a, pfs["owl"]["DatatypeProperty"]) )
    graph.add( (pfs["opla"]["owlRepresentation"], pfs["rdfs"]["label"], Literal(f"Owl Representation", lang="en")) )
    graph.add( (pfs["opla"]["renderedSchemaDiagram"], a, pfs["owl"]["DatatypeProperty"]) )
    graph.add( (pfs["opla"]["renderedSchemaDiagram"], pfs["rdfs"]["label"], Literal(f"Rendered Schema Diagram", lang="en")) )
    graph.add( (pfs["opla"]["htmlDocumentation"], a, pfs["owl"]["DatatypeProperty"]) )
    graph.add( (pfs["opla"]["htmlDocumentation"], pfs["rdfs"]["label"], Literal(f"Owl HTML Documentation", lang="en")) )

    graph.add( (pfs["opla"]["owlRepresentation"], pfs["rdfs"]["domain"], pattern_uri) )
    graph.add( (pfs["opla"]["owlRepresentation"], pfs["rdfs"]["range"], pfs["xsd"]["string"]) )
    graph.add( (pfs["opla"]["renderedSchemaDiagram"], pfs["rdfs"]["domain"], pattern_uri) )
    graph.add( (pfs["opla"]["renderedSchemaDiagram"], pfs["rdfs"]["range"], pfs["xsd"]["string"]) )
    graph.add( (pfs["opla"]["htmlDocumentation"], pfs["rdfs"]["domain"], pattern_uri) )
    graph.add( (pfs["opla"]["htmlDocumentation"], pfs["rdfs"]["range"], pfs["xsd"]["string"]) )
    graph.add( (pfs["opla"]["categorization"], pfs["rdfs"]["domain"], pattern_uri))
    graph.add( (pfs["opla"]["categorization"], pfs["rdfs"]["range"], category_uri) )


    html_counter=1
    for boxology_patterns in result_files: # For each pattern directory
        if not os.path.isdir(os.path.join(pattern_path, boxology_patterns)): # skip auxiliary files (not patterns)
            continue 
        if "all-together" in str(boxology_patterns):
            continue
        if "example" in str(boxology_patterns): # skip the instantiated example
            continue
        if boxology_patterns == "easy-ai-index.ttl": # Skip the index file
            continue

        filenames = os.listdir(os.path.join(pattern_path, boxology_patterns))
        
        for filename in filenames:
            pattern_file, ext = filename.split(".")
            pattern, _ = pattern_file.split("-schema")
            pattern_identifier,_ = pattern.split("-easy-ai")
            # Mint a Noun Pattern
            boxology_pattern_uri = Namespace(easyai_ontology)[f"{pattern_identifier}"]
            # Bind
            graph.add( (boxology_pattern_uri, a, OWL.NamedIndividual) )
            graph.add( (boxology_pattern_uri, a, pattern_uri) )

            ### Added artifact for CoModIDE
            path = pattern_path.split("../")[-1]
            ttl_path = path+f"{pattern}/{filename}"
            schema_path = f"/schema-diagrams/{pattern_identifier}-elementary-pattern/{pattern}-elementary-pattern.pdf"

            graph.add( (boxology_pattern_uri, pfs["opla"]["owlRepresentation"], Literal(f"{ttl_path}", datatype=XSD.string)) )
            graph.add( (boxology_pattern_uri, pfs["rdfs"]["label"], Literal(f"{pattern}", lang="en")) )
            graph.add( (boxology_pattern_uri, pfs["opla"]["renderedSchemaDiagram"], Literal(f"{schema_path}", datatype=XSD.string)) )

            # graph.add( (noun_pattern_uri, pfs["opla"]["categorization"], Literal(f"{ttl_path}", datatype=XSD.string)) )
            # graph.add( (noun_pattern_uri, pfs["rdfs"]["label"], Literal("Category", lang="en")) )
            
            noun_html = f'''
                <html>
                <body>
                <h3 class="sectionHead"><span class="titlemark">{html_counter}
                </span> <a id="x1-10001"></a>{pattern}</h3>

                <body/>
                <html/>
            '''

            graph.add( (boxology_pattern_uri, pfs["opla"]["htmlDocumentation"], Literal(f"{noun_html}", datatype=XSD.string)) )
            graph.add( (boxology_pattern_uri, pfs["rdfs"]["label"], Literal(f"{pattern}", lang="en")) )
            html_counter+=1

    # output details: index file name
    output_name = "easy-ai-index.ttl"               
    output_path = "../schema"
    output_path = os.path.join(output_path, output_name)
    graph.serialize(format="turtle", encoding="utf-8", destination=output_path)

if __name__ == "__main__":
    generate_index()

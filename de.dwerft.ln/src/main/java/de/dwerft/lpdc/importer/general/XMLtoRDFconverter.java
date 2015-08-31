package de.dwerft.lpdc.importer.general;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Model;

public abstract class XMLtoRDFconverter {
	
	protected OntologyConnector ontConn;
	protected XMLProcessor xmlProc;
	protected Mapper mapper;
	protected RdfProcessor rdfProc;

	/**
	 * The XMLtoRDFconverter is the controller of the OntologyConnector, XMLProcessor,
	 * Mapper, and RdfProcessor.
	 * 
	 * Specific XML converters inherit from this class and implement specific conversion
	 * routines that are execute before and after the general conversion process based
	 * on the mapping definitions.
	 * 
	 * @param ontologyFileName Filename of the OWL ontology to be used.
	 * @param ontologyFormat Format, e.g., RDF/XML, of the ontology file.
	 * @param mappingsFilename Filename of the mappings definitions.
	 */
	public XMLtoRDFconverter(String ontologyFileName, String ontologyFormat, String mappingsFilename) {
		ontConn = new OntologyConnector(ontologyFileName, ontologyFormat);
		mapper = new Mapper(mappingsFilename);
		rdfProc = new RdfProcessor(ontConn);
	}
	
	public void convert(InputStream is) {
		
		xmlProc = new XMLProcessor(is);
		
		processingBeforeConvert();
		
		Node node;		
		while((node = xmlProc.nextElement()) != null) {
			List<MappingDefinition> mappingsForNode = mapper.getMappingsForNode(node);
			for (MappingDefinition mappingDefinition : mappingsForNode) {
				rdfProc.createRDF(node, mappingDefinition);
			}
		}
		
		processingAfterConvert();
	}
	
	public Model getGeneratedModel() {
		return rdfProc.getGeneratedModel();
	}
	
	public abstract void processingBeforeConvert();

	public abstract void processingAfterConvert();

}
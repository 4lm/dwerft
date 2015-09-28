package de.werft.tools.importer.general;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.werft.tools.general.OntologyConstants;

/**
 * The OntologyConnector is responsible for retrieving ontology model elements based on the names and prefixes.
 * 
 * @author hagt
 *
 */
public class OntologyConnector {
	
	private OntModel ontologyModel;

	/**
	 * Creates a new OntologyConnector and reads the ontology from an URL.
	 * 
	 * @param url
	 * @param format
	 */
	public OntologyConnector(String url, String format) {
		this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.read(url,format);
	}
	
	/**
	 * Gets the ontology class or null if the class uri does not exist.
	 *
	 * @param uri The URI of the ontology class
	 *            
	 * @return the ontology class
	 */
	public OntClass getOntologyClass(String uri) {
		ExtendedIterator<OntClass> classes = ontologyModel.listClasses();
		Optional<OntClass> firstClass = classes.toList().stream().filter(o -> o.getURI().equals(uri)).findFirst();
		return firstClass.isPresent() ? firstClass.get() : null;
	}
	
	/**
	 * Gets a ontology datatype property.
	 *
	 * @param uri The URI of the datatype property
	 * @return The ontology datatype property
	 */
	public DatatypeProperty getOntologyDatatypeProperty(String uri) {
		ExtendedIterator<DatatypeProperty> props = ontologyModel.listDatatypeProperties();
		Optional<DatatypeProperty> firstProp = props.toList().stream().filter(o -> o.getURI().equals(uri)).findFirst();
		return firstProp.isPresent() ? firstProp.get() : null;
	}	
	
	/**
	 * Gets a ontology datatype property.
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology datatype property
	 */
	public DatatypeProperty getOntologyDatatypeProperty(String prefix, String propertyName) {
		return getOntologyDatatypeProperty(ontologyModel.getNsPrefixURI(prefix)+propertyName);
	}
	
	/**
	 * Get a ontology property
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology property
	 */
	public Property getProperty(String prefix, String propertyName) {
		return ontologyModel.getProperty(ontologyModel.getNsPrefixURI(prefix), propertyName);
	}
	
	
	/**
	 * Gets a ontology object property from the ontology model.
	 *
	 * @param uri The URI of the object property
	 * @return the ontology object property
	 */
	
	public ObjectProperty getOntologyObjectProperty(String uri) {
		ExtendedIterator<ObjectProperty> props = ontologyModel.listObjectProperties();
		Optional<ObjectProperty> firstProp = props.toList().stream().filter(o -> o.getURI().equals(uri)).findFirst();
		return firstProp.isPresent() ? firstProp.get() : null;
	}
	
	/**
	 * Gets a ontology object property from the ontology model.
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology object property
	 */
	public ObjectProperty getOntologyObjectProperty(String prefix, String propertyName) {
		return getOntologyObjectProperty(ontologyModel.getNsPrefixURI(prefix)+propertyName);
	}
	
	/**
	 * Retrieves the set of declared properties in the ontology model
	 * for the specific ontology class.
	 * @param uri The URI of the ontology class
	 * @return A set of property names
	 */
	public Set<Property> getDeclaredProperties(String uri) {
		Set<Property> result = new HashSet<Property>();
		
		OntClass ontClass = ontologyModel.getOntClass(uri);
		if (ontClass != null) {
			ExtendedIterator<OntProperty> props = ontClass.listDeclaredProperties();
			while (props.hasNext()) {
				OntProperty prop = (OntProperty) props.next();
				if (prop.getURI().startsWith(OntologyConstants.ONTOLOGY_NAMESPACE)) {
					result.add(prop);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the ontology model.
	 * 
	 * @return The ontology model.
	 */
	public OntModel getOntologyModel() {
		return ontologyModel;
	}
}
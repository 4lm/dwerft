package de.werft.tools.general.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateException;
import de.werft.tools.update.Update;
import de.werft.tools.update.UpdateFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.riot.RDFDataMgr;

import java.util.List;

/**
 * this class specifies the upload command used for
 * uploading rdf files to a triple store. It provides the
 * update based on the input granularity and the provided rdf file.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Parameters(commandDescription = "Upload RDF Models to SPARQL endpoints.")
public class UploadCommand {

    @Parameter(arity = 1, required = true, description = "Uploads a file to a specified sparql endpoint. Valid formats are" +
            " *.(rdf|ttl|nt|jsonld)")
    private List<String> uploadFile;

    @Parameter(names = {"-g"}, /*converter = GranularityConverter.class,*/ arity = 1,
            description = "Give a granularity for the upload command. Possible options are 0, 1, 2 where" +
            "0 deletes the given model; 1 inserts a given model; 2 creates a diff with the remote endpoint. Default is 1.")
    private String granularity = "1";

    @Parameter(names = {"-graph"}, arity = 1, required = true,
            description = "Provide a graph name to store the rdf.")
    private String graphName = "";

    private Model getUploadModel() throws ParameterException {
        if (StringUtils.substringAfterLast(uploadFile.get(0), ".").toLowerCase().matches("(rdf|ttl|nt|jsonld)")) {
            return RDFDataMgr.loadModel(uploadFile.get(0));
        } else {
            throw new ParameterException("Only (rdf|ttl|nt|jsonld) are valid file endings.");
        }
    }

    private Update.Granularity getGranularity() {
        try {
            String s = StringUtils.substringAfterLast(granularity, "_");
            return Update.Granularity.valueOf(s);
        } catch (IllegalArgumentException e) {
            return Update.Granularity.LEVEL_1;
        }
    }

    public String getGraphName() {
        return graphName;
    }

    public Update getUpdate() throws UpdateException {
        try {
            return UpdateFactory.createUpdate(getGranularity(), getUploadModel());
        } catch (ParameterException e) {
            throw new UpdateException("Update could not be done. " + e.getMessage());
        }
    }
}
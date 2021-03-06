package de.werft;

import de.hpi.rdf.tailrapi.Delta;
import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.Tailr;
import io.swagger.annotations.*;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;

/**
 * Root resource exposed at "/upload".
 * This class handles the file uploads as a restful
 * service. All API calls are documented via swagger.
 *
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@SwaggerDefinition(
        info = @Info(
                description = "Upload and store rdf data",
                version = "0.5",
                title = "DWERFT Upload Service"
        ),
        externalDocs = @ExternalDocs(value = "Dwerft", url = "https://github.com/yovisto/dwerft")
)
@Path("/")
@Api()
public class MyResource {

    private final static Logger L = org.apache.logging.log4j.LogManager.getLogger("UploadService.class");

    @Inject
    private ServiceConfig conf;

    @Inject
    private Tailr tailrClient;

    @Inject
    private Uploader uploader;

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Stores RDF files.",
            notes = "Receives RDF files and adds them to version control and stores them into a triple store.",
            consumes = "application/octet-stream"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Everything is fine."),
            @ApiResponse(code = 204, message = "No content provided."),
            @ApiResponse(code = 206, message = "Provided content is not valid RDF."),
            @ApiResponse(code = 304, message = "Not modified due to an error with tailr or the triple store."),
            @ApiResponse(code = 400, message = "Not enough or the wrong parameters provided.")
    })

    public Response uploadFile(byte[] fileBytes,
                               @ApiParam(value = "The key used in tailr", required = true) @QueryParam(value = "key") String tailrKey,
                               @ApiParam(value = "The triple store graph name", required = true) @QueryParam(value = "graph") String graphName,
                               @ApiParam(value = "The used upload procedure.") @DefaultValue("2") @QueryParam(value = "level") int level,
                               @ApiParam(value = "The rdf language.") @DefaultValue("ttl") @QueryParam(value = "lang") String lang,
                               @ApiParam(value = "The tailr key for merge") @DefaultValue("") @QueryParam(value = "mkey") String mergeKey) {
        /* handle failure cases */
        Lang format = RDFLanguages.nameToLang(lang);
        Granularity g = null;
        try {
            g = Granularity.valueOf("LEVEL_" + level);
        } catch (IllegalArgumentException e) {
            L.error("Illegal granularity level.", e);
        }

        if (fileBytes == null || fileBytes.length == 0) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else if ("".equals(tailrKey) || format == null || g == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (!isRdfFile(new ByteArrayInputStream(fileBytes), format)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

        Delta d;
        if (Granularity.LEVEL_0.equals(g)) {
            d = getDelta("", tailrKey);
        } else if (Granularity.LEVEL_3.equals(g)) {
            /* create ntriples from input */
            if (mergeKey.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            String input = convertToNtTriples(new ByteArrayInputStream(fileBytes), format);
            Memento m = getMemento(mergeKey);
            input = Merge.merge(convertMementoToNtTriples(m), input);
            L.info("Merge input with ");
            d = getDelta(input, tailrKey);

        } else {
            /* create ntriples from input */
            String input = convertToNtTriples(new ByteArrayInputStream(fileBytes), format);
            L.info("Input:\n" + input);
            d = getDelta(input, tailrKey);
        }

        if ("".equals(graphName)) {
            graphName = "http://filmontology.org";
        }

        L.info("Got delta from Tailr:\n" + d);
        if (d == null) {
            return Response.status(Response.Status.NOT_MODIFIED).build();
        }

        /* create authentication */
/*        HttpClientBuilder builder = HttpClientBuilder.create();
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(uploader.getEndpoint(), 80),
                new UsernamePasswordCredentials(conf.getRemoteUser(), conf.getRemotePass()));
        builder.setDefaultCredentialsProvider(provider);
*/
        L.info("Start uploading...");
        uploader.uploadModel(d, graphName);

        L.info("Upload done.");
        return Response.ok(fileBytes, MediaType.APPLICATION_OCTET_STREAM).build();
    }

    /* store the uploaded rdf file and retrieve the delta determined by tailr */
    private Delta getDelta(String input, String tailrKey) {
        try {
            Repository repo = new Repository(conf.getTailrUser(), conf.getTailrRepo());
            return tailrClient.putMemento(repo, tailrKey, input);
        } catch (IOException | URISyntaxException e) {
            L.error("Returning not modified.", e);
        }
        return null;
    }

    /* store the uploaded rdf file and retrieve the delta determined by tailr */
    private Memento getMemento(String tailrKey) {
        try {
            Repository repo = new Repository(conf.getTailrUser(), conf.getTailrRepo());
            return tailrClient.getLatestMemento(repo, tailrKey);
        } catch (IOException e) {
            L.error("Returning not modified.", e);
        }
        return null;
    }


    /* check if an inputstream is valid rdf */
    private boolean isRdfFile(InputStream stream, Lang format) {
        try {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, stream, format);
        } catch (RiotException e) {
            return false;
        }
        return true;
    }

    /* convert some string input to ntriples */
    private String convertToNtTriples(InputStream stream, Lang format) {
        StringWriter writer = new StringWriter();
        try {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, stream, format);
            RDFDataMgr.write(writer, m, Lang.NT);
        } catch (RiotException e) {
            L.error("Failed to convert the input.", e);
        }
        return writer.toString();
    }


    /* retrieve ntriples from tailr memento */
    private static String convertMementoToNtTriples(Memento m) {
        StringWriter writer = new StringWriter();
        try {
            Graph g = m.resolve();
            RDFDataMgr.write(writer, g, Lang.NT);
        } catch (RiotException | UnsupportedEncodingException | URISyntaxException e) {
            L.error("Failed to convert the input to n-triples.", e);
        } catch (IOException e) {
            L.error("Failed to resolve memento.", e);
        }
        return writer.toString();
    }

    private enum Granularity {
        /**
         * The Level 0 deletes data.
         */
        LEVEL_0,
        /**
         * The Level 1 inserts data.
         */
        LEVEL_1,
        /**
         * Level 2 creates a diff.
         */
        LEVEL_2,
        /**
         * Merge old tailr and input
         */
        LEVEL_3
    }
}

package de.werft.tools.general.commands;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.RequireOnlyOne;
import com.github.rvesse.airline.annotations.restrictions.Required;
import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.TailrClient;
import de.werft.tools.general.DwerftTools;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A basic airlift command providing access to the tailr api.
 * The command is invoked with the version argument and retrieves
 * time map, model or delta information from tailr.
 *
 *
 * created by Henrik Jürges (juerges.henrik@gmail.com)
 */
@Command(name = "version", description = "Access the tailr version information for a project key")
public class Version extends DwerftTools {

    @Arguments(description = "Provide a key name from tailr.")
    @Required
    private String key = "";

    @Option(name = {"-l", "--list" }, description = "Shows the time map for a key.")
    @RequireOnlyOne(tag = "version")
    private boolean isList = false;

    @Option(name = {"-s", "--show"}, description = "Shows a specific revision or the latest when latest keyword provided.")
    @RequireOnlyOne(tag = "version")
    private String revision = "";

    @Option(name = {"-d", "--delta"}, description = "Shows the delta for a specific revision or the latest when latest keyword provided.")
    @RequireOnlyOne(tag = "version")
    private String delta = "";

    @Option(name = {"-p", "--private"}, description = "If the repository is private.")
    private boolean isPrivate = false;

    @Override
    public void run() {
        super.run();
        logger.debug("Show version information.");

        try {
            TailrClient t = getClient();
            Repository repo = new Repository(config.getTailrUser(), config.getTailrRepo());
            logger.debug("Created client with repo " + repo);

            if (isList) {
                logger.debug("Show revisions");
                List<Memento> revisions = t.getMementos(repo, key);
                logger.info(t.prettifyTimemap(revisions));
            } else if (isShow()) {
                logger.debug("Show " + revision + " revision");
                Memento m = isLatest() ? t.getLatestMemento(repo, key) : new Memento(repo, key, new DateTime(revision));
                RDFDataMgr.write(System.out, m.resolve(), Lang.TTL);

            } else if (isDelta()) {
                logger.debug("Show delta for revision " + revision);
                if (isLatest()) {
                    logger.info(t.getLatestDelta(repo, key));
                } else {
                    logger.info(t.getDelta(new Memento(repo, key, new DateTime(delta))));
                }
            }

        } catch (URISyntaxException e) {
            logger.error("Could not generate tailr client from uri " + config.getTailrBase());
        } catch (UnsupportedEncodingException e) {
            logger.error("Could not retrieve external rdf model.");
        } catch (IOException e) {
            logger.error("Could not retrieve revision information.");
        }
    }

    /* build a new tailr client */
    private TailrClient getClient() throws URISyntaxException {
        return TailrClient.getInstance(config.getTailrBase(), config.getTailrUser(), config.getTailrToken(), isPrivate);
    }

    /* since we can not distinct between different options, we need a latest keyword */
    private boolean isShow() {
        return !isList && !revision.isEmpty() && delta.isEmpty();
    }

    private boolean isDelta() {
        return !isList && !delta.isEmpty() && revision.isEmpty();
    }

    private boolean isLatest() {
        return "latest".equals(delta) || "latest".equals(revision);
    }
}
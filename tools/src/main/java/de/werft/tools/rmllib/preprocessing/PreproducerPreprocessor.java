package de.werft.tools.rmllib.preprocessing;

import de.werft.tools.rmllib.Document;
import org.apache.commons.codec.binary.Base64;
import org.atteo.xmlcombiner.XmlCombiner;
import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This preprocessor fetches the xml files from the
 * preproducer api and stores them as a combined xml file
 * in the temp folder.
 * <p>
 * Created by Henrik Jürges (juerges.henrik@gmail.com)
 */
public class PreproducerPreprocessor extends BasicPreprocessor {

    private static final String BASE_URL = "https://software.preproducer.com/api";

    private static final List<String> methodOrder = Arrays.asList(
        "info", "listCharacters", "listCrew", "listDecorations", "listExtras", "listFigures",
        "listScenes", "listSchedule");

    private String key;

    private String secret;

    private String appSecret;

    /**
     * Instantiates a new Preproducer preprocessor.
     *
     * @param key       the key by preproducer
     * @param secret    the secret provided by preproducer
     * @param appSecret the app secret provided by preproducer
     */
    public PreproducerPreprocessor(String key, String secret, String appSecret) {
        this.key = key;
        this.secret = secret;
        this.appSecret = appSecret;
    }

    @Override
    protected URL preprocessInput(Document doc) {
        XmlCombiner combiner;
        Path tmpFile;

        try {
            combiner = new XmlCombiner();

            /* fetch all xml files and combine them */
            for (String method : methodOrder) {
                String parameters = getBasicParameters(method);
                String signature = generateSignature(parameters);
                fetchAndCombine(parameters, signature, combiner);
            }

            tmpFile = Files.createTempFile("prepro", ".xml");
            combiner.buildDocument(tmpFile);
            return tmpFile.toUri().toURL();
        } catch (ParserConfigurationException | IOException | TransformerException | SAXException e) {
            //TODO tinylogger
            e.printStackTrace();
        }

        return null;
    }

    /* fetch and clean xml file then combine */
    private void fetchAndCombine(String signature, String parameters, XmlCombiner combiner) throws IOException, SAXException {
        if (signature != null) {
            URL url = new URL(BASE_URL + "?" + parameters + "&signature=" + signature);
            String cleanedContent = removePrefixes(readContent(url));
            combiner.combine(new ByteArrayInputStream(cleanedContent.getBytes()));
        }
    }

    /* read xml file from a url connection */
    private String readContent(URL url) {
        StringBuilder builder = new StringBuilder();
        try {
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            //TODO tinylogger
            e.printStackTrace();
        }

        return builder.toString();
    }

    /**
     * Gets basic commands.
     *
     * @param methodName the method name
     * @return the basic commands
     */
    private String getBasicParameters(String methodName) {
        return "key=" + key + "&method=" + methodName + "&time=" + new Date().getTime();
    }

    /**
     * Generate signature string.
     *
     * @param parameters the commands
     * @return the string
     */
    private String generateSignature(String parameters) {
        String result = "";

        try {
            String hmacSecret = appSecret + secret;

            //HMAC generation
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(hmacSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            //Base64 & URL encoding
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(parameters.getBytes()));
            result = URLEncoder.encode(hash, "UTF-8");

        } catch (Exception e) {
            //TODO tinylog
            e.printStackTrace();
        }
        return result;
    }

    /* remove namespaces from an xml file, this is maybe unnecessary if rml fixed the utf8 problem,
     * code taken from https://stackoverflow.com/a/15996472 */
    private static String removePrefixes(String input1) {
        String ret = null;
        int strStart = 0;
        boolean finished = false;
        if (input1 != null) {
            //BE CAREFUL : allocate enough size for StringBuffer to avoid expansion
            StringBuffer sb = new StringBuffer(input1.length());
            while (!finished) {

                int start = input1.indexOf('<', strStart);
                int end = input1.indexOf('>', strStart);
                if (start != -1 && end != -1) {
                    // Appending anything before '<', including '<'
                    sb.append(input1, strStart, start + 1);

                    String tag = input1.substring(start + 1, end);
                    if (tag.charAt(0) == '/') {
                        // Appending '/' if it is "</"
                        sb.append('/');
                        tag = tag.substring(1);
                    }

                    int colon = tag.indexOf(':');
                    int space = tag.indexOf(' ');
                    if (colon != -1 && (space == -1 || colon < space)) {
                        tag = tag.substring(colon + 1);
                    }
                    // Appending tag with prefix removed, and ">"
                    sb.append(tag).append('>');
                    strStart = end + 1;
                } else {
                    finished = true;
                }
            }
            //BE CAREFUL : use new String(sb) instead of sb.toString for large Strings
            ret = new String(sb);
        }
        return ret;
    }
}
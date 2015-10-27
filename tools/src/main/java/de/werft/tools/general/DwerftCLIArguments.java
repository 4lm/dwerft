package de.werft.tools.general;

import com.beust.jcommander.Parameter;

/**
 * The Class DwerftCLIArguments.
 * Contains all valid command line arguments for use with the main method.
 * Uses the jcommander framework.
 */
public class DwerftCLIArguments {

	/** The input file. */
	@Parameter(names = {"-i", "--input"}, description = "Specify an XML input file", required = true)
	private String inputFile;

	/** The input type. */
	@Parameter(names = {"-t", "--type"}, description = "Specify an input type. Available options are PreProducer ('prp'), DramaQueen ('dq'), and Generic ('g')", required = true)
	private String inputType;
	
	/** The output file. */
	@Parameter(names = {"-o", "--output"}, description = "Specify an RDF output file", required = true)
	private String outputFile;

	/** If the generic converter is used, a custom mapping must be specified. */
	@Parameter (names = {"-m", "--mapping"}, description = "Specifiy a custom mapping file for use with the generic XML to RDF converter")
	private String customMapping;

	@Parameter (names = {"-c", "--prpconfig"}, description = "Querying the PreProducer API requires valid credentials. Please specify a config file with said credentials.")
	private String prpConfigFile;

	/** Flag indicating if RDF should be printed to console */
	@Parameter(names = {"-p", "--print"}, description = "Print output to console")
	private boolean printToCli = false;

	/** The output RDF format. */
	@Parameter(names = {"-f", "--format"}, description = "Specify an RDF output format. Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.")
	private String outputFormat = "ttl";

	/** Prints the help information */
	@Parameter(names = {"-h", "--help"}, help = true)
	private boolean help = false;

	public String getInputFile() {
		return inputFile;
	}

	public String getInputType() {
		return inputType;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getCustomMapping() {
		return customMapping;
	}

	public String getPrpConfigFile() {
		return prpConfigFile;
	}

	public boolean isPrintToCli() {
		return printToCli;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public boolean isHelp() {
		return help;
	}
}
package de.werft.tools.general;

import de.werft.tools.importer.dramaqueen.DramaqueenToRdfTest;
import de.werft.tools.importer.preproducer.PreProducerToRdfTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.werft.tools.exporter.ExporterTest;

/**
 * Dwerft test suite
 *
 * All test classes collected.
 *
 * @author Henrik (juerges.henrik@gmail.com)
 */
@RunWith(Suite.class)
@SuiteClasses({ ExporterTest.class, DramaqueenToRdfTest.class, PreProducerToRdfTest.class})
public class DwerftTestSuite { }
package org.openforis.collect.manager;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SpeciesImportProcessTest {

	@Autowired
	private SpeciesManager speciesManager;
	
	@Test
	public void testImportCSV() throws Exception {
		File file = getTestFile();
		SpeciesImportProcess process = new SpeciesImportProcess(speciesManager, "it_tree", file);
		process.call();
		assertEquals(SpeciesImportProcess.Step.COMPLETE, process.getStep());
	}

	protected File getTestFile() throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource("test-species.csv");
		File file = new File(fileUrl.toURI());
		return file;
	}
}

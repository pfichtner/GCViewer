package com.tagtraum.perf.gcviewer.imp;

import static com.tagtraum.perf.gcviewer.view.UnittestHelper.getResource;
import static com.tagtraum.perf.gcviewer.view.UnittestHelper.Folder.OPENJDK;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

/**
 * Tests the implementation of {@link DataReaderFacade}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>
 *         created on: 28.11.2012
 *         </p>
 */
public class TestDataReaderFacade {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";

	private static final String PARENT_PATH = "src/test/resources/" + OPENJDK.getFolderName() + "/";

	private DataReaderFacade dataReaderFacade = new DataReaderFacade();

	/**
	 * Tests {@link DataReaderFacade#loadModel(GCResource)} with filename that does
	 * exist.
	 */
	@Test
	public void loadModelStringFileExistsNoWarnings() throws Exception {
		TestLogHandler handler = new TestLogHandler();
		handler.setLevel(Level.WARNING);
		GCResource gcResource = new GcResourceFile(PARENT_PATH + SAMPLE_GCLOG_SUN1_6_0);
		gcResource.getLogger().addHandler(handler);

		GCModel model = dataReaderFacade.loadModel(gcResource);

		assertEquals("has no errors", 0, handler.getCount());
		assertNotNull("Model returned", model);
		assertNotNull("Model returned contains URL", model.getURL());
	}

	/**
	 * Tests {@link DataReaderFacade#loadModel(GCResource)} with a malformed url.
	 */
	@Test
	public void loadModelMalformedUrl() throws Exception {
		expectedException.expect(DataReaderException.class);
		expectedException.expectCause(isA(MalformedURLException.class));
		dataReaderFacade.loadModel(new GcResourceFile("httpblabla"));
	}

	/**
	 * Tests {@link DataReaderFacade#loadModel(GCResource)} with a malformed url.
	 */
	@Test
	public void loadModelIllegalArgument() throws Exception {
		expectedException.expect(DataReaderException.class);
		expectedException.expectCause(isA(IllegalArgumentException.class));
		dataReaderFacade.loadModel(new GcResourceFile("http://"));
	}

	/**
	 * Tests {@link DataReaderFacade#loadModel(GCResource)} with filename that does
	 * not exist.
	 */
	@Test
	public void loadModelFileDoesntExists() throws Exception {
		expectedException.expect(DataReaderException.class);
		expectedException.expectCause(isA(FileNotFoundException.class));
		dataReaderFacade.loadModel(new GcResourceFile("dummy.txt"));
	}

	@Test
	public void testLoadModel_forSeries() throws IOException, DataReaderException {
		GcResourceSeries series = new GcResourceSeries(asList( //
				load("SampleSun1_8_0Series-Part4.txt"), load("SampleSun1_8_0Series-Part3.txt"),
				load("SampleSun1_8_0Series-Part6.txt"), load("SampleSun1_8_0Series-Part1.txt"),
				load("SampleSun1_8_0Series-Part7.txt"), load("SampleSun1_8_0Series-Part2.txt"),
				load("SampleSun1_8_0Series-Part5.txt") //
		));

		GCModel result = dataReaderFacade.loadModelFromSeries(series);
		GCModel expectedModel = dataReaderFacade.loadModel(load("SampleSun1_8_0Series-ManuallyMerged.txt"));
		assertThat(result.toString(), is(expectedModel.toString()));
	}

	@Test
	public void testLoadModelFromSeries() throws IOException, DataReaderException {
		GcResourceSeries series = new GcResourceSeries(asList( //
				load("SampleSun1_8_0Series-Part4.txt"), load("SampleSun1_8_0Series-Part3.txt"),
				load("SampleSun1_8_0Series-Part6.txt"), load("SampleSun1_8_0Series-Part1.txt"),
				load("SampleSun1_8_0Series-Part7.txt"), load("SampleSun1_8_0Series-Part2.txt"),
				load("SampleSun1_8_0Series-Part5.txt") //
		));

		GCModel result = dataReaderFacade.loadModel(series);
		GCModel expectedModel = dataReaderFacade.loadModel(load("SampleSun1_8_0Series-ManuallyMerged.txt"));
		assertThat(result.toString(), is(expectedModel.toString()));
	}

	private GcResourceFile load(String name) throws IOException {
		return new GcResourceFile(getResource(OPENJDK, name).getPath());
	}
	
}
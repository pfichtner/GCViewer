package com.tagtraum.perf.gcviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.tagtraum.perf.gcviewer.ctrl.impl.GCViewerGuiController;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import com.tagtraum.perf.gcviewer.view.UnittestHelper;

/**
 * @author martin.geldmacher
 */
public class GCViewerTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void singleArgumentOpensGui() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		int exitValue = gcViewer.doMain("some_gc.log");
		verify(controller).startGui(new GcResourceFile("some_gc.log"));
		assertThat("exitValue of doMain", exitValue, is(0));
	}

	@Test
	public void singleArgumentWithSeriesOpensGui() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		int exitValue = gcViewer.doMain("some_gc.log.0;some_gc.log.1;some_gc.log.2");
		verify(controller).startGui(new GcResourceSeries(Arrays.asList(new GcResourceFile("some_gc.log.0"),
				new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"))));
		assertThat("result of doMain", exitValue, is(0));
	}

	@Test
	public void moreThan3ArgumentsPrintsUsage() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		int exitValue = gcViewer.doMain("argument1", "argument2", "argument3", "argument4");
		verify(controller, never()).startGui(any(GCResource.class));
		assertThat("result of doMain", exitValue, is(-3));
	}

	@Test
	public void export() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		File in = new File(UnittestHelper.getResource("openjdk/SampleSun1_7_0-01_G1_young.txt").toURI());
		int exitValue = gcViewer.doMain(filename(in), filename(temporaryFolder.newFile("export.csv")),
				filename(temporaryFolder.newFile("export.png")), "-t", "PLAIN");
		verify(controller, never()).startGui(any(GCResource.class));
		assertThat("result of doMain", exitValue, is(0));
	}

	@Test
	public void exportFileNotFound() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		int exitValue = gcViewer.doMain("doesNotExist.log", "export.csv", "-t", "PLAIN");
		verify(controller, never()).startGui(any(GCResource.class));
		assertThat("result of doMain", exitValue, is(-1));
	}

	@Test
	public void illegalExportFormat() throws Exception {
		GCViewerGuiController controller = mock(GCViewerGuiController.class);
		GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

		int exitValue = gcViewer.doMain("-t", "INVALID");
		assertThat("result of doMain", exitValue, is(-2));
	}

	private static String filename(File file) {
		return file.getAbsoluteFile().toString();
	}
}

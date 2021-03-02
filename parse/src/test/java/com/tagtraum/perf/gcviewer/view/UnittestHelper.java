package com.tagtraum.perf.gcviewer.view;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.imp.DataReader;
import com.tagtraum.perf.gcviewer.imp.DataReaderFactory;
import com.tagtraum.perf.gcviewer.imp.TestLogHandler;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;

/**
 * Helper class to support the unittests.
 * .
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 22.05.2013</p>
 */
public class UnittestHelper {

	private static final String FOLDER_OPENJDK = "openjdk";

    public enum Folder {
        GO("go"),
        HP("hp"),
        IBM("ibm"),
        JROCKIT("jrockit"),
        OPENJDK(FOLDER_OPENJDK),
        OPENJDK_UJL(OPENJDK.getFolderName() + File.separator + "unified-jvm-logging"),
        HTTP("http");

        private final String folderName;
        
        private Folder(String folderName) {
            this.folderName = folderName;
        }

        public String getFolderName() {
            return folderName;
        }

    }

    /**
     * Load resource as stream if it is present somewhere in the classpath.
     * @param name Name of the resource
     * @return instance of an input stream or <code>null</code> if the resource couldn't be found
     * @throws IOException if resource can't be found
     */
    public static InputStream getResourceAsStream(String name) throws IOException {
        InputStream in = classloader().getResourceAsStream(name);
        if (in == null) {
            throw new IOException("could not find " + name + " in classpath");
        }
        
        return in; 
    }
    
    /**
     * Load a resource as stream from a given <code>folder</code>.
     * 
     * @see #getResourceAsStream(String)
     */
    public static InputStream getResourceAsStream(Folder folder, String name) throws IOException {
        return getResourceAsStream(folder.getFolderName() + File.separator + name);
    }

    /**
     * Get URL name of resource and check if it exists somewhere in the classpath.
     * @param name Name of the resource
     * @return instance of an input stream or <code>null</code> if the resource couldn't be found
     * @throws IOException if resource can't be found
     */
    public static URL getResource(String name) throws IOException {
    	URL url = classloader().getResource(name);
        if (url == null) {
            throw new IOException("could not find " + name + " in classpath");
        }
        
        return url; 
    }

	private static ClassLoader classloader() {
		return UnittestHelper.class.getClassLoader();
	}
    
    /**
     * Get URL of a resource from a given <code>folder</code>.
     * 
     * @see #getResource(String)
     */
    public static URL getResource(Folder folder, String name) throws IOException {
        return getResource(folder.getFolderName() + "/" + name);
    }
    
    /**
     * Get string name of resource.
     * 
     * @param folder folder name to look in
     * @param name resource name
     * @return string representation of resource name present in classpath (absolut path)
     * @throws IOException if resource could not be found
     * @see {@link #getResource(String)}
     */
    public static String getResourceAsString(Folder folder, String name) throws IOException {
        return getResource(folder, name).getFile();
    }

    /**
     * Converter from bytes to kilobytes.
     *
     * @param bytes value in bytes
     * @return value in kilobytes
     */
    public static int toKiloBytes(long bytes) {
        return (int)Math.rint(bytes / (double)1024);
    }

    /**
     * Return GCModel from given input <code>fileName</code>.
     *
     * @param fileName file to be parsed
     * @param folderName folder, where <code>fileName</code> can be found
     * @param expectedDataReaderClass Expected DataReaderFactory class for the given fileName parameter
     * @return GCModel model containing contents of parsed file
     * @throws IOException thrown, if something went wrong reading the file
     */
    public static GCModel getGCModelFromLogFile(String fileName, Folder folderName, Class<?> expectedDataReaderClass) throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile(fileName);
        gcResource.getLogger().addHandler(handler);

        try (InputStream in = getResourceAsStream(folderName, gcResource.getResourceName())) {
            DataReader reader = new DataReaderFactory().getDataReader(gcResource, in);
            assertThat("reader from factory", reader.getClass().getName(), is(expectedDataReaderClass.getName()));

            GCModel model = reader.read();
            assertThat("number of errors", handler.getCount(), is(0));
            return model;
        }
    }

    /**
     * Tests a given <code>event</code> for several of its attribute values.
     * @param event event under test
     * @param testName expressive name for the current test
     * @param expectedType expected type
     * @param expectedPause expected pause duration
     * @param expectedHeapBefore expected heap size before
     * @param expectedHeapAfter expected heap size after
     * @param expectedHeapTotal expected total heap size
     * @param expectedValueForIsFull expected value for "is full gc event"
     */
    public static void testMemoryPauseEvent(AbstractGCEvent<?> event,
                                      String testName,
                                      Type expectedType,
                                      double expectedPause,
                                      int expectedHeapBefore,
                                      int expectedHeapAfter,
                                      int expectedHeapTotal,
                                      Generation expectedGeneration,
                                      boolean expectedValueForIsFull) {

        assertThat(testName + " type", event.getTypeAsString(), startsWith(expectedType.getName()));
        assertThat(testName + " pause", event.getPause(), closeTo(expectedPause, 0.00001));
        assertThat(testName + " heap before", event.getPreUsed(), is(expectedHeapBefore));
        assertThat(testName + " heap after", event.getPostUsed(), is(expectedHeapAfter));
        assertThat(testName + " total heap", event.getTotal(), is(expectedHeapTotal));
        assertThat(testName + " generation", event.getGeneration(), is(expectedGeneration));
        assertThat(testName + " isFull", event.isFull(), is(expectedValueForIsFull));
    }

}
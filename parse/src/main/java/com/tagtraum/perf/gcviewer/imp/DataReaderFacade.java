package com.tagtraum.perf.gcviewer.imp;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.HttpUrlConnectionHelper;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * DataReaderFacade is a helper class providing a simple interface to read a gc log file
 * including standard error handling.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class DataReaderFacade {

    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

    /**
     * Add propertyChangeListener for underlying MonitoredBufferedInputStreams property "progress".
     *
     * @param listener component requiring to listen to progress changes
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    /**
     * Loads a model from a given <code>gcResource</code> logging all exceptions that occur.
     *
     * @param gcResource where to find data to be parsed
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     */
    public GCModel loadModel(GCResource gcResource) throws DataReaderException {
        if (gcResource == null) {
            throw new NullPointerException("gcResource must never be null");
        }
        if (gcResource instanceof  GcResourceSeries) {
            return loadModelFromSeries((GcResourceSeries) gcResource);
        }
        if (!(gcResource instanceof GcResourceFile))
            throw new UnsupportedOperationException("Only supported for files!");

        DataReaderException dataReaderException = new DataReaderException();
        GCModel model = null;
        Logger logger = gcResource.getLogger();

        try {
            logger.info("GCViewer version " + BuildInfoReader.getVersion()
                    + " (" + BuildInfoReader.getBuildDate() + ")");
            model = readModel((GcResourceFile) gcResource);
        }
        catch (RuntimeException | IOException e) {
            dataReaderException.initCause(e);
            logger.warning(LocalisationHelper.getString("fileopen_dialog_read_file_failed")
                    + "\n" + e.toString() + " " + e.getLocalizedMessage());
        }

        if (dataReaderException.getCause() != null) {
            throw dataReaderException;
        }

        return model;
    }

    /**
     * Loads the {@link GCResource}s as a rotated series of logfiles. Takes care of ordering them
     *
     * @param gcResource the {@link GcResourceSeries} to load
     * @return a {@link GCModel} containing all events found in the given {@link GCResource}s that were readable
     * @throws DataReaderException thrown in case of some parser failure
     */
    protected GCModel loadModelFromSeries(GcResourceSeries gcResource) throws DataReaderException {
        GcSeriesLoader seriesLoader = new GcSeriesLoader(this);
        return seriesLoader.load(gcResource);
    }

    /**
     * Open and parse data designated by <code>gcResource</code>.
     *
     * @param gcResource where to find data to be parsed
     * @return GCModel containing events parsed from <code>gcResource</code>
     * @throws IOException problem reading the data
     */
    private GCModel readModel(GcResourceFile gcResource) throws IOException {
        URL url = gcResource.getResourceNameAsUrl();
        DataReaderFactory factory = new DataReaderFactory();
        long contentLength = 0L;
        InputStream in = null;
        try {
            if (url.getProtocol().startsWith("http")) {
                AtomicLong atomicContentLength = new AtomicLong();
                URLConnection conn = url.openConnection();
                in = HttpUrlConnectionHelper.openInputStream((HttpURLConnection) conn,
                        HttpUrlConnectionHelper.GZIP,
                        atomicContentLength);
                contentLength = atomicContentLength.get();
            }
            else {
                in = url.openStream();
                if (url.getProtocol().startsWith("file")) {
                    File file = new File(url.getFile());
                    if (file.exists()) {
                        contentLength = file.length();
                    }
                }
            }
            if (contentLength > 100L) {
                in = new MonitoredBufferedInputStream(in, DataReaderFactory.FOUR_KB, contentLength);
                for (PropertyChangeListener listener : propertyChangeListeners) {
                    ((MonitoredBufferedInputStream) in).addPropertyChangeListener(listener);
                }
            }

            DataReader reader = factory.getDataReader(gcResource, in);
            GCModel model = reader.read();
            model.setURL(url);

            return model;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    gcResource.getLogger().warning("A problem occurred trying to close the InputStream: " + e.toString());
                }
            }
        }
    }
    
    /**
     * Allows to load an (unordered) list of {@link GCResource} and treats them as a consecutive series of {@link GCResource}s.
     *
     * @author martin.geldmacher
     */
    public static class GcSeriesLoader {

        private static final Logger logger = Logger.getLogger(GcSeriesLoader.class.getName());
        private final DataReaderFacade dataReaderFacade;

        public GcSeriesLoader(DataReaderFacade dataReaderFacade) {
            this.dataReaderFacade = dataReaderFacade;
        }

        public GCModel load(GcResourceSeries series) throws DataReaderException {
            if (series == null || series.getResourcesInOrder().size() == 0) {
                throw new IllegalArgumentException("No resources to load!");
            }
            return mergeModels(sortResources(determineStartTimePerGcModel(series)));
        }

        private Map<Timestamp, GCModel> determineStartTimePerGcModel(GcResourceSeries series) throws DataReaderException {
            Map<Timestamp, GCModel> startTimeToGcModel = new HashMap<>();
            for (GCResource resource : series.getResourcesInOrder()) {
                Optional<GCModel> model = loadGcModel(resource);
                if (model.isPresent()) {
                    startTimeToGcModel.put(getCreationDate(model.get()), model.get());
                } else {
                    logger.log(Level.WARNING, "Failed to load " + resource + " - ignoring it");
                }
            }
            return startTimeToGcModel;
        }

    	public Timestamp getCreationDate(GCModel model) throws DataReaderException {
    		Optional<Timestamp> firstDateStamp = getFirstDateStampFromModel(model);
    		if (firstDateStamp.isPresent()) {
    			return firstDateStamp.get();
    		}
    		Optional<Timestamp> firstTimeStamp = getFirstTimeStampFromModel(model);
    		if (firstTimeStamp.isPresent()) {
    			return firstTimeStamp.get();
    		}
    		logger.log(Level.WARNING, "Logfile contains neither date- nor timestamp. Using file creation date as"
    				+ " fallback. Consider using -XX:+PrintGCDateStamps to enable logging of dates for GC events.");
    		return getCreationDateFromFile(model);
    	}

        public Optional<Timestamp> getFirstDateStampFromModel(GCModel model) {
            return Optional.ofNullable(model.getFirstDateStamp()).map(GcDateStamp::new);
        }
        protected Optional<Timestamp> getFirstTimeStampFromModel(GCModel model) {
            return model.getFirstTimeStamp().map(GcTimeStamp::new);
        }

        public Timestamp getCreationDateFromFile(GCModel model) {
            return new GcDateStamp(Instant.ofEpochMilli(model.getCreationTime()).atZone(ZoneId.systemDefault()));
        }

        public List<GCModel> sortResources(Map<Timestamp, GCModel> startTimeToGcModel) throws DataReaderException {
            try {
                return startTimeToGcModel.entrySet()
                        .stream()
                        .sorted(comparing(Map.Entry::getKey))
                        .map(Map.Entry::getValue)
                        .collect(toList());
            }
            catch (Exception ex) {
                throw new DataReaderException("Logfile series has mixed date- and timestamps. Can't determine logfile order", ex);
            }
        }

        private Optional<GCModel> loadGcModel(GCResource resource) {
            try {
                return Optional.of(dataReaderFacade.loadModel(resource));
            } catch (DataReaderException ex) {
                logger.log(Level.WARNING, "Failed to read " + resource + ". Reason: " + ex.getMessage());
                logger.log(Level.FINER, "Details: ", ex);
                return Optional.empty();
            }
        }

        private GCModel mergeModels(List<GCModel> models) {
            GCModel mergedModel = models.get(0);
            for (int i = 1; i < models.size(); i++) {
                models.get(i).getEvents().forEach(mergedModel::add);
            }

            // Use URL of last contained file. In case of a refresh this is the only file that can have changed
            mergedModel.setURL(models.get(models.size() - 1).getURL());
            return mergedModel;
        }

        public interface Timestamp extends Comparable<Timestamp> {
        }

        /**
         * Datestamp of a GC log.
         * Created when -XX:+PrintGCDateStamps is used
         */
        public static class GcDateStamp implements Timestamp {

            private ZonedDateTime time;
            public GcDateStamp(ZonedDateTime time) {
                this.time = time;
            }

            @Override
            public int compareTo(Timestamp o) {
                if(o instanceof GcDateStamp)
                    return this.time.compareTo(((GcDateStamp)o).time);
                throw new IllegalArgumentException("Can't compare Datestamp with Timestamp: " +o);
            }

            @Override
            public String toString() {
                return "GcDateStamp{" +
                        "time=" + time +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                GcDateStamp that = (GcDateStamp) o;

                return time != null ? time.equals(that.time) : that.time == null;

            }

            @Override
            public int hashCode() {
                return time != null ? time.hashCode() : 0;
            }
        }

        /**
         * Timestamp of a GC log. Relative to application start
         * Created when -XX:+PrintGCTimeStamps is used
         */
        public static class GcTimeStamp implements Timestamp {

            private double time;
            public GcTimeStamp(double time) {
                this.time = time;
            }

			@Override
			public int compareTo(Timestamp o) {
				if (!(o instanceof GcTimeStamp)) {
					throw new IllegalArgumentException("Can't compare Timestamp with Datestamp: " + o);
				}
				return Double.compare(time, ((GcTimeStamp) o).time);
			}

            @Override
			public String toString() {
				return "GcTimeStamp{" + "time=" + time + '}';
			}

            @Override
            public boolean equals(Object o) {
                if (this == o) {
					return true;
				}
                if (o == null || getClass() != o.getClass()) {
					return false;
				}

                GcTimeStamp that = (GcTimeStamp) o;
                return Double.compare(that.time, time) == 0;
            }

            @Override
            public int hashCode() {
                long temp = Double.doubleToLongBits(time);
                return (int) (temp ^ (temp >>> 32));
            }
        }
    }

}

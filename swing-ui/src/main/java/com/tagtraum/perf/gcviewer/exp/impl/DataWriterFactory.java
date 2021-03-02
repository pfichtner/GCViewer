package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Factory for all available {@link DataWriter} implementations.
 *
 * <p>Date: Feb 1, 2002
 * <p>Time: 10:34:39 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataWriterFactory {

    public static final String GC_PREFERENCES = "gcPreferences";

    /**
     * Factory method to retrieve one of the <code>DataWriter</code> implementations including
     * the option to add a map of configuration objects. The map will be passed to the DataWriter,
     * which can use its contents.
     * @param type type of DataWriter
     *
     * @return instance of DataWriter according to <code>type</code> parameter
     * @throws IOException unknown DataWriter or problem creating file
     */
    public static DataWriter getDataWriter(DataWriterType type) throws IOException {
        switch (type) {
            case PLAIN   : return new PlainDataWriter();
            case CSV     : return new CSVDataWriter();
            case CSV_TS  : return new CSVTSDataWriter();
            case SIMPLE  : return new SimpleGcWriter();
            case SUMMARY : return new SummaryDataWriter();
            case PNG     : return new PNGDataWriter();
            default : throw new IOException(LocalisationHelper.getString("datawriterfactory_instantiation_failed") + " " + type);
        }
    }

}

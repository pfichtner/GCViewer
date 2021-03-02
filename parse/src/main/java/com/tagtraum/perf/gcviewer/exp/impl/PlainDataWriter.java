package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Writes the model using the toString()-methode of {@link GCEvent}.
 *
 * Date: Feb 1, 2002
 * Time: 9:58:11 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class PlainDataWriter implements DataWriter {

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model, OutputStream outputstream, Map<String, Object> configuration) throws IOException {
    	PrintWriter out = new PrintWriter(outputstream);
        Iterator<AbstractGCEvent<?>> i = model.getEvents();
        while (i.hasNext()) {
            out.println(i.next().toString());
        }
        out.flush();
    }

}

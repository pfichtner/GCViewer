package com.tagtraum.perf.gcviewer.exp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Writes a GCModel into a given Stream.
 *
 * Date: Feb 1, 2002
 * Time: 9:56:19 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface DataWriter {
	
    void write(GCModel model, OutputStream outputstream, Map<String, Object> configuration) throws IOException;

}

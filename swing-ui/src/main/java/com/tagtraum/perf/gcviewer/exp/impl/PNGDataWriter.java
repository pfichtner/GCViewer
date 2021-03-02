package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.SimplePngChartRenderer;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * PNG data writter
 *
 * @author Angel Olle Blazquez
 *
 */
public class PNGDataWriter implements DataWriter {

	@Override
	public void write(GCModel model, OutputStream outputstream, Map<String, Object> configuration) throws IOException {
		new SimplePngChartRenderer().render(model, outputstream, gcPreferences(configuration));
	}

	private static GCPreferences gcPreferences(Map<String, Object> configuration) {
		GCPreferences gcPreferences = (GCPreferences) configuration.get(DataWriterFactory.GC_PREFERENCES);
		return gcPreferences == null ? new GCPreferences() : gcPreferences;
	}

}

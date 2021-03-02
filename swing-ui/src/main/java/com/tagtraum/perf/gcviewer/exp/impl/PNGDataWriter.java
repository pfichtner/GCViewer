package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.SimpleChartRenderer;
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
		SimpleChartRenderer simpleChartRenderer = new SimpleChartRenderer();

		GCPreferences gcPreferences = (GCPreferences) configuration.get(DataWriterFactory.GC_PREFERENCES);
		if (gcPreferences == null) {
			simpleChartRenderer.render(model, outputstream);
		} else {
			simpleChartRenderer.render(model, outputstream, gcPreferences);
		}
	}

}

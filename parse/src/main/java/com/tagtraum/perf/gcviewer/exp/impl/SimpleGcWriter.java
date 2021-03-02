package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Exports stop-the-world events in the "simple gc log" format (compatible to
 * GCHisto).
 * <p>
 * This writer writes every event on its own line with the following format
 * <p>
 * {@code GC_TYPE START_SEC DURATION_SEC}
 *
 * @see <a href=
 *      "http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html">http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html</a>
 * @see <a href="http://java.net/projects/gchisto">GCHisto</a>
 * @see <a href=
 *      "https://svn.java.net/svn/gchisto~svn/trunk/www/index.html">GCHisto
 *      documentation</a>
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class SimpleGcWriter implements DataWriter {

	private static final Locale NO_LOCALE = null;

	@Override
	public void write(GCModel model, OutputStream outputstream, Map<String, Object> configuration) throws IOException {
		PrintWriter out = new PrintWriter(outputstream);
		model.getEvents().stream().filter(a -> a.isStopTheWorld())
				.forEach(e -> out.printf(NO_LOCALE, "%s %f %f%n", getSimpleType(e), e.getTimestamp(), e.getPause()));
		out.flush();
	}

	/**
	 * Simple GC Logs GC_TYPE must not contain spaces. This method makes sure they
	 * don't.
	 *
	 * @param typeName name of the gc event type
	 * @return name without spaces
	 */
	private String getSimpleType(AbstractGCEvent<?> event) {
		if (isYoungOnly(event)) {
			return "YoungGC";
		} else if (event.isInitialMark()) {
			return "InitialMarkGC";
		} else if (event.isRemark()) {
			return "RemarkGC";
		} else if (event.isFull()) {
			return "FullGC";
		} else {
			return stripBlanks(event.getTypeAsString());
		}
	}

	/**
	 * Does the event consist of young generation events only (main and detail
	 * events).
	 *
	 * @param event event to be analyzed.
	 * @return <code>true</code> if the event is only in the young generation,
	 *         <code>false</code> otherwise
	 */
	private boolean isYoungOnly(AbstractGCEvent<?> event) {
		if (!event.hasDetails() && event.getExtendedType().getGeneration().equals(Generation.YOUNG)) {
		} else if (event.getExtendedType().getGeneration().equals(Generation.YOUNG)) {
			@SuppressWarnings("unchecked")
			List<AbstractGCEvent<?>> list =  (List<AbstractGCEvent<?>>) event.details();
			return !list.stream().anyMatch(e -> !e.getExtendedType().getGeneration().equals(Generation.YOUNG));
		}
		return true;
	}

	private String stripBlanks(String eventTypeName) {
		StringBuilder sb = new StringBuilder(eventTypeName);
		for (int i = sb.length() - 1; i >= 0; --i) {
			if (sb.charAt(i) == ' ') {
				sb.deleteCharAt(i);
			}
		}

		return sb.toString();
	}

}

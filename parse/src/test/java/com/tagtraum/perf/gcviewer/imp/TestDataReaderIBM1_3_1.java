package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.view.UnittestHelper;
import com.tagtraum.perf.gcviewer.view.UnittestHelper.Folder;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderIBM1_3_1 {

    @Test
    public void testParse1() throws Exception {
        String fileName = "SampleIBM1_3_1.txt";
        InputStream in = UnittestHelper.getResourceAsStream(Folder.IBM, fileName);
        DataReader reader = new DataReaderIBM1_3_1(new GcResourceFile(fileName), in);
        GCModel model = reader.read();
        
        assertEquals("number of events", 21, model.size());
    }
}

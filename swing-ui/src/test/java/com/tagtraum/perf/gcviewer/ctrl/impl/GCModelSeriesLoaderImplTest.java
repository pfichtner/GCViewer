package com.tagtraum.perf.gcviewer.ctrl.impl;

import static com.tagtraum.perf.gcviewer.view.UnittestHelper.getResource;
import static com.tagtraum.perf.gcviewer.view.UnittestHelper.Folder.OPENJDK;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

public class GCModelSeriesLoaderImplTest {
	
    @Test
    public void getGcResource() throws Exception {
        List<GCResource> gcResourceList = new ArrayList<>();
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));

        GCModelLoader loader = new GCModelSeriesLoaderImpl(new GcResourceSeries(gcResourceList));
        assertThat(loader.getGcResource(), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void GCModelSeriesLoaderImpl_ForEmptySeries() throws Exception {
        new GCModelSeriesLoaderImpl(new GcResourceSeries(new ArrayList<>()));
    }

    @Test
    public void loadGcModel() throws Exception {
        List<GCResource> gcResourceList = new ArrayList<>();
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        gcResourceList.add(new GcResourceFile(getResource(OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));
        GCModelSeriesLoaderImpl loader = new GCModelSeriesLoaderImpl(new GcResourceSeries(gcResourceList));

        assertThat(loader.loadGcModel(), notNullValue());
    }
}

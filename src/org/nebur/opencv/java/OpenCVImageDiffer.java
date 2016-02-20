package org.nebur.opencv.java;

import java.io.File;
import java.util.Map;

abstract class OpenCVImageDiffer {

	protected abstract void setUp(File image1, File image2, Map<String, String> options);
	
	protected abstract void tearDown();
	
	public abstract ImageDiffResult diff();
}

package org.nebur.opencv.java;

import org.opencv.core.Core;

public final class OpenCVImageDifferFactory {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	private OpenCVImageDifferFactory() {
	}

	public static OpenCVImageDiffer createDiffer(String impl) {
		
		switch (impl) {
		case "matchResult":
			return new OpenCVMatchResultImageDiff();
		case "compareHist":
			return new OpenCVCompareHistImageDiff();
		}
		
		return null;
	}

	public static OpenCVImageDiffer createDefaultDiffer() {
		return new OpenCVMatchResultImageDiff();
	}
}

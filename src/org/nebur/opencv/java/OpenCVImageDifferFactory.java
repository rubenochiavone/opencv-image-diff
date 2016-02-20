package org.nebur.opencv.java;

import org.opencv.core.Core;

public final class OpenCVImageDifferFactory {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	private OpenCVImageDifferFactory() {
	}

	public static OpenCVImageDiffer createDiffer(String impl) {
		// TODO Auto-generated method stub
		
		switch (impl) {
		case "matchResult":
			return new OpenCVMatchResultImageDiff();
		}
		
		return null;
	}

	public static OpenCVImageDiffer createDefaultDiffer() {
		return new OpenCVMatchResultImageDiff();
	}
}

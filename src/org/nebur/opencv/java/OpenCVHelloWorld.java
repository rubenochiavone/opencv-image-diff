package org.nebur.opencv.java;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class OpenCVHelloWorld {

	public static void main(String[] args) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
			
			System.out.println("mat = " + mat.dump());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

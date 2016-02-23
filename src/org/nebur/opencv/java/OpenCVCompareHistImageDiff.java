package org.nebur.opencv.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

public final class OpenCVCompareHistImageDiff extends OpenCVImageDiffer {

	private static final double DEFAULT_THRESHOLD = 0.9999;
	private static final double DEFAULT_EPSILON = 0.0019;
	private static final int DEFAULT_METHOD = Imgproc.CV_COMP_CORREL;

	private File base;
	private File test;
	private int method;
	private boolean verbose;
	private double threshold;
	private double epsilon;

	OpenCVCompareHistImageDiff() {
		// default values
		this.verbose = false;
		this.method = DEFAULT_METHOD;
		this.threshold = DEFAULT_THRESHOLD;
		this.epsilon = DEFAULT_EPSILON;
	}

	@Override
	protected void setUp(File base, File test, Map<String, String> options) {
		this.base = base;
		this.test = test;

		for (Map.Entry<String, String> entry : options.entrySet()) {

			String key = entry.getKey();
			String value = entry.getValue();

			switch (key) {
			case "verbose":
				this.verbose = Boolean.parseBoolean(value);
				break;
			case "method":
				switch (value) {
				case "CV_COMP_CORREL":
					this.method = Imgproc.CV_COMP_CORREL;
					break;
				case "CV_COMP_CHISQR":
					this.method = Imgproc.CV_COMP_CHISQR;
					break;
				case "CV_COMP_INTERSECT":
					this.method = Imgproc.CV_COMP_INTERSECT;
					break;
				case "CV_COMP_BHATTACHARYYA":
					this.method = Imgproc.CV_COMP_BHATTACHARYYA;
					break;
				}
				break;
			case "threshold":
				this.threshold = Double.parseDouble(value);
				break;
			case "epsilon":
				this.epsilon = Double.parseDouble(value);
				break;
			}
		}
	}

	@Override
	protected void tearDown() {
	}

	@Override
	public ImageDiffResult diff() {

		ImageDiffResult diffResult = new ImageDiffResult();

		try {

			Mat baseMat = OpenCVUtil.image2Mat(base); // , CvType.CV_32FC1);
			Mat testMat = OpenCVUtil.image2Mat(test); // , CvType.CV_32FC1);

			Mat hsvBaseMat = new Mat();
			Mat hsvTestMat = new Mat();

			Imgproc.cvtColor(baseMat, hsvBaseMat, Imgproc.COLOR_BGR2HSV);
			Imgproc.cvtColor(testMat, hsvTestMat, Imgproc.COLOR_BGR2HSV);

			// / Using 50 bins for hue and 60 for saturation
			int hBins = 50;
			int sBins = 60;
			MatOfInt histSize = new MatOfInt(hBins, sBins);

			// hue varies from 0 to 179, saturation from 0 to 255
			MatOfFloat ranges = new MatOfFloat(0f, 180f, 0f, 256f);

			// we compute the histogram from the 0-th and 1-st channels
			MatOfInt channels = new MatOfInt(0, 1);

			Mat histBase = new Mat();
			Mat histTest = new Mat();

			ArrayList<Mat> histImages = new ArrayList<Mat>();
			histImages.add(hsvBaseMat);
			Imgproc.calcHist(histImages, channels, new Mat(), histBase,
					histSize, ranges, false);
			Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX, -1,
					new Mat());

			histImages = new ArrayList<Mat>();
			histImages.add(hsvTestMat);
			Imgproc.calcHist(histImages, channels, new Mat(), histTest,
					histSize, ranges, false);
			Core.normalize(histTest, histTest, 0, 1, Core.NORM_MINMAX, -1,
					new Mat());

			// compare histograms
			double res = Imgproc.compareHist(histBase,
					histTest,
					method);

			if (verbose) {
				System.out.println(res);
				System.out.println(Math.abs(threshold - res));
			}

			if (Math.abs(threshold - res) > epsilon) {

				diffResult.setDiffResult(null);
				diffResult.setEquals(false);
				diffResult.setErrorMessage("Images are not equals.");
				diffResult.setException(null);

				return diffResult;
			}

			// success
			diffResult.setDiffResult(null);
			diffResult.setEquals(true);
			diffResult.setErrorMessage(null);
			diffResult.setException(null);
		} catch (Exception e) {
			diffResult.setDiffResult(null);
			diffResult.setEquals(false);
			diffResult.setErrorMessage(e.getMessage());
			diffResult.setException(e);
		}

		return diffResult;
	}
}

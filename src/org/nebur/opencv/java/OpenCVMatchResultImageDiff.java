package org.nebur.opencv.java;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import javax.imageio.ImageIO;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public final class OpenCVMatchResultImageDiff extends OpenCVImageDiffer {

	private static final double DEFAULT_THRESHOLD = 0.9999;
	private static final double DEFAULT_EPSILON = 0.0019;
	private static final int DEFAULT_METHOD = Imgproc.TM_CCORR_NORMED;
	
	private File template;
	private File image;
	private int method;
	private boolean verbose;
	private double threshold;
	private double epsilon;
	
	OpenCVMatchResultImageDiff() {
		// default values
		this.verbose = false;
		this.method = DEFAULT_METHOD;
		this.threshold = DEFAULT_THRESHOLD;
		this.epsilon = DEFAULT_EPSILON;
	}
	
	@Override
	protected void setUp(File template, File image, Map<String, String> options) {
		this.template = template;
		this.image = image;
		
		for (Map.Entry<String, String> entry : options.entrySet()) {
			
			String key = entry.getKey();
			String value = entry.getValue();
			
			switch (key) {
			case "verbose":
				this.verbose = Boolean.parseBoolean(value);
				break;
			case "method":
				switch (value) {
				case "CV_TM_SQDIFF":
					this.method = Imgproc.TM_SQDIFF;
					break;
				case "CV_TM_SQDIFF_NORMED":
					this.method = Imgproc.TM_SQDIFF_NORMED;
					break;
				case "CV_TM_CCORR":
					this.method = Imgproc.TM_CCORR;
					break;
				case "CV_TM_CCORR_NORMED":
					this.method = Imgproc.TM_CCORR_NORMED;
					break;
				case "CV_TM_CCOEFF":
					this.method = Imgproc.TM_CCOEFF;
					break;
				case "CV_TM_CCOEFF_NORMED":
					this.method = Imgproc.TM_CCOEFF_NORMED;
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
			Mat img = OpenCVUtil.image2Mat(image);
			Mat templ = OpenCVUtil.image2Mat(template);
	
			if (img.cols() != templ.cols() || img.rows() != templ.rows()) {
				diffResult.setDiffResult(null);
				diffResult.setEquals(false);
				diffResult.setErrorMessage("Error: both images need to have same size.");
				diffResult.setException(null);
				
				return diffResult;
			}
	
			if (verbose) {
				System.out
						.println("Images size are " + img.cols() + " x " + img.rows());
			}
	
			// result matrix
			// crops most times have the same size
			Mat result = new Mat(1, 1, CvType.CV_32FC1);
	
			for (int i = 0; i < img.width(); i += 10) {
				for (int j = 0; j < img.height(); j += 10) {
					int width = 20;
					int height = 20;
	
					if ((i + 20) > img.width()) {
						width = img.width() - i;
					}
	
					if ((j + 20) > img.height()) {
						height = img.height() - j;
					}
	
					if (verbose) {
						System.out.println("rect is from (" + i + "," + j + ")"
								+ "to (" + (i + width) + "," + (j + height) + ")");
					}
	
					try {
						Rect rect = new Rect(i, j, width, height);
						
						Mat imgRect = img.submat(rect);
						Mat templRect = templ.submat(rect);
	
						Mat matchTemplateResult = null;
	
						if (imgRect.cols() != templRect.cols()
								|| imgRect.rows() != templRect.rows()) {
							// create the result matrix
							int result_cols = imgRect.cols() - templRect.cols() + 1;
							int result_rows = imgRect.rows() - templRect.rows() + 1;
							
							matchTemplateResult = new Mat(result_rows,
									result_cols, CvType.CV_32FC1);
						} else {
							matchTemplateResult = result; // use cached
						}
	
						// match template
						Imgproc.matchTemplate(imgRect, templRect,
								matchTemplateResult,
								method);
	
						// normally matchTemplateResult.rows(),
						// matchTemplateResult.rows()
						// and resultData.length are equals to 1
						for (int ii = 0; ii < matchTemplateResult.rows(); ii += 1) {
							for (int jj = 0; jj < matchTemplateResult.cols(); jj += 1) {
								double[] resultData = matchTemplateResult.get(ii, jj);
	
								for (int k = 0; k < resultData.length; k++) {
									double res = resultData[k];
	
									if (Math.abs(threshold - res) > epsilon) {
										// System.out.println(Math.abs(threshold - res));
										
										String errorMessage = "Images contains differences between ("
														+ i + "," + j + ")"
														+ " and ("
														+ (i + width) + ","
														+ (j + height) + ")";
										
										if (verbose) {
											System.out.println(errorMessage);
										}
										
										Mat imgError = img.clone();
										
										Imgproc.rectangle(imgError,
												new Point(i, j),
												new Point(i + width, j + height),
												new Scalar(0, 0, 255, 255),
												1);
										
										File errorFile = createErrorFile(imgError);
										
										diffResult.setDiffResult(errorFile);
										diffResult.setEquals(false);
										diffResult.setErrorMessage(errorMessage);
										diffResult.setException(null);
										
										return diffResult;
									}
								}
							}
						}
					} catch (CvException ignore) {
						// ignore.printStackTrace();
					}
				}
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

	private File createErrorFile(Mat imgError) throws IOException {
		
		int type = BufferedImage.TYPE_BYTE_GRAY;
		
		if (imgError.channels() > 1) {
		    Mat m = new Mat();
		    Imgproc.cvtColor(imgError, m, Imgproc.COLOR_BGR2RGB);
		    type = BufferedImage.TYPE_3BYTE_BGR;
		    imgError = m;
		}
		
		byte [] b = new byte[imgError.channels() * imgError.cols() * imgError.rows()];
		
		// get all the pixels
		imgError.get(0, 0, b);
		
		final BufferedImage image = new BufferedImage(imgError.cols(), imgError.rows(), type);
		image.getRaster().setDataElements(0, 0, imgError.cols(), imgError.rows(), b);
		
		File file = Files.createTempFile(
				new File(System.getProperty("java.io.tmpdir")).toPath(), // temp dir
				"opencv-image-diff-error",                               // prefix
				"bmp"                                                    // suffix
				).toFile();
		
		ImageIO.write(image, "bmp", file);
		
		return file;
	}
}

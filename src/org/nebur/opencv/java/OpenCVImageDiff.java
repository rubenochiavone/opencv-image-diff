package org.nebur.opencv.java;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class OpenCVImageDiff {

	public static void main(String[] args) {

		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			String imageFile = OpenCVImageDiff.class.getResource(
					"/res/visu_1.jpg").getPath();
			String templateFile = OpenCVImageDiff.class.getResource(
					"/res/visu.jpg").getPath();
			double threshold = 0.9999;

			Mat img = image2Mat(imageFile);
			Mat templ = image2Mat(templateFile);

			if (img.cols() != templ.cols() || img.rows() != templ.rows()) {
				System.err
						.println("Error: both images need to have same size.");

				return;
			}

			System.out
					.println("Images size are " + img.cols() + " x " + img.rows());

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

					// System.out.println("rect is from (" + i + "," + j + ")"
					// + "to (" + (i + width) + "," + (j + height) + ")");

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
								Imgproc.TM_CCORR_NORMED);

						// normally matchTemplateResult.rows(),
						// matchTemplateResult.rows()
						// and resultData.length are equals to 1
						for (int ii = 0; ii < matchTemplateResult.rows(); ii += 1) {
							for (int jj = 0; jj < matchTemplateResult.cols(); jj += 1) {
								double[] resultData = matchTemplateResult.get(ii, jj);

								for (int k = 0; k < resultData.length; k++) {
									double res = resultData[k];

									if (Math.abs(threshold - res) > 0.01) {
										// System.out.println(Math.abs(threshold - res));
										
										System.err.println("Images contains differences between ("
														+ i + "," + j + ")"
														+ " and ("
														+ (i + width) + ","
														+ (j + height) + ")");
										
										// TODO: warn error in an output file
										
										Mat imgError = img.clone();
										
										Imgproc.rectangle(imgError,
												new Point(i, j),
												new Point(i + width, j + height),
												new Scalar(0, 0, 255, 255),
												1);
										
										showError(imgError);
										
										return;
									}
								}
							}
						}
					} catch (CvException e) {
						// e.printStackTrace();
					}
				}
			}

			System.out.println("Images are propably " + (threshold * 100) + "% equals");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static Mat image2Mat(String filePath) throws IOException {
		File input = new File(filePath);

		BufferedImage image = ImageIO.read(input);

		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer())
				.getData();

		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);

		return mat;
	}
	
	private static void showError(Mat imgError) {
		
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
		
		final JFrame frame = new JFrame();
		frame.setSize(imgError.cols(), imgError.rows());
		frame.setContentPane(new JPanel() {
			
			private static final long serialVersionUID = 1L;

			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        g.drawImage(image, 0, 0, null);            
		    }
		});
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

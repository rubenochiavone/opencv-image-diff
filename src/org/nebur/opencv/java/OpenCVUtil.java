package org.nebur.opencv.java;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

final class OpenCVUtil {

	private OpenCVUtil() {
	}
	
	/**
	 * TODO: doc it
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static Mat image2Mat(String filePath) throws IOException {
		return image2Mat(new File(filePath), CvType.CV_8UC3);
	}
	
	/**
	 * TODO: doc it
	 * 
	 * @param filePath
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static Mat image2Mat(String filePath, int type) throws IOException {
		return image2Mat(new File(filePath), type);
	}
	
	/**
	 * TODO: doc it
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Mat image2Mat(File file) throws IOException {
		return image2Mat(file, CvType.CV_8UC3);
	}
	
	/**
	 * TODO: doc it
	 * 
	 * @param file
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public static Mat image2Mat(File file, int type) throws IOException {
		BufferedImage image = ImageIO.read(file);

		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer())
				.getData();

		Mat mat = new Mat(image.getHeight(), image.getWidth(), type);
		mat.put(0, 0, data);

		return mat;
	}
}

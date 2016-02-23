package org.nebur.opencv.java;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OpenCVImageDiff {

	private static OpenCVImageDiffer differ = null;
	private static Map<String, String> options = new HashMap<String, String>();
	private static File image1 = null;
	private static File image2 = null;
	private static boolean showDiffImage = false;
	// FIXME: find a usage to verbose here
	// private static boolean verbose = false;
	
	public static void main(String[] args) {

		parseArgs(args);
		
		if (differ == null) {
			System.out.println("Unable to configure differ.");
			printUsage();
			System.exit(1);
		}
		
		// setup differ
		differ.setUp(image1, image2, options);
		
		ImageDiffResult imageDiffResult = differ.diff();
		
		if (!imageDiffResult.isEquals()) {
			if (imageDiffResult.getErrorMessage() != null) {
				System.err.println(imageDiffResult.getErrorMessage());
			}
			
			if (imageDiffResult.getException() != null) {
				imageDiffResult.getException().printStackTrace();
			}
		} else {
			System.err.println("Images are equals");
		}
		
		if (showDiffImage 
				&& imageDiffResult.getDiffResult() != null) {
			try {
				showDiffImage(imageDiffResult.getDiffResult());
			} catch (IOException e) {
				System.err.println("Unable to show diff result: " + e.getMessage());
			}
		}
		
		// release allocated resources
		differ.tearDown();
	}
	
	private static void parseArgs(String[] args) {
		
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		boolean nextShouldBeAlgorithm = false;
		
		String key = null;
		boolean nextShouldBeAnOption = false;
		
		String image1Path = null;
		String image2Path = null;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if (nextShouldBeAlgorithm) {
				
				differ = OpenCVImageDifferFactory.createDiffer(arg);
				
				nextShouldBeAlgorithm = false;
				continue;
			}
			
			if (nextShouldBeAnOption) {
				
				options.put(key, arg);
				
				key = null;
				nextShouldBeAnOption = false;
				continue;
			}
			
			switch (arg) {
			case "-h":
			case "--help":
				printUsage();
				System.exit(1);
				break;
			case "-a":
			case "--algorithm":
				nextShouldBeAlgorithm = true;
				break;
			case "-m":
			case "--method":
				key = "method";
				nextShouldBeAnOption = true;
				break;
			case "-t":
			case "--threshold":
				key = "threshold";
				nextShouldBeAnOption = true;
				break;
			case "-e":
			case "--epsilon":
				key = "epsilon";
				nextShouldBeAnOption = true;
				break;
			case "-dd":
			case "--dispaly-diff":
				showDiffImage = true;
				break;
			case "-v":
			case "--verbose":
				// FIXME: find a usage to verbose here
				// verbose = true;
				options.put("verbose", "true");
				break;
			default:
				if (i == (args.length - 2)) {
					if (new File(arg).exists()) {
						image1Path = arg;
						break;
					}
				}
				
				if (i == (args.length - 1)) {
					if (new File(arg).exists()) {
						image2Path = arg;
						break;
					}
				}
				
				System.out.println("Unrecognized option: '" + arg + "'");
			}
		}
		
		if (image1Path == null
				|| image2Path == null) {
			printUsage();
			System.exit(1);
		}
		
		image1 = new File(image1Path);
		
		if (!image1.exists()) {
			System.err.println("No such file or directory: '" + image1Path + "'");
			System.exit(1);
		}
		
		image2 = new File(image2Path);
		
		if (!image2.exists()) {
			System.err.println("No such file or directory: '" + image2Path + "'");
			System.exit(1);
		}
		
		if (differ == null) {
			// default
			differ = OpenCVImageDifferFactory.createDefaultDiffer();
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage: [OPTIONS]... IMAGE1 IMAGE2\n"
				+ "Measure difference between two images using OpenCV.\n"
				+ "\n"
				+ "OPTIONS can be the following:\n"
				+ "  -a, --algorithm        OpenCV algorithm to be used\n"
				+ "    Following OpenCV algorithms are supported:\n"
				+ "      * matchResult (default)\n"
				+ "        * IMAGE1 = template\n"
				+ "        * IMAGE2 = test\n"
				+ "      * compareHist\n"
				+ "        * IMAGE1 = base\n"
				+ "        * IMAGE2 = test\n"
				+ "    In order to better understand returned values and else take a look at:\n"
				+ "      * <http://docs.opencv.org/3.0.0/de/da9/tutorial_template_matching.html>\n"
				+ "      * <http://docs.opencv.org/3.0.0/d8/dc8/tutorial_histogram_comparison.html>\n"
				+ "  -m, --method           OpenCV method of the specified algorithm\n"
				+ "    Available methods for each algorithm:\n"
				+ "      * matchResult:\n"
				+ "        * CV_TM_SQDIFF\n"
				+ "        * CV_TM_SQDIFF_NORMED\n"
				+ "        * CV_TM_CCORR\n"
				+ "        * CV_TM_CCORR_NORMED (default)\n"
				+ "        * CV_TM_CCOEFF\n"
				+ "        * CV_TM_CCOEFF_NORMED\n"
				+ "      * compareHist:\n"
				+ "        * CV_COMP_CORREL (default)\n"
				+ "        * CV_COMP_CHISQR\n"
				+ "        * CV_COMP_INTERSECT\n"
				+ "        * CV_COMP_BHATTACHARYYA\n"
				+ "  -t, --threshold        Threshold used in diff comparisons. Default: each algorithm has it own\n"
				+ "  -e, --epsilon          Epsilon used in diff comparisons: (threshold - diff) > epsilon. Default: each algorithm has it own\n"
				+ "  -dd, --display-diff    Display on a graphical window the diff result\n"
				+ "  -mr, --merge-rects     Merge error rectangles when display diff is enabled\n"
				+ "  -v, --verbose          Improve output log\n"
				+ "  -h, --help             Show this help and exit\n"
				+ "\n"
				+ "Report bugs to: rubenochiavone@gmail.com\n"
				+ "opencv-diff-image home page: <https://github.com/rubenochiavone/opencv-image-diff>");
	}
	
	private static void showDiffImage(File diffImage) throws IOException {
		
		final BufferedImage bufferedImage = ImageIO.read(diffImage);
		
		final JFrame frame = new JFrame();
		frame.setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		frame.setContentPane(new JPanel() {
			
			private static final long serialVersionUID = 1L;

			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        g.drawImage(bufferedImage, 0, 0, null);            
		    }
		});
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

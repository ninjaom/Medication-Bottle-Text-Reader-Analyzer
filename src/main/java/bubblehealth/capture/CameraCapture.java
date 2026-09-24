package bubblehealth.capture;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class CameraCapture {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public String captureFrame(String outputPath) {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Error: Camera is not available.");
            return null;
        }

        Mat frame = new Mat();
        System.out.println("Press 'c' to capture the label, 'q' to quit.");
        String savedPath = null;
        try {
            while (true) {
                if (!camera.read(frame)) {
                    System.out.println("Error: Failed to capture image.");
                    break;
                }
                HighGui.imshow("Camera Feed - press 'c' to capture, 'q' to quit", frame);
                int key = HighGui.waitKey(30);
                if (key == 'c') {
                    Imgcodecs.imwrite(outputPath, frame);
                    savedPath = outputPath;
                    break;
                } else if (key == 'q') {
                    break;
                }
            }
        } finally {
            camera.release();
            HighGui.destroyAllWindows();
        }
        return savedPath;
    }
}

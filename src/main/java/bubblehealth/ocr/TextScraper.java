package bubblehealth.ocr;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TextScraper {

    private final Tesseract tesseract;

    public TextScraper(String tessdataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessdataPath);
        this.tesseract.setLanguage("eng");
    }

    public String performOCR(String imagePath) throws TesseractException {
        return tesseract.doOCR(new File(imagePath));
    }

    /** Recognized text lines with their on-image position, top to bottom. */
    public List<Word> getTextLines(String imagePath) throws TesseractException, IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        if (image == null) {
            throw new IOException("Could not read image: " + imagePath);
        }
        return tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
    }
}

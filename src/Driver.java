import java.awt.image.BufferedImage;
import java.awt.Color;

import java.io.File;
import java.io.IOException;

import javax.imageio.*;

public class Driver {

	public static void main (String[] args) throws IOException {
        // handle stup
        BufferedImage inImg, outImg;
        inImg = ImageIO.read(new File(args[0])); 
        int height, width;
        height = inImg.getHeight();
        width = inImg.getWidth();
        outImg = new BufferedImage(width, height, inImg.getType());

        // get the array of data from the image in grayscale
        int[] canvas = inImg.getRGB(0, 0, width, height, null, 0, width);
        Complex[][] complexCanvas = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(canvas[height * i + j]);
                double real = (float)Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
                complexCanvas[i][j] = new Complex((real / height), 0);
            }
        }
        // get the fft of each row and write its value as mag and hue as rotation note 100% saturation
        Complex[][] outComplexCanvas = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            outComplexCanvas[i] = FFT.fft(complexCanvas[i]);
        }

        int[] outCanvas = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float hue, saturation, brightness;
                brightness = 1.0f;
                hue = (float)outComplexCanvas[i][j].phase();
                saturation = (float)outComplexCanvas[i][j].abs();
                outCanvas[i * height + j] = Color.HSBtoRGB(hue, saturation, brightness);
            }
        }

        // handles output
        outImg.setRGB(0, 0, width, height, outCanvas, 0, width);
        ImageIO.write(outImg, args[2], new File(args[1]));
    }
}

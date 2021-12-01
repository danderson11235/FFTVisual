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
                Color c = new Color(canvas[(width * i) + j]);
                double real = (float)Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
                complexCanvas[i][j] = new Complex((real / height), 0);
            }
        }
        // get the fft of each row and write its value as mag and hue as rotation note 100% saturation
        Complex[][] outComplexCanvas = new Complex[height][width];
        
        if (args[3] == "-l") lateralFFT(height, complexCanvas, outComplexCanvas);
        else radialFT(height, complexCanvas, outComplexCanvas);

        int[] outCanvas = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                float hue, saturation, brightness;
                brightness = 1.0f;
                hue = (float)outComplexCanvas[i][j].phase();
                saturation = (float)outComplexCanvas[i][j].abs();
                outCanvas[i * width + j] = Color.HSBtoRGB(hue, saturation, brightness);
            }
        }

        // handles output
        outImg.setRGB(0, 0, width, height, outCanvas, 0, width);
        ImageIO.write(outImg, args[2], new File(args[1]));
    }

    public static void lateralFFT(int height, Complex[][] complexCanvas, Complex[][] outComplexCanvas) {
        for (int i = 0; i < height; i++) {
            outComplexCanvas[i] = FFT.fft(complexCanvas[i]);
        }
    }

    public static void radialFT(int height, Complex[][] complexCanvas, Complex[][] outComplexCanvas) {
        Complex negi2pi = new Complex(0, -2 * Math.PI);
        Complex e = new Complex(Math.E, 0);
        for (int k = 0; k < height; k++) {
            for (int l = 0; l < height; l++) { // these handle looping over F(k,l)
                Complex sum = new Complex(0, 0);
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < height; j++) {
                        Complex temp = new Complex((k*i + j*l), 0);
                        temp = temp.divideBy(height);
                        temp = temp.times(negi2pi);
                        temp = temp.exp();
                        temp = temp.times(complexCanvas[i][j]);
                        sum = Complex.plus(temp, sum);
                    }
                }
                outComplexCanvas[k][l] = sum;
            }
        }
    }
}

/*Christos Kydonieus
CSC112 Fall 2021
Programming Assignment 2
October 13, 2021
This program reads a black and white raw file and outputs a dithered raw file where the user chooses between
threshold, random, pattern, or error diffusion dithering.
*/

import java.io.*;
import java.sql.SQLOutput;
import java.util.Random;
import java.util.Scanner;

/*Sample image files provided for this program.
        Image1.raw      a little girl           225 x 180
        Image2.raw      hibiscus flower         300 x 200
        Image3.raw      boys in masks           500 x 500
        Image4.raw      Australian shepherd     500 x 350
        Image 5.raw     trotting horse          500 x 500

 */

public class Main {
    public static void main(String[] args) throws IOException {
        /* This will ask the user for an image, and output file, and then a dithering form. You can do this until
        * you tell the computer you would like to stop. The bulk of the main code was provided */
        while (true) {
            Scanner scnr = new Scanner(System.in);
            System.out.println("What is the input file name?");
            String inputFile = scnr.next();
            System.out.println("What is width of the input?");
            int w = scnr.nextInt();
            System.out.println("What is height of the input?");
            int h = scnr.nextInt();
            System.out.println("What is name of the output file?");
            String outputFile = scnr.next();
            InputStream inputStream = new FileInputStream(inputFile);
            OutputStream outputStream = new FileOutputStream(outputFile);
            System.out.println("What dithering method do you want to use?");
            System.out.print(" 1 for threshold,");
            System.out.print(" 2 for random,");
            System.out.print(" 3 for pattern,");
            System.out.println(" 4 for error diffusion");
            int ditherMethod = scnr.nextInt();
            switch (ditherMethod) {
                case 1:
                    threshold(inputStream, outputStream, w, h);
                    break;
                case 2:
                    random(inputStream, outputStream, w, h);
                    break;
                case 3:
                    pattern(inputStream, outputStream, w, h);
                    break;
                case 4:
                    errDiff(inputStream, outputStream, w, h);
                    break;
                default:
                    System.out.println("Not a valid choice");
                    System.exit(1);
            }
            System.out.println("Would you like to dither again? (y/n)");
            String answer = scnr.next();
            if ((answer.equals("n")) || (answer.equals("N"))){
                break;
            }
        }
    }

    // This method was supplied to me
    // This method analyzes the brightness and sets it to white or black depending on the shade.
    public static void threshold(InputStream inputStream, OutputStream outputStream, int w, int h) throws IOException {
        int r = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pixel = inputStream.read();
                if (pixel < 128) {
                    outputStream.write(0);
                } else {
                    outputStream.write(255);
                }
            }
        }
    }

    // This method was supplied to me
    // This method chooses a random number every iteration and then compares the brightness to the current pixel
    public static void random(InputStream inputStream, OutputStream outputStream, int w, int h) throws IOException {
        Random rnd = new Random();
        int r;
        int[][] pixels = new int[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixels[i][j] = inputStream.read();
                System.out.print(pixels[i][j]);
                r = rnd.nextInt(256);
                if (pixels[i][j] < r) {
                    outputStream.write(0);
                    System.out.println(" ----- 0");
                } else {
                    outputStream.write(255);
                    System.out.println(" ----- 255");
                }
            }
        }
    }

    /* This method dithers the picture using the pattern method. Basically, each pixel is normalized to fit into a
    * 3 by 3 array called a mask. The standard number is compared to the value in the mask to chose between white
    * if it is higher or black if its lower. The mask pattern is used then to create different shades of grey to
    * make the image.
    */
    public static void pattern(InputStream inputStream, OutputStream outputStream, int w, int h) throws IOException {
        // this array is the mask or the pattern the original is compared to
        int[][] mask =  new int[][]{
                {8, 3, 4},
                {6, 1, 2},
                {7, 5, 9}
        };

        // these two variables track the current position of the mask
        int curRow = 0;
        int curCol = 0;

        // this is the 2D array the original data is put into.
        int[][] pixels = new int[h][w];

        // for the length of the entire array,
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // this is the current pixel
                pixels[i][j] = inputStream.read();

                // this is the current pixel after standardizing it
                int pDash = (int) (pixels[i][j]/(25.6));
                // this is the current value of the mask
                int curMask = mask[curCol][curRow];

                //if the standard value is less than the value in the mask, make it black. Otherwise,
                // make it white.
                if (pDash < curMask) {
                    outputStream.write(0);
                } else {
                    outputStream.write(255);
                }

                // This handles the current position in the mask. It increases teh row three times then the row by one
                // every time either value reaches three return to 0
                curRow++;
                if (curRow == 3){
                    curRow = 0;
                    curCol++;
                }
                if (curCol == 3){
                    curCol = 0;
                }
            }
        }

    }

    /* This method dithers the picture using error diffusion. Each pixel is brought in. Then the program
    * runs through the array a second time, applying an error term multiplied by a constant to the points around to the
    * right of that point and below it. This error is calculated by finding the closest black or white value and then
    * multiplying it by a constant based on its position relative to the initial point. The program then goes through
    * the array a 3rd time, thresholding above or below 128 to choose white or black pixels.
     */
    public static void errDiff(InputStream inputStream, OutputStream outputStream, int w, int h) throws IOException {
        double[][] pixels = new double[h][w];

        // this creates an array with all of the pixel values.
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // pulls in the pixels value
                pixels[i][j] = inputStream.read();
            }
        }

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // this is the error term
                double e;
                if (pixels[i][j] < 128){
                    e = pixels[i][j];
                } else {
                    e = pixels[i][j] - 255;
                }

                // this applies the error to the space around the pixel
                // if that point exists
                if (j != w - 1) {
                    // to the right
                    pixels[i][j + 1] += (e * (7.0 / 16.0));
                }

                if ((j != 0) && (i != h - 1)){
                    // below to the left
                    pixels[i + 1][j - 1] += (e * (3.0 / 16.0));
                }

                if (i != h - 1){
                    // below
                    pixels[i + 1][j] += (e * (5.0 / 16.0));
                }

                if ((i != h - 1) && (j != w - 1)) {
                    // below to the right
                    pixels[i + 1][j + 1] += (e * (1.0 / 16.0));
                }

            }
        }

        // this checks the final value of each pixel and chooses black or white if its above
        // or below 128
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (pixels[i][j] < 128) {
                    outputStream.write(0);
                } else {
                    outputStream.write(255);
                }
            }
        }
    }
}

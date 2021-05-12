package Image;
/*
 * This class can change the image style
 */
import java.awt.*;

public class ImageStyle {
    private int width;
    private int height;
    private int[] rgbArray;
    private double[] yuvArray;
    private int[] newArray1;
    public ImageStyle(int w, int h, int[] rgb, int[] n1) {
        width = w;
        height = h;
        rgbArray = rgb;
        newArray1 = n1;
        getVUV();
    }

    //Change the RGB into YUV
    private double[] RGB_YUV(int i, int j){
        double[] yuv = new double[3];
        yuv[0] = 0.299 * rgbArray[i * width*3 + j] + 0.587 * rgbArray[i * width*3 + j+1] + 0.114 * rgbArray[i * width*3 + j+2];
        yuv[1] = -0.299 * rgbArray[i * width*3 + j] - 0.587 * rgbArray[i * width*3 + j+1] + 0.886 * rgbArray[i * width*3 + j+2];
        yuv[2] = 0.701 * rgbArray[i * width*3 + j] - 0.587 * rgbArray[i * width*3 + j+1] - 0.114 * rgbArray[i * width*3 + j+2];
        return yuv;
    }

    private void getVUV(){
        yuvArray = new double[rgbArray.length];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                double[] yuv = RGB_YUV(i, j);
                yuvArray[i*width*3 + j] = yuv[0];
                yuvArray[i*width*3 + j + 1] = yuv[1];
                yuvArray[i*width*3 + j + 2] = yuv[2];
            }
        }
        //System.out.println();
    }

    //Change image style into original
    public void original(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                int r = rgbArray[i * width*3 + j];
                int g = rgbArray[i * width*3 + j + 1];
                int b = rgbArray[i * width*3 + j + 2];
                newArray1[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }

    //Change image style into grayscale
    public void grayScale(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                double[] yuv = RGB_YUV(i, j);
                newArray1[i * width + j/3] = new Color((int)(yuv[0]), (int)(yuv[0]), (int)(yuv[0])).getRGB();
            }
        }
    }
    //Change image style into ordered dithering
    public void orderedDithering(){
        int n = 4;
        int[][] matrix = new int[n][n];
        /*matrix[0][0] = 0;
        matrix[0][1] = 3;
        matrix[1][0] = 2;
        matrix[1][1] = 1;*/
        /*matrix[0][0] = 6;
        matrix[0][1] = 7;
        matrix[0][2] = 0;
        matrix[1][0] = 5;
        matrix[1][1] = 4;
        matrix[1][2] = 1;
        matrix[2][0] = 8;
        matrix[2][1] = 3;
        matrix[2][2] = 2;*/
        matrix[0][0] = 11; matrix[1][0] = 5; matrix[2][0] = 4; matrix[3][0] = 12;
        matrix[0][1] = 7; matrix[1][1] = 9; matrix[2][1] = 3; matrix[3][1] = 13;
        matrix[0][2] = 14; matrix[1][2] = 1; matrix[2][2] = 2; matrix[3][2] = 8;
        matrix[0][3] = 0; matrix[1][3] = 10; matrix[2][3] = 6; matrix[3][3] = 15;
        /*matrix[0][0] = 18; matrix[1][0] = 5; matrix[2][0] = 20; matrix[3][0] = 12; matrix[4][0] = 4;
        matrix[0][1] = 7; matrix[1][1] = 15; matrix[2][1] = 3; matrix[3][1] = 2; matrix[4][1] = 13;
        matrix[0][2] = 23; matrix[1][2] = 1; matrix[2][2] = 21; matrix[3][2] = 14; matrix[4][2] = 22;
        matrix[0][3] = 9; matrix[1][3] = 10; matrix[2][3] = 11; matrix[3][3] = 0; matrix[4][3] = 8;
        matrix[0][4] = 16; matrix[1][4] = 17; matrix[2][4] = 6; matrix[3][4] = 19; matrix[4][4] = 24;*/
        /*matrix[0][0] = 18; matrix[1][0] = 26; matrix[2][0] = 20; matrix[3][0] = 12; matrix[4][0] = 4; matrix[5][0] = 30;
        matrix[0][1] = 8; matrix[1][1] = 15; matrix[2][1] = 3; matrix[3][1] = 25; matrix[4][1] = 13; matrix[5][1] = 16;
        matrix[0][2] = 23; matrix[1][2] = 35; matrix[2][2] = 21; matrix[3][2] = 14; matrix[4][2] = 22; matrix[5][2] = 32;
        matrix[0][3] = 9; matrix[1][3] = 10; matrix[2][3] = 33; matrix[3][3] = 0; matrix[4][3] = 28; matrix[5][3] = 11;
        matrix[0][4] = 31; matrix[1][4] = 17; matrix[2][4] = 6; matrix[3][4] = 19; matrix[4][4] = 24; matrix[5][4] = 34;
        matrix[0][5] = 2; matrix[1][5] = 5; matrix[2][5] = 27; matrix[3][5] = 7; matrix[4][5] = 29; matrix[5][5] = 1;*/

        double index = 256 / ((float)(n * n + 1));
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                double[] yuv = RGB_YUV(i, j);
                double gray = yuv[0];
                int dither = (int) (gray/index);
                if(dither > matrix[i%n][(j/3)%n])
                    gray = 255;
                else
                    gray = 0;
                newArray1[i * width + j/3] = new Color((int)gray, (int)gray, (int)gray).getRGB();
            }
        }
    }

    //Change image style into dynamic range
    public void dynamicRange(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                int r, g, b;
                double[] yuv = RGB_YUV(i, j);
                double y = ((128-yuv[0])/300+1)*yuv[0];
                r = (int) (y+yuv[2]);
                g = (int) (y-0.1942*yuv[1]-0.5094*yuv[2]);
                b = (int) (y+yuv[1]);
                if(r > 255)
                    r = 255;
                if(r < 0)
                    r = 0;
                if(g > 255)
                    g = 255;
                if(g < 0)
                    g = 0;
                if(b > 255)
                    b = 255;
                if(b < 0)
                    b = 0;
                /*int r, g, b;
                r = rgbArray[i][j];
                g = rgbArray[i][j+1];
                b = rgbArray[i][j+2];
                double temp = (r+g+b)/3;
                if(temp > 128){
                    r *= 3.0 /4;
                    g *= 3.0 /4;
                    b *= 3.0 /4;
                }
                else{
                    r *= 5.0 /4;
                    g *= 5.0 /4;
                    b *= 5.0 /4;
                }
                if(r > 255)
                    r = 255;
                if(r < 0)
                    r = 0;
                if(g > 255)
                    g = 255;
                if(g < 0)
                    g = 0;
                if(b > 255)
                    b = 255;
                if(b < 0)
                    b = 0;*/
                newArray1[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }
}

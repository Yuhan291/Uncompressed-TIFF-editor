package CompressImage;

import java.awt.*;
import java.util.List;

public class DecompressImage {
    private int width;
    private int height;
    private double[] yuvArray;
    private byte[] RGBArray;
    private int[] newArray1;
    private int[] newArray2;
    private int[] rgbSaving;
    private String fileName;
    public double compressionRate;

    public DecompressImage(int width, int height, byte[] RGBArray, int[] newArray1, int[] newArray2, int[] rgbSaving, String fileName) {
        this.width = width;
        this.height = height;
        this.RGBArray = RGBArray;
        this.newArray1 = newArray1;
        this.newArray2 = newArray2;
        this.rgbSaving = rgbSaving;
        this.fileName = fileName;
        getVUV();
    }

    private int[] changeRGB(){
        int[] rgbArray = new int[RGBArray.length];
        for(int i = 0; i < height*width*3; i++){
            rgbArray[i] = RGBArray[i]&0xff;
        }
        return rgbArray;
    }

    private void getVUV(){
        yuvArray = new double[RGBArray.length];
        int[] rgbArray = changeRGB();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                yuvArray[i*width*3 + j] = 0.299 * rgbArray[i * width*3 + j] + 0.587 * rgbArray[i * width*3 + j+1] + 0.114 * rgbArray[i * width*3 + j+2];
                yuvArray[i*width*3 + j + 1] = -0.299 * rgbArray[i * width*3 + j] - 0.587 * rgbArray[i * width*3 + j+1] + 0.886 * rgbArray[i * width*3 + j+2];
                yuvArray[i*width*3 + j + 2] = 0.701 * rgbArray[i * width*3 + j] - 0.587 * rgbArray[i * width*3 + j+1] - 0.114 * rgbArray[i * width*3 + j+2];
            }
        }
        //System.out.println();
    }

    public void original(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width*3; j += 3) {
                int r = RGBArray[i * width*3 + j]&0xff;
                int g = RGBArray[i * width*3 + j + 1]&0xff;
                int b = RGBArray[i * width*3 + j + 2]&0xff;
                rgbSaving[i * width*3 + j] = r;
                rgbSaving[i * width*3 + j + 1] = g;
                rgbSaving[i * width*3 + j + 2] = b;
                newArray1[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }

    //Get LZW lossless decompressed image
    public void LZWImage(){
        LZWCompression lzw1 = new LZWCompression(RGBArray, fileName, height, width);
        long start = System.currentTimeMillis();
        List<Integer> code = lzw1.encode();
        long end = System.currentTimeMillis();
        System.out.println("LZW compression time for " + fileName + ": " + (end - start) + "ms");
        /*double codeByte = 0;
        for(int i : code){
            if(i >= 128){
                codeByte += 2;
            }
            else{
                codeByte++;
            }
        }
        this.compressionRate = (double)(RGBArray.length)/codeByte;*/
        this.compressionRate = (double)(RGBArray.length)/(double)code.size();
        start = System.currentTimeMillis();
        LZWCompression lzw2 = new LZWCompression();
        byte[] RGB = lzw2.decode("compressed_files\\Lossless "+fileName+".txt");
        end = System.currentTimeMillis();
        System.out.println("LZW decompression time for " + fileName + ": " + (end - start) + "ms");
        for (int i = 0; i < lzw2.height; i++) {
            for (int j = 0; j < lzw2.width*3; j += 3) {
                int r = (RGB[i * width*3 + j]&0xff);
                int g = (RGB[i * width*3 + j + 1]&0xff);
                int b = (RGB[i * width*3 + j + 2]&0xff);
                rgbSaving[i * width*3 + j] = r;
                rgbSaving[i * width*3 + j + 1] = g;
                rgbSaving[i * width*3 + j + 2] = b;
                newArray2[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }

    //Get lossy image with ratio about 10
    public void LossyImage1(){
        int type = 1;
        double rate = 2;
        int size = 8;
        if(this.fileName.equals("board")){
            rate = 3;
        }
        if(this.fileName.equals("parrots")){
            rate = 1.8;
        }
        Lossy lossyE = new Lossy(width, height, yuvArray,this.fileName,rate,size,type);
        this.compressionRate = lossyE.compressionRate;
        Lossy lossyD = new Lossy(this.fileName,type);
        long start = System.currentTimeMillis();
        lossyD.deCompress("compressed_files\\Lossy1 "+fileName+".txt");
        long end = System.currentTimeMillis();
        System.out.println("Huffman decode time for " + fileName + ": " + (end - start) + "ms");
        int[][] YUV = lossyD.rebuildYUV();
        //System.out.println(YUV[0].length);
        for (int i = 0; i < lossyD.height; i++) {
            for (int j = 0; j < lossyD.width * 3; j += 3) {
                int r, g, b;
                r = (int) (YUV[i][j] + YUV[i][j+2]);
                g = (int) (YUV[i][j] - 0.1942 * YUV[i][j+1] - 0.5094 * YUV[i][j+2]);
                b = (int) (YUV[i][j] + YUV[i][j+1]);
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
                rgbSaving[i * width*3 + j] = r;
                rgbSaving[i * width*3 + j + 1] = g;
                rgbSaving[i * width*3 + j + 2] = b;
                newArray1[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }

    //Get lossy image with ratio about 20
    public void LossyImage2(){
        int type = 2;
        double rate = 10;
        int size = 8;
        if(this.fileName.equals("board")){
            rate = 8;
        }
        if(this.fileName.equals("parrots")){
            rate = 4;
        }
        Lossy lossyE = new Lossy(width, height, yuvArray,this.fileName,rate,size,type);
        this.compressionRate = lossyE.compressionRate;
        Lossy lossyD = new Lossy(this.fileName,type);
        long start = System.currentTimeMillis();
        lossyD.deCompress("compressed_files\\Lossy2 "+fileName+".txt");
        long end = System.currentTimeMillis();
        System.out.println("Lossy decompression time for " + fileName + ": " + (end - start) + "ms");
        int[][] YUV = lossyD.rebuildYUV();
        for (int i = 0; i < lossyD.height; i++) {
            for (int j = 0; j < lossyD.width * 3; j += 3) {
                int r, g, b;
                r = (int) (YUV[i][j] + YUV[i][j+2]);
                g = (int) (YUV[i][j] - 0.1942 * YUV[i][j+1] - 0.5094 * YUV[i][j+2]);
                b = (int) (YUV[i][j] + YUV[i][j+1]);
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
                rgbSaving[i * width*3 + j] = r;
                rgbSaving[i * width*3 + j + 1] = g;
                rgbSaving[i * width*3 + j + 2] = b;
                newArray1[i * width + j/3] = new Color(r, g, b).getRGB();
            }
        }
    }
}

package Image;
/*
* This class can read the TIFF file by bytes
*  and get some important values of image
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInformation {
    public String fileType;
    public byte[] fileData;
    public String[] tagID;
    public int stripOffsets;
    public int positionIFD;
    public int numDE;
    public int fileLength;
    public int height;
    public int width;
    public int[] rgb;
    public byte[] RGB;
    public int totalImageByte;
    public boolean isComp;

    public FileInformation(File file) {
        positionIFD = 0;
        fileLength = (int) file.length();
        fileData = new byte[fileLength];
        InputStream read;

        //read image and store initial data into filedata
        try{
            read = new FileInputStream(file);
            read.read(fileData,0,fileData.length);
        }catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /*
         * Identify if the file is TIFF
         * if is TIFF, then get the offset of IFD and number of DE
         * Identify every teg in DE, and get every information I have to use
         */
        if(isTIFF()){
            if(fileType.equals("49492A00")){
                /*for(int i = 4; i < 8; i++)
                    positionIFD += (fileData[i]&0xff)*pover(16, (i-4)*2);*/
                positionIFD = (fileData[4])&0x000000ff|
                            (fileData[5])<<8&0x0000ff00|
                            (fileData[6])<<16&0x00ff0000|
                            (fileData[7])<<24&0xff000000;
            }
            else{
                /*for(int i = 4; i < 8; i++)
                    positionIFD += (fileData[i]&0xff)*pover(16, (7-i)*2);*/
                positionIFD = (fileData[4])<<24&0xff000000|
                        (fileData[5])<<16&0x00ff0000|
                        (fileData[6])<<8&0x0000ff00|
                        (fileData[7])&0x000000ff;
            }

            if(fileType.equals("49492A00")){
                numDE = (fileData[positionIFD]&0x000000ff)|
                        (fileData[positionIFD + 1]<<8&0x0000ff00);
                identifyTag1();
            }
            else{
                numDE = (fileData[positionIFD]<<8&0x0000ff00)|
                        (fileData[positionIFD + 1]&0x000000ff);
                identifyTag2();
            }

            /*System.out.println(totalImageByte);
            System.out.println(stripOffsets);
            System.out.println(width);
            System.out.println(height);
            System.out.println(isComp);*/
            /*
             * Get the value of RGB and store them into rgb
             */
            getRGB();

        }
    }

    //Function used to identify the file type
    public boolean isTIFF() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            fileType = Integer.toHexString(fileData[i] & 0xff).toUpperCase();
            if (fileType.length() < 2)
                builder.append(0);
            builder.append(fileType);
        }
        fileType = builder.toString();
        return fileType.equals("49492A00") || fileType.equals("4D4D002A");
    }

    //Function used to get teg information for "49492A00"
    private void identifyTag1() {
        tagID = new String[numDE];
        for(int i = 0; i < numDE; i++){
            int tag =  (fileData[positionIFD+2+i*12]&0x000000ff)|
                    (fileData[positionIFD+3+i*12]<<8&0x0000ff00);
            String t = Integer.toHexString(tag&0xffff).toUpperCase();
            tagID[i] = t;
        }
        int next = (fileData[positionIFD+2+numDE*12]&0x000000ff)|
                (fileData[positionIFD+3+numDE*12]<<8&0x0000ff00)|
                (fileData[positionIFD+4+numDE*12]<<16&0x00ff0000)|
                (fileData[positionIFD+5+numDE*12]<<24&0xff000000);
        for(int i = 0; i < numDE; i++){
            if(tagID[i].equals("100")){
                width = valueOfDE1(fileData, i);
            }
            if(tagID[i].equals("101")){
                height = valueOfDE1(fileData, i);
            }
            if(tagID[i].equals("117")){
                totalImageByte = valueOfDE1(fileData, i);
            }
            if(tagID[i].equals("111")){
                int temp = valueOfDE1(fileData, i);
                if(temp > 255)
                    stripOffsets = fileData[temp]&0xff;
                else
                    stripOffsets = temp;
                System.out.println(stripOffsets);
            }
            if(tagID[i].equals("103")){
                int v = valueOfDE1(fileData, i);
                isComp = v != 1;
            }
        }
    }

    //Function used to get teg information for "4D4D002A"
    private void identifyTag2() {
        tagID = new String[numDE];
        for(int i = 0; i < numDE; i++){
            int tag =  (fileData[positionIFD+2+i*12]<<8&0x0000ff00)|
                    (fileData[positionIFD+3+i*12]&0x000000ff);
            String t = Integer.toHexString(tag&0xffff).toUpperCase();
            tagID[i] = t;
        }
        int next = (fileData[positionIFD+2+numDE*12]<<24&0xff000000)|
                (fileData[positionIFD+3+numDE*12]<<16&0x00ff0000)|
                (fileData[positionIFD+4+numDE*12]<<8&0x0000ff00)|
                (fileData[positionIFD+5+numDE*12]&0x000000ff);
        //System.out.println(next);
        for(int i = 0; i < numDE; i++){
            if(tagID[i].equals("100")){
                width = valueOfDE2(fileData, i);
            }
            if(tagID[i].equals("101")){
                height = valueOfDE2(fileData, i);
            }
            if(tagID[i].equals("117")){
                totalImageByte = valueOfDE2(fileData, i);
            }
            if(tagID[i].equals("111")){
                int temp = valueOfDE2(fileData, i);
                System.out.println(temp);
                if(temp > 255)
                    stripOffsets = fileData[temp]&0xff;
                else
                    stripOffsets = temp;
                System.out.println(stripOffsets);
            }
            if(tagID[i].equals("103")){
                int v = valueOfDE2(fileData, i);
                isComp = v != 1;
            }
        }
    }

    //Function used to calculate value of RGB, and store the value
    private void getRGB() {
        rgb = new int[height*width*3];
        RGB = new byte[height*width*3];
        int j = 0;
        int k = 0;
        for(int i = stripOffsets; i < height*width*3 + stripOffsets; i++){
            rgb[j] = fileData[i]&0xff;
            RGB[j] = fileData[i];
            j++;
        }
    }

    //Function used to calculate teg value for "49492A00"
    private int valueOfDE1(byte[] fileData, int i) {
        return (fileData[positionIFD + 10 + i * 12]&0x000000ff)|
                (fileData[positionIFD + 11 + i * 12]<<8&0x0000ff00)|
                (fileData[positionIFD + 12 + i * 12]<<16&0x00ff0000)|
                (fileData[positionIFD + 13 + i * 12]<<24&0xff000000);
    }

    //Function used to calculate teg value for "4D4D002A"
    private int valueOfDE2(byte[] fileData, int i) {
        int a = (fileData[positionIFD+4+i*12]<<8&0x0000ff00)|
                (fileData[positionIFD+5+i*12]&0x000000ff);
        String type = Integer.toHexString(a&0xffff).toUpperCase();
        if(type.equals("4"))
            return (fileData[positionIFD + 10 + i * 12]<<24&0xff000000)|
                    (fileData[positionIFD + 11 + i * 12]<<16&0x00ff0000)|
                    (fileData[positionIFD + 12 + i * 12]<<8&0x0000ff00)|
                    (fileData[positionIFD + 13 + i * 12]&0x000000ff);
        else
            return (fileData[positionIFD + 10 + i * 12]<<8&0x0000ff00)|
                    (fileData[positionIFD + 11 + i * 12]&0x000000ff);
    }
}

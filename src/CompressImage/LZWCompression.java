package CompressImage;
/*
LZW coding which used for lossless compression
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class LZWCompression {
    public byte[] fileData;
    public String fileName;
    public int height;
    public int width;
    public Dictionary<String, Integer> dictionaries = new Hashtable<>();

    public LZWCompression(byte[] fileData, String fileName, int height, int width) {
        this.fileData = fileData;
        this.fileName = fileName;
        this.height = height;
        this.width = width;
        for(int i = 0; i < 256; i++){
            dictionaries.put(Integer.toString(i),i);
        }
    }

    public LZWCompression(){}

    public List<Integer> encode(){
        int code = dictionaries.size();
        List<Integer> result = new ArrayList<>();
        String P;
        String PC;
        P = Integer.toString(fileData[0]&0xff);
        for(int i = 1; i < fileData.length; i++){
            PC = P + " " + (fileData[i] & 0xff);
            if(dictionaries.get(PC) == null){
                int c = dictionaries.get(P);
                result.add(c);
                if(i != fileData.length){
                    if(code < 65536){
                        dictionaries.put(PC, code++);
                    }
                }
                P = Integer.toString(fileData[i]&0xff);
            }
            else{
                P = PC;
            }
        }
        try{
            /*FileWriter output = new FileWriter("compressed_files\\Lossless "+fileName+".txt");
            output.write(height+" "+width+" \n");
            for(int a : result){
                output.write(a+" ");
            }

            output.flush();
            output.close();*/
            File file = new File("compressed_files\\Lossless "+fileName+".txt");
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter output = new BufferedWriter(fileWriter);
            output.write(height+" "+width+" \n");
            for(int a : result){
                output.write(a+" ");
            }

            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] decode(String path){
        File file = new File(path);
        List<Integer> code = new ArrayList<>();
        try{
            BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8));
            String line;
            int begin = 0;
            int end;
            if((line = read.readLine()) != null){
                end = line.indexOf(" ", begin);
                this.height = Integer.parseInt(line.substring(begin,end));
                begin = end + 1;
                end = line.indexOf(" ", begin);
                this.width = Integer.parseInt(line.substring(begin,end));
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    code.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Dictionary<Integer, String>dictionary = new Hashtable<>();
        for(int i = 0; i < 256; i++){
            dictionary.put(i,Integer.toString(i));
        }
        int Code = 256;
        byte[] result = new byte[this.height*this.width*3];
        int P;
        int C = code.get(0);
        result[0] = (byte)(C);
        int index = 1;
        for(int i = 1; i < code.size(); i++){
            P = C;
            C = code.get(i);
            if(dictionary.get(C) != null){
                String out = dictionary.get(C) + " ";
                int begin = 0;
                int end = 0;
                while(begin < out.length()){
                    end = out.indexOf(" ",begin);
                    int output = Integer.parseInt(out.substring(begin, end));
                    //System.out.println(output);
                    result[index] = (byte) output;
                    index++;
                    begin = end + 1;
                }
                String temp = dictionary.get(P);
                out = out.substring(0,out.indexOf(" ",0));
                dictionary.put(Code, temp+" "+out);
                Code++;
            }
            else{
                String temp = dictionary.get(P);
                String out = dictionary.get(P) + " ";
                out = out.substring(0,out.indexOf(" ",0));
                String PC = temp+" "+out;
                dictionary.put(Code, PC);
                Code++;
                int begin = 0;
                int end = 0;
                PC = PC + " ";
                while(begin < PC.length()){
                    end = PC.indexOf(" ",begin);
                    int output = Integer.parseInt(PC.substring(begin, end));
                    result[index] = (byte) output;
                    index++;
                    begin = end + 1;
                }
            }
        }
        return result;
    }
}

/*
* This class is used for lossy compression
*/

package CompressImage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lossy {
    public int width;
    public int height;
    public double quantRate;
    public int blockSize;
    public int lossyType;
    public double compressionRate;
    public String fileName;
    public double[] YUV;
    public double[][] Y;
    public double[][] U;
    public double[][] V;
    public double[][] dctMatrix;
    public int[][] quantTable;
    public HuffmanCompression huffYD;
    public HuffmanCompression huffYA;
    public HuffmanCompression huffUD;
    public HuffmanCompression huffUA;
    public HuffmanCompression huffVD;
    public HuffmanCompression huffVA;
    public List<int[][]> dctY= new ArrayList<>();
    public List<int[][]> dctU= new ArrayList<>();
    public List<int[][]> dctV= new ArrayList<>();
    public List<Integer> YDC = new ArrayList<>();
    public List<Integer> YAC = new ArrayList<>();
    public List<Integer> UDC = new ArrayList<>();
    public List<Integer> UAC = new ArrayList<>();
    public List<Integer> VDC = new ArrayList<>();
    public List<Integer> VAC = new ArrayList<>();
    public List<Integer> newYDC = new ArrayList<>();
    public List<Integer> newYAC = new ArrayList<>();
    public List<Integer> newUDC = new ArrayList<>();
    public List<Integer> newUAC = new ArrayList<>();
    public List<Integer> newVDC = new ArrayList<>();
    public List<Integer> newVAC = new ArrayList<>();

    public Lossy(String fileName, int lossyType){
        this.fileName = fileName;
        this.lossyType = lossyType;
    }

    public Lossy(int width, int height, double[] YUV, String fileName, double rate, int blockSize, int lossyType) {
        this.width = width;
        this.height = height;
        this.quantRate = rate;
        this.lossyType = lossyType;
        this.YUV = YUV;
        this.fileName = fileName;
        this.blockSize = blockSize;
        getQuantTable(blockSize);

        long start = System.currentTimeMillis();
        separateYUV(width, height);
        long end = System.currentTimeMillis();
        System.out.println("Separate YUV time for " + fileName + rate + ": " + (end - start) + "ms");
        this.dctMatrix = DCTMatrix();
        start = System.currentTimeMillis();
        getDctYUV();
        end = System.currentTimeMillis();
        System.out.println("YUV DCT process time for " + fileName + rate + ": " + (end - start) + "ms");
        start = System.currentTimeMillis();
        getDctScanCode();
        end = System.currentTimeMillis();
        System.out.println("zigzag scan time for " + fileName + rate + ": " + (end - start) + "ms");
        start = System.currentTimeMillis();
        compress();
        end = System.currentTimeMillis();
        System.out.println("Huffman encode time for " + fileName + rate + ": " + (end - start) + "ms");
        getCompressionRate();
    }

    //Change RGB value into YUV planes
    private void separateYUV(int width, int height) {
        this.Y = new double[height][width];
        this.U = new double[height/2][width/2];
        this.V = new double[height/2][width/2];
        double[][] Utemp = new double[height][width];
        double[][] Vtemp = new double[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width *3; j += 3){
                this.Y[i][j/3] = this.YUV[i* width *3 + j];
                Utemp[i][j/3] = this.YUV[i* width *3 + j + 1];
                Vtemp[i][j/3] = this.YUV[i* width *3 + j + 2];
            }
        }
        for(int i = 0; i < height/2; i++) {
            for (int j = 0; j < width/2; j++) {
                double avgU = (Utemp[i*2][j*2] + Utemp[i*2][j*2+1] + Utemp[i*2][j*2] + Utemp[i*2+1][j*2])/((double) 4);
                double avgV = (Vtemp[i*2][j*2] + Vtemp[i*2][j*2+1] + Vtemp[i*2][j*2] + Vtemp[i*2+1][j*2])/((double) 4);
                U[i][j] = avgU;
                V[i][j] = avgV;
            }
        }
    }

    //set quantization table
    private void getQuantTable(int n) {
        this.quantTable = new int[n][n];
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(this.fileName.equals("board")){
                    if(i > j){
                        this.quantTable[i][j] = (int) Math.pow(this.quantRate, (float)(i));
                    }else{
                        this.quantTable[i][j] = (int) Math.pow(this.quantRate, (float)(j));
                    }
                }
                else{
                    if(i == 0 && j ==0 ){
                        this.quantTable[i][j] = 1;
                    }
                    else{
                        if(i > j){
                            this.quantTable[i][j] = (int) Math.pow(this.quantRate, (float)(i-1));
                        }else{
                            this.quantTable[i][j] = (int) Math.pow(this.quantRate, (float)(j-1));
                        }
                    }
                }
            }
        }
    }

    //DCT transform
    private void getDctYUV(){
        List<double[][]> blockY = blockPreparation(Y, height, width);
        List<double[][]> blockU = blockPreparation(U, height/2, width/2);
        List<double[][]> blockV = blockPreparation(V, height/2, width/2);
        for(double[][] y : blockY){
            this.dctY.add(QuantCoefficients(DCTCoefficients(y,this.dctMatrix)));
        }
        for(double[][] u : blockU){
            this.dctU.add(QuantCoefficients(DCTCoefficients(u,this.dctMatrix)));
        }
        for(double[][] v : blockV){
            this.dctV.add(QuantCoefficients(DCTCoefficients(v,this.dctMatrix)));
        }
    }

    //get DC and AC for YUV planes
    private void getDctScanCode(){
        List<Integer> f = new ArrayList<>();
        for(int[][] y : this.dctY){
            List<Integer> temp = zigzagScan(y);
            f.add(temp.get(0));
            int numOfZero = 0;
            for(int i = 1; i < temp.size(); i++){
                if(temp.get(i) == 0){
                    numOfZero++;
                }
                else{
                    YAC.add(numOfZero);
                    YAC.add(temp.get(i));
                    numOfZero = 0;
                }
            }
            YAC.add(0);YAC.add(0);
        }
        this.YDC.add(f.get(0));
        while(f.size() > 1){
            this.YDC.add(f.get(0)-f.get(1));
            f.remove(0);
        }
        f.remove(0);
        for(int[][] u : this.dctU){
            List<Integer> temp = zigzagScan(u);
            f.add(temp.get(0));
            int numOfZero = 0;
            for(int i = 1; i < temp.size(); i++){
                if(temp.get(i) == 0){
                    numOfZero++;
                }
                else{
                    UAC.add(numOfZero);
                    UAC.add(temp.get(i));
                    numOfZero = 0;
                }
            }
            UAC.add(0);UAC.add(0);
        }
        this.UDC.add(f.get(0));
        while(f.size() > 1){
            this.UDC.add(f.get(0)-f.get(1));
            f.remove(0);
        }
        f.remove(0);
        for(int[][] v : this.dctV){
            List<Integer> temp = zigzagScan(v);
            f.add(temp.get(0));
            int numOfZero = 0;
            for(int i = 1; i < temp.size(); i++){
                if(temp.get(i) == 0){
                    numOfZero++;
                }
                else{
                    VAC.add(numOfZero);
                    VAC.add(temp.get(i));
                    numOfZero = 0;
                }
            }
            VAC.add(0);VAC.add(0);
        }
        this.VDC.add(f.get(0));
        while(f.size() > 1){
            this.VDC.add(f.get(0)-f.get(1));
            f.remove(0);
        }
        f.remove(0);
    }

    //Change YUV planes into 8x8 blocks
    public List<double[][]> blockPreparation(double[][] YUV, int h, int w){
        int H = (h-1)/this.blockSize+1;
        int W = (w-1)/this.blockSize+1;
        double[][] yuv = new double[H*this.blockSize][W*this.blockSize];
        for(int i = 0; i < H*this.blockSize; i++){
            for(int j = 0; j < W*this.blockSize; j++){
                if(i < h && j < w) {
                    yuv[i][j] = YUV[i][j];
                }
                else{
                    yuv[i][j] = 0;
                }
            }
        }
        List<double[][]> result = new ArrayList<>();
        for(int i = 0; i < H; i++){
            for(int j = 0; j < W; j++) {
                double[][] temp = new double[this.blockSize][this.blockSize];
                for(int l = 0; l < this.blockSize; l++){
                    for(int k = 0; k < this.blockSize; k++){
                        temp[l][k] = yuv[i*this.blockSize+l][j*this.blockSize+k];
                    }
                }
                result.add(temp);
            }
        }

        return result;
    }

    //get DCT Matrix
    public double[][] DCTMatrix(){
        double size = this.blockSize;
        double[][] C = new double[(int) size][(int) size];
        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(i == 0){
                    C[i][j] = Math.sqrt(1/size)*
                            Math.cos((2* (double)j +1)* (double)i *Math.PI / (2 * size));
                }else{
                    C[i][j] = Math.sqrt(2/size)*
                            Math.cos((2* (double)j +1)* (double)i *Math.PI / (2 * size));
                }
            }
        }
        return C;
    }

    //get DCT Coefficients
    public int[][] DCTCoefficients(double[][] X, double[][] T){
        double[][] A = new double[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++) {
            for (int j = 0; j < this.blockSize; j++) {
                A[i][j] = 0;
            }
        }
        for(int k = 0; k < this.blockSize; k++){
            for(int i = 0; i < this.blockSize; i++){
                for(int j = 0; j < this.blockSize; j++){
                    A[k][i] = A[k][i]+T[k][j]*X[j][i];
                }
            }
        }
        double[][] C = new double[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++) {
            for (int j = 0; j < this.blockSize; j++) {
                C[i][j] = 0;
            }
        }
        for(int k = 0; k < this.blockSize; k++){
            for(int i = 0; i < this.blockSize; i++){
                for(int j = 0; j < this.blockSize; j++){
                    C[k][i] = C[k][i] + A[k][j]*T[i][j];
                }
            }
        }
        int[][] out = new int[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++){
            for(int j = 0; j < this.blockSize; j++){
                out[i][j] = (int) Math.round(C[i][j]);
            }
        }

        return out;
    }

    //change DCT coeficients back to origial YUV blocks
    public int[][] deDCT(int[][] Y, double[][] T){
        double[][] A = new double[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++) {
            for (int j = 0; j < this.blockSize; j++) {
                A[i][j] = 0;
            }
        }
        for(int k = 0; k < this.blockSize; k++){
            for(int i = 0; i < this.blockSize; i++){
                for(int j = 0; j < this.blockSize; j++){
                    A[k][i] = A[k][i]+T[j][k]*Y[j][i];
                }
            }
        }
        double[][] C = new double[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++) {
            for (int j = 0; j < this.blockSize; j++) {
                C[i][j] = 0;
            }
        }
        for(int k = 0; k < this.blockSize; k++){
            for(int i = 0; i < this.blockSize; i++){
                for(int j = 0; j < this.blockSize; j++){
                    C[k][i] = C[k][i] + A[k][j]*T[j][i];
                }
            }
        }
        int[][] out = new int[this.blockSize][this.blockSize];
        for(int i = 0; i < this.blockSize; i++){
            for(int j = 0; j < this.blockSize; j++){
                out[i][j] = (int) Math.round(C[i][j]);
            }
        }

        return out;
    }

    //get quantized coefficients
    public int[][] QuantCoefficients(int[][] D){
        int[][] result = new int[D.length][D.length];
        for(int i = 0; i < D.length; i++){
            for(int j = 0; j < D.length; j++){
                result[i][j] = (int) ((D[i][j])/(quantTable[i][j]));
            }
        }
        return result;
    }

    //change quantized coefficients back to DCT coeficients
    public int[][] deQuant(int[][] D){
        int[][] result = new int[D.length][D.length];
        for(int i = 0; i < D.length; i++){
            for(int j = 0; j < D.length; j++){
                result[i][j] = (int) ((D[i][j])*(quantTable[i][j]));
            }
        }
        return result;
    }

    public List<Integer> zigzagScan(int[][] DCT){
        List<Integer> result = new ArrayList<>();
        int size = DCT.length;
        int hor = 0;
        int i = 0;
        int j = 0;
        while(hor < size){
            hor++;
            while(j < hor){
                result.add(DCT[i][j]);
                j++;
                if(i > 0)
                    i--;
            }
            hor++;
            if(j < size){
                while(i < hor){
                    result.add(DCT[i][j]);
                    i++;
                    if(j > 0)
                        j--;
                }
            }
        }
        if(i == size){
            i--;
            while(j < size && i < size){
                j++;
                while(j < size){
                    result.add(DCT[i][j]);
                    j++;i--;
                }
                j--;i += 2;
                while(i < size){
                    result.add(DCT[i][j]);
                    i++;j--;
                }
                i--;j++;
            }
        }
        if(j == size){
            j--;
            while(j < size && i < size){
                i++;
                while(i < size){
                    result.add(DCT[i][j]);
                    i++;j--;
                }
                i--;j += 2;
                while(j < size){
                    result.add(DCT[i][j]);
                    j++;i--;
                }
                j--;i++;
            }
        }
        return result;
    }

    public List<int[][]> zigBack(List<Integer> DC, List<Integer> AC){
        List<int[][]> result = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < DC.size(); i++){
            int[][] temp = new int[this.blockSize][this.blockSize];
            List<Integer> list = new ArrayList<>();
            list.add(DC.get(i));
            if(AC.size() != 0){
                while(AC.get(j+1) != 0){
                    for(int k = 0; k < AC.get(j); k++){
                        list.add(0);
                    }
                    list.add(AC.get(j+1));
                    j += 2;
                }
                j += 2;
            }
            int last = this.blockSize*this.blockSize - list.size();
            for(int k = 0; k < last; k++){
                list.add(0);
            }
            int k = 0, l = 0, hor = 0, index = 0;
            while(hor < this.blockSize){
                hor++;
                while(l < hor){
                    temp[k][l] = list.get(index);
                    l++; index++;
                    if(k > 0)
                        k--;
                }
                hor++;
                if(l < this.blockSize){
                    while(k < hor){
                        temp[k][l] = list.get(index);
                        k++; index++;
                        if(l > 0)
                            l--;
                    }
                }
            }if(k == this.blockSize){
                k--;
                while(l < this.blockSize && k < this.blockSize){
                    l++;
                    while(l < this.blockSize){
                        temp[k][l] = list.get(index);
                        l++;k--;index++;
                    }
                    l--;k += 2;
                    while(k < this.blockSize){
                        temp[k][l] = list.get(index);
                        k++;l--;index++;
                    }
                    k--;l++;
                }
            }
            result.add(temp);
        }

        return result;
    }

    //compression process and write data into txt files
    public void compress(){
        this.huffYD = new HuffmanCompression(YDC);
        String ydc = huffYD.code;
        this.huffYA = new HuffmanCompression(YAC);
        String yac = huffYA.code;
        this.huffUD = new HuffmanCompression(UDC);
        String udc = huffUD.code;
        this.huffUA = new HuffmanCompression(UAC);
        String uac = huffUA.code;
        this.huffVD = new HuffmanCompression(VDC);
        String vdc = huffVD.code;
        this.huffVA = new HuffmanCompression(VAC);
        String vac = huffVA.code;
        try{
            File file = new File("compressed_files\\Lossy" + this.lossyType + " " +this.fileName+ ".txt");
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter output = new BufferedWriter(fileWriter);
            output.write(this.height+" "+this.width+" "+this.blockSize+" "+"\n");
            for(int i = 0; i < this.blockSize; i++){
                for(int j = 0; j < this.blockSize; j++){
                    output.write(this.quantTable[i][j] + " ");
                }
                output.write("\n");
            }
            for(int a : huffYD.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffYD.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(ydc + "\n");
            for(int a : huffYA.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffYA.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(yac + "\n");
            for(int a : huffUD.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffUD.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(udc + "\n");
            for(int a : huffUA.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffUA.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(uac + "\n");
            for(int a : huffVD.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffVD.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(vdc + "\n");
            for(int a : huffVA.data){
                output.write(a+" ");
            }
            output.write("\n");
            for(String a : huffVA.codeList){
                output.write(a+" ");
            }
            output.write("\n");
            output.write(vac);

            output.flush();
            output.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //calculate compression rate
    public void getCompressionRate() {
        double listLengthYD = 0;
        double listLengthYA = 0;
        double listLengthUD = 0;
        double listLengthUA = 0;
        double listLengthVD = 0;
        double listLengthVA = 0;
        for(String i : huffYD.codeList){
            listLengthYD += (double)i.length();
        }
        for(String i : huffYA.codeList){
            listLengthYA += (double)i.length();
        }
        for(String i : huffUD.codeList){
            listLengthUD += (double)i.length();
        }
        for(String i : huffUA.codeList){
            listLengthUA += (double)i.length();
        }
        for(String i : huffVD.codeList){
            listLengthVD += (double)i.length();
        }
        for(String i : huffVA.codeList){
            listLengthVA += (double)i.length();
        }
        this.compressionRate = (double)(this.YUV.length*8)/
                (double)(huffVD.code.length() + huffVA.code.length() +
                        listLengthYD + listLengthYA +
                        huffVD.data.size()*16 + huffVA.data.size()*16 +
                        huffUD.code.length() + huffUA.code.length() +
                        listLengthUD + listLengthUA +
                        huffUD.data.size()*16 + huffUA.data.size()*16 +
                        huffYD.code.length() + huffYA.code.length() +
                        listLengthVD + listLengthVA +
                        huffYD.data.size()*16 + huffYA.data.size()*16 + this.blockSize*this.blockSize*8*4);
    }

    //get data from txt files and decode them
    public void deCompress(String path){
        File file = new File(path);
        try{
            HuffmanCompression huff = new HuffmanCompression();
            BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8));
            String line;
            int begin = 0, end;
            if((line = read.readLine()) != null){
                end = line.indexOf(" ", begin);
                this.height = Integer.parseInt(line.substring(begin,end));
                begin = end+1; end = line.indexOf(" ", begin);
                this.width = Integer.parseInt(line.substring(begin, end));
                begin = end+1; end = line.indexOf(" ", begin);
                this.blockSize = Integer.parseInt(line.substring(begin, end));
            }
            begin = 0;
            this.dctMatrix = DCTMatrix();
            this.quantTable = new int[this.blockSize][this.blockSize];
            for(int i = 0; i <this.blockSize; i++){
                line = read.readLine();
                for(int j = 0; j < this.blockSize; j++){
                    end = line.indexOf(" ", begin);
                    this.quantTable[i][j] = Integer.parseInt(line.substring(begin,end));
                    begin = end + 1;
                }
                begin = 0;
            }
            List<Integer> dataYD = new ArrayList<>();
            List<String> codeListYD = new ArrayList<>();
            String codeYD = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataYD.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListYD.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                codeYD = line;
            }
            List<Integer> temp;
            temp = huff.decode(codeYD, dataYD, codeListYD);
            this.newYDC.add(temp.get(0));
            int f = temp.get(0);
            while(temp.size() > 1){
                f = f-temp.get(1);
                this.newYDC.add(f);
                temp.remove(0);
            }
            temp.remove(0);
            List<Integer> dataYA = new ArrayList<>();
            List<String> codeListYA = new ArrayList<>();
            String codeYA = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataYA.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListYA.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                codeYA = line;
            }
            this.newYAC = huff.decode(codeYA, dataYA, codeListYA);
            List<Integer> dataUD = new ArrayList<>();
            List<String> codeListUD = new ArrayList<>();
            String codeUD = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataUD.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListUD.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                codeUD = line;
            }
            temp = huff.decode(codeUD, dataUD, codeListUD);
            this.newUDC.add(temp.get(0));
            f = temp.get(0);
            while(temp.size() > 1){
                f = f-temp.get(1);
                this.newUDC.add(f);
                temp.remove(0);
            }
            temp.remove(0);
            List<Integer> dataUA = new ArrayList<>();
            List<String> codeListUA = new ArrayList<>();
            String codeUA = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataUA.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListUA.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                codeUA = line;
            }
            this.newUAC = huff.decode(codeUA, dataUA, codeListUA);
            List<Integer> dataVD = new ArrayList<>();
            List<String> codeListVD = new ArrayList<>();
            String codeVD = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataVD.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListVD.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                codeVD = line;
            }
            temp = huff.decode(codeVD, dataVD, codeListVD);
            this.newVDC.add(temp.get(0));
            f = temp.get(0);
            while(temp.size() > 1){
                f = f-temp.get(1);
                this.newVDC.add(f);
                temp.remove(0);
            }
            temp.remove(0);
            List<Integer> dataVA = new ArrayList<>();
            List<String> codeListVA = new ArrayList<>();
            String codeVA = null;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    dataVA.add(Integer.parseInt(line.substring(begin,end)));
                    begin = end + 1;
                }
            }
            begin = 0;
            if((line = read.readLine()) != null){
                while(begin < line.length()){
                    end = line.indexOf(" ", begin);
                    codeListVA.add(line.substring(begin,end));
                    begin = end + 1;
                }
            }
            if((line = read.readLine()) != null){
                codeVA = line;
            }
            this.newVAC = huff.decode(codeVA, dataVA, codeListVA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //after decompression get new YUV planes
    public int[][] rebuildYUV(){
        long start = System.currentTimeMillis();
        List<int[][]> newDctY = zigBack(newYDC, newYAC);
        List<int[][]> newDctU = zigBack(newUDC, newUAC);
        List<int[][]> newDctV = zigBack(newVDC, newVAC);
        long end = System.currentTimeMillis();
        System.out.println("Zigzag back time for " + fileName + ": " + (end - start) + "ms");
        start = System.currentTimeMillis();
        int H1 = (height-1)/this.blockSize+1;
        int W1 = (width-1)/this.blockSize+1;
        int H2 = (height/2-1)/this.blockSize+1;
        int W2 = (width/2-1)/this.blockSize+1;
        int[][] result = new int[height][width*3];
        int[][] tempY = new int[H1*this.blockSize][W1*this.blockSize];
        int[][] tempU = new int[H2*this.blockSize][W2*this.blockSize];
        int[][] tempV = new int[H2*this.blockSize][W2*this.blockSize];
        for(int i = 0; i < H1; i++){
            for(int j = 0; j < W1; j++){
                int[][] temp = deDCT(deQuant(newDctY.get(i*W1+j)),this.dctMatrix);
                for(int k = 0; k < this.blockSize; k++){
                    for(int l = 0; l < this.blockSize; l++){
                        tempY[i*this.blockSize+k][j*this.blockSize+l] = temp[k][l];
                    }
                }
            }
        }
        for(int i = 0; i < H2; i++){
            for(int j = 0; j < W2; j++){
                int[][] temp1 = deDCT(deQuant(newDctU.get(i*W2+j)),this.dctMatrix);
                int[][] temp2 = deDCT(deQuant(newDctV.get(i*W2+j)),this.dctMatrix);
                for(int k = 0; k < this.blockSize; k++){
                    for(int l = 0; l < this.blockSize; l++){
                        tempU[i*this.blockSize+k][j*this.blockSize+l] = temp1[k][l];
                        tempV[i*this.blockSize+k][j*this.blockSize+l] = temp2[k][l];
                    }
                }
            }
        }

        for(int i = 0; i < height; i++){
            for(int j = 0; j < width*3; j += 3){
                result[i][j] = tempY[i][j/3];
                result[i][j+1] = tempU[i/2][j/6];
                result[i][j+2] = tempV[i/2][j/6];
            }
        }
        end = System.currentTimeMillis();
        System.out.println("rebuild YUV time for " + fileName + ": " + (end - start) + "ms");
        return result;
    }
}
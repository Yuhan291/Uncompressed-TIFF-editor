package CompressImage;
/*
Huffman coding which used for lossy compression
 */

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class HuffmanCompression {
    private static class Tree{
        Tree left;
        Tree right;
        int data;
        String code;
        double prob;

        public Tree(Tree left, Tree right, int data, String code, double prob) {
            this.left = left;
            this.right = right;
            this.data = data;
            this.code = code;
            this.prob = prob;
        }

        public double getProb() {
            return prob;
        }

        public void setLeft(Tree left) {
            this.left = left;
        }

        public void setRight(Tree right) {
            this.right = right;
        }

        public void setData(int data) {
            this.data = data;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setProb(double prob) {
            this.prob = prob;
        }
    }
    public List<Tree> TreeList = new ArrayList<>();
    public List<Integer> data = new ArrayList<>();
    public List<String> codeList = new ArrayList<>();
    public Tree huffTree;
    public List<Integer> fileData;
    public String code;
    public int longestCode;

    public HuffmanCompression() {};

    public HuffmanCompression(List<Integer> fileData){
        code = "";
        this.fileData = fileData;
        Dictionary<Integer, Double> dic = new Hashtable<>();

        double num = 0;
        for(int i = 0; i < this.fileData.size(); i++){
            if(dic.get(this.fileData.get(i)) == null){
                int d = this.fileData.get(i);
                for(int j = i; j < this.fileData.size(); j++){
                    if(d == this.fileData.get(j))
                        num++;
                }
                double p = (num/((double)this.fileData.size()));
                Tree node = new Tree(null, null, d, "", p);
                TreeList.add(node);
                dic.put(d,p);
                num = 0;
            }
        }
        sortData();
        huffTree = buildTree();
        data = new ArrayList<>();
        setCode(huffTree, "");
        getCode();
        this.longestCode = getLongestCode(this.codeList);
    }

    //Sort tree node by probability
    private void sortData(){
        int n = this.TreeList.size();
        for(int i = 1; i < n; i++){
            Tree key1 = this.TreeList.get(i);
            int j = i-1;
            while(j >= 0 && key1.prob < this.TreeList.get(j).prob){
                this.TreeList.set(j+1, this.TreeList.get(j));
                j--;
            }
            this.TreeList.set(j+1, key1);
        }
    }

    //get Huffman tree
    private Tree buildTree(){
        while(!(this.TreeList.size() == 1)){
            Tree child1 = TreeList.get(0);
            Tree child2 = TreeList.get(1);
            this.TreeList.remove(1);
            this.TreeList.remove(0);
            Tree temp = new Tree(child1, child2, 0, "", child1.prob+ child2.prob);
            this.TreeList.add(temp);
            sortData();
        }
        return this.TreeList.get(0);
    }

    //set code for each tree leaf
    private void setCode(Tree head, String code){
        if(head != null){
            head.setCode(code);
            setCode(head.left,code+"0");
            setCode(head.right,code+"1");
            if(head.left == null && head.right == null){
                this.data.add(head.data);
                this.codeList.add(head.code);
            }
        }
    }

    private void getCodeList(Tree head){
        if(head != null){
            if(head.data != -1){
                int i = this.data.indexOf(head.data);
                this.codeList.set(i, head.code);
                //System.out.println(head.data + ": " + head.code);
            }
            getCodeList(head.left);
            getCodeList(head.right);
        }
    }

    //get compreesion code
    private void getCode(){
        for(int i : this.fileData){
            int index = this.data.indexOf(i);
            this.code = this.code + this.codeList.get(index);
        }
    }

    private int getLongestCode(List<String> codeList){
        int Code = 0;
        for(String c : codeList){
            if(Code < c.length()){
                Code = c.length();
            }
        }
        return Code;
    }

    public List<Integer> decode(String C, List<Integer> data, List<String> codeList){
        List<Integer> result = new ArrayList<>();
        int l = getLongestCode(codeList);
        int begain = 0;
        int end = l;
        String newCode = C;
        for(int i = 0; i < l; i++){
            newCode = newCode + "bb";
        }
        while(end > begain){
            String temp = newCode.substring(begain, end);
            if(codeList.contains(temp)){
                int index = codeList.indexOf(temp);
                result.add(data.get(index));
                begain = end;
                end = begain+l;
            }
            if(!codeList.contains(temp)){
                end--;
            }
        }

        return result;
    }
}

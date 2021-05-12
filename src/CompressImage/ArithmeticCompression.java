package CompressImage;

import java.text.DecimalFormat;
import java.util.*;

public class ArithmeticCompression {
    public List<Integer> data = new ArrayList<>();
    public Dictionary<Integer, String> rangeE = new Hashtable<>();
    public Dictionary<String, Integer> rangeD = new Hashtable<>();

    public ArithmeticCompression(List<Integer> data){
        this.data = data;

        double l = 0;
        for(int i = 0; i < data.size(); i++){
            if(this.rangeE.get(data.get(i)) == null){
                double num = 0;
                for(int j = i; j < data.size(); j++){
                    if(data.get(i).equals(data.get(j)))
                        num++;
                }
                double h = num/((double) data.size()) + l;
                String a = new DecimalFormat("#.0000").format(l);
                String b = new DecimalFormat("#.0000").format(h);
                //0.22278736026275228
                String pair = a + " " + b;
                //Pair pair = new Pair(l, h);
                this.rangeE.put(data.get(i), pair);
                this.rangeD.put(pair, data.get(i));
                l = h;
            }
        }
    }

    public double encode(List<Integer> data){
        double low = 0;
        double high = 1;
        double r;
        for(int d : data){
            r = high - low;
            String pair = rangeE.get(d);
            int begain = 0;
            int end = pair.indexOf(" ", begain);
            double l = Double.parseDouble(pair.substring(begain, end));
            begain = end + 1;
            double h = Double.parseDouble(pair.substring(begain));
            low = low + r*l;
            high = low + r*h;
        }

        return low;
    }

    public List<Integer> decode(double code){
        double low = 0;
        double high = 1;
        double temp = code;
        List<Integer> result = new ArrayList<>();
        List<String> pair = new ArrayList<>();
        Enumeration<String> P = rangeE.elements();
        while (P.hasMoreElements()){
            pair.add(P.nextElement());
        }
        while(temp > low){
            for(String p : pair){
                int begain = 0;
                int end = p.indexOf(" ", begain);
                double l = Double.parseDouble(p.substring(begain, end));
                begain = end + 1;
                double h = Double.parseDouble(p.substring(begain));
                if(temp > l && temp < h){
                    //System.out.println(rangeD.get(p));
                    result.add(rangeD.get(p));
                    temp = (temp - l)/(h - l);
                    break;
                }
            }
        }
        return result;
    }
}

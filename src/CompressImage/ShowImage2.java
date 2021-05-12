package CompressImage;

import Image.FileInformation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ShowImage2 {
    public static int width;
    public static int height;
    public static int[] rgbArray;
    public static byte[] RGB;
    public static int[] newArray1;
    public static int[] newArray2;
    public static int[] rgbSaving;
    public static int imageCondition = 0;
    public static File file = new File("");
    public static DecompressImage process;
    public static FileInformation fileIfo;

    private static void showImage1(JFrame chooseImage, String type, JLabel imageLable1, JLabel imageLable2,
                                   JLabel titleLabel1, JLabel titleLabel2, JLabel rateLabel) {
        BufferedImage image1 = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++){
                image1.setRGB(j, i, newArray1[i * width + j]);
            }
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++){
                image2.setRGB(j, i, newArray2[i * width + j]);
            }
        ImageIcon fimage1 = new ImageIcon(image1);
        ImageIcon fimage2 = new ImageIcon(image2);
        imageLable1.setText(null);
        imageLable2.setText(null);
        titleLabel1.setText(null);
        titleLabel2.setText(null);
        rateLabel.setText(null);
        imageLable1.setIcon(fimage1);
        imageLable2.setIcon(fimage2);
        titleLabel1.setText("Left: Lossless Image     ");
        titleLabel2.setText("   Right: "+ type +" Image");
        rateLabel.setText("Compression Ratio: "+process.compressionRate);
        chooseImage.setSize(width*2 + 160, height + 100);
    }

    public static void readImage(){
        //Create GUI by JavaSwing
        //http://c.biancheng.net/swing/
        final JFrame imageFrame = new JFrame("CMPT365 Project1");
        imageFrame.setLayout(new BorderLayout());
        imageFrame.setSize(550,400);
        JPanel buttomPanel = new JPanel();
        buttomPanel.setLayout(new GridLayout(5,1,10,10));
        final JPanel imagePanel = new JPanel();
        final JPanel titlePanel = new JPanel();
        final JPanel ratePanel = new JPanel();
        final JLabel imagelabel1 = new JLabel();
        final JLabel imagelabel2 = new JLabel();
        final JLabel titlelabel1 = new JLabel();
        final JLabel titlelabel2 = new JLabel();
        final JLabel ratelabel = new JLabel();
        JButton choose = new JButton("choose");
        JButton Lossless = new JButton("Lossless");
        JButton Lossy1 = new JButton("Lossy1");
        JButton Lossy2 = new JButton("Lossy2");
        JButton original = new JButton("original");
        JButton save = new JButton("save");
        choose.setBounds(0,0,50,50);
        buttomPanel.add(choose);
        buttomPanel.add(Lossless);
        buttomPanel.add(Lossy1);
        buttomPanel.add(Lossy2);
        buttomPanel.add(save);
        titlePanel.add(titlelabel1,0);
        titlePanel.add(titlelabel2,1);
        ratePanel.add(ratelabel);
        imageFrame.add(buttomPanel,BorderLayout.EAST);
        imageFrame.add(imagelabel1,BorderLayout.CENTER);
        imageFrame.add(imagelabel2,BorderLayout.WEST);
        imageFrame.add(titlePanel,BorderLayout.NORTH);
        imageFrame.add(ratePanel,BorderLayout.SOUTH);
        imageFrame.setVisible(true);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Function used to open file and display original image
        choose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser chooser = new JFileChooser("images");
                chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
                int choose = chooser.showOpenDialog(null);
                if(choose == JFileChooser.CANCEL_OPTION){
                    return;
                }
                File f;
                f = chooser.getSelectedFile();
                if(f != null){
                    if(!file.getName().equals(f.getName())){
                        file = f;
                        fileIfo = new FileInformation(file);
                        if(fileIfo.isTIFF()){
                            width = fileIfo.width;
                            height = fileIfo.height;
                            int W = ((width-1)/8+1)*8;
                            int H = ((height-1)/8+1)*8;
                            rgbArray = fileIfo.rgb;
                            RGB = fileIfo.RGB;
                            int index = file.getName().lastIndexOf(".");
                            String fileName = file.getName().substring(0,index);
                            newArray1 = new int[height * width + width];
                            newArray2 = new int[height * width + width];
                            rgbSaving = new int[height * width * 3 + width];
                            process = new DecompressImage(width,height,RGB,newArray1,newArray2,rgbSaving,fileName);
                            process.original();
                            process.LZWImage();
                            showImage1(imageFrame, "Original", imagelabel1, imagelabel2,
                                    titlelabel1, titlelabel2, ratelabel);
                            imageCondition = 1;
                        }
                        else{
                            System.out.println("null");
                            imagePanel.removeAll();
                            titlelabel1.setText(null);
                            titlelabel2.setText(null);
                            imagelabel1.setIcon(null);
                            imagelabel2.setIcon(null);
                            ratelabel.setText(null);
                            imagelabel2.setText("Wrong Format");
                            imageFrame.setSize(550, 400);
                        }
                    }
                }
            }
        });

        //Get image from Lossless image file
        losslessImage(imageFrame,imagelabel1,imagelabel2,titlelabel1,titlelabel2,ratelabel,Lossless);

        //Get image from Lossy image file
        lossyImage1(imageFrame,imagelabel1,imagelabel2,titlelabel1,titlelabel2,ratelabel,Lossy1);

        //Get image from Lossy image file
        lossyImage2(imageFrame,imagelabel1,imagelabel2,titlelabel1,titlelabel2,ratelabel,Lossy2);


        //Save the image into images folder
        save.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width == 0 && height == 0)
                    return;
                File outfile = null;
                JFileChooser chooser = new JFileChooser("images");
                String path;
                //get the file name without .tif
                //https://blog.csdn.net/zahuopuboss/article/details/51643807
                String fileName = file.getName();
                int d = fileName.lastIndexOf('.');
                fileName =  fileName.substring(0, d);

                //save image in png
                int choose = 10000;
                switch (imageCondition){
                    case 1:
                        chooser.setSelectedFile(new File(("Lossless " + fileName) + ".tif"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    case 2:
                        chooser.setSelectedFile(new File(("Lossy10 " + fileName) + ".tif"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    case 3:
                        chooser.setSelectedFile(new File(("Lossy20 " + fileName) + ".tif"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    default:
                        break;
                }

                if(choose == JFileChooser.APPROVE_OPTION){
                    OutputStream output;
                    try{
                        assert outfile != null;
                        if(!outfile.exists())
                            outfile.createNewFile();
                        System.out.println(outfile.getName());
                        output = new FileOutputStream(outfile);
                        byte[] data = fileIfo.fileData;
                        for(int i = fileIfo.stripOffsets; i < height*width*3 + fileIfo.stripOffsets; i++){
                            data[i] = (byte) rgbSaving[i - fileIfo.stripOffsets];
                        }
                        output.write(data);
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private static void losslessImage(JFrame chooseImage, JLabel imageLable1, JLabel imageLable2,
                                      JLabel titleLabel1, JLabel titleLabel2, JLabel rateLabel, JButton lossless) {
        lossless.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.original();
                    process.LZWImage();
                    showImage1(chooseImage, "Original", imageLable1, imageLable2,
                            titleLabel1, titleLabel2, rateLabel);
                    imageCondition = 1;
                }
            }
        });
    }

    private static void lossyImage1(JFrame chooseImage, JLabel imageLable1, JLabel imageLable2,
                                    JLabel titleLabel1, JLabel titleLabel2, JLabel rateLabel, JButton lossy) {
        lossy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.LossyImage1();
                    showImage1(chooseImage, "Lossy", imageLable1, imageLable2,
                            titleLabel1, titleLabel2, rateLabel);
                    imageCondition = 2;
                }
            }
        });
    }

    private static void lossyImage2(JFrame chooseImage, JLabel imageLable1, JLabel imageLable2,
                                    JLabel titleLabel1, JLabel titleLabel2, JLabel rateLabel, JButton lossy) {
        lossy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.LossyImage2();
                    showImage1(chooseImage, "Lossy", imageLable1, imageLable2,
                            titleLabel1, titleLabel2, rateLabel);
                    imageCondition = 3;
                }
            }
        });
    }
}

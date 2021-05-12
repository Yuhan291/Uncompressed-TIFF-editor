package Image;
/*
 * This class can choose TIFF image from local disk, and display image in GUI
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ShowImage1 {
    public static int width;
    public static int height;
    public static int[] rgbArray;
    public static int[] newArray1;
    public static int imageCondition = 0;
    public static File file = new File("");

    public static ImageStyle process;

    static class newPanel extends JPanel{
        BufferedImage image;

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        public newPanel(){
        }

        @Override
        public void paintComponent(Graphics g){
            if(!image.equals(null)){
                g.drawImage(image,0,0,image.getWidth(),image.getHeight(),null);
            }
        }
    }

    private static void showImage1(JFrame chooseImage, newPanel panel, JPanel p) {
        BufferedImage image1 = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++){
                image1.setRGB(j, i, newArray1[i * width + j]);
            }
        //image.setRGB(0, 0, width, height, newArray, 0, width);
        panel.setImage(image1);
        chooseImage.remove(p);
        chooseImage.add(panel,BorderLayout.CENTER);
        chooseImage.setSize(width + 160, height + 100);
    }

    public static void readImage(){
        //Create GUI by JavaSwing
        //http://c.biancheng.net/swing/
        final JFrame imageFrame = new JFrame("CMPT365 Project1");
        imageFrame.setLayout(new BorderLayout());
        imageFrame.setSize(550,400);
        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(6,1,10,10));
        final newPanel panel = new newPanel();
        final JPanel p = new JPanel();
        final JLabel label = new JLabel();
        JButton choose = new JButton("choose");
        JButton gray = new JButton("grayscale");
        JButton dither = new JButton("ordered dithering");
        JButton range = new JButton("dinamic range");
        JButton original = new JButton("original");
        JButton save = new JButton("save");
        choose.setBounds(0,0,50,50);
        jp.add(choose);
        jp.add(gray);
        jp.add(dither);
        jp.add(range);
        jp.add(original);
        jp.add(save);
        panel.add(label);
        imageFrame.add(jp,BorderLayout.EAST);
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
                        FileInformation fileIfo = new FileInformation(file);
                        if(fileIfo.isTIFF()){
                            width = fileIfo.width;
                            height = fileIfo.height;
                            rgbArray = fileIfo.rgb;
                            newArray1 = new int[height * width + width];
                            process = new ImageStyle(width,height,rgbArray,newArray1);
                            panel.repaint();
                            process.original();
                            showImage1(imageFrame, panel, p);
                            imageCondition = 0;
                        }
                        else{
                            System.out.println("null");
                            label.setText("Wrong Format");
                            panel.repaint();
                            p.add(label);
                            imageFrame.remove(panel);
                            imageFrame.add(p,BorderLayout.CENTER);
                            imageFrame.setSize(550, 400);
                        }
                    }
                }
            }
        });

        //Change image style into grayscale
        grayImage(label, imageFrame, panel, p, gray);

        //Change image style into ordered dithering
        ditherImage(label, imageFrame, panel, p, dither);

        //Change image style into dynamic range
        dinamicImage(label, imageFrame, panel, p, range);

        //Change image style into original
        originalImage(label, imageFrame, panel, p, original);

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
                    case 0:
                        chooser.setSelectedFile(new File(("origin " + fileName) + ".png"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    case 1:
                        chooser.setSelectedFile(new File(("gray " + fileName) + ".png"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    case 2:
                        chooser.setSelectedFile(new File(("dither " + fileName) + ".png"));
                        choose = chooser.showSaveDialog(null);
                        path = chooser.getSelectedFile().getPath();
                        outfile = new File(path);
                        break;
                    case 3:
                        chooser.setSelectedFile(new File(("dynamic " + fileName) + ".png"));
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
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                        for(int i = 0; i < height; i++)
                            for(int j = 0; j < width; j++)
                                image.setRGB(j, i, newArray1[i * width + j]);
                        ImageIO.write(image, "png", output);
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private static void originalImage(JLabel label, JFrame imageFrame, newPanel panel, JPanel p, JButton original) {
        original.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.original();
                    panel.repaint();
                    showImage1(imageFrame, panel, p);
                    imageCondition = 0;
                }
            }
        });
    }

    private static void dinamicImage(JLabel label, JFrame imageFrame, newPanel panel, JPanel p, JButton range) {
        range.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.dynamicRange();
                    panel.repaint();
                    showImage1(imageFrame, panel, p);
                    imageCondition = 3;
                }
            }
        });
    }

    private static void ditherImage(JLabel label, JFrame imageFrame, newPanel panel, JPanel p, JButton dither) {
        dither.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.orderedDithering();
                    panel.repaint();
                    showImage1(imageFrame, panel, p);
                    imageCondition = 2;
                }
            }
        });
    }

    private static void grayImage(JLabel label, JFrame imageFrame, newPanel panel, JPanel p, JButton gray) {
        gray.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(width > 0 && height > 0){
                    process.grayScale();
                    panel.repaint();
                    showImage1(imageFrame, panel, p);
                    imageCondition = 1;
                }
            }
        });
    }
}

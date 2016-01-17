/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bamamoto.mactools.png2icns;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author barmeier
 */
public class Scaler {

    protected static String runProcess(String[] commandLine) throws IOException {
        StringBuilder cl = new StringBuilder();
        for (String i : commandLine) {
            cl.append(i);
            cl.append(" ");
        }
        
        String result = "";
       
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        Map<String, String> env = builder.environment();
        env.put("PATH", "/usr/sbin:/usr/bin:/sbin:/bin");
        builder.redirectErrorStream(true);
        Process process = builder.start();

        String line;

        InputStream stdout = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

        while ((line = reader.readLine()) != null) {
            result += line + "\n";
        }

        boolean isProcessRunning = true;
        int maxRetries = 60;
        
        do {
            try {
                isProcessRunning = process.exitValue() < 0;
            } catch (IllegalThreadStateException ex) {
                System.out.println ("Process not terminated. Waiting ...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException iex) {
                    //nothing todo
                }
                maxRetries--;
            }
        } while (isProcessRunning && maxRetries > 0);
        System.out.println("Process has terminated");
        if (process.exitValue() != 0) {
            throw new IllegalStateException("Exit value not equal to 0: "+result);
        }
        if (maxRetries == 0 && isProcessRunning) {
            System.out.println("Process does not terminate. Try to kill the process now.");
            process.destroy();
        }
        
        return result;
    }
    
    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("i", "input-filename", true, "Filename ofthe image containing the icon. The image should be a square with at least 1024x124 pixel in PNG format.");
        options.addOption("o", "iconset-foldername", true, "Name of the folder where the iconset will be stored. The extension .iconset will be added automatically.");
        String folderName;
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("i")) {
                if (new File(cmd.getOptionValue("i")).isFile()) {
                    
                    if (cmd.hasOption("o")) {
                        folderName = cmd.getOptionValue("o");
                    }
                    else {
                        folderName = "/tmp/noname.iconset";
                    }

                    if (!folderName.endsWith(".iconset")) {
                        folderName= folderName + ".iconset";
                    }
                    new File(folderName).mkdirs();

                    BufferedImage source = ImageIO.read(new File(cmd.getOptionValue("i")));
                    BufferedImage resized = resize(source, 1024, 1024);
                    save(resized, folderName+"/icon_512x512@2x.png");
                    resized = resize(source, 512, 512);
                    save(resized, folderName+"/icon_512x512.png");
                    save(resized, folderName+"/icon_256x256@2x.png");

                    resized = resize(source, 256, 256);
                    save(resized, folderName+"/icon_256x256.png");
                    save(resized, folderName+"/icon_128x128@2x.png");

                    resized = resize(source, 128, 128);
                    save(resized, folderName+"/icon_128x128.png");

                    resized = resize(source, 64, 64);
                    save(resized, folderName+"/icon_32x32@2x.png");

                    resized = resize(source, 32, 32);
                    save(resized, folderName+"/icon_32x32.png");
                    save(resized, folderName+"/icon_16x16@2x.png");

                    resized = resize(source, 16, 16);
                    save(resized, folderName+"/icon_16x16.png");
                    
                    Scaler.runProcess(new String[] {"/usr/bin/iconutil","-c","icns",folderName});
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading image: "+ cmd.getOptionValue("i"));
            e.printStackTrace();
            
        } catch (ParseException ex) {
            Logger.getLogger(Scaler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static BufferedImage resize(BufferedImage source, int width, int height) {

        double xScale = ((double) width) / (double) source.getWidth();
        double yScale = ((double) height) / (double) source.getHeight();
        BufferedImage result = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(width, height, source.getColorModel().getTransparency());
        Graphics2D newImage = null;
        try {
            newImage = result.createGraphics();
            newImage.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            newImage.drawRenderedImage(source, AffineTransform.getScaleInstance(xScale, yScale));
        } finally {
            if (newImage != null) {
                newImage.dispose();
            }
        }
        return result;
    }

    public static void save(BufferedImage image, String filename) throws IOException {
        Iterator writers = ImageIO.getImageWritersByFormatName("PNG");
        if (writers.hasNext()) {
            ImageWriter imageWriter = (ImageWriter) writers.next();
            ImageWriteParam params = imageWriter.getDefaultWriteParam();

            File outFile = new File(filename);
            try (FileImageOutputStream output = new FileImageOutputStream(outFile)) {
                imageWriter.setOutput(output);
                IIOImage outImage = new IIOImage(image, null, null);
                imageWriter.write(null, outImage, params);
            }
        }
    }
}

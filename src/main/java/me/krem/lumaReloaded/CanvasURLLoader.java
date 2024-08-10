package me.krem.lumaReloaded;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.map.MapPalette;

public class CanvasURLLoader extends Thread {
    private final String name;
    private final String url;
    private final int width;
    private final int height;
    private final BufferedImage scaledImg;
    private final CommandSender sender;

    public CanvasURLLoader(CommandSender sender, String name, int width, int height, String url) {
        this.name = name;
        this.url = url;
        this.width = width;
        this.height = height;
        this.sender = sender;
        this.scaledImg = new BufferedImage(128 * width, 128 * height, 2);
    }

    public CanvasURLLoader(CommandSender sender, LumaCanvas canvas, String url) {
        this(sender, canvas.getName(), canvas.getWidth(), canvas.getHeight(), url);
    }

    public void run() {
        try {
            List<BufferedImage> images = this.getImagesFromURL(new URL(this.url));
            if (images.isEmpty()) {
                this.sendMessage(ChatColor.GOLD + "Unable to load image!");
                return;
            }

            int frames = images.size();
            byte[] data = new byte[16384 * this.width * this.height * frames];

            for(int i = 0; i < frames; ++i) {
                BufferedImage img = (BufferedImage)images.get(i);
                AffineTransformOp atOp = this.generateAffineTransformOp(img, this.width, this.height);
                atOp.filter(img, this.scaledImg);

                for(int x = 0; x < this.width; ++x) {
                    for(int y = 0; y < this.height; ++y) {
                        Image subImage = this.scaledImg.getSubimage(128 * x, 128 * y, 128, 128);
                        byte[] mapData = MapPalette.imageToBytes(subImage);
                        int mapDataOffset = 16384 * this.width * this.height * i + 16384 * this.width * y + 16384 * x;

                        for(int j = 0; j < 16384; ++j) {
                            data[mapDataOffset + j] = mapData[j];
                        }
                    }
                }
            }

            byte[] fileData = data;
            int fileFrames = frames;
            if (!Settings.ANIMATIONS) {
                data = Arrays.copyOf(data, 16384 * this.width * this.height);
                frames = 1;
                this.sendMessage(ChatColor.GOLD + "Note: Animations are disabled, but will work if enabled in the config.yml");
            }

            int finalFrames = frames;
            byte[] finalData = data;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(LumaReloaded.instance, () -> {
                try {
                    if (CanvasManager.hasCanvasByName(this.name)) {
                        CanvasManager.getCanvasByName(this.name).setData(finalFrames, finalData);
                        this.sendMessage(ChatColor.GOLD + "Successfully changed image!");
                    } else {
                        CanvasManager.createCanvasForData(this.name, finalData, this.width, this.height, finalFrames);
                        this.sendMessage(ChatColor.GOLD + "Successfully loaded image! " + ChatColor.GRAY + "Use " + ChatColor.ITALIC + "/lu print " + this.name + ChatColor.GRAY + " to get the created maps");
                    }

                    this.writeBlob(this.name, this.width, this.height, fileFrames, fileData);
                } catch (Exception var6) {
                    this.sendMessage(ChatColor.GOLD + "Unable to load image! " + ChatColor.GRAY + var6.getMessage());
                }

            }, 0L);
        } catch (Exception var13) {
            this.sendMessage(ChatColor.GOLD + "Unable to load image! " + ChatColor.GRAY + var13.getMessage());
        }

    }

    private void sendMessage(String message) {
        Synchronizer.add(() -> {
            this.sender.sendMessage(message);
        });
    }

    private List<BufferedImage> getImagesFromURL(URL url) throws IOException {
        System.out.println("Getting image from URL");
        ArrayList<BufferedImage> images = new ArrayList();
        HTTP.HTTPResponse response = HTTP.get(url);
        System.out.println("HTTP response: " + (String)((List)response.getHeaders().get((Object)null)).get(0));
        String mimeType = url.openConnection().getContentType();
        if (!((String)((List)response.getHeaders().get((Object)null)).get(0)).contains("OK")) {
            return images;
        } else if (mimeType == null) {
            return images;
        } else {
            ByteArrayInputStream contentInput = new ByteArrayInputStream(response.getContent());
            BufferedImage img;
            switch (mimeType.split(";")[0]) {
                case "image/jpg":
                case "image/jpeg":
                case "image/png":
                case "image/bmp":
                    img = ImageIO.read(contentInput);
                    images.add(img);
                    break;
                case "image/gif":
                    images.addAll(GifUtil.readGif(contentInput));
                    break;
                case "application/zip":
                    HashMap<String, byte[]> imageFiles = new HashMap();
                    byte[] unzipBuffer = new byte[1024];
                    ZipInputStream zis = new ZipInputStream(contentInput);
                    ZipEntry ze = zis.getNextEntry();

                    while(true) {
                        while(ze != null) {
                            if (ze.isDirectory()) {
                                System.err.println("Skipping non-conforming file in ZIP..");
                                zis.closeEntry();
                                ze = zis.getNextEntry();
                            } else {
                                String fileName = ze.getName();
                                System.out.println("File: " + fileName);
                                if (!fileName.matches("^[1-9][0-9]{0,2}\\.(jpg|jpeg|png|bmp)$")) {
                                    System.err.println("Skipping non-conforming file in ZIP..");
                                    zis.closeEntry();
                                    ze = zis.getNextEntry();
                                } else {
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                    int len;
                                    while((len = zis.read(unzipBuffer)) > 0) {
                                        baos.write(unzipBuffer, 0, len);
                                    }

                                    imageFiles.put(fileName.substring(0, fileName.indexOf(".")), baos.toByteArray());
                                    zis.closeEntry();
                                    ze = zis.getNextEntry();
                                }
                            }
                        }

                        zis.close();

                        try {
                            for(int i = 1; imageFiles.containsKey(i + ""); ++i) {
                                img = ImageIO.read(new ByteArrayInputStream((byte[])imageFiles.get(i + "")));
                                images.add(img);
                            }

                            return images;
                        } catch (IOException var16) {
                            return images;
                        }
                    }
                default:
                    this.sendMessage(ChatColor.GOLD + "Unknown file format! " + ChatColor.GRAY + "Make sure you are pasting a deep link. These usually contain a picture file ending");
                    return images;
            }

            return images;
        }
    }

    private AffineTransformOp generateAffineTransformOp(BufferedImage srcImg, int width, int height) {
        double scaleX = 128.0 * (double)width / (double)srcImg.getWidth();
        double scaleY = 128.0 * (double)height / (double)srcImg.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        return new AffineTransformOp(at, 3);
    }

    private void writeBlob(String name, int width, int height, int frames, byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        byte[] deflatedBuffer = new byte[data.length];

        int deflatedLength;
        for(deflatedLength = 0; !deflater.finished(); deflatedLength += deflater.deflate(deflatedBuffer, deflatedLength, deflatedBuffer.length - deflatedLength)) {
        }

        deflater.end();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(76);
        baos.write(19541);
        baos.write(5002573);
        baos.write(1280658753);
        baos.write(width);
        baos.write(height);
        baos.write(0);
        baos.write(0);
        baos.write(0);
        baos.write(frames);
        FileOutputStream fos = new FileOutputStream(new File(LumaReloaded.instance.getDataFolder(), "images/" + name + ".bin"));
        fos.write(baos.toByteArray());
        fos.write(deflatedBuffer, 0, deflatedLength);
        fos.flush();
        fos.close();
    }
}

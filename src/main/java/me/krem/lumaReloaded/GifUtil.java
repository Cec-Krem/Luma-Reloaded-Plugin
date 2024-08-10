package me.krem.lumaReloaded;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GifUtil {
    public GifUtil() {
    }

    public static ArrayList<BufferedImage> readGif(InputStream stream) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList(2);
        ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(stream));
        int lastx = 0;
        int lasty = 0;
        int width = -1;
        int height = -1;
        IIOMetadata metadata = reader.getStreamMetadata();
        Color backgroundColor = null;
        IIOMetadataNode colorEntry;
        int x;
        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode)metadata.getAsTree(metadata.getNativeMetadataFormatName());
            NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
            NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");
            IIOMetadataNode colorTable;
            if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0) {
                colorTable = (IIOMetadataNode)globalScreeDescriptor.item(0);
                if (colorTable != null) {
                    width = Integer.parseInt(colorTable.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(colorTable.getAttribute("logicalScreenHeight"));
                }
            }

            if (globalColorTable != null && globalColorTable.getLength() > 0) {
                colorTable = (IIOMetadataNode)globalColorTable.item(0);
                if (colorTable != null) {
                    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                    for(colorEntry = (IIOMetadataNode)colorTable.getFirstChild(); colorEntry != null; colorEntry = (IIOMetadataNode)colorEntry.getNextSibling()) {
                        if (colorEntry.getAttribute("index").equals(bgIndex)) {
                            int red = Integer.parseInt(colorEntry.getAttribute("red"));
                            int green = Integer.parseInt(colorEntry.getAttribute("green"));
                            x = Integer.parseInt(colorEntry.getAttribute("blue"));
                            backgroundColor = new Color(red, green, x);
                            break;
                        }
                    }
                }
            }
        }

        BufferedImage master = null;
        boolean hasBackround = false;
        int frameIndex = 0;

        while(true) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException var23) {
                reader.dispose();
                ArrayList images = new ArrayList();
                Iterator var28 = frames.iterator();

                while(var28.hasNext()) {
                    ImageFrame frame = (ImageFrame)var28.next();
                    images.add(frame.getImage());
                }

                return images;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode)reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            colorEntry = (IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
            NodeList children = root.getChildNodes();
            String disposal = colorEntry.getAttribute("disposalMethod");
            if (master == null) {
                master = new BufferedImage(width, height, 2);
                master.createGraphics().setColor(backgroundColor);
                master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());
                hasBackround = image.getWidth() == width && image.getHeight() == height;
                master.createGraphics().drawImage(image, 0, 0, (ImageObserver)null);
            } else {
                x = 0;
                int y = 0;

                for(int nodeIndex = 0; nodeIndex < children.getLength(); ++nodeIndex) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                if (disposal.equals("restoreToPrevious")) {
                    BufferedImage from = null;

                    for(int i = frameIndex - 1; i >= 0; --i) {
                        if (!((ImageFrame)frames.get(i)).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                            from = ((ImageFrame)frames.get(i)).getImage();
                            break;
                        }
                    }

                    ColorModel model = from.getColorModel();
                    boolean alpha = from.isAlphaPremultiplied();
                    WritableRaster raster = from.copyData((WritableRaster)null);
                    master = new BufferedImage(model, raster, alpha, (Hashtable)null);
                } else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null && (!hasBackround || frameIndex > 1)) {
                    master.createGraphics().fillRect(lastx, lasty, ((ImageFrame)frames.get(frameIndex - 1)).getWidth(), ((ImageFrame)frames.get(frameIndex - 1)).getHeight());
                }

                master.createGraphics().drawImage(image, x, y, (ImageObserver)null);
                lastx = x;
                lasty = y;
            }

            ColorModel model = master.getColorModel();
            boolean alpha = master.isAlphaPremultiplied();
            WritableRaster raster = master.copyData((WritableRaster)null);
            BufferedImage copy = new BufferedImage(model, raster, alpha, (Hashtable)null);
            frames.add(new ImageFrame(copy, disposal, image.getWidth(), image.getHeight()));
            master.flush();
            ++frameIndex;
        }
    }

    private static class ImageFrame {
        private final BufferedImage image;
        private final String disposal;
        private final int width;
        private final int height;

        public ImageFrame(BufferedImage image, String disposal, int width, int height) {
            this.image = image;
            this.disposal = disposal;
            this.width = width;
            this.height = height;
        }

        public ImageFrame(BufferedImage image) {
            this.image = image;
            this.disposal = null;
            this.width = -1;
            this.height = -1;
        }

        public BufferedImage getImage() {
            return this.image;
        }

        public String getDisposal() {
            return this.disposal;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }
}

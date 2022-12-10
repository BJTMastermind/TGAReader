package test.sample.opengl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import net.npe.tga.TGAReader;

public class Texture {
    private int id;
    private int width;
    private int height;

    public Texture(String filename) {
        BufferedImage bi;
        try {
            if(filename.endsWith(".tga")) {
                bi = fromTGA(filename);
            } else {
                bi = ImageIO.read(new File(filename));
            }
            width = bi.getWidth();
            height = bi.getHeight();

            int[] pixelsRaw = new int[width * height * 4];
            pixelsRaw = bi.getRGB(0, 0, width, height, null, 0, width);

            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);

            for(int x = 0; x < width; x++) {
                for(int y = 0; y < width; y++) {
                    int pixel = pixelsRaw[x * width + y];
                    pixels.put((byte) ((pixel >> 16) & 0xFF)); // RED
                    pixels.put((byte) ((pixel >> 8) & 0xFF));  // GREEN
                    pixels.put((byte) ((pixel) & 0xFF));       // BLUE
                    pixels.put((byte) ((pixel >> 24) & 0xFF)); // ALPHA
                }
            }
            pixels.flip();

            id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    private BufferedImage fromTGA(String filename) {
        BufferedImage output;
        try {
            byte[] buffer = Files.readAllBytes(Paths.get(filename));

            int[] pixels = TGAReader.read(buffer, TGAReader.ARGB);
            int width = TGAReader.getWidth(buffer);
            int height = TGAReader.getHeight(buffer);
            output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            output.setRGB(0, 0, width, height, pixels, 0, width);

            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

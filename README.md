# TGAReader

Targa TGA image reader and writer for Java.

## Getting Started

### 1. Add the library to your project.

- Download `TGAReader.jar` from Releases tab.
- Add `TGAReader.jar` to your project.
- Import `net.npe.tga.<TGAReader/TGAWriter>` to use them.

### 2. Create a TGA binary data buffer.

```java
byte[] buffer = Files.readAllBytes(Paths.get("test.tga"));
```

### 3. Create pixels with the RGBA byte order parameter.

ByteOrder|Order Name|Comments
---|---|---
ARGB|TGAReader.ARGB|for BufferedImage
ABGR|TGAReader.ABGR|for OpenGL Texture(GL_RGBA)

```java
byte[] buffer = ...; // Create TGA binary data buffer.
int[] pixels = TGAReader.read(buffer, TGAReader.ARGB); // Get TGA pixels.
int width = TGAReader.getWidth(buffer); // Get TGA width.
int height = TGAReader.getHeight(buffer); // Get TGA height.
```

### 4. Use created pixels in your application.

#### 4.1. OpenGL Application

Sample code to create Java OpenGL texture.

```java
public int createTGATexture() {
    int texture = 0;

    try {
        FileInputStream fis = new FileInputStream(path);
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();

        int[] pixels = TGAReader.read(buffer, TGAReader.ABGR);
        int width = TGAReader.getWidth(buffer);
        int height = TGAReader.getHeight(buffer);

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);

        gl.glEnable(GL_TEXTURE_2D);
        gl.glBindTexture(GL_TEXTURE_2D, textures[0]);
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        IntBuffer texBuffer = IntBuffer.wrap(pixels);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texBuffer);

        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        texture = textures[0];
    } catch(Exception e) {
        e.printStackTrace();
    }
    return texture;
}
```

#### 4.2. Swing Application

Sample code to create a BufferedImage.

```java
private static JLabel createTGALabel(String path) throws IOException {
    byte[] buffer = Files.readAllBytes(Paths.get(path));

    int[] pixels = TGAReader.read(buffer, TGAReader.ARGB);
    int width = TGAReader.getWidth(buffer);
    int height = TGAReader.getHeight(buffer);
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, width, height, pixels, 0, width);

    ImageIcon icon = new ImageIcon(image.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH));
    return new JLabel(icon);
}
```

For more details, please refer to the sample project [here](https://github.com/BJTMastermind/TGAReader/tree/master/samples/TGASwingBufferedImage).

## Supported
- Colormap(Indexed) Image, RGB Color Image, Grayscale Image
- Run Length Encoding
- Colormap origin offset
- Image origin(LowerLeft, LowerRight, UpperLeft, UpperRight)

## Unsupported
- Image Type 0, 32, 33
- 16bit RGB Color image
- X/Y origin offset of image

## TGAWriter
- RLE(Run Length Encoding) support
- Only RGB Color Image support
- Only UpperLeft Image origin support

### Write a tga image from BufferedImage

```java
String path = "images/Mandrill.bmp";

try {
    BufferedImage image = ImageIO.read(new File(path));
    int width = image.getWidth();
    int height = image.getHeight();
    int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

    byte[] buffer = TGAWriter.write(pixels, width, height, TGAReader.ARGB);
    FileOutputStream fos = new FileOutputStream(path.replace(".bmp", ".tga"));
    fos.write(buffer);
    fos.close();
} catch (IOException e) {
    e.printStackTrace();
}
```

For more details, please see the sample project [here](https://github.com/BJTMastermind/TGAReader/tree/master/samples/TGAConverter_BufferedImage).

Thank you for reading through. Enjoy your programming life!

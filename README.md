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

#### 4.1. OpenGL (LWJGL 3) Application

Sample code to allow Java OpenGL to render TGA texture using LWJGL 3.

```java
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
```

For more details, please refer to the sample project [here](https://github.com/BJTMastermind/TGAReader/tree/master/samples/TGAOpenGL_LWJGL/src/test/sample/opengl).

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

For more details, please refer to the sample project [here](https://github.com/BJTMastermind/TGAReader/tree/master/samples/TGASwingBufferedImage/src/test/sample/swing).

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

For more details, please see the sample project [here](https://github.com/BJTMastermind/TGAReader/tree/master/samples/TGAConverter_BufferedImage/src/test/sample/converter).

Thank you for reading through. Enjoy your programming life!

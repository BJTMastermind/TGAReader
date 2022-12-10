package test.sample.opengl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class OpenGLSample {

    public static void main(String[] args) {
        if(!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        long window = glfwCreateWindow(640, 480, "OpenGL Sample Using LWJGL 3", 0, 0);
        if(window == 0) {
            throw new IllegalStateException("Failed to create window.");
        }

        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (videoMode.width() - 640) / 2, (videoMode.height() - 480) / 2);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);

        Texture texture = new Texture("images/rgb_a_LL.tga");

        while(!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT);

            texture.bind();

            glBegin(GL_QUADS);

            glTexCoord2f(0, 0);
            glVertex2f(-0.5f, 0.5f);
            glTexCoord2f(1, 0);
            glVertex2f(0.5f, 0.5f);
            glTexCoord2f(1, 1);
            glVertex2f(0.5f, -0.5f);
            glTexCoord2f(0, 1);
            glVertex2f(-0.5f, -0.5f);

            glEnd();

            glfwSwapBuffers(window);
        }
        glfwTerminate();
    }
}

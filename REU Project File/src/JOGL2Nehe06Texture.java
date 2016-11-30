import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.swing.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;



/**
 * NeHe Lesson #6 (JOGL 2 Port): Texture
 * @author Hock-Chuan Chua
 * @version May 2012
 */
@SuppressWarnings("serial")
public class JOGL2Nehe06Texture extends GLCanvas implements GLEventListener {
   // Define constants for the top-level container
   private static String TITLE = "NeHe Lesson #6: Texture";
   private static final int CANVAS_WIDTH = 320;  // width of the drawable
   private static final int CANVAS_HEIGHT = 240; // height of the drawable
   private static final int FPS = 60; // animator's target frames per second
   static PerlinNoises white = new PerlinNoises(800,800);
   /** The entry main() method to setup the top-level container and animator */
   public static void main(String[] args) {
      // Run the GUI codes in the event-dispatching thread for thread safety
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            // Create the OpenGL rendering canvas
            GLCanvas canvas = new JOGL2Nehe06Texture();
            canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

            // Create a animator that drives canvas' display() at the specified FPS. 
            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
            
            // Create the top-level container
            final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
            frame.getContentPane().add(canvas);
            frame.addWindowListener(new WindowAdapter() {
               @Override 
               public void windowClosing(WindowEvent e) {
                  // Use a dedicate thread to run the stop() to ensure that the
                  // animator stops before program exits.
                  new Thread() {
                     @Override 
                     public void run() {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                     }
                  }.start();
               }
            });
            frame.setTitle(TITLE);
            frame.pack();
            frame.setVisible(true);
            animator.start(); // start the animation loop
         }
      });
   }
   
   // Setup OpenGL Graphics Renderer
   
   private GLU glu;  // for the GL Utility
   // Rotational angle about the x, y and z axes in degrees
   private static float angleX = 0.0f;
   private static float angleY = 0.0f;
   private static float angleZ = 0.0f;
   // Rotational speed about x, y, z axes in degrees per refresh
   private static float rotateSpeedX = 0.3f;
   private static float rotateSpeedY = 0.2f;
   private static float rotateSpeedZ = 0.4f;

   // Texture
   private Texture texture;
   private String textureFileName = "images/nehe.png";
   private String textureFileType = ".png";

   // Texture image flips vertically. Shall use TextureCoords class to retrieve the
   // top, bottom, left and right coordinates.
   private float textureTop, textureBottom, textureLeft, textureRight;
   
   /** Constructor to setup the GUI for this Component */
   public JOGL2Nehe06Texture() {
      this.addGLEventListener(this);
   }
   
   // ------ Implement methods declared in GLEventListener ------

   /**
    * Called back immediately after the OpenGL context is initialized. Can be used 
    * to perform one-time initialization. Run only once.
    */
   @Override
   public void init(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
      glu = new GLU();                         // get GL Utilities
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
      //gl.glClearDepth(1.0f);      // set clear depth value to farthest
      gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
      //gl.glDepthFunc(GL.GL_LEQUAL);  // the type of depth test to do
      //gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // best perspective correction
      //gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting

      // Load texture from image
      try {
         // Create a OpenGL Texture object from (URL, mipmap, file suffix)
         // Use URL so that can read from JAR and disk file.
         texture = TextureIO.newTexture(
               getClass().getClassLoader().getResource(textureFileName), // relative to project root 
               false, textureFileType);

         // Use linear filter for texture if image is larger than the original texture
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
         // Use linear filter for texture if image is smaller than the original texture
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

         // Texture image flips vertically. Shall use TextureCoords class to retrieve
         // the top, bottom, left and right coordinates, instead of using 0.0f and 1.0f.
         white.display();
         Texture[] textures = new Texture[1];
         	try{
         		texture = AWTTextureIO.newTexture(GLProfile.getDefault(), white.savedimage, true);
         	}
         	catch(Exception e){
         	}
         	texture.enable(gl);
         	texture.bind(gl);
         	
         TextureCoords textureCoords = texture.getImageTexCoords();
         textureTop = textureCoords.top();
         textureBottom = textureCoords.bottom();
         textureLeft = textureCoords.left();
         textureRight = textureCoords.right();
      } catch (GLException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Call-back handler for window re-size event. Also called when the drawable is 
    * first set to visible.
    */
   @Override
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

      if (height == 0) height = 1;   // prevent divide by zero
      float aspect = (float)width / height;

      // Set the view port (display area) to cover the entire window
      gl.glViewport(0, 0, width, height);

      // Setup perspective projection, with aspect ratio matches viewport
      gl.glMatrixMode(GL2.GL_PROJECTION);  // choose projection matrix
      gl.glLoadIdentity();             // reset projection matrix
      glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

      // Enable the model-view transform
      gl.glMatrixMode(GL2.GL_MODELVIEW);
      gl.glLoadIdentity(); // reset
   }

   /**
    * Called back by the animator to perform rendering.
    */
   @Override
   public void display(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // clear color and depth buffers

      // ------ Render a Cube with texture ------
      gl.glLoadIdentity();  // reset the model-view matrix
      gl.glTranslatef(0.0f, 0.0f, -5.0f); // translate into the screen
      //gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
      //gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f); // rotate about the y-axis
      //gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f); // rotate about the z-axis

      // Enables this texture's target in the current GL context's state.
      texture.enable(gl);  // same as gl.glEnable(texture.getTarget());
      // gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      // Binds this texture to the current GL context.
      texture.bind(gl);  // same as gl.glBindTexture(texture.getTarget(), texture.getTextureObject());
 
      //white.newdisplay(pointlist, canvas.getSize().width, canvas.getSize().height);   
      
          // Use linear filter for texture if image is smaller than the original texture
   /*       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
      	gl.glBegin(GL2.GL_QUADS);
      	gl.glTexCoord2f(-1f, -1f);
          gl.glVertex2f(-1f, -1f); // bottom-left of the texture and quad
          gl.glTexCoord2f(-1f, 1f);
          gl.glVertex2f(-1f, 1f);  // bottom-right of the texture and quad
          gl.glTexCoord2f(1f, 1f);
          gl.glVertex2f(1f, 1f);   // top-right of the texture and quad
          gl.glTexCoord2f(1f, -1f);
          gl.glVertex2f(1f, -1f); 
          gl.glEnd();
          gl.glFlush();*/
      
      
      gl.glBegin(GL2.GL_QUADS);

      // Front Face
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f); // bottom-left of the texture and quad
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);  // bottom-right of the texture and quad
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);   // top-right of the texture and quad
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);  // top-left of the texture and quad

      // Back Face
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);
      
      // Top Face
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);
      
      // Bottom Face
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f);
      
      // Right face
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);
      
      // Left Face
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);

      gl.glEnd();

      // Disables this texture's target (e.g., GL_TEXTURE_2D) in the current GL
      // context's state.
      //texture.disable(gl);  // same as gl.glDisable(texture.getTarget());

      // Update the rotational angel after each refresh by the corresponding
      // rotational speed
      //angleX += rotateSpeedX;
      //angleY += rotateSpeedY;
      //angleZ += rotateSpeedZ;
   }

   /** 
    * Called back before the OpenGL context is destroyed. Release resource such as buffers. 
    */
   @Override
   public void dispose(GLAutoDrawable drawable) { }
}

class PerlinNoises {

	// Just a Random class object so I can fill my noise map with random directions.
	public static final Random random = new Random();

	// Width and Height of the map.
	public int width, height;
	public BufferedImage savedimage = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);
	public char[][][] noiseimg;
	// Random directions of length 1.
	private vec2[] values;

	/**
	 * Creates a noise map with specified dimensions.
	 * @param width of the noise map.
	 * @param height of the noise map.
	 */
	public PerlinNoises(int width, int height) {
		this.width = width;
		this.height = height;

		values = new vec2[(width + 1) * (height + 1)]; // Create an array to store random directions.

		for (int y = 0; y < height + 1; y++) {
			for (int x = 0; x < width + 1; x++) {
				int rot = (int) (Math.random()*359); // Random direction.

				// Store random direction of length 1 to our directions array.
				values[x + y * width] = Rotation.point(new vec2(0, 0), new vec2(0, -1), rot);
			}
		}

		// If you're wondering why "width + 1" "height + 1", it is because map looks blurry 
		// at right and bottom edges of the image without it. Try removing it, you will see.
		
	}

	public float noise(float x, float y) {

		// Grid cell coordinates in integer values.
		int gx0 = (int) (Math.floor(x)); // Top-Left
		int gy0 = (int) (Math.floor(y)); // Top-Left
		int gx1 = gx0 + 1; // Down-Right
		int gy1 = gy0 + 1; // Down-Right

		// Random directions.
		vec2 g00 = g(gx0, gy0); // Top-Left
		vec2 g10 = g(gx1, gy0); // Top-Right
		vec2 g11 = g(gx1, gy1); // Down-Right
		vec2 g01 = g(gx0, gy1); // Down-Left

		// Subtract grid cells values from the point specified.
		vec2 delta00 = new vec2(x - gx0, y - gy0); // Top-Left
		vec2 delta10 = new vec2(x - gx1, y - gy0); // Top-Right
		vec2 delta11 = new vec2(x - gx1, y - gy1); // Down-Right
		vec2 delta01 = new vec2(x - gx0, y - gy1); // Down-Left

		// Compute a dot product between random directions and corresponding delta values.
		float s = dot(g00, new vec2(delta00.x, delta00.y)); // Top-Left
		float t = dot(g10, new vec2(delta10.x, delta10.y)); // Top-Right
		float u = dot(g11, new vec2(delta11.x, delta11.y)); // Down-Right
		float v = dot(g01, new vec2(delta01.x, delta01.y)); // Down-Left

		// Compute the weights for x and y axis.
		float sx = weigh(delta00.x);
		float sy = weigh(delta00.y);
		
		// Interpolate between values.
		float a = lerp(sy, s, v); // Interpolate Top-Left(s) and Down-Left(v). We can also call this LEFT
		float b = lerp(sy, t, u); // Interpolate Top-Right(t) and Down-Right(u) We can also call this RIGHT
		float h = lerp(sx, a, b); // Interpolate LEFT(a) and RIGHT(b). We can call this height(h)
		
		h *= 3; // Multiply here so adjust contrast.
		
		// Make sure it is -1 to 1. If you don't change contrast, you don't have to do this.
		if(h > 1) h = 1;
		if(h < -1) h = -1;
		
		return h;
	}

	/**
	 * Computes a weight using S-curve function f(x) = 3 * (x * x) - 2 * (x * x * x).
	 * @param x NOT as in axis, but as a variable.
	 */
	private float weigh(float x) {
		return 3 * (x * x) - 2 * (x * x * x);
	}
	
	/**
	 * Interpolate between 2 values, using weight.
	 */
	private float lerp(float weight, float a, float b) {
		float result = a + weight * (b - a);
		return result;
	}
	
	/**
	 * Compute a dot product.
	 * Example: dot product between (a, b) and (c, d) is:
	 * a * c + b * d
	 */
	private float dot(vec2 v0, vec2 v1) {
		return (v0.x * v1.x) + (v0.y * v1.y);
	}

	/**
	 * Get the random direction.
	 */
	private vec2 g(int x, int y) {
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (x >= width) x = width;
		if (y >= height) y = height;
		return values[x + y * width];
	}

	/*public static void main(String args[]) {

		while (true) { // Hope you can click stop application from your IDE. :P

			display();

		}

	}*/

	public void display() {

		long time0 = System.nanoTime(); // Take a time stamp.
		
		int size = 32; // Perlin's noise map. (the amount of static)
		PerlinNoises noise = new PerlinNoises(size, size);
		
		int width = 100; // Width of the finished image.
		int height = 100; // Height of the finished image.

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Image to store pixel data in.

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				float xx = (float) x / width * size; // Where does the point lie in the noise space according to image space. 
				float yy = (float) y / height * size; // Where does the point lie in the noise space according to image space. 
				
				float n = (float) noise.noise(xx, yy); // Noise values from Perlin's noise.
				int col = (int) ((n + 1) * 255 / 2f); // Since noise value returned is -1 to 1, we make it so that -1 is black, and 1 is white.
				
				Color color = new Color(col, col, col); // java.AWT color to get RGB from.
				image.setRGB(x, y, color.getRGB()); // set XY image value to our generated color.
				
			}
		}

		long time1 = System.nanoTime(); // Take another time stamp.
		savedimage = image;
		String time = "It took: " + (time1 - time0) / 1000000 + "MS to generate the image."; // Make a string which says how long it took to generate the image.
		
		//JOptionPane.showMessageDialog(null, null, "Perlin Noise | " + time, JOptionPane.YES_NO_OPTION, new ImageIcon(image.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
		//JOptionPane.showMessageDialog(null, null, "Perlin Noise | " + time, JOptionPane.YES_NO_OPTION, new ImageIcon(savedimage.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
	}
	
	public void newdisplay(PointList pointlist, float widthx, float heightx) { //LIC IMAGE

		long time0 = System.nanoTime(); // Take a time stamp.
		
		int size = 128; // Perlin's noise map. (the amount of static)
		PerlinNoises noise = new PerlinNoises(size, size);
		
		int width = 700; // Width of the finished image.
		int height = 700; // Height of the finished image.

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Image to store pixel data in.
		noiseimg = new char[width][height][3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				float xx = (float) x / width * size; // Where does the point lie in the noise space according to image space. 
				float yy = (float) y / height * size; // Where does the point lie in the noise space according to image space. 
				
				float n = (float) noise.noise(xx, yy); // Noise values from Perlin's noise.
				int col = (int) ((n + 1) * 255 / 2f); // Since noise value returned is -1 to 1, we make it so that -1 is black, and 1 is white.
				
				noiseimg[x][y][0] =
				noiseimg[x][y][1] =
				noiseimg[x][y][2] = (char)col;
				
				Color color = new Color((char)col, (char)col, (char)col); // java.AWT color to get RGB from.
				image.setRGB(x, y, color.getRGB()); // set XY image value to our generated color.
				
			}
		}
		
		
		long time1 = System.nanoTime(); // Take another time stamp.
		savedimage = image;
		String time = "It took: " + (time1 - time0) / 1000000 + "MS to generate the image."; // Make a string which says how long it took to generate the image.
		
		//edit streamline
		int addall = 0;
		for(int i = 0; i < pointlist.nelems; i++){
        	Point2D temp = pointlist.point_elems[i];
        	
        	int x= (int) ((temp.getX()+1) *(widthx)/2);
        	int y= (int) ((temp.getY()-1) *(heightx)/(-2));
        	//System.out.println(x + " " +y);
        	if(!(x>700 || y>700)){
	        	Color color = new Color(savedimage.getRGB(x,y));
	        	int sum = (color.getRed()+color.getBlue()+color.getGreen())/(height*width); //n=48
	        	addall+=sum;
        	}
        }
		addall= addall/pointlist.nelems;
		
		for(int i = 0; i < pointlist.nelems; i++){
        	Point2D temp = pointlist.point_elems[i];
        	//float x=(float) 2.0f* temp.getX() / canvas.getSize().width -1.0f;
			//float y=(float) -2.0f *temp.getY() / canvas.getSize().height + 1.0f;
        	
        	//these will be representations...onto the white noise
        	
        	int x= (int) ((temp.getX()+1) *(widthx)/2);
        	int y= (int) ((temp.getY()-1) *(heightx)/(-2));
        	//System.out.println(x + " " +y);
        	if(!(x>700 || y>700)){
	        	Color color = new Color(savedimage.getRGB(x,y));
	        	int sum = (color.getRed()+color.getBlue()+color.getGreen())/(addall); //n=48
	        	Color c = new Color(sum, sum, sum);
	        	//savedimage.setRGB(x, y, Color.CYAN.getRGB());
	        	
	        	savedimage.setRGB(x, y, addall);
	        	//System.out.println(sum+"vs "+color.getRed()+" "+color.getBlue()+" "+color.getGreen());
        	}
        	/**/
        }
		
		
		//JOptionPane.showMessageDialog(null, null, "Perlin Noise | " + time, JOptionPane.YES_NO_OPTION, new ImageIcon(image.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
		//JOptionPane.showMessageDialog(null, null, "Perlin Noise | " + time, JOptionPane.YES_NO_OPTION, new ImageIcon(savedimage.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
	}
	
	public void show(){
		JOptionPane.showMessageDialog(null, null, "Perlin Noise | ", JOptionPane.YES_NO_OPTION, new ImageIcon(savedimage.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
	}
	public static class vec2 {

		public float x, y;

		/**
		 * Just holds some float values.
		 * @param x
		 * @param y
		 */
		public vec2(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return (int) x;
		}
		public int getY() {
			return (int) y;
		}
		
	}
	
	public static class Rotation {

		/** 
		 * Rotates specified point around pivot.
		 * @param pivot to rotate around.
		 * @param point to rotate around pivot.
		 * @param rotation - how many degrees to rotate.
		 * @return a new point, which was created by rotating given point around pivot by some degrees.
		 */
		public static vec2 point(vec2 pivot, vec2 point, float rotation) {
			
			float rot = (float)(1f / 180 * rotation * Math.PI);
			
			float x = point.x - pivot.x;
			float y = point.y - pivot.y;
			
			float newx = (float)(x * Math.cos(rot) - y * Math.sin(rot));
			float newy = (float)(x * Math.sin(rot) + y * Math.cos(rot));
			
			
			newx += pivot.x;
			newy += pivot.y;
			
			return new vec2(newx, newy);
		}
					
	}
	
}

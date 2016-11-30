import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.xml.soap.Text;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


public class SampleC extends JFrame implements GLEventListener {
	private static GraphicsEnvironment graphicsEnvironment;
	private static boolean isFullScreen = false;

	private static Dimension xgraphic;
	private static Point point = new Point(0,0);
	
	static GLProfile glp = GLProfile.getDefault();
	static GLCapabilities caps = new GLCapabilities(glp);
	static GLCanvas canvas = new GLCanvas(caps);
	static JFrame frame = new JFrame("Vector Field");
    
	static boolean trigger = false;
	static float px;
	static float py;
	final private int width = 800;
	final private int height = 600;
	double theta = 0;
	double s = 0;
	double c = 0;
	
	int NPN = 64;
	int NMESH = 100;
	float DM = ((float) (1.0/(NMESH-1.0)));
	static int NPIX = 512;
	double SCALE = 20.0;

	int    iframe = 0; 
	int    Npat   = 32;
	int[] textures = new int[Npat];
	int    alpha  = (int) (0.12*255);
	float  sa;
	float  tmax   = (float) (NPIX/(SCALE*NPN));
	float  dmax   = (float) (SCALE/NPIX);
	int box = 0;
	
	static ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	static ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8, 0}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	GLU glu = new GLU();
	private Texture texture;
	private String textureFileName = "images/crate.png";
	   private String textureFileType = ".png";
	   
	@Override
	public void display(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		//Added
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		int   i, j; 
		   float x1, x2, y; 
		   px = 0; py = 0;
		   sa = (float) (0.010*Math.cos(iframe*2.0*Math.PI/200.0));
		   for (i = 0; i < NMESH-1; i++) {
		      x1 = DM*i; x2 = x1 + DM;
		      gl.glBegin(GL2.GL_QUAD_STRIP);
		      for (j = 0; j < NMESH; j++) {
		          y = DM*j;
		          gl.glTexCoord2f(x1, y);
		          getDP(x1, y); 
		          gl.glVertex2f(px, py);

		          gl.glTexCoord2f(x2, y); 
		          getDP(x2, y); 
		          gl.glVertex2f(px, py);
		      }
		      gl.glEnd();
		   }
		   iframe = iframe + 1;
		   
		   //The things that change. An update draw.
		   /**/gl.glEnable(GL2.GL_BLEND); 
		   gl.glCallList(iframe % Npat + 1);
		   gl.glBegin(GL2.GL_QUAD_STRIP);
			   gl.glTexCoord2f(0.0f,  0.0f);  
			   gl.glVertex2f(0, 0);
			   gl.glTexCoord2f( 0.0f,  tmax); 
			   gl.glVertex2f(0, 1);
			   gl.glTexCoord2f(tmax,  0.0f);  
			   gl.glVertex2f(1, 0);
			   gl.glTexCoord2f(tmax, tmax); 
			   gl.glVertex2f(1, 1);
		   gl.glEnd();
		   gl.glDisable(GL2.GL_BLEND);
		   gl.glCopyTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, 
		                    0, 0, NPIX, NPIX, 0);
		   
		   //gl.glFlush();
		   drawable.swapBuffers();
		   
	}
	/*@Override
	public void display(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		//Added
		//gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		
		   iframe = iframe + 1;
		   
		   //The things that change. An update draw.
		   gl.glEnable(GL2.GL_BLEND); 
		   gl.glCallList(iframe % Npat + 1);
		   gl.glBegin(GL2.GL_QUAD_STRIP);
			   gl.glTexCoord2f(0.0f,  0.0f);  
			   gl.glVertex2f(0, 0);
			   gl.glTexCoord2f( 0.0f,  tmax); 
			   gl.glVertex2f(0, 1);
			   gl.glTexCoord2f(tmax,  0.0f);  
			   gl.glVertex2f(1, 0);
			   gl.glTexCoord2f(tmax, tmax); 
			   gl.glVertex2f(1, 1);
		   gl.glEnd();
		   gl.glDisable(GL2.GL_BLEND);
		   gl.glCopyTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, 
		                    0, 0, NPIX, NPIX, 0);
		   
		   //gl.glFlush();
		   drawable.swapBuffers();
		   
	}*/
	
	void getDP(float x, float y) 
	{
	   float dx, dy, vx, vy, r;

	   dx = (float) (x - 0.5);         
	   dy = (float) (y - 0.5); 
	   r  = dx*dx + dy*dy; 
	   if (r < 0.0001) r = (float) 0.0001;
	   vx = (float) (sa*dx/r + 0.02);  
	   vy = sa*dy/r;
	   r  = vx*vx + vy*vy;
	   if (r > dmax*dmax) { 
	      r  = (float) Math.sqrt(r); 
	      vx *= dmax/r; 
	      vy *= dmax/r; 
	   }
	   px = x + vx;         
	   py = y + vy;
	}
	
	void makePatterns(GLAutoDrawable drawable) 
	{ 
	   GL2 gl = drawable.getGL().getGL2();
	   int[] lut = new int[256];
	   int[][] phase = new int[NPN][NPN];
	   //int[][][] pat = new int[NPN][NPN][4]; //WHAT IS GLUBYTE?? Besides an array. Could be unsignedval 0-256
	   byte[][][] pat = new byte[NPN][NPN][4];
	   int i, j, k, t;
	    
	   for (i = 0; i < 256; i++) lut[i] = i < 127 ? 0 : 255;
	   for (i = 0; i < NPN; i++)
	   for (j = 0; j < NPN; j++) phase[i][j] = (int) (Math.random() % 256); 
	   
	   box = gl.glGenLists(Npat);
	   for (k = 0; k < Npat; k++) {
	      t = k*256/Npat;
	      for (i = 0; i < NPN; i++) 
	      for (j = 0; j < NPN; j++) {
	          pat[i][j][0] =
	          pat[i][j][1] =
	          pat[i][j][2] = (byte) lut[(t + phase[i][j]) % 255];
	          pat[i][j][3] = (byte) alpha;
	      }
	      
	      
		  byte[] flat = new byte[NPN*NPN*4];
		  for(int x = 0; x< NPN; x++)
			  for(int y = 0; y< NPN; y++)
				  for(int z = 0; z<4; z++){
					  int index = (x*NPN+y)*4+z;
					  flat[index] = pat[x][y][z];
				  }
		  
		  gl.glNewList(k + 1, GL2.GL_COMPILE);
		  //ByteBuffer a = ByteBuffer.wrap(flat);
		    try {
		    	BufferedImage imag=ImageIO.read(new ByteArrayInputStream(flat));
		    	
		    	//ByteBuffer textureBuffer = ByteBuffer.convertImageData(imag,texture); 
		    	//Texture texture = AWTTextureIO.newTexture(GLProfile.get(GLProfile.GL2), imag, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  ByteBuffer a = null;
		  a = ByteBuffer.allocateDirect(flat.length);
		  a.order(ByteOrder.nativeOrder());
		  a = ByteBuffer.wrap(flat);
		  
		  //gl.glGenTextures(textures.length, textures, 0); int textureID = textures[k];  // bind this texture // 
		  //gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); 
		  //a.put(flat, 0, flat.length);
		  	
		      gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, get2Fold(4), get2Fold(NPN), get2Fold(NPN), 0, //Draws a texture
		    		  GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, a);
		      gl.glEndList(); //Stores the image in a list
	      
	      
	   }
	}
	private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    } 
	static public BufferedImage loadImage(String resourceName)  throws IOException { 
		BufferedImage bufferedImage = null;  
		try { bufferedImage = ImageIO.read(new File(resourceName)); } 
		catch (Exception e) { // file not found or bad format maybe  // do something good 
		} 
		if (bufferedImage != null) { // Flip Image // 
		AffineTransform tx = AffineTransform.getScaleInstance(1, - 1); 
		tx.translate(0, -bufferedImage.getHeight(null)); 
		AffineTransformOp op = new AffineTransformOp(tx,  AffineTransformOp.TYPE_NEAREST_NEIGHBOR); bufferedImage = op.filter(bufferedImage, null); 
		} 
		return bufferedImage; 
		}
	/*public static ByteBuffer convertImageData(BufferedImage bufferedImage) throws Exception {
		ByteBuffer imageBuffer = null; 
		try { WritableRaster raster; BufferedImage texImage;  
		int texWidth = 2; int texHeight = 2;  
		while (texWidth < bufferedImage.getWidth()) { texWidth *= 2; } 
		while (texHeight < bufferedImage.getHeight()) { texHeight *= 2; }  
		if (bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth,  texHeight, 4, null); 
			texImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable()); } 
		else { raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth,  texHeight, 3, null); 
		texImage = new BufferedImage(glColorModel, raster, false, new Hashtable()); }  
		texImage.getGraphics().drawImage(bufferedImage, 0, 0, null);  
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();  
		imageBuffer = ByteBuffer.allocateDirect(data.length); 
		imageBuffer.order(ByteOrder.nativeOrder()); imageBuffer.put(data, 0, data.length); } 
		catch (Exception e) { throw new Exception("Unable to convert data  for texture " + e); } 
		return imageBuffer; }*/

	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		//drawable.getGL().setSwapInterval(1);
		GL2 gl = drawable.getGL().getGL2();
		//gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		//gl.glClearColor(1f, 0f, 0f, 0f);
		//gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glViewport(0, 0,  NPIX,  NPIX);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity(); 
		gl.glTranslatef(-1.0f, -1.0f, 0.0f); 
		gl.glScalef(2.0f, 2.0f, 1.0f);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, 
				GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT); 
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, 
				GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT); 
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, 
				GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);//Use linear filter for texture when given texture is larger
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, 
				GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);//When texture is small
		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, 
				GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glShadeModel(GL2.GL_FLAT);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		if(!trigger){
			makePatterns(drawable);
			trigger = true;
		}
		
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
	         TextureCoords textureCoords = texture.getImageTexCoords();
	      } catch (GLException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height  ) {
		// TODO Auto-generated method stub
		
		
		
	}
	
	public static void main(String[] args){
		
		caps.setDoubleBuffered(true);
        frame.setSize(NPIX, NPIX);
        //canvas.setSize(NPIX, NPIX);
        frame.add(canvas);
        frame.setVisible(true);
        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        canvas.addGLEventListener(new SampleC());
        
        Animator animator = new Animator(canvas);
        //animator.setUpdateFPSFrames(50, null);

        animator.start();
	}
	public SampleC() {
		//WORKING g4 code
		/*GLProfile gip = GLProfile.get(GLProfile.GL4);
		GLCapabilities caps = new GLCapabilities(gip);
		GLCanvas canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);
		
		         this.setName("Minimal OpenGL");
		  
		         this.setName("Minimal OpenGL");
		         this.getContentPane().add(canvas);
		         this.setSize(width, height);
		         this.setLocationRelativeTo(null);
		         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		         this.setVisible(true);
		         this.setResizable(false);
		         canvas.requestFocusInWindow();*/
		
	}
	
	public void play() {
		 
	}

}

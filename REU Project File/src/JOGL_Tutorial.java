import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;


public class JOGL_Tutorial extends JFrame implements GLEventListener {
	private static GraphicsEnvironment graphicsEnvironment;
	private static boolean isFullScreen = false;
	public static DisplayMode dm, dm_old;
	private static Dimension xgraphic;
	private static Point point = new Point(0,0);
	final private int width = 800;
	final private int height = 600;
	GLU glu = new GLU();
	
	@Override
	public void display(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		GL4 gl = arg0.getGL().getGL4();
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		gl.glFlush();
		
		GL4 gl2 = arg0.getGL().getGL4();
		
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height  ) {
		// TODO Auto-generated method stub
		
		GL4 gl = drawable.getGL().getGL4();
		
		if(height <= 0)
			height = 1;
		final float h = (float)width/(float)height;
		gl.glViewport(0, 0, width, height);
		//gl.glMatrixMode(GL4.gl_PROJECTION);
		
		
	}
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
				//set opGL
				GLProfile.initSingleton();
				JOGL_Tutorial game = new JOGL_Tutorial();
				game.play();
				
				
	
				/*Frame frame = new Frame();
				frame.setSize(500,500);
				frame.add(canvas);
				frame.setVisible(true);
				frame.addWindowListener(new WindowAdapter(){
					public void WindowClosing(WindowEvent e){
						System.exit(0);
					}
				});*/
				
				
				
			/**/	final GLProfile profile = GLProfile.get(GLProfile.GL2);
				GLCapabilities capabilities = new GLCapabilities(profile);
				
				//Canvas
				final GLCanvas glcanvas = new GLCanvas(capabilities);
				JOGL_Tutorial r = new JOGL_Tutorial();
				glcanvas.addGLEventListener(r);
				glcanvas.setSize(400,400);
				
				final FPSAnimator animator = new FPSAnimator(glcanvas, 300, true);
				final JFrame frame = new JFrame("nehe: Lesosn 1");
				frame.getContentPane().add(glcanvas);
				
				frame.addWindowListener(null);
				frame.addWindowListener(new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						if(animator.isStarted())
							animator.stop();
						System.exit(0);
					}
				});
				
				frame.setSize(frame.getContentPane().getPreferredSize());
				
				graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
				dm_old = devices[0].getDisplayMode();
				dm = dm_old;
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int windowX = Math.max(0, (screenSize.width - frame.getWidth()) /2);
				int windowY = Math.max(0, (screenSize.height - frame.getHeight()) /2);
				frame.setLocation(windowX, windowY);
				
				frame.setVisible(true);
		
	}
	public JOGL_Tutorial() {
		GLProfile gip = GLProfile.get(GLProfile.GL4);
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
		         canvas.requestFocusInWindow();
		     }
	public void play() {
		 
	}

}

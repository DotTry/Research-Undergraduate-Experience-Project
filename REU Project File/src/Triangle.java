import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.awt.event.MouseEvent;
import javax.swing.JFrame;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;


public class Triangle extends JFrame implements GLEventListener{
	private static GraphicsEnvironment graphicsEnvironment;
	private static boolean isFullScreen = false;
	public static DisplayMode dm, dm_old;
	private static Dimension xgraphic;
	private static Point point = new Point(0,0);
	final private int width = 800; //Screen width
	final private int height = 600;
	static boolean trigger;
	static float DEG2RAD = (float) (3.14159/180.0);
	double theta = 0;
	double s = 0;
	double c = 0;
	
	GLU glu = new GLU();
	
	@Override
	public void display(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		update();
		render(arg0);
		
		
	}
	
	private void update(){
		theta += 0.01;
	    s = Math.sin(theta);
	    c = Math.cos(theta);
	}
	
	private void render(GLAutoDrawable drawable){
		//GL2 gl = drawable.getGL().getGL2();
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
	    // draw a triangle filling the window
	   /* gl.glBegin(GL.GL_TRIANGLES);
	    gl.glColor3f(1, 0, 0);
	    gl.glVertex2f(-1, -1);
	    gl.glColor3f(0, 1, 0);
	    gl.glVertex2f(0, 1);
	    gl.glColor3f(0, 0, 1);
	    gl.glVertex2f(1, -1);
	    gl.glEnd();*/
		
		/*	//Animated Triangle
			gl.glBegin(GL.GL_TRIANGLES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex2d(-c, -c);
			gl.glColor3f(0, 1, 0);
			gl.glVertex2d(0, c);
			gl.glColor3f(0, 0, 1);
			gl.glVertex2d(s, -s);
			gl.glEnd();*/
		
	    float a = -.9f, b=.0f;
	    float x1=.9f,y1=0.0f;
	    gl.glBegin(GL2.GL_LINES);
	    gl.glVertex2f(a, b);
	    gl.glVertex2f(x1, y1);
	    
	    gl.glVertex2f(b, a);
	    gl.glVertex2f(y1, x1);
	    gl.glEnd();
	    
	    
		//PLOT SAMPLE POINTS. nX*nY = row*col. Width&Height range [-.9,.9]
	    float width = 2f;
	    float x_int = width/(30-1);
	    for(float y = -.9f; y <= .9f ; y+= x_int)
			for(float x = -.9f; x <= .9f ; x+= x_int){
				float radiusX = (float) .01f; float radiusY = (float) .1f;
			    int i;
			    int triangleAmount = 20; //# of triangles used to draw circle
			    float twicePi = (float) (2.0f * Math.PI);
			    gl.glBegin(GL2.GL_TRIANGLE_FAN);
			    gl.glVertex2f(x, y); // center of circle
				for(i = 0; i <= triangleAmount;i++) { 
					gl.glVertex2f(
				            (float)(x + (radiusX * Math.cos(i *  twicePi / triangleAmount))), 
					    (float)(y + (radiusX * Math.sin(i * twicePi / triangleAmount)))
					);
				}
				gl.glEnd();
				
			}
	    
	    
	    gl.glFlush();
	    
		
		   
		/*final com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
		gl.glBegin (GL2.GL_LINES);
		//static field
		gl.glVertex3f(0.50f,-0.50f,0);
		gl.glVertex3f(-0.50f,0.50f,0);
		gl.glEnd();*/
	}
	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		drawable.getGL().setSwapInterval(1);
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height  ) {
		// TODO Auto-generated method stub
		float radiusX = (float) 0; float radiusY = (float) 0;
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		int i;
		 
		   gl.glBegin(GL2.GL_LINE_LOOP);
		   gl.glColor3f(1, 1, 0);
		 
		   for(i=0;i<360;i++)
		   {
		      float rad = i*DEG2RAD;
		      gl.glVertex2f((float)Math.cos(rad)*radiusX*100.0f+200.0f,
		    		  (float)Math.sin(rad)*radiusY*100.0f+200.0f);
		   }
		 
		   gl.glEnd();
		   gl.glFlush();
		   
	}
	
	public static void main(String[] args){
		GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);

        Frame frame = new Frame("AWT Window Test");
        frame.setSize(500, 500);
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
        Triangle t = new Triangle();
        //canvas.addGLEventListener(new Triangle());
        
        //Animator animator = new Animator(canvas);
        //animator.add(canvas);
        //animator.start();
		
	}
	
	public void add(int i){
		i += 1;
	}
	public Triangle() {
		int[][][] pat = new int[4][5][6];
		int index = 0;
		for(int i = 0; i< 4; i++)
			  for(int j = 0; j< 5; j++)
				  for(int k = 0; k<6; k++){
					  //int index = x*NPN*4 + y*4 +z;
					  //int index = x+NPN*(y +NPN *z);
					  //int index = (z * NPN * NPN) + (y * NPN) + x;
					  pat[i][j][k] = (int) index++;
				  }
		      
		int[] flat = new int[4*5*6];
		  for(int x = 0; x< 4; x++)
			  for(int y = 0; y< 5; y++)
				  for(int z = 0; z<6; z++){
					  
					  //int n = x*4*6 + y*6 +z;
					  int n = (x*5+y)*6+z;
					  
					  //int index = x+NPN*(y +NPN *z);
					  //int n = (z * 4 * 5) + (y * 4) + x;
					  flat[n] = pat[x][y][z];
				  }
		  for(int a : flat){
			  System.out.println(a);
		  }
		  System.out.println(flat.length);
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
	
	public class Handlerclass implements MouseListener{
    	@Override
		public void mouseClicked(MouseEvent e) {
		    double x=(double)e.getX();
		    double y=(double)e.getY();
		    System.out.println(x+","+y);//these co-ords are relative to the component
		  //  x0 = x;
		  //  y0 = y;
		    

		    trigger = true;
		   // repaint();
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseWheelMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    }
	
	public void play() {
		 
	}

}

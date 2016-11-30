
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;

public class TestApp implements GLEventListener
{
    private GLU glu;

    public static void main(String[] args)
    {
        GLProfile.initSingleton();

        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        //GLCanvas canvas = new GLCanvas(caps);
        GLJPanel canvas = new GLJPanel(caps);

        TestApp testJoglApp = new TestApp();
        canvas.addGLEventListener(testJoglApp);

        JFrame frame = new JFrame("AWT Window Test");
        Container pane = frame.getContentPane();
        pane.add(canvas, BorderLayout.CENTER);
        
        
        JPanel p = new JPanel();
        p.setBackground(Color.YELLOW);
        
        JButton test = new JButton("Test");
        JLabel lab = new JLabel("Test Label");
        
        p.add(test);
        p.add(lab);
        frame.add(p, BorderLayout.SOUTH);
        //frame.add(canvas);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        frame.setSize(800, 640);
        frame.setVisible(true);
        
        double[][] entry = new double[2][2]; 
         double[][] scale = new double[2][2]; 
          double[][] transf = new double[2][2];
     	 double[][] transp = new double[2][2]; 
        transf[0][0] = 1;
        transf[0][1] = 2;
        transf[1][0] = 3;
        transf[1][1] = 4;
        scale[0][0] = 1;
        scale[0][1] = 2;
        scale[1][0] = 3;
        scale[1][1] = 4;
	  	
	  	
        entry[0][0] = transf[0][0]*scale[0][0]+transf[0][1]*scale[1][0];
        entry[0][1] = transf[0][0]*scale[0][1]+transf[0][1]*scale[1][1];
        entry[1][0] = transf[1][0]*scale[0][0]+transf[1][1]*scale[1][0];
        entry[1][1] = transf[1][0]*scale[0][1]+transf[1][1]*scale[1][1];
		System.out.println(entry[0][0]+" "+entry[0][1]+" "+entry[1][0]+" "+entry[1][1]+" ");
    }

    public void dispose1(GLAutoDrawable drawable)
    {
    }

    public void init(GLAutoDrawable drawable)
    {
        glu = new GLU();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        float aspect = width / height;
        glu.gluPerspective(50.0, aspect, 1.0, 100.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(
                0.0, 0.0, 10.0,
                0.0, 0.0, 0.0,
                0.0, 1.0, 0.0);
        
        
    }

    public void display(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glPushMatrix();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        float z = -5.0f;
        gl.glTranslatef(0.0f, 0.0f, z);

        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex2f(0, 1);
        gl.glVertex2f(1, -1);
        gl.glVertex2f(-1, -1);
        gl.glEnd();

        gl.glPopMatrix();
        
        
    }


	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}


}
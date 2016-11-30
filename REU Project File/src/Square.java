import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import com.jogamp.opengl.glu.GLU;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

//import com.jogamp.newt.event.MouseEvent;
//import com.jogamp.newt.event.MouseListener;
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
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;


public class Square extends JFrame implements GLEventListener{
	private static GraphicsEnvironment graphicsEnvironment;
	private static boolean isFullScreen = false;
	public static DisplayMode dm, dm_old;
	private static Dimension xgraphic;
	private static Point point = new Point(0,0);
	final private int width = 700; //Screen width
	final private int height = 700;
	static boolean trigger;
	static float DEG2RAD = (float) (3.14159/180.0);
	static JFrame f = new JFrame("AWT Window Test");
	static JFrame f2 = new JFrame("Input Matrix(Rotation)");
	static JTextField t1 = new JTextField("", 2);
	static JTextField t2 = new JTextField("", 2);
	static JTextField t3 = new JTextField("", 2);
	static JTextField t4 = new JTextField("", 2);
	static JTextField s1 = new JTextField("X", 2);
	static JTextField s2 = new JTextField("Y", 2);
	static GLProfile glp = GLProfile.getDefault();
	static GLCapabilities caps = new GLCapabilities(glp);
	static GLCanvas canvas = new GLCanvas(caps);
	static float rotation;
	static JTextField a1 = new JTextField(3);
	static JTextField b1 = new JTextField(3);
	static JPanel in;
	static boolean vectormode;
	static boolean meshmode;
	static boolean licmode;
	static Texture textures;
	
	private Point pickPoint = new Point();
	Point start;
	Point end;
	float x1Reg, y1Reg, x2Reg, y2Reg;
	float x0,y0 = -1;
    static Matrix2x2 mat = new Matrix2x2(0);
    static int typeMat;
    static SingularElemList list = new SingularElemList();
    static RegularElemList reglist = new RegularElemList();
    static PointList pointlist = new PointList();
    static MeshList meshlist = new MeshList();
    static PerlinNoise white = new PerlinNoise(800,800);
    static int mode; //0-singular, 1-regular(drag vector)
    static boolean selected;
    static SingularElem selectedElem;
    static SingularElem selectedElem2;
    static JRadioButton vecButton;
    static JRadioButton meshButton;
    static JRadioButton licButton;
    static JRadioButton streamButton;
    
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
		
	    
		//PLOT SAMPLE POINTS. nX*nY = row*col. Width&Height range [-.9,.9]
		//plotStream(drawable, 0.8f,0.8f,60);
		//plotStream(drawable, -0.8f,0.8f,60);
		//plotStream(drawable, 0.8f,-0.8f,60);
		//plotStream(drawable, -0.8f,-0.8f,60);
		//plotStream(drawable, 0.1f,0.1f,60);
		//plotStream(drawable, 0.2f,0.2f,60);
	    float width = 2f;
	    float x_int = width/(50-1);
	    for(float y = -.9f; y <= .9f ; y+= x_int)
			for(float x = -.9f; x <= .9f ; x+= x_int){
				float radiusX = (float) .01f; float radiusY = (float) .1f;
			    int i;
			    int triangleAmount = 20; //# of triangles used to draw circle
			    float twicePi = (float) (2.0f * Math.PI);
			    gl.glBegin(GL2.GL_TRIANGLE_FAN);
			    gl.glColor3f(1, 0, 1);
			    gl.glVertex2f(x, y); // center of circle
				for(i = 0; i <= triangleAmount;i++) {
				//	gl.glColor3f(1, 0, 1);
					gl.glVertex2f(
				            (float)(x + (radiusX * Math.cos(i *  twicePi / triangleAmount))), 
					    (float)(y + (radiusX * Math.sin(i * twicePi / triangleAmount)))
					);
				}
				
				

				
				//VECTORIZATION(While doing sample points)
				if(vecButton.isSelected())
					plotVector(drawable, x, y);
				gl.glEnd();
				
				//TRIANGULATION i,i+(n+1),i+nx / i,i+1,i+(nx+1). Correction: i,i+9
				if(x<.9-x_int && y<.9-x_int){
					Mesh triangle1 = new Mesh(x,y,0);
					triangle1.setVert(x+x_int, y, x, y+x_int);
					meshlist.add(triangle1);
					Mesh triangle2 = new Mesh(x+x_int,y+x_int,1);
					triangle2.setVert(x+x_int, y, x, y+x_int);
					meshlist.add(triangle2);
				}
				if(meshButton.isSelected()){
					if(x<.9-x_int && y<.9-x_int){
						gl.glBegin(GL2.GL_LINE_STRIP);
				        gl.glColor3f(1, 1, 12);
				        gl.glVertex2f(x, y);
				        gl.glVertex2f(x+x_int, y+x_int);
				        gl.glVertex2f(x, y);
				        gl.glVertex2f(x+x_int, y);
				        gl.glVertex2f(x, y);
				        gl.glVertex2f(x, y+x_int);
				        
				        gl.glVertex2f(x, y+x_int);
				        gl.glVertex2f(x+x_int, y+x_int);
				        gl.glVertex2f(x+x_int, y);
				        gl.glVertex2f(x+x_int, y+x_int);
				        gl.glEnd();
					}
				}
			}
	    pointlist = new PointList();
	    //PLOT SINGULARITIES
	    for(int n = 0; n < list.nelems; n++){
        	SingularElem temp = list.singular_elems[n];
        	//	g2.fillOval(temp.x, temp.y, 10, 10);
        	int triangleAmount = 20; //# of triangles used to draw circle
		    float twicePi = (float) (2.0f * Math.PI);
		    //plotStream(drawable, temp.x,temp.y,60);
		    //if(streamButton.isSelected())
				plotStream(drawable, temp.x+0.1f,temp.y+0.1f,1000);
			//plotStream(drawable, temp.x-0.1f,temp.y+0.1f,60);
			//plotStream(drawable, temp.x+0.1f,temp.y-0.1f,60);
			//plotStream(drawable, temp.x-0.1f,temp.y-0.1f,60);
			
			//plotStream(drawable, temp.x+0.2f,temp.y+0.2f,180);
				//plotReverseStream(drawable, temp.x+0.1f,temp.y+0.1f,2000);
		    //plotReverseStream(drawable,  temp.x-0.1f,temp.y+0.1f,10);
		    //plotReverseStream(drawable,  temp.x+0.1f,temp.y-0.1f,10);
		    //plotReverseStream(drawable,  temp.x-0.1f,temp.y-0.1f,10);
			//plotReverseStream(drawable, temp.x+0.1f,temp.y+0.1f,30);
			//plotReverseStream(drawable, temp.x+0.2f,temp.y+0.2f,180);
		    gl.glColor3f(0, 1, 0);
		    gl.glBegin(GL2.GL_TRIANGLE_FAN);
		    gl.glVertex2f(temp.x, temp.y); // center of circle
			for(int i = 0; i <= triangleAmount;i++) { 
				gl.glVertex2f(
			            (float)(temp.x + (.01f * Math.cos(i *  twicePi / triangleAmount))), 
				    (float)(temp.y + (.01f * Math.sin(i * twicePi / triangleAmount)))
				);
				
			}
			gl.glEnd();
			//BOX AROUND POINT. INcorporate angle
			gl.glColor3f(1, 0, 0);
			gl.glLineWidth(2);
	        gl.glEnable(GL2.GL_LINE_SMOOTH);
			gl.glBegin( GL2.GL_LINES );
			float x1 = temp.box[0][0]; float y1 =temp.box[0][1];
			float x2 = temp.box[1][0]; float y2 =temp.box[1][1];
			float x3 = temp.box[2][0]; float y3 =temp.box[2][1];
			float x4 = temp.box[3][0]; float y4 =temp.box[3][1];
			float rot = temp.rotang;
			//Rotate it 'rot' deg
			gl.glColor3f(1, 10, 0);
			gl.glVertex2f( x1, y1);
		    gl.glVertex2f( x2,y2);
		    gl.glColor3f(1, 0, 0);
		    gl.glVertex2f( x1, y1);
		    gl.glVertex2f( x3,y3);
		    gl.glVertex2f( x2, y2);
		    gl.glVertex2f( x4,y4);
		    gl.glVertex2f( x3, y3);
		    gl.glVertex2f( x4,y4);
		    //gl.glVertex2f( temp.x, temp.y);
		    //gl.glVertex2f( temp.testx,temp.testy);
			/*gl.glVertex2f( temp.x-0.05f, temp.y+0.05f);
		    gl.glVertex2f( temp.x+.05f,temp.y+0.05f);
		    gl.glVertex2f( temp.x-0.05f, temp.y-0.05f);
		    gl.glVertex2f( temp.x+.05f,temp.y-0.05f);
		    gl.glVertex2f( temp.x-0.05f, temp.y+0.05f);
		    gl.glVertex2f( temp.x-.05f,temp.y-0.05f);
		    gl.glVertex2f( temp.x+0.05f, temp.y+0.05f);
		    gl.glVertex2f( temp.x+.05f,temp.y-0.05f);*/
		    gl.glEnd();
		    gl.glFlush();
		    gl.glDisable(GL2.GL_LINE_SMOOTH);
        }
	    //Plot Regular/attach/diverg
	    for(int n = 0; n < reglist.nelems; n++){
        	RegularElem temp = reglist.regular_elems[n];
        	plotStream(drawable, temp.x+0.1f,temp.y+0.1f,1000);
        	//	g2.fillOval(temp.x, temp.y, 10, 10);
        	int triangleAmount = 20; //# of triangles used to draw circle
		    float twicePi = (float) (2.0f * Math.PI);
		    gl.glColor3f(0, 1, 10);
		    gl.glBegin(GL2.GL_TRIANGLE_FAN);
		    gl.glVertex2f(temp.x, temp.y); // center of circle
			for(int i = 0; i <= triangleAmount;i++) { 
				gl.glVertex2f(
			            (float)(temp.x + (.02f * Math.cos(i *  twicePi / triangleAmount))), 
				    (float)(temp.y + (.02f * Math.sin(i * twicePi / triangleAmount)))
				);
				
			}
			gl.glEnd();
			float dx = temp.bx - temp.x, dy = temp.by - temp.y;
	        double angle = Math.atan2(dy, dx);
	        float len = (float) Math.sqrt(dx*dx + dy*dy);
	        len*=7;
			float x225=(float) (temp.x+dx/len+.01*Math.cos(angle+3.9f));
	        float y225=(float) (temp.y+dy/len+.01*Math.sin(angle+3.9f));
	        float x135=(float) (temp.x+dx/len+.01*Math.cos(angle+2.26f));
	        float y135=(float) (temp.y+dy/len+.01*Math.sin(angle+2.26f));
	        gl.glColor3f(0, 1, 10);
	        gl.glLineWidth(2);
	        gl.glEnable(GL2.GL_LINE_SMOOTH);
	        gl.glBegin(GL2.GL_LINE_STRIP);
	        
	        gl.glVertex2f(temp.x, temp.y);
	        gl.glVertex2f(temp.x+dx/len, temp.y+dy/len);
	        gl.glEnd();
	        
	        gl.glBegin(GL2.GL_LINES);
	        gl.glVertex2f(temp.x+dx/len, temp.y+dy/len);
	        gl.glVertex2f(x225, y225);
	        gl.glVertex2f(temp.x+dx/len, temp.y+dy/len);
	        gl.glVertex2f(x135, y135);
	        gl.glEnd();/**/
			//BOX AROUND POINT. INcorporate angle
		    gl.glFlush();
        }
	    gl.glColor3f(0, 1, 0);
	    gl.glLineWidth(1);
        gl.glDisable(GL2.GL_LINE_SMOOTH);
        
        //DRAW THE LIC IMAGE
        if(list.nelems != 0)
        	white.newdisplay(pointlist, canvas.getSize().width, canvas.getSize().height);   
        
        if(licmode){
        	try{
        		textures = AWTTextureIO.newTexture(GLProfile.getDefault(), white.savedimage, true);
        	}
        	catch(Exception e){
        	}
        	textures.enable(gl);
        	textures.bind(gl);
        	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        	gl.glLoadIdentity();
        	gl.glViewport(0, 0, 700, 700);
        	gl.glMatrixMode(GL2.GL_PROJECTION); 
        	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            // Use linear filter for texture if image is smaller than the original texture
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            float textureTop, textureBottom, textureLeft, textureRight;
            TextureCoords textureCoords = textures.getImageTexCoords();
            textureTop = textureCoords.top();
            textureBottom = textureCoords.bottom();
            textureLeft = textureCoords.left();
            textureRight = textureCoords.right();
            glu = new GLU();
            float aspect = (float)width / height;
            glu.gluPerspective(45.0, aspect, 0.1, 100.0);
            
        	/*gl.glBegin(GL2.GL_QUADS);
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
            gl.glVertex3f(-1.0f, 1.0f, 1.0f);
            gl.glEnd();
            gl.glFlush();
        }
        
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
		/*float radiusX = (float) 0; float radiusY = (float) 0;
		GL2 gl = drawable.getGL().getGL2();
		//gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
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
		   gl.glFlush();*/
		   
	}
	
	void drawArrow(GLAutoDrawable drawable, float x1, float y1, float x2, float y2) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		float dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        float len = (float) Math.sqrt(dx*dx + dy*dy);
        //Calculate standard deviation of x2,y2
      /*  float sd = (x2+y2)/2;
        sd = (float) Math.sqrt(((sd-x2)*(sd-x2)+(sd-y2)*(sd-y2))/2);
        float edistance = (float) Math.sqrt((dx-dy)*(dx-dy)/sd);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        float x, y;
        float barb = 20f;
        float rho = (float) (angle + Math.toRadians(40));*/
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glColor3f(1, 1, 0);
        gl.glVertex2f(x1, y1);
        len*=13;
        gl.glVertex2f(x1+dx/len, y1+dy/len);
        //gl.glVertex2f((x1+dx)/len, (y1+dy)/len);
        //gl.glVertex2f(x2, y2);
        gl.glEnd();
        gl.glFlush();
        
        //DRAW ARROW HEAD
         gl.glBegin(GL2.GL_LINES);
   /*     //gl.glColor3f(0.1f, 0.0f, 0.0f);
       float x225=(float) (x2+.01*Math.cos(angle+3.9f));
        float y225=(float) (y2+.01*Math.sin(angle+3.9f));
        float x135=(float) (x2+.01*Math.cos(angle+2.26f));
        float y135=(float) (y2+.01*Math.sin(angle+2.26f));
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(x2, y2);
        //gl.glVertex2f(x1+dx/len, y1+dy/len);
        gl.glVertex2f(x225, y225);
        gl.glVertex2f(x2, y2);
        //gl.glVertex2f(x1+dx/len, y1+dy/len);
        gl.glVertex2f(x135, y135);
        gl.glEnd();*/
        
        float x225=(float) (x1+dx/len+.01*Math.cos(angle+3.9f));
        float y225=(float) (y1+dy/len+.01*Math.sin(angle+3.9f));
        float x135=(float) (x1+dx/len+.01*Math.cos(angle+2.26f));
        float y135=(float) (y1+dy/len+.01*Math.sin(angle+2.26f));
        gl.glColor3f(0, 1, 0);
        //gl.glVertex2f(x2, y2);
        gl.glVertex2f(x1+dx/len, y1+dy/len);
        gl.glVertex2f(x225, y225);
        //gl.glVertex2f(x2, y2);
        gl.glVertex2f(x1+dx/len, y1+dy/len);
        gl.glVertex2f(x135, y135);
        gl.glEnd();/**/
        
        gl.glFlush();
    }
	
	public void plotVector(GLAutoDrawable drawable, float x, float y){
		if(trigger != false){
			float vx = 0;
			float vy = 0;
			
			float DistanceThreshold = 1.e-8f;
			
			//Calculate vx,vy for singular
			for(int n = 0; n < list.nelems; n++){
				SingularElem temp = list.singular_elems[n];
				float xc = temp.x;
				float yc = temp.y;
				float dx = ((float)x-xc);
				float dy = ((float)y-yc);
				float distance = (float) Math.sqrt(dx*dx+dy*dy);
				
			//	if (distance < DistanceThreshold)
			//		distance = DistanceThreshold;
				
				//float weight = (float) (0.6*Math.exp(-2.5*distance*distance));
				float weight = temp.calcWeight(distance);
				
				//double weight = 1/Math.sqrt(distance);
				
				//vx += weight*((temp.Jacobian.entry[0][0]*(dx)) + (temp.Jacobian.entry[0][1]*(dy)))/distance;
				//vy += weight*((temp.Jacobian.entry[1][0]*(dx)) + (temp.Jacobian.entry[1][1]*(dy)))/distance;
				vx += weight*((temp.Jacobian.jacobianP[0][0]*(dx)) + (temp.Jacobian.jacobianP[0][1]*(dy)))/distance;
				vy += weight*((temp.Jacobian.jacobianP[1][0]*(dx)) + (temp.Jacobian.jacobianP[1][1]*(dy)))/distance;

			}
			
			//Calculate for regular elements
/**/			for(int n = 0; n < reglist.nelems; n++){
				RegularElem temp = reglist.regular_elems[n];
				
					float xc = temp.x;
					float yc = temp.y;
					float dx = ((float)x-xc);
					float dy = ((float)y-yc);
					float distance = (float) Math.sqrt(dx*dx+dy*dy);
					float r = dx*dx+dy*dy;
					float p = (float) (-Math.sin(Math.toRadians(temp.rotang))*dx+Math.cos(Math.toRadians(temp.rotang))*dy);
					//p =(float) (-Math.sin(45)*dx+Math.cos(45)*dy);
					
				//	if (distance < DistanceThreshold)
				//		distance = DistanceThreshold;	
					float weight = (float) (0.4*Math.exp(-2.5*distance*distance));
					if(temp.type==0){//scaling+rotation
						//vx += temp.bx-temp.x;
						//vy += temp.by-temp.y;
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*0*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*0*p/r)/r;
					}
					if(temp.type==2){//ATTACHMENT
						//vx += weight;
						//vy += weight*-2*dy;
						
						/*vx+= weight*(Math.cos(Math.toRadians(temp.rotang)))-(Math.sin(Math.toRadians(temp.rotang)))*-2*p;
						vy+= weight*(Math.sin(Math.toRadians(temp.rotang)))+(Math.cos(Math.toRadians(temp.rotang)))*-2*p;*/
						
						//vx+= weight*(Math.cos(Math.toRadians(temp.rotang)))-(Math.sin(Math.toRadians(temp.rotang)))*-2*p;
						//vy+= weight*(Math.sin(Math.toRadians(temp.rotang)))+(Math.cos(Math.toRadians(temp.rotang)))*-2*p;
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-(Math.sin(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+(Math.cos(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						
					}
					if(temp.type==3){//SEPARATION/DIVERGENT
						//vx += weight;
						//vy += weight*2*dy;
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*2*p/r)/r;
						/*float tvx = (float) ((Math.cos(Math.toRadians(temp.rotang)))-Math.sin(Math.toRadians(temp.rotang))*2*p);
						float tvy = (float) ((Math.sin(Math.toRadians(temp.rotang)))+Math.cos(Math.toRadians(temp.rotang))*2*p);
						tvx = 1/r;
						tvy = tvy/r;
						vy+= tvx/r;
						vy+= tvy/r;*/
					}
			}
			drawArrow(drawable, x, y, x+vx, y+vy);
		}
	}
	
	public float calcVectorX(float x, float y){
		if(trigger != false){
			float vx = 0;
			float vy = 0;	
			float DistanceThreshold = 1.e-8f;
			//Calculate vx,vy for singular
			for(int n = 0; n < list.nelems; n++){
				SingularElem temp = list.singular_elems[n];
				float xc = temp.x;
				float yc = temp.y;
				float dx = ((float)x-xc);
				float dy = ((float)y-yc);
				float distance = (float) Math.sqrt(dx*dx+dy*dy);
				float weight = (float) (0.6*Math.exp(-2.5*distance*distance));
				vx += weight*((temp.Jacobian.jacobianP[0][0]*(dx)) + (temp.Jacobian.jacobianP[0][1]*(dy)))/distance;
				vy += weight*((temp.Jacobian.jacobianP[1][0]*(dx)) + (temp.Jacobian.jacobianP[1][1]*(dy)))/distance;

			}
			//Calculate for regular elements
/**/			for(int n = 0; n < reglist.nelems; n++){
				RegularElem temp = reglist.regular_elems[n];
				
					float xc = temp.x;
					float yc = temp.y;
					float dx = ((float)x-xc);
					float dy = ((float)y-yc);
					float distance = (float) Math.sqrt(dx*dx+dy*dy);
					float r = dx*dx+dy*dy;
					float p = (float) (-Math.sin(Math.toRadians(temp.rotang))*dx+Math.cos(Math.toRadians(temp.rotang))*dy);
					float weight = (float) (0.4*Math.exp(-2.5*distance*distance));
					if(temp.type==0){//scaling+rotation
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*0*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*0*p/r)/r;
					}
					if(temp.type==2){//ATTACHMENT
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-(Math.sin(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+(Math.cos(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						
					}
					if(temp.type==3){//SEPARATION/DIVERGENT
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*2*p/r)/r;
					}
			}
			return vx;
		}
		return 0f;
	}
	public float calcVectorY(float x, float y){
		if(trigger != false){
			float vx = 0;
			float vy = 0;	
			float DistanceThreshold = 1.e-8f;
			//Calculate vx,vy for singular
			for(int n = 0; n < list.nelems; n++){
				SingularElem temp = list.singular_elems[n];
				float xc = temp.x;
				float yc = temp.y;
				float dx = ((float)x-xc);
				float dy = ((float)y-yc);
				float distance = (float) Math.sqrt(dx*dx+dy*dy);
				float weight = (float) (0.6*Math.exp(-2.5*distance*distance));
				vx += weight*((temp.Jacobian.jacobianP[0][0]*(dx)) + (temp.Jacobian.jacobianP[0][1]*(dy)))/distance;
				vy += weight*((temp.Jacobian.jacobianP[1][0]*(dx)) + (temp.Jacobian.jacobianP[1][1]*(dy)))/distance;

			}
			//Calculate for regular elements
/**/			for(int n = 0; n < reglist.nelems; n++){
				RegularElem temp = reglist.regular_elems[n];
				
					float xc = temp.x;
					float yc = temp.y;
					float dx = ((float)x-xc);
					float dy = ((float)y-yc);
					float distance = (float) Math.sqrt(dx*dx+dy*dy);
					float r = dx*dx+dy*dy;
					float p = (float) (-Math.sin(Math.toRadians(temp.rotang))*dx+Math.cos(Math.toRadians(temp.rotang))*dy);
					float weight = (float) (0.4*Math.exp(-2.5*distance*distance));
					if(temp.type==0){//scaling+rotation
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*0*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*0*p/r)/r;
					}
					if(temp.type==2){//ATTACHMENT
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-(Math.sin(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+(Math.cos(Math.toRadians(temp.rotang)))*-2*p/r)/r;
						
					}
					if(temp.type==3){//SEPARATION/DIVERGENT
						weight = (float) (0.9*Math.exp(-1.4*distance*distance));
						vx+= (weight*(Math.cos(Math.toRadians(temp.rotang)))/r-Math.sin(Math.toRadians(temp.rotang))*2*p/r)/r;
						vy+= (weight*(Math.sin(Math.toRadians(temp.rotang)))/r+Math.cos(Math.toRadians(temp.rotang))*2*p/r)/r;
					}
			}
			return vy;
		}
		return 0f;
	}
	
	public void plotStream(GLAutoDrawable drawable, float x, float y, int t){ //start at the origin, then keep on stepping an calc new x,y
		if(trigger != false && t != 0 && (x < 1 && y < 1) && (x > -1 && y > -1)){
			float vx = 0; vx = calcVectorX(x, y);
			float vy = 0; vy = calcVectorY(x, y);
			float stepsize = 2.3f; //h
			Point2D.Float temp = new Point2D.Float(x,y);
			//if(t % 10 == 0 && !(pointlist.contain(temp)))
			if(!(pointlist.contain(temp)))
				pointlist.add(temp);

			//drawArrow(drawable, x, y, x+vx, y+vy);
			GL2 gl = drawable.getGL().getGL2();
	        float len = (float) Math.sqrt(vx*vx + vy*vy);
	        float x1 = calcVectorX(x, y); float y1 = calcVectorY(x, y);
	        float x2 = calcVectorX(x+(1/2)*x1*stepsize, y+(1/2)*y1*stepsize); float y2 = calcVectorY(x+(1/2)*x1*stepsize, y+(1/2)*y1*stepsize);
	        float x3 = calcVectorX(x+(1/2)*x2*stepsize, y+(1/2)*y2*stepsize); float y3 = calcVectorY(x+(1/2)*x2*stepsize, y+(1/2)*y2*stepsize);
	        float x4 = calcVectorX(x+x3*stepsize, y+y3*stepsize); float y4 = calcVectorY(x+x3*stepsize, y+y3*stepsize);
	        float sumx = (x1+2*x2 +2*x3+x4)/6; float sumy = (y1+2*y2 +2*y3+y4)/6;
	        /*
	        gl.glLineWidth(2);
	        gl.glEnable(GL2.GL_LINE_SMOOTH);
			gl.glBegin( GL2.GL_LINES );
			gl.glBegin(GL2.GL_LINE_STRIP);
		    gl.glColor3f(1, 0, 1);
		    gl.glVertex2f(x, y);
		    len*=250; //normalized length of each step
		    gl.glVertex2f(x+sumx/len, y+sumy/len);
		        //gl.glVertex2f((x1+dx)/len, (y1+dy)/len);
		        //gl.glVertex2f(x2, y2);
		    gl.glEnd();
		    gl.glFlush();*/
		    
			plotStream(drawable, x+sumx/len,y+sumy/len,--t);
			gl.glDisable(GL2.GL_LINE_SMOOTH);
			gl.glFlush();
		}
	}
	
	public void plotStream2(GLAutoDrawable drawable, float x, float y, int t){ //start at the origin, then keep on stepping an calc new x,y
		if(trigger != false && t != 0 && (x < 2 && y < 2)){
			float vx = 0; vx = calcVectorX(x, y);
			float vy = 0; vy = calcVectorY(x, y);
			float stepsize = 1.3f; //h
			Point2D.Float temp = new Point2D.Float(x,y);
			if(t % 50 == 0)
				pointlist.add(temp);
			
			//drawArrow(drawable, x, y, x+vx, y+vy);
			GL2 gl = drawable.getGL().getGL2();
	        float len = (float) Math.sqrt(vx*vx + vy*vy);
	        float x1 = calcVectorX(x, y); float y1 = calcVectorY(x, y);
	        float x2 = calcVectorX(x+(stepsize/2), y+(1/2)*y1*stepsize); float y2 = calcVectorY(x+stepsize, y+(1/2)*y1*stepsize);
	        float x3 = calcVectorX(x+(stepsize/2), y+(1/2)*y2*stepsize); float y3 = calcVectorY(x+stepsize, y+(1/2)*y2*stepsize);
	        float x4 = calcVectorX(x+(stepsize), y+y3*stepsize); float y4 = calcVectorY(x+(stepsize), y+y3*stepsize);
	        float sumx = (x1+2*x2 +2*x3+x4)/6; float sumy = (y1+2*y2 +2*y3+y4)/6;
	        gl.glLineWidth(2);
	        gl.glEnable(GL2.GL_LINE_SMOOTH);
			gl.glBegin( GL2.GL_LINES );
			gl.glBegin(GL2.GL_LINE_STRIP);
		    gl.glColor3f(1, 0, 1);
		    gl.glVertex2f(x, y);
		    len*=150;
		    gl.glVertex2f(x+sumx/len, y+sumy/len);
		        //gl.glVertex2f((x1+dx)/len, (y1+dy)/len);
		        //gl.glVertex2f(x2, y2);
		    gl.glEnd();
		    gl.glFlush();
		    
			plotStream(drawable, x+sumx/len,y+sumy/len,--t);
			gl.glDisable(GL2.GL_LINE_SMOOTH);
			gl.glFlush();
		}
	}
	
	public void plotReverseStream(GLAutoDrawable drawable, float x, float y, int t){ //start at the origin, then keep on stepping an calc new x,y
		if(trigger != false && t != 0 && (x < 2 && y < 2)){
			float vx = 0; vx = calcVectorX(x, y);
			float vy = 0; vy = calcVectorY(x, y);
			float stepsize = 7f; //h
			
			
			//drawArrow(drawable, x, y, x+vx, y+vy);
			GL2 gl = drawable.getGL().getGL2();
	        float len = (float) Math.sqrt(vx*vx + vy*vy);
	        float x1 = calcVectorX(x, y); float y1 = calcVectorY(x, y);
	        float x2 = calcVectorX(x+(stepsize/2), y+(1/2)*y1*stepsize); float y2 = calcVectorY(x+stepsize, y+(1/2)*y1*stepsize);
	        float x3 = calcVectorX(x+(stepsize/2), y+(1/2)*y2*stepsize); float y3 = calcVectorY(x+stepsize, y+(1/2)*y2*stepsize);
	        float x4 = calcVectorX(x+(stepsize), y+y3*stepsize); float y4 = calcVectorY(x+(stepsize), y+y3*stepsize);
	        float sumx = (x1+2*x2 +2*x3+x4)/6; float sumy = (y1+2*y2 +2*y3+y4)/6;
	        gl.glLineWidth(2);
	        gl.glEnable(GL2.GL_LINE_SMOOTH);
			gl.glBegin( GL2.GL_LINES );
			gl.glBegin(GL2.GL_LINE_STRIP);
		    gl.glColor3f(1, 0, 1);
		    gl.glVertex2f(x, y);
		    len*=150;
		    gl.glVertex2f(x-sumx/len, y-sumy/len);
		        //gl.glVertex2f((x1+dx)/len, (y1+dy)/len);
		        //gl.glVertex2f(x2, y2);
		    gl.glEnd();
		    gl.glFlush();
		    
			plotReverseStream(drawable, x-sumx/len,y-sumy/len,--t);
			gl.glDisable(GL2.GL_LINE_SMOOTH);
		}
	}
	
	public static void main(String[] args){
		//GLProfile glp = GLProfile.getDefault();
        //GLCapabilities caps = new GLCapabilities(glp);
        //GLCanvas canvas = new GLCanvas(caps);

        //Frame frame = new Frame("AWT Window Test");
		Container pane = f.getContentPane();
        pane.add(canvas, BorderLayout.CENTER);
        Container pane2 = f2.getContentPane();
        f2.setLayout(new GridLayout(0,2));
        //pane2.add(canvas, BorderLayout.CENTER);
        JPanel p2 = new JPanel(new GridLayout()); //p2.setBorder(new EmptyBorder(2, 3, 2, 3)); //p2.setLayout(new FlowLayout());
        t1.setHorizontalAlignment(JTextField.CENTER); f2.add(t1);
        t2.setHorizontalAlignment(JTextField.CENTER); f2.add(t2);
        t3.setHorizontalAlignment(JTextField.CENTER); f2.add(t3);
        t4.setHorizontalAlignment(JTextField.CENTER); f2.add(t4);
        s1.setHorizontalAlignment(JTextField.CENTER); s2.setHorizontalAlignment(JTextField.CENTER);
        JButton ok = new JButton("OK");
        class ok implements ActionListener{
			public void actionPerformed (ActionEvent e){
				if(!(t1.getText().isEmpty() || t2.getText().isEmpty() || t3.getText().isEmpty() || t4.getText().isEmpty())){
					SingularElem temp = new SingularElem(0);
					//Change jacobian, xy. add onto list.
					temp.set(Float.valueOf(s1.getText()), Float.valueOf(s2.getText()));
					temp.Jacobian.entry[0][0] = Float.valueOf(t1.getText());
					temp.Jacobian.entry[0][1] = Float.valueOf(t2.getText());
					temp.Jacobian.entry[1][0] = Float.valueOf(t3.getText());
					temp.Jacobian.entry[1][1] = Float.valueOf(t4.getText());
					temp.Jacobian.setScale(1,1);
					temp.setRotation((float)0);
					list.add(temp);
					trigger = true;
				}

			}
		}
        ok.addActionListener(new ok()); //p2.add(ok); //f2.add(p2);
        //f2.add(p2, BorderLayout.CENTER);
        f2.add(ok);
        f2.add(new JLabel(""));
        f2.add(s1);  f2.add(s2);
        
        f2.setSize(140, 140);
        //f2.setVisible(true);
        
        
        ////
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new GridLayout(0,6,10,10));
        JButton source = new JButton("Source");
        class source implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				typeMat = 0;
				mat = new Matrix2x2(typeMat);
			}
		}
		source.addActionListener(new source());
        p.add(source);
        
        JButton sink = new JButton("Sink");
        class sink implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				typeMat = 1;
				mat = new Matrix2x2(typeMat);
			}
		}
		sink.addActionListener(new sink());
        p.add(sink);
        
        JButton saddle = new JButton("Saddle");
        class saddle implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				typeMat = 3;
				mat = new Matrix2x2(typeMat);
			}
		}
		saddle.addActionListener(new saddle());
        p.add(saddle);
        
        JButton cwcenter = new JButton("CW-Center");
        class cwcenter implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				typeMat = 4;
				mat = new Matrix2x2(typeMat);
			}
		}
		cwcenter.addActionListener(new cwcenter());
        p.add(cwcenter);
        
        JButton center = new JButton("Center");
        class center implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				typeMat = 2;
				mat = new Matrix2x2(typeMat);
			}
		}
		center.addActionListener(new center());
        p.add(center);
        
        JButton reg = new JButton("Regular");
        class reg implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 1;
			}
		}
        reg.addActionListener(new reg());
        p.add(reg);
        
        JButton attach = new JButton("Attach");
        class attach implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 2;
			}
		}
        attach.addActionListener(new attach());
        p.add(attach);
        
        JButton sep = new JButton("Separat");
        class sep implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 3;
			}
		}
        sep.addActionListener(new sep());
        p.add(sep);
        
        JButton reset = new JButton("Reset");
        class reset implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 0;
				list = new SingularElemList();
				reglist = new RegularElemList();
				pointlist = new PointList();
				canvas.repaint();
				licmode=false;
			}
		}
        reset.addActionListener(new reset());
        p.add(reset);
        
        JButton rotate = new JButton("Rotate");
        class rotate implements ActionListener{
			public void actionPerformed (ActionEvent e){
				mode = 4;
			}
		}
        rotate.addActionListener(new rotate());
        p.add(rotate);
        
        JButton lic = new JButton("LIC");
        class lic implements ActionListener{
			public void actionPerformed (ActionEvent e){
				white.show();
				//licmode = true;
			}
		}
        lic.addActionListener(new lic());
        p.add(lic);
        
        
        JButton save = new JButton("Save Weight Values");
        class save implements ActionListener{
			public void actionPerformed (ActionEvent e){
				
			}
		}
        save.addActionListener(new save());
        //p.add(save);
        p.add(new JLabel(""));
        
        vecButton = new JRadioButton("Vector");
        class vecButton implements ActionListener{
			public void actionPerformed (ActionEvent e){
			}
		}
        vecButton.addActionListener(new vecButton());
        vecButton.setSelected(true);
        p.add(vecButton);
        
        meshButton = new JRadioButton("Mesh");
        class meshButton implements ActionListener{
			public void actionPerformed (ActionEvent e){
				
			}
		}
        meshButton.addActionListener(new meshButton());
        //meshButton.setSelected(true);
        p.add(meshButton);
        
        licButton = new JRadioButton("LIC");
        class licButton implements ActionListener{
			public void actionPerformed (ActionEvent e){
				licmode = true;
			}
		}
        licButton.addActionListener(new meshButton());
        p.add(licButton);
        
        streamButton = new JRadioButton("Stream");
        streamButton.setSelected(true);
        //p.add(streamButton);
        
        in = new JPanel(new FlowLayout(2));
        in.setBorder(new TitledBorder("Weights"));
        JPanel input1 = new JPanel(new BorderLayout());
        JLabel a = new JLabel(new String("\u221D:"));
        
        input1.add(a, BorderLayout.WEST);
        input1.add(a1, BorderLayout.CENTER);
        in.add(input1);
        
        JPanel input2 = new JPanel(new BorderLayout());
        JLabel b = new JLabel(new String("\u03B2:"));
        
        input2.add(b, BorderLayout.WEST);
        input2.add(b1, BorderLayout.CENTER);
        in.add(input2);
        //in.setPreferredSize(new Dimension(10,40));
        //p.add(in);
        
        JButton select = new JButton("Select MODE");
        class select implements ActionListener{
			public void actionPerformed (ActionEvent e){
				selected = true;
				mode = 5;
			}
		}
        select.addActionListener(new select());
        in.add(select);
        
        JButton confirm = new JButton("OK"); //use this button to confirm changes on textfields, change offset values, recalculate then repaint
        class confirm implements ActionListener{
			public void actionPerformed (ActionEvent e){
				selectedElem.setOffset(Float.valueOf(a1.getText()), Float.valueOf(b1.getText()));
			}
		}
        confirm.addActionListener(new confirm());
        in.add(confirm);
        
        f.add(in, BorderLayout.LINE_END);
        
        f.add(p, BorderLayout.SOUTH);
        f.pack();
        /*JPanel p2 = new JPanel();
        p2.setBackground(Color.YELLOW);
        JRadioButton aButton = new JRadioButton("Attachment");
        
        f.add(p2, BorderLayout.EAST);*/
        
        canvas.setSize(700, 700);
        f.setSize(900, 700);
        f.add(canvas);
        f.setVisible(true);
        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        canvas.addGLEventListener(new Square());
        
        
        
        Animator animator = new Animator(canvas);
        //animator.add(canvas);
        animator.start();

	}
	public Square() {
		Handlerclass handle = new Handlerclass();
		canvas.addMouseListener(handle);
	}
	
	public class Handlerclass implements MouseListener{
		
		float save1,save2;
		SingularElem rotateElem;
		boolean rotatetrigger = true;
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			//System.out.println(canvas.getSize().width + " " + canvas.getSize().height);
			float x=(float) 2.0f* e.getX() / canvas.getSize().width -1.0f;
			float y=(float) -2.0f *e.getY() / canvas.getSize().height + 1.0f;
			if(mode==0){
				//float x=(float) 2.0f* e.getX() / canvas.getSize().width -1.0f;
				//float y=(float) -2.0f *e.getY() / canvas.getSize().height + 1.0f;

			    pickPoint = e.getPoint();
			    System.out.println(x+","+y);//these co-ords are relative to the component
			    
			    x0 = x;
			    y0 = y;
			    SingularElem element = new SingularElem(typeMat);
			    element.Jacobian.setScale(1,1);
			    element.setRotation((float)0);
			    element.set(x0, y0);
			    list.add(element);
			    trigger = true;
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			save1=0;save2 = 0;
			rotateElem = null;
			trigger = true;
			float x=(float) 2.0f* e.getX() / canvas.getSize().width -1.0f;
			float y=(float) -2.0f *e.getY() / canvas.getSize().height + 1.0f;
			if(mode==1 || mode==2 || mode==3){
				
				x1Reg=x; y1Reg=y;
				start = e.getPoint();
			}
			if(mode==4){
				for(int i = 0; i < list.nelems; i++){
					SingularElem n = list.singular_elems[i];
					for(int j =0; j<4; j++){
						if((x>=n.box[j][0]-.01&&x<=n.box[j][0]+.01) && (y>=n.box[j][1]-.01&&y<=n.box[j][1]+.01)){
							//Find the testx, testy points
							save1=n.box[j][0]; save2=n.box[j][1];
							//save1=n.testx; save2=n.testy;
							rotateElem = n;
							rotatetrigger = false;
						}
					}
				}
			}
			
			else if(mode==5){
				for(int i = 0; i < list.nelems; i++){
					SingularElem n = list.singular_elems[i];
					for(int j =0; j<4; j++){
						if((x>=n.box[j][0]-.01&&x<=n.box[j][0]+.01) && (y>=n.box[j][1]-.01&&y<=n.box[j][1]+.01)){
							//Find the testx, testy points
							//save1=n.box[j][0]; save2=n.box[j][1];
							//save1=n.testx; save2=n.testy;
							selectedElem = n;
							a1.setText(Float.toString(n.offset1));
							b1.setText(Float.toString(n.offset2));
							a1.repaint();
							b1.repaint();
							in.repaint();
						}
						else if((x>=n.x-.01&&x<=n.x+.01) && (y>=y-.01&&y<=n.y+.01)){
							//Find the testx, testy points
							//save1=n.box[j][0]; save2=n.box[j][1];
							//save1=n.testx; save2=n.testy;
							selectedElem = n;
							a1.setText(Float.toString(n.offset1));
							b1.setText(Float.toString(n.offset2));
							a1.repaint();
							b1.repaint();
							in.repaint();
						}
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			float x=(float) 2.0f* e.getX() / canvas.getSize().width -1.0f;
				float y=(float) -2.0f *e.getY() / canvas.getSize().height + 1.0f;
				x2Reg=x; y2Reg=y;
			if(mode==1){
				
				
				
				end = e.getPoint();
				RegularElem element = new RegularElem(typeMat);
				element.type = 0;
				element.setRotation(element.getAngle(x1Reg, y1Reg,x2Reg, y2Reg)); //getAngle() 0&180 angles are right, but 90 and 270 angles are wrong.
				//element.set((float)start.getX(), (float)start.getY());
				element.set(x1Reg, y1Reg, x2Reg, y2Reg);
				reglist.add(element);
				trigger = true;
				//System.out.println(element.getAngle(x1Reg, y1Reg,x2Reg, y2Reg));
			}
			else if(mode==2){
				RegularElem element = new RegularElem(-1);
				element.setRotation(element.getAngle(x1Reg, y1Reg,x2Reg, y2Reg));
				element.type = 2;
				//element.set(x, y);
				element.set(x1Reg, y1Reg, x2Reg, y2Reg);
				reglist.add(element);
				trigger = true;
			}
			else if(mode==3){
				RegularElem element = new RegularElem(-1);
				element.setRotation(element.getAngle(x1Reg, y1Reg,x2Reg, y2Reg));
				element.type = 3;
				//element.set(x, y);
				element.set(x1Reg, y1Reg, x2Reg, y2Reg);
				reglist.add(element);
				trigger = true;
			}
			else if(mode==4&&!rotatetrigger){
				//Calculate angle between starting and ending
				//change the angle of the singularity
				float angle1 = (float)Math.toDegrees(Math.atan2(save2 - rotateElem.y, save1 - rotateElem.x));
				float angle2 = (float)Math.toDegrees(Math.atan2(y - rotateElem.y, x - rotateElem.x));
				rotateElem.setRotation(angle2-angle1);
				System.out.println(angle2-angle1);
				
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
    	
    }
	

}

class Matrix2x2{
	int type; //0 - source, 1 - sink, 2-center. Can just define matrix based off type defiend with
	public double[][] entry = new double[2][2]; 
	public double[][] transf = new double[2][2];
	public double[][] stransf = new double[2][2];
	public double[][] transp = new double[2][2]; 
	public double[][] stransp = new double[2][2]; 
	public double[][] temp = new double[2][2]; 
	public double[][] jacobianP = new double[2][2]; 
	public double[][] scale = new double[2][2]; 
	public Matrix2x2(int x){
		type = x;
		if(type == 0){ //source
			entry[0][0] = 1;
		  	entry[0][1] = 0;
		  	entry[1][0] = 0;
		  	entry[1][1] = 1;
	  	}
	  	else if(type == 1){ //sink
			entry[0][0] = -1;
		  	entry[0][1] = 0;
		  	entry[1][0] = 0;
		  	entry[1][1] = -1;
	  	}
	  	else if(type == 2){ //center
			entry[0][0] = 0;
		  	entry[0][1] = -1;
		  	entry[1][0] = 1;
		  	entry[1][1] = 0;
	  	}
	  	else if(type == 3){ //saddle
			entry[0][0] = 1;
		  	entry[0][1] = 0;
		  	entry[1][0] = 0;
		  	entry[1][1] = -1;
	  	}
	  	else if(type == 4){ //Cw-center
			entry[0][0] = 0;
		  	entry[0][1] = 1;
		  	entry[1][0] = -1;
		  	entry[1][1] = 0;
	  	}
	}
	public void setScale(int x, int y){
		scale[0][0] = x;
		scale[1][1] = y;
	}
	
	public void setRotation(float rotang){
		transf[0][0] = Math.cos(Math.toRadians(rotang));
		transf[0][1] = -Math.sin(Math.toRadians(rotang));
		transf[1][0] = Math.sin(Math.toRadians(rotang));
		transf[1][1] = Math.cos(Math.toRadians(rotang));
		//Transpose
		transp[0][0] = Math.cos(Math.toRadians(rotang));
		transp[1][0] = -Math.sin(Math.toRadians(rotang));
		transp[0][1] = Math.sin(Math.toRadians(rotang));
		transp[1][1] = Math.cos(Math.toRadians(rotang));
		
		//SCALE STUFF
		/*transf[0][0] = transf[0][0]*scale[0][0]+transf[0][1]*scale[1][0];
		transf[0][1] = transf[0][0]*scale[0][1]+transf[0][1]*scale[1][1];
		transf[1][0] = transf[1][0]*scale[0][0]+transf[1][1]*scale[1][0];
		transf[1][1] = transf[1][0]*scale[0][1]+transf[1][1]*scale[1][1];
		transp[0][0] = transp[0][0]*scale[0][0]+transp[0][1]*scale[1][0];
		transp[0][1] = transp[0][0]*scale[0][1]+transp[0][1]*scale[1][1];
		transp[1][0] = transp[1][0]*scale[0][0]+transp[1][1]*scale[1][0];
		transp[1][1] = transp[1][0]*scale[0][1]+transp[1][1]*scale[1][1];*/
		
		stransf[0][0] = transf[0][0]*scale[0][0]+transf[0][1]*scale[1][0];
		stransf[0][1] = transf[0][0]*scale[0][1]+transf[0][1]*scale[1][1];
		stransf[1][0] = transf[1][0]*scale[0][0]+transf[1][1]*scale[1][0];
		stransf[1][1] = transf[1][0]*scale[0][1]+transf[1][1]*scale[1][1];
		stransp[0][0] = transp[0][0]*scale[0][0]+transp[0][1]*scale[1][0];
		stransp[0][1] = transp[0][0]*scale[0][1]+transp[0][1]*scale[1][1];
		stransp[1][0] = transp[1][0]*scale[0][0]+transp[1][1]*scale[1][0];
		stransp[1][1] = transp[1][0]*scale[0][1]+transp[1][1]*scale[1][1];
		

	}
	
	public void calcPrime(){
		/*jacobianP[0][0] = transf[0][0]*transp[0][0]+transf[0][1]*transp[1][0];
		jacobianP[0][1] = transf[0][0]*transp[0][1]+transf[0][1]*transp[1][1];
		jacobianP[1][0] = transf[1][0]*transp[0][0]+transf[1][1]*transp[1][0];
		jacobianP[1][1] = transf[1][0]*transp[0][1]+transf[0][1]*transp[1][1];
		
		jacobianP[0][0] = jacobianP[0][0]*entry[0][0]+jacobianP[0][1]*entry[1][0];
		jacobianP[0][1] = jacobianP[0][0]*entry[0][1]+jacobianP[0][1]*entry[1][1];
		jacobianP[1][0] = jacobianP[1][0]*entry[0][0]+jacobianP[1][1]*entry[1][0];
		jacobianP[1][1] = jacobianP[1][0]*entry[0][1]+jacobianP[0][1]*entry[1][1];*/
		
		temp[0][0] = stransp[0][0]*entry[0][0]+stransp[0][1]*entry[1][0];
		temp[0][1] = stransp[0][0]*entry[0][1]+stransp[0][1]*entry[1][1];
		temp[1][0] = stransp[1][0]*entry[0][0]+stransp[1][1]*entry[1][0];
		temp[1][1] = stransp[1][0]*entry[0][1]+stransp[1][1]*entry[1][1];
		
		jacobianP[0][0] = temp[0][0]*stransf[0][0]+temp[0][1]*stransf[1][0];
		jacobianP[0][1] = temp[0][0]*stransf[0][1]+temp[0][1]*stransf[1][1];
		jacobianP[1][0] = temp[1][0]*stransf[0][0]+temp[1][1]*stransf[1][0];
		jacobianP[1][1] = temp[1][0]*stransf[0][1]+temp[1][1]*stransf[1][1];
		
	}
}

class SingularElem{
	int ID;
	int Triangle_ID;
	float x,y;
	float rotang, sx, sy;//rotation angle, scale values
	int type;  //0—source, 1—sink, 2—saddle, 3—cwcenter,
	Matrix2x2 Jacobian; //Just matches the type
	float testx,testy;
	float offset1 = 12.6f; //small this value, larger the influence of element. 0.6
	float offset2 = -2.5f;
	float[][] box = new float[4][2];
	float[][] rotbox = new float[4][2];
	public SingularElem(int x){
		type = x;
		if(x != -1)
			Jacobian = new Matrix2x2(type);
	}
	public void set(float x, float y){
		this.x = x;
		this.y = y;
		//BOX angled at 0/90 degrees
		box[0][0] = x-0.05f; box[0][1] = y+0.05f;
		box[1][0] = x+0.05f; box[1][1] = y+0.05f;
		box[2][0] = x-0.05f; box[2][1] = y-0.05f;
		box[3][0] = x+0.05f; box[3][1] = y-0.05f;
		testx = x; testy = y+.1f;
		
	}
	public void setRotation(float rot){
		//Jacobian = new Matrix2x2(3);
		rotang = rot;
		Jacobian.setRotation(rot);
		Jacobian.calcPrime();
		
		float x1=(float) ((box[0][0]-x)*Math.cos(Math.toRadians(rot))-(box[0][1]-y)*Math.sin(Math.toRadians(rot))+x);
		float y1=(float) ((box[0][0]-x)*Math.sin(Math.toRadians(rot))+(box[0][1]-y)*Math.cos(Math.toRadians(rot))+y);
		float x2=(float) ((box[1][0]-x)*Math.cos(Math.toRadians(rot))-(box[1][1]-y)*Math.sin(Math.toRadians(rot))+x);
		float y2=(float) ((box[1][0]-x)*Math.sin(Math.toRadians(rot))+(box[1][1]-y)*Math.cos(Math.toRadians(rot))+y);
		float x3=(float) ((box[2][0]-x)*Math.cos(Math.toRadians(rot))-(box[2][1]-y)*Math.sin(Math.toRadians(rot))+x);
		float y3=(float) ((box[2][0]-x)*Math.sin(Math.toRadians(rot))+(box[2][1]-y)*Math.cos(Math.toRadians(rot))+y);
		float x4=(float) ((box[3][0]-x)*Math.cos(Math.toRadians(rot))-(box[3][1]-y)*Math.sin(Math.toRadians(rot))+x);
		float y4=(float) ((box[3][0]-x)*Math.sin(Math.toRadians(rot))+(box[3][1]-y)*Math.cos(Math.toRadians(rot))+y);
		testx =(float) ((testx-x)*Math.cos(Math.toRadians(rot))-(testy-y)*Math.sin(Math.toRadians(rot))+x);
		testy =(float) ((testx-x)*Math.sin(Math.toRadians(rot))+(testy-y)*Math.cos(Math.toRadians(rot))+y);
		box[0][0]=x1;
		box[0][1]=y1;
		box[1][0]=x2;
		box[1][1]=y2;
		box[2][0]=x3;
		box[2][1]=y3;
		box[3][0]=x4;
		box[3][1]=y4;
	}
	public void setOffset(float a, float b){
		offset1 = a; //usually 0.6
		offset2 = b;
	}
	public float calcWeight(float distance){
		return (float) (offset1*Math.exp(offset2*distance*distance));
	}
	
}

class SingularElemList{
	static SingularElem[] singular_elems;
	int nelems;
	int curMaxNum;
	
	public SingularElemList(){
		nelems = 0;
		curMaxNum = 10;
		singular_elems = new SingularElem[curMaxNum];
		for(int i =0; i <curMaxNum; i++){
		//	singular_elems[i] = new SingularElem(-1);
		}
	}
	
	public boolean isFull(){
		if(nelems==curMaxNum-1)
			return true;
		return false;
	}
	
	public boolean extend(){
		SingularElem[] temp = singular_elems;
		singular_elems = new SingularElem[singular_elems.length+10];
		for(int i = 0; i<curMaxNum-1; i++)
			singular_elems[i] = temp[i];
			
		curMaxNum += 10;
		return true;
	}
	
	public boolean add(SingularElem temp){
		if(isFull()){
			extend();
			singular_elems[nelems] = temp;
			nelems++;
		}
		else{
			singular_elems[nelems] = temp;
			nelems++;	
		}
			
		return true;
	}
	

}

class RegularElem{ //have setTransform method for con/div elements.
	int ID;
	int Triangle_ID;
	float x,y, bx,by; //start values, end value coordinates
	int type;  //0-regular,1-attachment,2-separation
	Matrix2x2 Jacobian; //Just matches the type. Jacobian will contain transform matrix, tranpose, and jacobian prime matrix
	float box[] = new float[4];
	float offset1 = 0.9f;
	float offset2 = -1.4f;
	//Matrix2x2 JacobianP;
	//Transform matrix
	//Matrix2x2 transform_matrix;
	//Matrix2x2 transposeRot;
	Point a;
	Point b;
	//direction vector determined through vector for xy?
	double rotang;
	double scale;
	
	public RegularElem(int x){
		type = x;
		if(x != -1)
			Jacobian = new Matrix2x2(type);
		
		//Set transform matrix to identity?
	}
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	public void set(float x, float y, float x2, float y2){
		this.x = x;
		this.y = y;
		bx=x2;
		by=y2;
	}
	public void set(Point start, Point end){
		a=start;
		b=end;
	}
	public void setRotation(float rot){
		Jacobian = new Matrix2x2(3);
		rotang = rot;
		//Jacobian.setRotation(rot);
		//Jacobian.calcPrime();
		/*transform_matrix.entry[0][0] = Math.cos(rotang);
		transform_matrix.entry[0][1] = -Math.sin(rotang);
		transform_matrix.entry[1][0] = Math.sin(rotang);
		transform_matrix.entry[1][1] = Math.cos(rotang);
		//Transpose
		transposeRot.entry[0][0] = Math.cos(rotang);
		transposeRot.entry[1][0] = -Math.sin(rotang);
		transposeRot.entry[0][1] = Math.sin(rotang);
		transposeRot.entry[1][1] = Math.cos(rotang);
		
		JacobianP.entry[0][0] = transform_matrix.entry[0][0]*transposeRot.entry[0][0]+transform_matrix.entry[0][1]*transposeRot.entry[1][0];
		JacobianP.entry[0][1] = transform_matrix.entry[0][0]*transposeRot.entry[0][1]+transform_matrix.entry[0][1]*transposeRot.entry[1][1];
		JacobianP.entry[1][0] = transform_matrix.entry[1][0]*transposeRot.entry[0][0]+transform_matrix.entry[1][1]*transposeRot.entry[1][0];
		JacobianP.entry[1][1] = transform_matrix.entry[1][0]*transposeRot.entry[0][1]+transform_matrix.entry[0][1]*transposeRot.entry[1][1];
		
		JacobianP.entry[0][0] = JacobianP.entry[0][0]*Jacobian.entry[0][0]+JacobianP.entry[0][1]*Jacobian.entry[1][0];
		JacobianP.entry[0][1] = JacobianP.entry[0][0]*Jacobian.entry[0][1]+JacobianP.entry[0][1]*Jacobian.entry[1][1];
		JacobianP.entry[1][0] = JacobianP.entry[1][0]*Jacobian.entry[0][0]+JacobianP.entry[1][1]*Jacobian.entry[1][0];
		JacobianP.entry[1][1] = JacobianP.entry[1][0]*Jacobian.entry[0][1]+JacobianP.entry[0][1]*Jacobian.entry[1][1];*/
	}
	
	public float getAngle(Point start, Point end){
		float angle = (float)Math.toDegrees(Math.atan2(end.getY() - start.getY(), end.getX() - start.getX()));
		//float angle = (float)Math.toDegrees(Math.atan2(end.getX() - start.getX(), end.getY() - start.getY()));
		if(angle < 0){
			angle+=360;
		}
		return angle;
	}
	
	public float getAngle(float targetx, float targety, float curx, float cury){
		//float angle = (float)Math.toDegrees(Math.atan2(targety - a.y, targetx - a.x));
		float angle = (float)Math.toDegrees(Math.atan2(targety - cury, targetx - curx));
		angle+= 180;
		if(angle < 0){
			angle+=360;
		}
		return angle;
	}
	public void setOffset(float a, float b){
		offset1 = a; //usually 0.6
		offset2 = b;
	}
	public float calcWeight(float distance){
		return (float) (offset1*Math.exp(offset2*distance*distance));
	}
}

class RegularElemList{
	static RegularElem[] regular_elems;
	int nelems;
	int curMaxNum;
	
	public RegularElemList(){
		nelems = 0;
		curMaxNum = 10;
		regular_elems = new RegularElem[curMaxNum];
		for(int i =0; i <curMaxNum; i++){
		//	singular_elems[i] = new SingularElem(-1);
		}
	}
	
	public boolean isFull(){
		if(nelems==curMaxNum-1)
			return true;
		return false;
	}
	
	public boolean extend(){
		RegularElem[] temp = regular_elems;
		regular_elems = new RegularElem[regular_elems.length+10];
		for(int i = 0; i<curMaxNum-1; i++)
			regular_elems[i] = temp[i];
			
		curMaxNum += 10;
		return true;
	}
	
	public boolean add(RegularElem temp){
		if(isFull()){
			extend();
			regular_elems[nelems] = temp;
			nelems++;
		}
		else{
			regular_elems[nelems] = temp;
			nelems++;	
		}
			
		return true;
	}

}

class Mesh{
	int id;
	float x, y;
	float x2, y2, x3, y3;
	int type; //1-Top, 0-bottom L
	float vx1,vx2,vx3,vy1,vy2,vy3;
	public Mesh(float x, float y, int type){
		this.x = x;
		this.y = y;
		this.type = type;
	}
	
	public void setVert(float x2, float y2, float x3, float y3){
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;
	}
	
	public void setVector(float vx1, float vy1, float vx2, float vy2, float vx3, float vy3){
		this.vx1 = vx1;
		this.vy1 = vy1;
		this.vx2 = vx2;
		this.vy2 = vy2;
		this.vx3 = vx3;
		this.vy3 = vy3;
	}
}
class MeshList{
	static Mesh[] mesh_elems;
	int nelems;
	int curMaxNum;
	
	public MeshList(){
		nelems = 0;
		curMaxNum = 10;
		mesh_elems = new Mesh[curMaxNum];
		for(int i =0; i <curMaxNum; i++){
		//	singular_elems[i] = new SingularElem(-1);
		}
	}
	
	public boolean isFull(){
		if(nelems==curMaxNum-1)
			return true;
		return false;
	}
	
	public boolean extend(){
		Mesh[] temp = mesh_elems;
		mesh_elems = new Mesh[mesh_elems.length+10];
		for(int i = 0; i<curMaxNum-1; i++)
			mesh_elems[i] = temp[i];
			
		curMaxNum += 10;
		return true;
	}
	
	public boolean add(Mesh temp){
		if(isFull()){
			extend();
			mesh_elems[nelems] = temp;
			nelems++;
		}
		else{
			mesh_elems[nelems] = temp;
			nelems++;	
		}
			
		return true;
	}

}
class PointList{
	static Point2D.Float[] point_elems;
	int nelems;
	int curMaxNum;
	
	public PointList(){
		nelems = 0;
		curMaxNum = 10;
		point_elems = new Point2D.Float[curMaxNum];
		for(int i =0; i <curMaxNum; i++){
		//	singular_elems[i] = new SingularElem(-1);
		}
	}
	
	public boolean isFull(){
		if(nelems==curMaxNum-1)
			return true;
		return false;
	}
	
	public boolean contain(Point2D.Float temp){
		for(int i = 0; i<curMaxNum; i++)
			if(temp.equals(point_elems[i]))
				return true;
		return false;
	}
	
	public boolean extend(){
		Point2D.Float[] temp = point_elems;
		point_elems = new Point2D.Float[point_elems.length+10];
		for(int i = 0; i<curMaxNum-1; i++)
			point_elems[i] = temp[i];
			
		curMaxNum += 10;
		return true;
	}
	
	public boolean add(Point2D.Float temp){
		if(isFull()){
			extend();
			point_elems[nelems] = temp;
			nelems++;
		}
		else{
			point_elems[nelems] = temp;
			nelems++;	
		}
			
		return true;
	}

}


class PerlinNoise {

	// Just a Random class object so I can fill my noise map with random directions.
	public static final Random random = new Random();

	// Width and Height of the map.
	public int width, height;
	public BufferedImage savedimage = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);
	// Random directions of length 1.
	public char[][][] noiseimg;
	private vec2[] values;

	/**
	 * Creates a noise map with specified dimensions.
	 * @param width of the noise map.
	 * @param height of the noise map.
	 */
	public PerlinNoise(int width, int height) {
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
		PerlinNoise noise = new PerlinNoise(size, size);
		
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
		JOptionPane.showMessageDialog(null, null, "Perlin Noise | " + time, JOptionPane.YES_NO_OPTION, new ImageIcon(savedimage.getScaledInstance(512, 512, Image.SCALE_DEFAULT)));
	}
	
	public void newdisplay(PointList pointlist, float widthx, float heightx) { //LIC IMAGE

		long time0 = System.nanoTime(); // Take a time stamp.
		
		int size = 128; // Perlin's noise map. (the amount of static)
		PerlinNoise noise = new PerlinNoise(size, size);
		
		int width = 700; // Width of the finished image.
		int height = 700; // Height of the finished image.
		noiseimg = new char[width][height][3];
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Image to store pixel data in.

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				float xx = (float) x / width * size; // Where does the point lie in the noise space according to image space. 
				float yy = (float) y / height * size; // Where does the point lie in the noise space according to image space. 
				
				float n = (float) noise.noise(xx, yy); // Noise values from Perlin's noise.
				int col = (int) ((n + 1) * 255 / 2f); // Since noise value returned is -1 to 1, we make it so that -1 is black, and 1 is white.
				noiseimg[x][y][0] =
						noiseimg[x][y][1] =
						noiseimg[x][y][2] = (char)col;
				
				Color color = new Color(col, col, col); // java.AWT color to get RGB from.
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
	        	int sum = (color.getRed()+color.getBlue()+color.getGreen()); //n=48
	        	addall+=sum;
        	}
        }
		addall= addall/pointlist.nelems;
		System.out.println(pointlist.nelems);
		System.out.println(addall);
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
	        	int sum = (color.getRed()+color.getBlue()+color.getGreen())/(height*width); //n=48
	        	Color c = new Color(sum, sum, sum);
	        	//savedimage.setRGB(x, y, Color.CYAN.getRGB());
	        	
	        	savedimage.setRGB(x, y, addall);
	        	//System.out.println(sum+"vs "+color.getRed()+" "+color.getBlue()+" "+color.getGreen());
        	}
        	else
        		System.out.println("?!?!");
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

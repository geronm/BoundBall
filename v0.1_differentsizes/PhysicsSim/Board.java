// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)
//


package PhysicsSim;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Random;
import java.lang.*;

public class Board extends JPanel implements ActionListener {

	//Dude p;
	Image bgimg;
	Timer time;
	Vector<Integer> myV;
	Random rgen;

	final static int STAGEWIDTH = 600;	
	final static int STAGEHEIGHT = 330;
	
	final static int GRIDX = 15;
	
	final static int NUM_OF_BALLS=200;
	Ball[] ball = new Ball[NUM_OF_BALLS];
	
	double gravity = 0.1;
	
	Boolean left,right,up,down;

	public Board() {
		rgen = new Random();
		
		for(int i=0; i<NUM_OF_BALLS; i++) {
			ball[i] = new Ball((i%GRIDX)*STAGEWIDTH/(GRIDX+1), ((int)(i/(NUM_OF_BALLS/GRIDX)))*STAGEHEIGHT/((NUM_OF_BALLS/GRIDX)+2), rgen.nextDouble()*20);
			ball[i].xv = rgen.nextDouble()-0.5;
		}
		
		/*	
		ball[NUM_OF_BALLS-1].r = 25;
		ball[NUM_OF_BALLS-1].x = STAGEWIDTH-ball[NUM_OF_BALLS-1].r;
		ball[NUM_OF_BALLS-1].y = ball[NUM_OF_BALLS-1].r;
		*/
		
		myV = new Vector<Integer>();
		myV.add(5);
		myV.add(4);

		left=false;
		right=false;
		up=false;
		down=false;
		
		addKeyListener(new AL());
		setFocusable(true);
		//setVisible(true);
		ImageIcon i = new ImageIcon("C:\\Documents and Settings\\Compaq_Owner.PAL\\My Documents\\SwingGame\\Background.png");
		bgimg = i.getImage();
		time = new Timer(30, this);
		time.start();
	}
	
	public void randomizeVelocities() {
		for(int i=0; i<NUM_OF_BALLS; i++) {	
			ball[i].xv = 10*rgen.nextDouble()-5;
			ball[i].yv = 10*rgen.nextDouble()-5;
		}
	}	
	
	public void actionPerformed(ActionEvent e) {
		//p.move();
		if(up)
			randomizeVelocities();		
		updatePhysics();
		repaint();
	}
	
	public void updatePhysics() {
		for(int i=0; i<NUM_OF_BALLS; i++) {	
			ball[i].yv += gravity;
			
			if(ball[i].y+ball[i].r+ball[i].yv > STAGEHEIGHT) {
				ball[i].yv *= -0.8;
				ball[i].y = STAGEHEIGHT-ball[i].r;
				//ball[i].xv *= 0.9;
			} else if (ball[i].y-ball[i].r+ball[i].yv < 0) {
				ball[i].yv *= -0.8;
				ball[i].y = ball[i].r;
				//ball[i].xv *= 0.9;
			}
			
			if(ball[i].x+ball[i].r+ball[i].xv > STAGEWIDTH) {
				ball[i].x = STAGEWIDTH-ball[i].r;
				ball[i].xv *= -0.8;
				//ball[i].yv *= 0.9;
			} else if (ball[i].x-ball[i].r+ball[i].xv < 0) {
				ball[i].x = ball[i].r;
				ball[i].xv *= -0.8;
				//ball[i].yv *= 0.9;
			}
			
			ball[i].x += ball[i].xv;
			ball[i].y += ball[i].yv;
			
			for(int j=i+1; j<NUM_OF_BALLS; j++) {
				lamePhysics(ball[i],ball[j]);
			}
		}
		
		try {
		if(!down) {
			ball[NUM_OF_BALLS-1].x = getMousePosition(true).x;
			ball[NUM_OF_BALLS-1].y = getMousePosition(true).y;
			ball[NUM_OF_BALLS-1].xv = 0;
			ball[NUM_OF_BALLS-1].yv = 0;
			gravity = 0.1;
		} else {//ADDED
			for(int i=0; i<NUM_OF_BALLS; i++) {
				ball[i].xv = 0;
				ball[i].yv = 0;
			}
			gravity = 0;
		}			
		} catch (Exception e) { };
	}
	
	public double lamePhysics(Ball ball1, Ball ball2) {
		double sepx = ball2.x-ball1.x;
		double sepy = ball2.y-ball1.y;
		
		double dist = Math.sqrt(sepx*sepx + sepy*sepy);
		double pen = dist-ball1.r-ball2.r;
		
		if(pen < 0) {
			double k = dist*(ball1.r*ball1.r + ball2.r*ball2.r); //denominator of both movement commands
			
			ball1.x += pen*sepx*ball2.r*ball2.r/k;
			ball1.y += pen*sepy*ball2.r*ball2.r/k;
			
			ball1.xv += pen*sepx*ball2.r*ball2.r/k;
			ball1.yv += pen*sepy*ball2.r*ball2.r/k;
			
			ball2.x -= pen*sepx*ball1.r*ball1.r/k;
			ball2.y -= pen*sepy*ball1.r*ball1.r/k;
			
			ball2.xv -= pen*sepx*ball1.r*ball1.r/k;
			ball2.yv -= pen*sepy*ball1.r*ball1.r/k;			
		}
			
		return pen;
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		RenderingHints rh = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(rh);
		
		//g2.drawImage(bgimg,0,0,null);
		//g2.drawImage(p.getImage(),p.getX(),p.getY(),null);
		
		for(int i=0; i<NUM_OF_BALLS; i++) {
			g2.setColor(Color.blue);
			g2.fillOval(ball[i].getX()-ball[i].getR(),ball[i].getY()-ball[i].getR(),ball[i].getR()*2,ball[i].getR()*2);
		}
		
		updateHUD(g2);
		
	}
	
	private void updateHUD(Graphics2D g2) {
		g2.setColor(Color.gray);
		if(left)
		g2.setColor(Color.black);
		g2.fillRect(30,50,10,10);
		
		g2.setColor(Color.gray);
		if(right)
		g2.setColor(Color.black);
		g2.fillRect(60,50,10,10);
		
		g2.setColor(Color.gray);
		if(up)
		g2.setColor(Color.black);
		g2.fillRect(45,35,10,10);
		
		g2.setColor(Color.gray);
		if(down)
		g2.setColor(Color.black);
		g2.fillRect(45,50,10,10);
	}
	
	private class AL extends KeyAdapter { //action listener
		public void keyReleased(KeyEvent e){
			int key = e.getKeyCode();
			
			if(key == KeyEvent.VK_LEFT)
				left = false;
			if(key == KeyEvent.VK_RIGHT)
				right = false;		
			if(key == KeyEvent.VK_UP)
				up = false;
			if(key == KeyEvent.VK_DOWN)
				down = false;		
		}
		public void keyPressed(KeyEvent e){
			int key = e.getKeyCode();
			
			if(key == KeyEvent.VK_LEFT)
				left = true;
			if(key == KeyEvent.VK_RIGHT)
				right = true;
			if(key == KeyEvent.VK_UP)
				up = true;
			if(key == KeyEvent.VK_DOWN)
				down = true;				
		}
	}

}
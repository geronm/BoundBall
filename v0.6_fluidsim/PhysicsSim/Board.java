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
   import java.util.LinkedList;
   import java.lang.*;

   public class Board extends JPanel implements ActionListener {
   
   //Dude p;
   //Image bgimg;
      Timer time;
      Vector<Integer> myV;
      Random rgen;
   
      final static int STAGEWIDTH = 593;	
      final static int STAGEHEIGHT = 330;
   
      final static int GRIDX = 5;
   
      final static int NUM_OF_BALLS=400;
      final static int INIT_RADIUS=7;
      Ball[] ball = new Ball[NUM_OF_BALLS];
   
      double gravity = 0.02;//0.5; //0.1
      double polar_attraction = 0.1; //0.1 for supersmall, 5 for moderate small
   
      boolean leftKey,rightKey,upKey,downKey;
      boolean zeroKey;
      boolean zeroHit;
      boolean F5_Key;
      boolean F5_Hit;
   
      public Board() {
      
         rgen = new Random();
      
      // 		for(int i=0; i<NUM_OF_BALLS; i++) {
      // 			ball[i] = new Ball((i%GRIDX)*STAGEWIDTH/(GRIDX+1), ((int)(i/(NUM_OF_BALLS/GRIDX+1)))*STAGEHEIGHT/((NUM_OF_BALLS/GRIDX)+2), INIT_RADIUS);//rgen.nextDouble()*20);
      // 			//ball[i].xv = rgen.nextDouble()-0.5;
      // 		}
      
      // 		//Make lipids
      // 		for(int i=0; i<NUM_OF_BALLS; i+=NUM_OF_BALLS/10+1) {
      // 			ball[i].type = Ball.LIPID;
      // 			ball[i].r = 10;
      // 		}
      
       //  ball[0] = new Ball(STAGEWIDTH/2, 3*STAGEHEIGHT/4, 30);
       //  ball[1] = new Ball(STAGEWIDTH/2-100,3*STAGEHEIGHT/4,15);
		 //  ball[0].addChild(ball[1]);
			for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS); i++) {
				for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS); j++) {
					ball[i*(int)Math.sqrt(NUM_OF_BALLS)+j] = new Ball(60+i*INIT_RADIUS*2,60+j*INIT_RADIUS*2,INIT_RADIUS);
					ball[i*(int)Math.sqrt(NUM_OF_BALLS)+j].elasticity = 0.75;
				}
  			}
			    
      /*	
      ball[NUM_OF_BALLS-1].r = 25;
      ball[NUM_OF_BALLS-1].x = STAGEWIDTH-ball[NUM_OF_BALLS-1].r;
      ball[NUM_OF_BALLS-1].y = ball[NUM_OF_BALLS-1].r;
      */
      
         myV = new Vector<Integer>();
         myV.add(5);
         myV.add(4);
      
         leftKey=false;
         rightKey=false;
         upKey=false;
         downKey=false;
      
         addKeyListener(new AL());
         setFocusable(true);
      //setVisible(true);
      //ImageIcon i = new ImageIcon("C:\\Documents and Settings\\Compaq_Owner.PAL\\My Documents\\SwingGame\\Background.png");
      //bgimg = i.getImage();
         time = new Timer(5, this);
         time.start();
      }
   
      public void randomizeVelocities() {
         for(int i=0; i<NUM_OF_BALLS; i++) {	
            ball[i].xv = 5*rgen.nextDouble()-2.5;
            ball[i].yv = 5*rgen.nextDouble()-2.5;
         }
      }	
   
      public void actionPerformed(ActionEvent e) {
      //p.move();
         if(upKey)
            randomizeVelocities();
			// if(leftKey) {
			// 	polar_attraction = -5;
			// } else {
			// 	polar_attraction = -5;
			// }
			updatePhysics();
			if(rightKey) {
				for(int i=0; i<NUM_OF_BALLS; i++) {
					ball[i].xv = 0;
					ball[i].yv = 0;
				}
			}
					
         repaint();
      }
   
      public void updatePhysics() {
		
			
         for(int i=0; i<NUM_OF_BALLS; i++) {	
            ball[i].x += ball[i].xv;
            ball[i].y += ball[i].yv;
            ball[i].ang += ball[i].angv;
				
        //Keeps balls onscreen in a rudimentary, pseudophysical way
            if(ball[i].y+ball[i].r > STAGEHEIGHT) {
               ball[i].yv *= -ball[i].elasticity;
               ball[i].y = STAGEHEIGHT-ball[i].r;
            } 
            else if (ball[i].y-ball[i].r+ball[i].yv < 0) {
					ball[i].yv *= -ball[i].elasticity;
               ball[i].y = ball[i].r;
            }			
            if(ball[i].x+ball[i].r+ball[i].xv > STAGEWIDTH) {
             	ball[i].xv *= -ball[i].elasticity;
            	ball[i].x = STAGEWIDTH-ball[i].r;
            } 
            else if (ball[i].x-ball[i].r+ball[i].xv < 0) {
               ball[i].xv *= -ball[i].elasticity;
	            ball[i].x = ball[i].r;
            }
         
           ball[i].yv += gravity;
			
			
			
         //Move the balls before applying physics (a mistake?)
         
         //Apply the physics algorithms! The first ball is checked against all the others, then the
         // second is checked against all but the first ball, then the third is checked against all
         // but the first two, etc. until all are checked. Iow, a single-pass algorithm
         // 			for(int j=i+1; j<NUM_OF_BALLS; j++) {
         // 				ballPhysics(ball[i],ball[j],true);
         // 			}
       //     if(i<2){
            	
			//		weldConstrain(ball[i],ball[1-i]);
       //     }

           	//parent-child system = much nicer
          	
        
     		}
			
			/****TEST OF COLLISION CODE FROM POOLBALLS****/
			for(int i=0; i<NUM_OF_BALLS; i++) {
				for(int j=i+1; j<NUM_OF_BALLS; j++) {
					ballForces(ball[i],ball[j]);
					//ballCollision(ball[i],ball[j]);
				}
			}	
			for(int i=NUM_OF_BALLS-1; i>0; i--) {
				for(int j=i-1; j>0; j--) {
					ballForces(ball[i],ball[j]);
					//ballCollision(ball[i],ball[j]);
				}
			}
			/****TEST OF COLLISION CODE FROM POOLBALLS****/
			for(int i=0; i<NUM_OF_BALLS; i++) {
				for(int j=i+1; j<NUM_OF_BALLS; j++) {
					ballForces(ball[i],ball[j]);
					//ballCollision(ball[i],ball[j]);
				}
			}	
			/****TEST OF COLLISION CODE FROM POOLBALLS****/
			for(int i=0; i<NUM_OF_BALLS; i++) {
				for(int j=i+1; j<NUM_OF_BALLS; j++) {
					ballForces(ball[i],ball[j]);
					//ballCollision(ball[i],ball[j]);
				}
			}											
			
	
			
			//weldConstrain2(ball[0],ball[1]);
 

		
      //Testing purposes; hit the "down" key to attach a ball to your cursor!
         try {
            if(downKey) {
               //ball[NUM_OF_BALLS-1].xv = 0;//getMousePosition(true).x - ball[NUM_OF_BALLS-1].x;
               //ball[NUM_OF_BALLS-1].yv = 0;//getMousePosition(true).y - ball[NUM_OF_BALLS-1].y;		
               //ball[NUM_OF_BALLS-1].x = getMousePosition(true).x;
               //ball[NUM_OF_BALLS-1].y = getMousePosition(true).y;
					for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS); i++) {
						for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS); j++) {
							ball[i*(int)Math.sqrt(NUM_OF_BALLS)+j].x = 60+i*INIT_RADIUS*3;
							ball[i*(int)Math.sqrt(NUM_OF_BALLS)+j].y = 60+j*INIT_RADIUS*3;
							ball[i*(int)Math.sqrt(NUM_OF_BALLS)+j].r = INIT_RADIUS;
						}
		  			}
            }
         } 
            catch (Exception e) { };
      }
   
	
      public void weldConstrain2(Ball ballp, Ball ballc) {
		
			double totalmass = ballc.mass + ballp.mass;
			
      	double deltax = ballc.x-(ballp.r*Math.cos(ballp.ang))-ballp.x;
      	double deltay = ballc.y-(ballp.r*Math.sin(ballp.ang))-ballp.y;			
			
			double deltax1 = deltax*ballc.mass/totalmass;
			double deltay1 = deltay*ballc.mass/totalmass;
			double deltax2 = deltax1 - deltax;
			double deltay2 = deltay1 - deltay;
			
			double vxprime1 = ballp.xv + deltax1;
			double vyprime1 = ballp.yv + deltay1;
			double vxprime2 = ballc.xv + deltax2;
			double vyprime2 = ballc.yv + deltay2;
			
			ballp.xv = (ballp.mass*vxprime1 + ballc.mass*vxprime2)/totalmass;
			ballc.xv = ballp.xv;
			ballp.yv = (ballp.mass*vyprime1 + ballc.mass*vyprime2)/totalmass;
			ballc.yv = ballp.xv;
			
			ballp.x += deltax1;
			ballp.y += deltay1;
			ballc.x += deltax2;
			ballc.y += deltay2;
      }
  
  		public double ballCollision(Ball ball1, Ball ball2) {
			double modx = ball2.xv;
			double mody = ball2.yv;
			
			ball2.xv = 0;
			ball2.yv = 0;
			ball1.xv -= modx;
			ball1.yv -= mody;
			
			double distance = getStaticBallCollision(ball1,ball2);
			
			ball2.xv = modx;
			ball2.yv = mody;

			//collision happened
			if(distance != 0) {
			
				//new def of modx, now it's the multiplier for the move vectors			
				double temp = 0;
				if((ball1.xv != 0) || (ball1.yv != 0))
					temp = distance/Math.sqrt(ball1.xv*ball1.xv + ball1.yv*ball1.yv);
				
				/*if (!((temp < 1) && (temp>0))) {
					System.out.println("Xv: " + ball1.xv + "\t Yv: " + ball1.yv + "\t Dist: " + distance);
					System.out.println("Math.sqrt(ball1.xv*ball1.xv + ball1.yv*ball1.yv: " + Math.sqrt(ball1.xv*ball1.xv + ball1.yv*ball1.yv));
					System.out.println("Temp: " + temp);
				}*/
				
				ball1.xv += modx;
				ball1.yv += mody;		

				ball1.x += ball1.xv * temp;
				ball1.y += ball1.yv * temp;
				ball2.x += ball2.xv * temp;
				ball2.y += ball2.yv * temp;
				
				/*
				//MOMENTUM STUFF:
				
				//normalized vector from center to center
				double dist = Math.sqrt((ball2.x-ball1.x)*(ball2.x-ball1.x) + (ball2.y-ball1.y)*(ball2.y-ball1.y));
				double n_x = (ball2.x-ball1.x)/dist;
				double n_y = (ball2.y-ball1.y)/dist;
				
				//components of v's along n
				double a1 = ball1.xv*n_x + ball1.yv*n_y;
				double a2 = ball2.xv*n_x + ball2.yv*n_y;
				
				double optimizedP = 2*(a1-a2)/(ball1.mass+ball2.mass);
				
				//total elasticity of collision
				double e = ball1.elasticity*ball2.elasticity;
				
				//calculate new velocities
				ball1.xv -= e*(optimizedP*ball2.mass*n_x); //yes, that IS ballTWO
				ball1.yv -= e*(optimizedP*ball2.mass*n_y);
				ball2.xv += e*(optimizedP*ball1.mass*n_x);
				ball2.yv += e*(optimizedP*ball1.mass*n_y);				
				
				*/
				
				return distance;
			}
					
			ball1.xv += modx;
			ball1.yv += mody;
			
			return distance;
			
		}			
				
  
		//To use this function effectively, after function is called do this:  
		//ball1.xv = xv_norm * distance;
		//ball1.yv = yv_norm * distance;
  		public double getStaticBallCollision(Ball ball1, Ball ball2) {

			double dist = Math.sqrt((ball2.x-ball1.x)*(ball2.x-ball1.x) + (ball2.y-ball1.y)*(ball2.y-ball1.y));
			double sumRadii = ball1.r + ball2.r;
			
			dist -= sumRadii;
			
			//if the total distance covered by the movement of the balls isn't enough to bring them
			// within range of one another, no collision could POSSIBLY have occured
			if(ball1.xv*ball1.xv+ball1.yv*ball1.yv < dist*dist)
				return 0; //no collision possible

			dist += sumRadii;

			//waited till after the above to do this, because the above scenario
			// is by far the most likely, so it's fastest to save the calculation
			// for unlikelihood
			double v_mag = Math.sqrt(ball1.xv*ball1.xv+ball1.yv*ball1.yv);			
			
			double xv_norm=0, yv_norm=0;
			
			if (v_mag > 0) {
				xv_norm = ball1.xv / v_mag;
				yv_norm = ball1.yv / v_mag;
			}
			
         double sepx = ball2.x-ball1.x;
         double sepy = ball2.y-ball1.y;
			
			//D = V_Norm dot Sep = dist * cos (velocity-displacement angle)
			double D = sepx*xv_norm + sepy*yv_norm;
			
			//D <= 0 would indicate that the two balls are moving in opposite directions
			if(D <= 0)
				return 0;
			
			//shortest dist from center of ball2 to line of ball1's velocity
			double F = dist*dist - D*D;
			
			//If the below is true, then ball1 necessarily misses ball2.
			if(F > sumRadii * sumRadii)
				return 0;
				
			double T = sumRadii*sumRadii - F;
			
			//Distance to move along movement vecotr such that they touch
			double distance = D - Math.sqrt(T);
			
			//ball must be traveling far enough to touch
			if (v_mag < distance)
				return 0;
				
			return distance;
		}
		
		public void ballForces(Ball ball1, Ball ball2) {
         double sepx = ball2.x-ball1.x;
         double sepy = ball2.y-ball1.y;
      
         double dist = Math.sqrt(sepx*sepx + sepy*sepy); //duh
			
			if(Math.abs(dist) <= 0.0001)
				dist = 0.0001;
			
				

         if(ball1.type == ball2.type) {
            ball1.xv += polar_attraction*(sepx/(dist*dist*dist));
            ball1.yv += polar_attraction*(sepy/(dist*dist*dist));
         
            ball2.xv -= polar_attraction*(sepx/(dist*dist*dist));
            ball2.yv -= polar_attraction*(sepy/(dist*dist*dist));
         }
			
			correctOverlap(ball1,ball2,sepx,sepy,dist);
		}			
  
      public double correctOverlap(Ball ball1, Ball ball2, double sepx, double sepy, double dist) {
         double pen = dist-ball1.r-ball2.r; //penetration b/w two disks; aka overlap
      
 /*     //arbitrary inverse-square attractive force
         if(ball1.type == ball2.type) {
            ball1.xv += polar_attraction*(sepx/(dist*dist*dist));
            ball1.yv += polar_attraction*(sepy/(dist*dist*dist));
         
            ball2.xv -= polar_attraction*(sepx/(dist*dist*dist));
            ball2.yv -= polar_attraction*(sepy/(dist*dist*dist));
         } */
      
      //standard collision detection
 
         //if overlap is present
            if(pen < 0) {
            //For each ball, the calculation takes its proportion of square radius b/w the two of them, then moves it
            // and modifies its velocity in the direction of overlap (sepxy/dist) by a magnitude equal to the overlap
            // itself. eg. x += pen * (sepx/dist) * (r1^2/(r1^2+r2^2)) 
            
               double k = dist*(ball1.r*ball1.r + ball2.r*ball2.r); //denominator of both movement commands
               //double v1v2 = 0.1*Math.sqrt(ball1.xv*ball2.xv+ball1.yv*ball2.yv);
            
               ball1.x += pen*sepx*ball2.r*ball2.r/k;
               ball1.y += pen*sepy*ball2.r*ball2.r/k;
            
            //Test line; let's use a dotproduct of v's inst of pen
              ball1.xv += pen*sepx*ball2.r*ball2.r/k;
              ball1.yv += pen*sepy*ball2.r*ball2.r/k;
            
            // 				ball1.xv += pen*sepx*ball2.r*ball2.r/k;
            // 				ball1.yv += pen*sepy*ball2.r*ball2.r/k;
            
               ball2.x -= pen*sepx*ball1.r*ball1.r/k;
               ball2.y -= pen*sepy*ball1.r*ball1.r/k;
            
              ball2.xv -= pen*sepx*ball1.r*ball1.r/k;
              ball2.yv -= pen*sepy*ball1.r*ball1.r/k;
            
            // 				ball2.xv -= pen*sepx*ball1.r*ball1.r/k;
            // 				ball2.yv -= pen*sepy*ball1.r*ball1.r/k;			
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
            if(ball[i].type == Ball.HYDRO) {	
               g2.setColor(Color.blue);
            } 
            else if (ball[i].type == Ball.LIPID) {
               g2.setColor(Color.yellow);
            }
            g2.drawOval(ball[i].getX()-ball[i].getR(),ball[i].getY()-ball[i].getR(),ball[i].getR()*2,ball[i].getR()*2);
						
        //    g2.setColor(Color.black);
        //    g2.drawLine(ball[i].getX(), ball[i].getY(), ball[i].getX()+(int)(ball[i].r*Math.cos(ball[i].ang)), ball[i].getY()+(int)(ball[i].r*Math.sin(ball[i].ang)));
         }
      
         updateHUD(g2);
      
      }
   
      private void updateHUD(Graphics2D g2) {
         g2.setColor(Color.gray);
         if(leftKey)
            g2.setColor(Color.black);
         g2.fillRect(30,50,10,10);
      
         g2.setColor(Color.gray);
         if(rightKey)
            g2.setColor(Color.black);
         g2.fillRect(60,50,10,10);
      
         g2.setColor(Color.gray);
         if(upKey)
            g2.setColor(Color.black);
         g2.fillRect(45,35,10,10);
      
         g2.setColor(Color.gray);
         if(downKey)
            g2.setColor(Color.black);
         g2.fillRect(45,50,10,10);
      }
   
      private class AL extends KeyAdapter { //action listener
         public void keyReleased(KeyEvent e){
            int key = e.getKeyCode();
         
            if(key == KeyEvent.VK_LEFT)
               leftKey = false;
            if(key == KeyEvent.VK_RIGHT)
               rightKey = false;		
            if(key == KeyEvent.VK_UP)
               upKey = false;
            if(key == KeyEvent.VK_DOWN)
               downKey = false;		
         }
         public void keyPressed(KeyEvent e){
            int key = e.getKeyCode();
         
            F5_Hit = false;
         
            if(key == KeyEvent.VK_LEFT)
               leftKey = true;
            if(key == KeyEvent.VK_RIGHT)
               rightKey = true;
            if(key == KeyEvent.VK_UP)
               upKey = true;
            if(key == KeyEvent.VK_DOWN)
               downKey = true;
            if(key == KeyEvent.VK_F5) {
               F5_Key = true;
               F5_Hit = true;
            }			
         }
      }
   
   }
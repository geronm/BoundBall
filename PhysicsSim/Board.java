// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)


	package PhysicsSim;

	import java.awt.*;
	import javax.swing.*;
	import java.awt.event.*;
	import java.util.Vector;
	import java.util.*;
	import java.lang.*;

	public class Board extends	JPanel implements	ActionListener	{
	
		javax.swing.Timer	time;
		Vector<Integer> myV;
		Random rgen;

		final	static int DELAY = 3;  
	
		final	static int STAGEWIDTH =	593;	
		final	static int STAGEHEIGHT = 323;//323;
		
		final	static int NEUTRAL =	56;
	
		final	static int GRIDX = 5;
	
		final	static int ITERATIONS =	10;
		int NUM_OF_BALLS= 161;//441;//324;//301;//441; Mesh/Salt
		final	static int INIT_RADIUS=8;
		final	static double FRICTION = 1;
		
		double[]	ballx;
		double[]	bally;
		double[]	ballr;		
		double[]	ballxv;
		double[]	ballyv;
		double[]	balle; //elasticity
		double[]	ballmass;
		int[]	balltype; //0 is neg, 1	is	pos, NEUTRAL is neutral, -1 is DNE
	
		LinkedList<Molecule>	moles	= new	LinkedList<Molecule>();	//linkedlist containing	all the molecules
		Map<Integer,Set<Integer>> moleMap =	new HashMap<Integer,Set<Integer>>((int)(NUM_OF_BALLS*1.4));	//map	that knows which atoms share molecules; an optimization thing ;)
		//COOL MEMORYSAVING IDEA: IMPLEMENT	A SENIORITY	SYSTEM IN THE HASHMAP, SO THAT INDICES
		//	WILL ONLY CONTAIN	THE INDICES	OF	SHARED ATOMS THAT	ARE GREATER	THAN THEY ARE.
		//	WOULD	REMOVE A	GREAT	DEAL OF REDUNDANCY..
		
		
		double gravity	= 0.05;//0.5;
		double polar_attraction	= 5; //0.1 for	supersmall,	5 for	moderate	small
		double custom_polar_attraction =	0;
		
		final	static boolean	LIKELIKE	= true;
		final	static boolean	OPPOSITE	= false;

		boolean fusion	= false; //Fusion makes it so that balls which overlap by a certain amount (the "fusionThreshold") fuse into a bigger ball
		double fusionThreshold = -1;
		boolean popping = false; //Popping makes it so that any ball with a radius that is greater than the INIT_RADIUS by a factor called the "poppingFactor" are kiled (balltype = -1)
		double poppingFactor = 6;
		boolean destruct = true; //Destruct allows the user to delete atoms and all of their bonds by touching them with the mouse pointer

		boolean attraction_type	= LIKELIKE;
	
		boolean leftKey,rightKey,upKey,downKey;
		boolean zeroKey;
		boolean zeroHit;
		boolean F5_Key;
		boolean F5_Hit;
	
		public Board()	{
		
		
 //RIGID TOWER (soon)
  			int meshHeight = 10;
			NUM_OF_BALLS -= (NUM_OF_BALLS-1)%meshHeight;
			
			initializeArrays();
			
			for(int i=0; i<NUM_OF_BALLS-1; i++)	{
				ballx[i]	= INIT_RADIUS+i*INIT_RADIUS/4;
				bally[i]	= INIT_RADIUS+2*INIT_RADIUS*(i%10);
				ballr[i]	= INIT_RADIUS;
			}
			LinkedList<Integer> list;
			for(int j=0; j<NUM_OF_BALLS-1; j+=meshHeight) {
				for(int i=0; i<meshHeight-1;	i++) {
					list = new LinkedList<Integer>();
					list.add(j+i);
					list.add(j+i+1);
					moles.add(createMolecule(list));
				}
			}
			for(int i=0; i<meshHeight; i++)	{
				for(int j=0; j<NUM_OF_BALLS-meshHeight-1; j+=meshHeight) {
					list = new LinkedList<Integer>();
					list.add(j+i);
					list.add(j+i+meshHeight);
					moles.add(createMolecule(list));
				}
			}
			ballr[NUM_OF_BALLS-1] =	INIT_RADIUS*5;
			balltype[NUM_OF_BALLS-1] =	-1; //last ball is dead, for now..
			
			
			
			for(int i=0; i<NUM_OF_BALLS; i++)
				ballmass[i]	= ballr[i]*ballr[i];
			
			myV =	new Vector<Integer>();
			myV.add(5);
			myV.add(4);
		
			leftKey=false;
			rightKey=false;
			upKey=false;
			downKey=false;
		
	
		
			addKeyListener(new AL());
			setFocusable(true);

			time = new javax.swing.Timer(DELAY,	this);
			time.start();			
		}
	
		public void	randomizeVelocities() {
			for(int i=0; i<NUM_OF_BALLS; i++) {	
				ballxv[i] =	.5*INIT_RADIUS*rgen.nextDouble()-INIT_RADIUS/4;
				ballyv[i] =	.5*INIT_RADIUS*rgen.nextDouble()-INIT_RADIUS/4;
			}
		}	
	
		public void	actionPerformed(ActionEvent e) {
		//p.move();
			if(!upKey) {
					//randomizeVelocities();

					//updateBoxPhysics();//updateBallPhysics();
					

//					moles	= new	LinkedList<Molecule>();
//					moleMap = new HashMap<Integer,Set<Integer>>((int)(NUM_OF_BALLS*1.4));				
//					LinkedList<Integer> list =	new LinkedList<Integer>();
//	//					for(int i=0; i<NUM_OF_BALLS-1; i++)
//	//						list.add(i);
//	//					moles.add(createMolecule(list));
//					for(int j=0; j<NUM_OF_BALLS-1; j+=10) {
//						list = new LinkedList<Integer>();
//						for(int i=0; i<10; i++)	{
//							list.add(j+i);
//						}								
//						moles.add(createMolecule(list));
//					}
			}				
			if(leftKey)	{
//					balltype[NUM_OF_BALLS-1] =	1;
//					ballx[NUM_OF_BALLS-1] =	STAGEWIDTH - 200;
//					bally[NUM_OF_BALLS-1] =	INIT_RADIUS*6;
			
				polar_attraction = 0;
			} else {
				polar_attraction = custom_polar_attraction;
			}
			updateBallPhysics();
			if(rightKey) {

				for(int i=0; i<NUM_OF_BALLS; i++) {
					ballxv[i] =	0;
					ballyv[i] =	0;
				}
			}
					
			repaint();
		}
	
		public void	updateBallPhysics() {
		
			double sepx	= 0;
			double sepy	= 0;
			ListIterator<Molecule> itr;			
			ListIterator<Ball> itr2;
			
			for(int i=0; i<NUM_OF_BALLS; i++) {	
			//This part of the code allows the user to kill balls with the mouse pointer, if "destruct"
			// is set to true.
			if(destruct) {
				Molecule	mole;
				try {
						sepx = ballx[i] -	getMousePosition(true).x;
						sepy = bally[i] -	getMousePosition(true).y;
						if(Math.abs(sepx) + Math.abs(sepy) < INIT_RADIUS*3) {
							if(Math.sqrt(sepx*sepx + sepy*sepy)	<=	INIT_RADIUS*2)	{
						
								//Kill the atom
								balltype[i]	= -1;
						
								//Delete all of its molecules (maybe later make it optional whether it kills the
								// molecule or simply removes the atom from it)
								LinkedList<Molecule> temp = new LinkedList<Molecule>();
								itr =	moles.listIterator();
								while(itr.hasNext())	{
									mole = itr.next();
									itr2 = mole.children.listIterator();
									while(itr2.hasNext())
										if(itr2.next().ID == i)
											temp.add(mole);
								}
								itr =	temp.listIterator();
								while(itr.hasNext())
									moles.remove(itr.next());							
								
								Iterator<Integer> itr3;
								//Remove all of the atom's references from the moleMap 
								HashSet<Integer> sharedAtoms;
								if(moleMap.containsKey(i))	{
									itr3 = moleMap.get(i).iterator();
									while(itr3.hasNext())
										moleMap.get(itr3.next()).remove(i);		
										moleMap.remove(i);
									}
								}		
							}
						
				} catch	(Exception e) { };
			}
			
			
				ballx[i]	+=	ballxv[i];
				bally[i]	+=	ballyv[i];
				//ball[i].ang += ball[i].angv;
				
		  //Keeps balls onscreen in a	rudimentary, pseudophysical way
				if(bally[i]+ballr[i]+ballyv[i] >	STAGEHEIGHT) {
					ballyv[i] *= -balle[i];
					bally[i]	= STAGEHEIGHT-ballr[i];
					
					ballxv[i] *= FRICTION;
				} 
				else if (bally[i]-ballr[i]+ballyv[i] <	0)	{
					ballyv[i] *= -balle[i];
					bally[i]	= ballr[i];
					
					ballxv[i] *= FRICTION;					
				}			
				if(ballx[i]+ballr[i]+ballxv[i] >	STAGEWIDTH)	{
					ballxv[i] *= -balle[i];
					ballx[i]	= STAGEWIDTH-ballr[i];
					
					ballyv[i] *= FRICTION;					
				} 
				else if (ballx[i]-ballr[i]+ballxv[i] <	0)	{
					ballxv[i] *= -balle[i];
					ballx[i]	= ballr[i];
					
					ballyv[i] *= FRICTION;					
				}
			
//				if(i>16)	{
			  ballyv[i]	+=	gravity;
//				} else {
//				  ballyv[i]	+=	gravity*0.05;			
//				}
			
			
			//Move the balls before	applying	physics (a mistake?)
			
			//Apply the	physics algorithms! The	first	ball is checked against	all the others, then	the
			//	second is checked	against all	but the first ball, then the third is checked against	all
			//	but the first two, etc.	until	all are checked. Iow, a	single-pass	algorithm
			//				for(int j=i+1;	j<NUM_OF_BALLS; j++)	{
			//					ballPhysics(ball[i],ball[j],true);
			//				}
		 //	  if(i<2){
					
			//		weldConstrain(ball[i],ball[1-i]);
		 //	  }

				//parent-child	system =	much nicer
				if(balltype[i]	==	-1) {
					ballx[i]	= -300;
					bally[i]	= -300;
				}
		  
			}
			
			//first pass, apply attraction forces
			for(int i=0; i<NUM_OF_BALLS; i++) {
				for(int j=i+1;	j<NUM_OF_BALLS; j++)	{
					if(!ballsShareMolecule(i,j))
						ballForces(i,j,true);
					//ballCollision(ball[i],ball[j]);
				}
			}
			

			double pen=0;
			//anti-overlap	iterations
			for(int n=0; n<ITERATIONS;	n++) {
			
//					for(int i=0; i<NUM_OF_BALLS; i++) {
//						for(int j=i+1;	j<NUM_OF_BALLS; j++)	{
//							if(!ballsShareMolecule(i,j)) {
//								pen =	ballForces(i,j,false);
//							}
//							//ballCollision(ball[i],ball[j]);
//						
//							if(fusion) {
//							//fuse high-colliding balls together
//								if(pen <	-fusionThreshold && n == ITERATIONS-1)	{
//									ballx[i]	= (ballx[i]*ballmass[i]	+ ballx[j]*ballmass[j])	/ (ballmass[i]+ballmass[j]);
//									bally[i]	= (bally[i]*ballmass[i]	+ bally[j]*ballmass[j])	/ (ballmass[i]+ballmass[j]);
//									ballmass[i]	= ballmass[i]+ballmass[j];
//									ballr[i]	= Math.sqrt(ballmass[i]);
//									balltype[j]	= -1;	
//									ballx[j]	= -300;
//									bally[j]	= -300;
//								}
//							}
//						}
//						
//						if(popping)
//							if(ballr[i]	> INIT_RADIUS*popFactor)
//									balltype[i]	= -1;
//					}		
//					
//					//correctmolecules
//					if(n%5 == 4) {
//						itr =	moles.listIterator();
//						while(itr.hasNext())
//							correctMolecule(itr.next());
//					}

				for(int i=NUM_OF_BALLS-1; i>=0; i--) {
					for(int j=i-1;	j>=0;	j--) {
						if(!ballsShareMolecule(i,j)) {
							pen =	ballForces(i,j,false);
						}
						//ballCollision(ball[i],ball[j]);
					
						if(fusion) {
						//fuse high-colliding balls together
							if(pen <	-0.1 && n == ITERATIONS-1)	{
								ballx[i]	= (ballx[i]*ballmass[i]	+ ballx[j]*ballmass[j])	/ (ballmass[i]+ballmass[j]);
								bally[i]	= (bally[i]*ballmass[i]	+ bally[j]*ballmass[j])	/ (ballmass[i]+ballmass[j]);
								ballmass[i]	= ballmass[i]+ballmass[j];
								ballr[i]	= Math.sqrt(ballmass[i]);
								balltype[j]	= -1;	
								ballx[j]	= -300;
								bally[j]	= -300;
							}
						}
					}
					
					if(ballr[i]	> INIT_RADIUS*6)
						balltype[i]	= -1;
				}		
				
				//correctmolecules
				if(n%5 == 4) {
					itr =	moles.listIterator();
					while(itr.hasNext())
						correctMolecule(itr.next());
				}
			}

			itr =	moles.listIterator();
			while(itr.hasNext())
				correctMolecule(itr.next());			

					
		//Testing purposes; hit	the "down" key	to	attach a	ball to your cursor!
			try {
				if(downKey)	{
					//ball[NUM_OF_BALLS-1].xv = 0;//getMousePosition(true).x	- ball[NUM_OF_BALLS-1].x;
					//ball[NUM_OF_BALLS-1].yv = 0;//getMousePosition(true).y	- ball[NUM_OF_BALLS-1].y;		
					//ball[NUM_OF_BALLS-1].x =	getMousePosition(true).x;
					//ball[NUM_OF_BALLS-1].y =	getMousePosition(true).y;
					for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS);	i++) {
						for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS);	j++) {
							int index =	i*(int)Math.sqrt(NUM_OF_BALLS)+j;
							ballx[index] =	60+i*INIT_RADIUS*2;
							bally[index] =	60+j*INIT_RADIUS*2;
							ballr[index] =	INIT_RADIUS;
						}
					}
					System.out.println("RESET!");
				}
			} 
				catch	(Exception e) { };
		}

		public void	correctMolecule(Molecule mole) {
			//take all of these atoms,	find their center	of	mass
			double centerx	= 0;
			double centery	= 0;
			int index;
			ListIterator<Ball> itr = mole.children.listIterator();
			while(itr.hasNext())	{
				index	= itr.next().ID;
				centerx += ballr[index]*ballr[index]*ballx[index];
				centery += ballr[index]*ballr[index]*bally[index];
			}
			centerx /= mole.mass;
			centery /= mole.mass;

			double centerang = 0, tempang;
			double tempx=0, tempy=0;
			Ball temp;
			itr =	mole.children.listIterator();
			while(itr.hasNext())	{
				temp = itr.next();

				if(ballx[temp.ID]<centerx)	{
					tempang = Math.atan((bally[temp.ID]-centery)/(ballx[temp.ID]-centerx))+Math.PI;
				} else if(ballx[temp.ID]>centerx) {
					tempang = Math.atan((bally[temp.ID]-centery)/(ballx[temp.ID]-centerx));
				} else {
					if(bally[temp.ID]>centery)	{
						tempang = Math.PI/2;
					} else {
						tempang = -Math.PI/2;
					}
				}
							
				tempx	+=	Math.cos(tempang - temp.ang)*ballr[temp.ID]*ballr[temp.ID];	//still need to "weight" the directions, eventually
				tempy	+=	Math.sin(tempang - temp.ang)*ballr[temp.ID]*ballr[temp.ID];
			}

			if(tempx<0)	{
				centerang =	Math.atan(tempy/tempx)+Math.PI;
			} else if(tempx>0) {
				centerang =	Math.atan(tempy/tempx);
			} else {
				if(tempy>0)	{
				centerang =	Math.PI/2;
				} else {
					centerang =	-Math.PI/2;
				}
			}
				
			//centerang	/=	mole.mass;
			//centerang	= 0;

			itr =	mole.children.listIterator();
			while(itr.hasNext())	{
				temp = itr.next();
				tempx	= centerx +	temp.r*Math.cos(centerang+temp.ang);
				tempy	= centery +	temp.r*Math.sin(centerang+temp.ang);
				ballxv[temp.ID] += tempx -	ballx[temp.ID];
				ballyv[temp.ID] += tempy -	bally[temp.ID];				
				ballx[temp.ID]	= tempx;
				bally[temp.ID]	= tempy;
			}
			
			mole.x =	centerx;
			mole.y =	centery;
		}

		public boolean	ballsShareMolecule(int i, int	j)	{
			if(!moleMap.containsKey(i)	||	!moleMap.containsKey(j))
				return false;
			
			if(moleMap.get(i).contains(j))
				return true;
				
			return false;
		
			
//				ListIterator<Molecule> itr	= moles.listIterator();
//				ListIterator<Ball> itr2;
//				Molecule	temp;
//				int tempID;
//				int hits;
//				while(itr.hasNext())	{
//					temp = itr.next();
//					itr2 = temp.children.listIterator();
//					hits = 0;
//					while(itr2.hasNext()) {
//						tempID =	itr2.next().ID;
//						if(tempID == i	||	tempID == j)
//							hits++;
//					}
//					if(hits > 1)
//						return true;
//				}
//				return false;
		}


		//FAILED; And unnecessary.. probably inefficient to boot
		public void	collideFromWalls() {
			
			LinkedList<LinkedList<Integer>> priority = new LinkedList<LinkedList<Integer>>();
			//priority.add(new LinkedList<Integer>);
			
			for(int i=0; i<NUM_OF_BALLS; i++) {
				if(ballx[i]-ballr[i]	< 1 || ballx[i]+ballr[i] >	STAGEWIDTH-1 || bally[i]-ballr[i] <	1 || bally[i]+ballr[i] > STAGEHEIGHT-1)
				{
					priority.getFirst().add(i);
				}
			}	
			
			//listIterator			
		}
				
		//OPTIMIZED	Done a very-basic	"are these balls anywhere near
		//	each other?" test.. ;)
		public double ballForces(int i, int	j,	boolean forces) {			
			double sepx	= ballx[j]-ballx[i];
			double sepy	= bally[j]-bally[i];
	
			if(balltype[i]	==	-1	||	balltype[j]	==	-1)
				return 0;
		
			//optimization
			if(!forces)	{
				if(Math.abs(sepx)+Math.abs(sepy)>1.5*(ballr[i]+ballr[j]))
					return 0;
			}
				
			double dist	= Math.sqrt(sepx*sepx +	sepy*sepy);	//duh
			
			//safety, the reason	why computer models are	imperfect 
			if(dist <= 0.05) {
				dist += 0.001;
				dist = Math.sqrt(dist);			}

			if(forces && balltype[i] != NEUTRAL	&&	balltype[j]	!=	NEUTRAL)	{
				if((balltype[i] != balltype[j] && attraction_type == OPPOSITE)	||	(balltype[i] == balltype[j] && attraction_type == LIKELIKE)) {
					ballxv[i] += polar_attraction*(sepx/(dist*dist*dist));
					ballyv[i] += polar_attraction*(sepy/(dist*dist*dist));
						  
					ballxv[j] -= polar_attraction*(sepx/(dist*dist*dist));
					ballyv[j] -= polar_attraction*(sepy/(dist*dist*dist));
				} else {
					ballxv[i] -= polar_attraction*(sepx/(dist*dist*dist));
					ballyv[i] -= polar_attraction*(sepy/(dist*dist*dist));
						  
					ballxv[j] += polar_attraction*(sepx/(dist*dist*dist));
					ballyv[j] += polar_attraction*(sepy/(dist*dist*dist));				
				}
			}
			
			return correctOverlap(i,j,sepx,sepy,dist);
			
		}
  
		public double correctOverlap(int	i,	int j, double sepx, double	sepy,	double dist) {
			double pen = dist-ballr[i]-ballr[j]; //penetration	b/w two disks;	aka overlap
		
	
		//standard collision	detection
 
			//if overlap is present
				if(pen <	0)	{
				//For	each ball, the	calculation	takes	its proportion	of	square radius b/w	the two of them, then moves it
				//	and modifies its velocity in the	direction of overlap	(sepxy/dist) by a	magnitude equal to the overlap
				//	itself. eg.	x += pen	* (sepx/dist) * (r1^2/(r1^2+r2^2)) 
				
					double k	= dist*(ballmass[i] + ballmass[j]);	//denominator of both movement commands
					//double	v1v2 = 0.1*Math.sqrt(ball1.xv*ball2.xv+ball1.yv*ball2.yv);
				
				ballx[i]	+=	pen*sepx*ballmass[j]/k;
				bally[i]	+=	pen*sepy*ballmass[j]/k;

				ballxv[i]	+=	pen*sepx*ballmass[j]/k;
				ballyv[i]	+=	pen*sepy*ballmass[j]/k;
				
				ballx[j]	-=	pen*sepx*ballmass[i]/k;
				bally[j]	-=	pen*sepy*ballmass[i]/k;
				
				ballxv[j]	-=	pen*sepx*ballmass[i]/k;
				ballyv[j]	-=	pen*sepy*ballmass[i]/k;
	
				}
			
			 return pen;
		
		}
	
		public Molecule createMolecule(LinkedList<Integer>	atoms) {
			//take all of these atoms,	find their center	of	mass
			double mass	= 0;
			double centerx	= 0;
			double centery	= 0;
			double temp;
			int index;
			ListIterator<Integer> itr = atoms.listIterator();
			while(itr.hasNext())	{
				index	= itr.next();
				temp = ballr[index]*ballr[index]; //here,	"temp" is mass
				mass += temp;
				centerx += temp*ballx[index];
				centery += temp*bally[index];
			}
			centerx /= mass;
			centery /= mass;
									
			//create	"Ball" instances,	each with x/y (or	r/theta?) coordinates relative to this	center
			itr =	atoms.listIterator();
			LinkedList<Ball> balls = new LinkedList<Ball>();
			while(itr.hasNext())	{
				index	= itr.next();
				balls.add(new Ball(ballx[index]-centerx,bally[index]-centery, index));
			}				
			
			//pass these instances in a linkedlist	to	a new	molecule
			Molecule	molecule	= new	Molecule(balls, centerx, centery, mass);
			
			//add	this molecule's data	to	the "moleMap",	the data	structure that	knows	which
			//	atoms	share	molecules
			itr =	atoms.listIterator();
			ListIterator<Integer> itr2;
			int index2;
			while(itr.hasNext())	{
				index	= itr.next();
				if(!moleMap.containsKey(index))
					moleMap.put(index,new HashSet<Integer>());
				itr2 = atoms.listIterator();
				while(itr2.hasNext()) {
					index2 =	itr2.next();
					if(index	!=	index2)
						moleMap.get(index).add(index2);
				}
			}
			
			return molecule;
		}
	
		public void	paint(Graphics	g)	{
			super.paint(g);
			Graphics2D g2 = (Graphics2D)g;
		
			RenderingHints	rh	= new	RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHints(rh);
		
		//g2.drawImage(bgimg,0,0,null);
		//g2.drawImage(p.getImage(),p.getX(),p.getY(),null);
		
			for(int i=0; i<NUM_OF_BALLS; i++) {
				if(balltype[i]	==	1)	{	
					g2.setColor(Color.blue);
				} else if (balltype[i] == 0) {
					g2.setColor(Color.red);
				} else if (balltype[i] == 2) {
					g2.setColor(new Color(100,0,100));
				} else if (balltype[i] == NEUTRAL) {
					g2.setColor(Color.gray);
				}
				g2.drawOval((int)(ballx[i]-ballr[i]),(int)(bally[i]-ballr[i]),(int)(ballr[i]*2),(int)(ballr[i]*2));
						
		  //	  g2.setColor(Color.black);
		  //	  g2.drawLine(ball[i].getX(),	ball[i].getY(), ball[i].getX()+(int)(ball[i].r*Math.cos(ball[i].ang)), ball[i].getY()+(int)(ball[i].r*Math.sin(ball[i].ang)));
			}
			
			ListIterator<Molecule> itr	= moles.listIterator();
			Molecule	mole;
			g2.setColor(Color.green);
			while(itr.hasNext())	{
				mole = itr.next();
				ListIterator<Ball> itr2	= mole.children.listIterator();
				Ball ball1,ball2;
				if(itr2.hasNext()) {
					ball1	= itr2.next();
					while(itr2.hasNext()) {
						ball2	= ball1;
						ball1	= itr2.next();
						
						g2.drawLine((int)ballx[ball1.ID],(int)bally[ball1.ID],(int)ballx[ball2.ID],(int)bally[ball2.ID]);
					}
				}
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
		
		public void initializeArrays() {
			ballx	= new	double[NUM_OF_BALLS];
			bally	= new	double[NUM_OF_BALLS];
			ballr	= new	double[NUM_OF_BALLS];		
			ballxv =	new double[NUM_OF_BALLS];
			ballyv =	new double[NUM_OF_BALLS];
			balle	= new	double[NUM_OF_BALLS]; //elasticity
			ballmass	= new	double[NUM_OF_BALLS];
			balltype	= new	int[NUM_OF_BALLS];		
		}
	
		private class AL extends KeyAdapter	{ //action listener
			public void	keyReleased(KeyEvent	e){
				int key = e.getKeyCode();
			
				if(key == KeyEvent.VK_LEFT)
					leftKey = false;
				if(key == KeyEvent.VK_RIGHT)
					rightKey	= false;		
				if(key == KeyEvent.VK_UP)
					upKey	= false;
				if(key == KeyEvent.VK_DOWN)
					downKey = false;		
			}
			public void	keyPressed(KeyEvent e){
				int key = e.getKeyCode();
			
				F5_Hit =	false;
			
				if(key == KeyEvent.VK_LEFT)
					leftKey = true;
				if(key == KeyEvent.VK_RIGHT)
					rightKey	= true;
				if(key == KeyEvent.VK_UP)
					upKey	= true;
				if(key == KeyEvent.VK_DOWN)
					downKey = true;
				if(key == KeyEvent.VK_F5) {
					F5_Key =	true;
					F5_Hit =	true;
				}			
			}
		}
			
	}
	
//	
//	
//		  public	void weldConstrain2(Ball ballp, Ball ballc) {
//			
//				double totalmass = ballc.mass	+ ballp.mass;
//				
//				double deltax = ballc.x-(ballp.r*Math.cos(ballp.ang))-ballp.x;
//				double deltay = ballc.y-(ballp.r*Math.sin(ballp.ang))-ballp.y;			
//				
//				double deltax1	= deltax*ballc.mass/totalmass;
//				double deltay1	= deltay*ballc.mass/totalmass;
//				double deltax2	= deltax1 -	deltax;
//				double deltay2	= deltay1 -	deltay;
//				
//				double vxprime1 =	ballp.xv	+ deltax1;
//				double vyprime1 =	ballp.yv	+ deltay1;
//				double vxprime2 =	ballc.xv	+ deltax2;
//				double vyprime2 =	ballc.yv	+ deltay2;
//				
//				ballp.xv	= (ballp.mass*vxprime1 + ballc.mass*vxprime2)/totalmass;
//				ballc.xv	= ballp.xv;
//				ballp.yv	= (ballp.mass*vyprime1 + ballc.mass*vyprime2)/totalmass;
//				ballc.yv	= ballp.xv;
//				
//				ballp.x += deltax1;
//				ballp.y += deltay1;
//				ballc.x += deltax2;
//				ballc.y += deltay2;
//			}
//	  
//			public double ballCollision(Ball	ball1, Ball	ball2) {
//				double modx	= ball2.xv;
//				double mody	= ball2.yv;
//				
//				ball2.xv	= 0;
//				ball2.yv	= 0;
//				ball1.xv	-=	modx;
//				ball1.yv	-=	mody;
//				
//				double distance =	getStaticBallCollision(ball1,ball2);
//				
//				ball2.xv	= modx;
//				ball2.yv	= mody;
//	
//				//collision	happened
//				if(distance	!=	0)	{
//				
//					//new	def of modx, now it's the multiplier for the	move vectors			
//					double temp	= 0;
//					if((ball1.xv != 0) || (ball1.yv != 0))
//						temp = distance/Math.sqrt(ball1.xv*ball1.xv + ball1.yv*ball1.yv);
//					
//					ball1.xv	+=	modx;
//					ball1.yv	+=	mody;		
//	
//					ball1.x += ball1.xv * temp;
//					ball1.y += ball1.yv * temp;
//					ball2.x += ball2.xv * temp;
//					ball2.y += ball2.yv * temp;
//					
//					/*
//					//MOMENTUM STUFF:
//					
//					//normalized vector from center to center
//					double dist	= Math.sqrt((ball2.x-ball1.x)*(ball2.x-ball1.x)	+ (ball2.y-ball1.y)*(ball2.y-ball1.y));
//					double n_x = (ball2.x-ball1.x)/dist;
//					double n_y = (ball2.y-ball1.y)/dist;
//					
//					//components of v's along n
//					double a1 =	ball1.xv*n_x +	ball1.yv*n_y;
//					double a2 =	ball2.xv*n_x +	ball2.yv*n_y;
//					
//					double optimizedP	= 2*(a1-a2)/(ball1.mass+ball2.mass);
//					
//					//total elasticity of collision
//					double e	= ball1.elasticity*ball2.elasticity;
//					
//					//calculate	new velocities
//					ball1.xv	-=	e*(optimizedP*ball2.mass*n_x); //yes, that IS ballTWO
//					ball1.yv	-=	e*(optimizedP*ball2.mass*n_y);
//					ball2.xv	+=	e*(optimizedP*ball1.mass*n_x);
//					ball2.yv	+=	e*(optimizedP*ball1.mass*n_y);				
//					
//					*/
//					
//					return distance;
//				}
//						
//				ball1.xv	+=	modx;
//				ball1.yv	+=	mody;
//				
//				return distance;
//				
//			}			
//					
//	  
//			//To use	this function effectively,	after	function	is	called do this:  
//			//ball1.xv = xv_norm	* distance;
//			//ball1.yv = yv_norm	* distance;
//			public double getStaticBallCollision(Ball	ball1, Ball	ball2) {
//	
//				double dist	= Math.sqrt((ball2.x-ball1.x)*(ball2.x-ball1.x)	+ (ball2.y-ball1.y)*(ball2.y-ball1.y));
//				double sumRadii =	ball1.r + ball2.r;
//				
//				dist -= sumRadii;
//				
//				//if the	total	distance	covered by the	movement	of	the balls isn't enough to bring them
//				//	within range of one another, no collision	could	POSSIBLY	have occured
//				if(ball1.xv*ball1.xv+ball1.yv*ball1.yv	< dist*dist)
//					return 0; //no	collision possible
//	
//				dist += sumRadii;
//	
//				//waited	till after the	above	to	do	this,	because the	above	scenario
//				//	is	by	far the most likely,	so	it's fastest to save	the calculation
//				//	for unlikelihood
//				double v_mag =	Math.sqrt(ball1.xv*ball1.xv+ball1.yv*ball1.yv);			
//				
//				double xv_norm=0,	yv_norm=0;
//				
//				if	(v_mag >	0)	{
//					xv_norm = ball1.xv /	v_mag;
//					yv_norm = ball1.yv /	v_mag;
//				}
//				
//				double sepx	= ball2.x-ball1.x;
//				double sepy	= ball2.y-ball1.y;
//				
//				//D =	V_Norm dot Sep	= dist *	cos (velocity-displacement	angle)
//				double D	= sepx*xv_norm	+ sepy*yv_norm;
//				
//				//D <= 0	would	indicate	that the	two balls are moving	in	opposite	directions
//				if(D <= 0)
//					return 0;
//				
//				//shortest dist from	center of ball2 to line	of	ball1's velocity
//				double F	= dist*dist	- D*D;
//				
//				//If the	below	is	true,	then ball1 necessarily misses	ball2.
//				if(F > sumRadii *	sumRadii)
//					return 0;
//					
//				double T	= sumRadii*sumRadii - F;
//				
//				//Distance to move along movement vecotr such that	they touch
//				double distance =	D - Math.sqrt(T);
//				
//				//ball must	be	traveling far enough	to	touch
//				if	(v_mag <	distance)
//					return 0;
//					
//				return distance;
//			}





//CREATES BUBBLE
//				for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS);	i++) {
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS);	j++) {
//						int index =	i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						ballx[index] =	60+i*INIT_RADIUS*2; //INIT_RADIUS*(index%2);
//						bally[index] =	INIT_RADIUS+j*INIT_RADIUS*1.2;//2.1;
//						ballr[index] =	INIT_RADIUS;
//						balle[index] =	0.75;
//						balltype[index] =	1;
//					}
//				}
//				
//				LinkedList<Integer> list =	new LinkedList<Integer>();			
//				for(int i=0; i<16; i++)	{
//					ballx[i]	= 100	+ INIT_RADIUS*5*Math.cos(i*Math.PI*2/16);
//					bally[i]	= STAGEHEIGHT - 5*INIT_RADIUS	+ INIT_RADIUS*5*Math.sin(i*Math.PI*2/16);
//					ballr[i]	= INIT_RADIUS*2;
//					list.add(i);
//				}
//				moles.add(createMolecule(list));
	

//CREATES WATER MOLECULES:
//				LinkedList<Integer> list;
//				int index;
//				for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS);	i++) {
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS);	j++) {
//						index	= i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						ballx[index] =	20+i*INIT_RADIUS*2.1+INIT_RADIUS*1.2*(index%2);	//INIT_RADIUS*(index%2);
//						bally[index] =	20+j*INIT_RADIUS*1.2;//2.1;
//						ballr[index] =	INIT_RADIUS;
//						balle[index] =	0.75;
//						balltype[index] =	1;
//					}
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS)-2; j+=3) {
//						index	= i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						list = new LinkedList<Integer>();
//						list.add(index);
//						list.add(index+1);
//						ballr[index+1]	= INIT_RADIUS * 1.5;//
//						balltype[index+1]	= 0;
//						list.add(index+2);	
//						moles.add(createMolecule(list));			
//					}				
//				} 
	
		//PHOSPHOLIPID
//				for(index =	5;	index< 5+7*(30); index+=7)	{		
//					list = new LinkedList<Integer>();
//					list.add(index);
//					ballx[index] =	index*INIT_RADIUS	+ 50 +	INIT_RADIUS*0.9;
//					bally[index] =	100-INIT_RADIUS*1.8;
//					ballr[index] =	INIT_RADIUS*1.8;
//					for(int j=1; j<5;	j++) {
//						ballx[index+j]	= index*INIT_RADIUS + 50 +	INIT_RADIUS*(j%2)*1;
//						bally[index+j]	= 100	+ INIT_RADIUS*j*1;
//						balltype[index+j]	= NEUTRAL;
//						ballr[index+j]	= INIT_RADIUS*1.5;
//						list.add(index+j);
//					}
//					moles.add(createMolecule(list));
//				}

//			SALTDEMO
//				LinkedList<Integer> list;
//				for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS);	i++) {
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS);	j++) {
//						int index =	i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						ballx[index] =	60+i*INIT_RADIUS*2; //INIT_RADIUS*(index%2);
//						bally[index] =	60+j*INIT_RADIUS*2;//2.1;
//						ballr[index] =	INIT_RADIUS;
//						balle[index] =	0.75;
//						balltype[index] =	0;
//						if((index%2==1	||	i%2==1) && !(index%2==1	&&	i%2==1))
//							balltype[index] =	1;
//					}
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS)-1; j+=2) {
//						int index =	i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						list = new LinkedList<Integer>();
//						list.add(index);
//						list.add(index+1);
//						if(i%2 == 1) {
//							list.add(index-(int)Math.sqrt(NUM_OF_BALLS));
//							list.add(index-(int)Math.sqrt(NUM_OF_BALLS)+1);
//						}
//						moles.add(createMolecule(list));			
//					}		
//				}
			
		
			
//				//CREATE	A MOLECULE
//				LinkedList<Integer> list =	new LinkedList<Integer>();
//				list.add(5);
//				list.add(17);
//				list.add(18);
//				list.add(22);
//				ballx[5]	= 400;
//				bally[5]	= 150;
//				ballx[17] =	400+1.5*INIT_RADIUS;
//				bally[17] =	150;
//				ballx[18] =	400+1.5*INIT_RADIUS;
//				bally[18] =	150+1.5*INIT_RADIUS;
//				ballx[22] =	400;
//				bally[22] =	150+1.5*INIT_RADIUS;
//				moles.add(createMolecule(list));
//				
//				//CREATE	A MOLECULE
//				list = new LinkedList<Integer>();
//				list.add(2);
//				list.add(3);
//				list.add(16);
//				ballx[2]	= 300;
//				bally[2]	= 150;
//				ballx[3]	= 300+2*INIT_RADIUS;
//				bally[3]	= 150;
//				ballx[16] =	300+INIT_RADIUS;
//				bally[16] =	150+INIT_RADIUS;
//				moles.add(createMolecule(list));			

//	//STRINGS
//				for(int i=0; i<NUM_OF_BALLS; i++) {
//					ballx[i]	= INIT_RADIUS+i*INIT_RADIUS/4;
//					bally[i]	= INIT_RADIUS+2*INIT_RADIUS*(i%10);
//					ballr[i]	= INIT_RADIUS;
//				}
//				LinkedList<Integer> list;
//				for(int j=0; j<NUM_OF_BALLS; j+=10)	{
//					for(int i=0; i<9;	i++) {
//						list = new LinkedList<Integer>();
//						list.add(j+i);
//						list.add(j+i+1);
//						moles.add(createMolecule(list));
//					}
//				}

//STRINGS
//				for(int i=0; i<NUM_OF_BALLS; i++) {
//					ballx[i]	= INIT_RADIUS+i*INIT_RADIUS/4;
//					bally[i]	= INIT_RADIUS+2*INIT_RADIUS*(i%10);
//					ballr[i]	= INIT_RADIUS;
//				}
//				LinkedList<Integer> list;
//				for(int j=0; j<NUM_OF_BALLS; j+=10)	{
//					for(int i=0; i<9;	i++) {
//						list = new LinkedList<Integer>();
//						list.add(j+i);
//						list.add(j+i+1);
//						moles.add(createMolecule(list));
//					}
//				}


//			//FUNKY MOLECULE
//				LinkedList<Integer> list =	new LinkedList<Integer>();			
//				for(int i=0; i<16; i++)	{
//					ballx[i]	= 125	+ INIT_RADIUS*8*Math.cos(i*Math.PI*2/15);
//					bally[i]	= STAGEHEIGHT - 8*INIT_RADIUS	+ -INIT_RADIUS*12*Math.sin(i*Math.PI*2/8);
//					ballxv[i] =	30;
//					ballyv[i] =	-5;
//					ballr[i]	= INIT_RADIUS*2;
//					balltype[i]	= 1;
//					list.add(i);
//				}
//				moles.add(createMolecule(list));		


//	//PHOSPHOLIPID	BI-LAYER:
//				rgen = new Random();
//				int index;
//				LinkedList<Integer> list;
//				for(int i=0; i<(int)Math.sqrt(NUM_OF_BALLS);	i++) {
//					for(int j=0; j<(int)Math.sqrt(NUM_OF_BALLS);	j++) {
//						index	= i*(int)Math.sqrt(NUM_OF_BALLS)+j;
//						ballx[index] =	INIT_RADIUS	+ i*INIT_RADIUS*4; //INIT_RADIUS*(index%2);
//						bally[index] =	INIT_RADIUS	+ j*INIT_RADIUS*2;//2.1;
//						ballr[index] =	INIT_RADIUS;
//						balle[index] =	0.75;
//						balltype[index] =	1;
//					}
//				}
//	
//			//PHOSPHOLIPID
//				double pr =	2;//1.8;	//phosphate	head radius	ratio
//				double numlinks =	7;
//				double init_y = 80; //down	from 110
//				for(index =	(int)Math.sqrt(NUM_OF_BALLS) - (int)numlinks; index <	NUM_OF_BALLS; index+=Math.sqrt(NUM_OF_BALLS)) {		
//					list = new LinkedList<Integer>();
//					list.add(index);
//					ballx[index] =	(index+numlinks-Math.sqrt(NUM_OF_BALLS))*INIT_RADIUS*2*pr/Math.sqrt(NUM_OF_BALLS) +	INIT_RADIUS*pr;
//					bally[index] =	init_y-INIT_RADIUS*pr;
//					ballr[index] =	INIT_RADIUS*pr;
//					for(int j=1; j<numlinks; j++)	{
//						ballx[index+j]	= (index+numlinks-Math.sqrt(NUM_OF_BALLS))*INIT_RADIUS*2*pr/Math.sqrt(NUM_OF_BALLS)	+ INIT_RADIUS*(j%2)*1;
//						bally[index+j]	= init_y	+ INIT_RADIUS*j*1;
//						balltype[index+j]	= 0;//NEUTRAL;
//						ballr[index+j]	= INIT_RADIUS*1.5;
//						list.add(index+j);
//					}
//					moles.add(createMolecule(list));
//					
//					index	-=	numlinks;
//					
//					list = new LinkedList<Integer>();
//					list.add(index);
//					ballx[index] =	(index+2*numlinks-Math.sqrt(NUM_OF_BALLS))*INIT_RADIUS*2*pr/Math.sqrt(NUM_OF_BALLS)	+ INIT_RADIUS*pr;
//					bally[index] =	init_y+INIT_RADIUS*(2+numlinks+pr*2);
//					ballr[index] =	INIT_RADIUS*pr;
//					balltype[index] =	1;
//					for(int j=1; j<numlinks;	j++) {
//						ballx[index+j]	= (index+2*numlinks-Math.sqrt(NUM_OF_BALLS))*INIT_RADIUS*2*pr/Math.sqrt(NUM_OF_BALLS) + INIT_RADIUS*pr +	INIT_RADIUS*(j%2)*1;
//						bally[index+j]	= init_y	+ INIT_RADIUS*(2+numlinks+pr*2) + -INIT_RADIUS*j*1;
//						balltype[index+j]	= 0;//NEUTRAL;
//						ballr[index+j]	= INIT_RADIUS*1.5;
//						list.add(index+j);
//					}
//					moles.add(createMolecule(list));
//					
//					index	+=	numlinks;
//				}
//				
//				//shift all	the other molecules to the	other	side
//				for(int i=0; i<NUM_OF_BALLS; i++) {
//					if(i%(int)Math.sqrt(NUM_OF_BALLS) <	((int)Math.sqrt(NUM_OF_BALLS))-numlinks*2) {
//						if(bally[i]	> init_y-INIT_RADIUS*pr) {
//							bally[i]	+=	INIT_RADIUS*(1+2*numlinks+pr*2);
//							balltype[i]	= 1;
//						}
//					}
//				}

// //MESH + Ball
//  			int meshHeight = 10;
// 			NUM_OF_BALLS -= (NUM_OF_BALLS-1)%meshHeight;
// 			
// 			initializeArrays();
// 			
// 			for(int i=0; i<NUM_OF_BALLS-1; i++)	{
// 				ballx[i]	= INIT_RADIUS+i*INIT_RADIUS/4;
// 				bally[i]	= INIT_RADIUS+2*INIT_RADIUS*(i%10);
// 				ballr[i]	= INIT_RADIUS;
// 			}
// 			LinkedList<Integer> list;
// 			for(int j=0; j<NUM_OF_BALLS-1; j+=meshHeight) {
// 				for(int i=0; i<meshHeight-1;	i++) {
// 					list = new LinkedList<Integer>();
// 					list.add(j+i);
// 					list.add(j+i+1);
// 					moles.add(createMolecule(list));
// 				}
// 			}
// 			for(int i=0; i<meshHeight; i++)	{
// 				for(int j=0; j<NUM_OF_BALLS-meshHeight-1; j+=meshHeight) {
// 					list = new LinkedList<Integer>();
// 					list.add(j+i);
// 					list.add(j+i+meshHeight);
// 					moles.add(createMolecule(list));
// 				}
// 			}
// 			ballr[NUM_OF_BALLS-1] =	INIT_RADIUS*5;
// 			balltype[NUM_OF_BALLS-1] =	-1; //last ball is dead, for now..
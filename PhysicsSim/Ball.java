// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)
//
//4-28-12
// Ball Class; will represent a 2D circle, ideally

package PhysicsSim;

import java.util.LinkedList;
import java.util.ListIterator;

public class Ball {

	final static int HYDRO = 1, SEMI = 2, LIPID = 3;
	public double x,y,xv,yv,ang,angv;
	public double r,mass,elasticity;
	public int type;
	public LinkedList<Ball> children;
	
	public int ID;
	
	public double xa,ya,anga;	
	
	public double welda, weldr;
	
	public Ball(double in_x, double in_y, int in_ID) {
		x = in_x;
		y = in_y;
		r = Math.sqrt(in_x*in_x + in_y*in_y);

		if(x<0) {
			ang = Math.atan(y/x)+Math.PI;
		} else if(x>0) {
			ang = Math.atan(y/x);
		} else {
			if(y>0) {
				ang = Math.PI/2;
			} else {
				ang = -Math.PI/2;
			}
		}
		
		ID = in_ID;
	}
	
	public Ball(double in_x, double in_y, double in_radius) {
		x = in_x;
		y = in_y;
		r = in_radius;
		mass = in_radius*in_radius;
		elasticity = 0.9;//to be changed, for sure
		
		children = new LinkedList<Ball>();

		type = HYDRO;
	}
	
	public Ball(double in_x, double in_y, double in_radius, double in_mass) {
		x = in_x;
		y = in_y;
		r = in_radius;
		mass = in_mass;
		
		children = new LinkedList<Ball>();

		type = HYDRO;
	}
	
	public void addChild(Ball ball) { children.add(ball); }	
	public LinkedList<Ball> getChildren() { return children; }
	
	public boolean isAncestorOf(Ball ball) {
		if(children.size() > 0) {
			Ball current;
			ListIterator<Ball> itr = children.listIterator();
			
			while (itr.hasNext()) {
				current = itr.next();
				if (current == ball) return true;
				if (current.isAncestorOf(ball)) return true;
			}
		}
		
		return false;
	}
	
	public boolean isChildOf(Ball ball) {
		return ball.isAncestorOf(this);
	}
	
	//NOTE! ONLY DIRECT LINEAGE; RECOGNIZES ANCESTOR/DESCENDANT, NOT "COUSINS" OR "UNCLES"
	public boolean isRelatedTo(Ball ball) {
		return (isAncestorOf(ball) || ball.isAncestorOf(this));
	} 
			
	public int getX() { return (int)x; }
	public int getY() { return (int)y; }
	public int getR() { return (int)r; }		
	
}
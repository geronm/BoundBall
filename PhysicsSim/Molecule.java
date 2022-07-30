// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)
//
//4-28-12
// Ball Class; will represent a 2D circle, ideally

package PhysicsSim;

import java.util.LinkedList;
import java.util.ListIterator;

public class Molecule {

	final static int HYDRO = 1, SEMI = 2, LIPID = 3;
	public double x,y,ang,mass;//,xv,yv,ang,angv;
	//public double r,mass,elasticity;
	//public int type;
	public LinkedList<Ball> children;
	
	//public double xa,ya,anga;	
	
	public Molecule(LinkedList<Ball> in_children, double in_x, double in_y, double in_mass) {
		children = in_children;
		x=in_x;
		y=in_y;
		mass=in_mass;
	}
	
	public void addChild(Ball ball) { children.add(ball); }	
	public LinkedList<Ball> getChildren() { return children; }
	
	public int getX() { return (int)x; }
	public int getY() { return (int)y; }	
	
}
// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)
//


//4-28-12
// Ball Class; will represent a 2D circle, ideally

package PhysicsSim;

public class Ball {

	public double x,y,xv,yv,r;
	
	public Ball(double in_x, double in_y, double in_radius) {
		x = in_x;
		y = in_y;		
		r = in_radius;
	}
	
	public int getX() { return (int)x; }
	public int getY() { return (int)y; }
	public int getR() { return (int)r; }		
	
}
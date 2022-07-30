// copyright Geronimo Mirano 2012 - 2022.
//
// MIT License (See LICENSE.txt for full license)


package PhysicsSim;

import javax.swing.*;

public class Frame {

	public static void main(String args[]) {
		JFrame f = new JFrame("2D Game");
		//f.getContentPane().add("Center", new Board());
		f.add("Center", new Board());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(600,365);
		// f.setSize(1200,700);
		f.setVisible(true);
	}
}
/*
  A coin can be clicked to generate money for the player. Each coin can have different values.
*/

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

@SuppressWarnings("serial") //make the linter/compiler shut up
public class Coin extends Entity {

	int value;

	public boolean isWithinRange(Point2D.Double testPoint) {
		/*
			checks if testPoint is within the vicinity of the coin's clickbox
		*/
		if(	testPoint.getX() >= this.position.getX()-imgWidth/2 && testPoint.getX()<= this.position.getX() + imgWidth/2 &&
			testPoint.getY() >= this.position.getY()-imgHeight/2 && testPoint.getY()<= this.position.getY() + imgHeight/2	) {
			return true;
		}
		else {
			return false;
		}


	}

	public Coin(Point2D.Double x, int value) {
		super(x, "assets/img/coins/coin1.png");
		this.speed = 0.5;
		this.value = value; //value dropped by the fish depending on maturity
			//temp value //should change depending on maturity

		System.out.println("New coin: "+value);

		imgWidth = img.getWidth();
    	imgHeight = img.getHeight();
		startThread();
	}

	public void die(){
		//remove from view
		//remove from current game coin list
		isAlive = false;
		App.getOngoingGame().getCoins().remove(this);
		App.getOngoingGame().addMoney(value);
	}

	public void update(){
		double x = this.position.getX(), y = this.position.getY();
        if(this.position.getY() >= App.getScreenHeight() - (App.getScreenHeight() * 0.2)) {
        	try {
        		Thread.sleep(3000);
        	}
        	catch(InterruptedException ex) {}
			die();
        }
        else {
        	y += this.speed;  // y-position
			// System.out.println("Location of Coin: " + y);
        }
        this.position.setLocation(x, y);
	}
}

/*
  Game object stores all data specific to the whole game. This is also the view that contains all gameplay objects for the GUI.
*/
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.util.*;

@SuppressWarnings("serial") //make the linter/compiler shut up
public class Game extends JPanel{
	private final long SCARY_TIMESTAMP = 214 * 1000000;

	private String playerName;
	private int foodNumber;
	private int money;
	private int timer;
	private ArrayList<Fish> fish = new ArrayList<Fish>();		//arraylist variables are the representations of the entities in the GUI
	private ArrayList<Coin> coins = new ArrayList<Coin>();
	private ArrayList<Food> foods = new ArrayList<Food>();
	private static boolean isPlaying; //if the game is paused or running
	private static boolean gameOver; //if gameOver
	private long clipTime; //to determine where the clip has paused
	private Clip clip;
	// Used to carry out the affine transform on images
  	private AffineTransform transform = new AffineTransform();

	private Random r = new Random();

	public Game(String name) {

		this.playerName = name;
		this.money = 0;
		this.foodNumber = 25;
		this.timer = 300;
		isPlaying = true; //run the game
		gameOver = false;
		final Random rng = new Random();
		Point2D.Double randomPoint;

		setSize(new Dimension(App.getScreenWidth(), App.getScreenHeight()));

		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e){
				// Gonna be a long code
				boolean clickedCoin = false; //flagger for click priority
				Point2D.Double pointClicked = new Point2D.Double(e.getX(), e.getY());

				if(pointClicked.getX()<100) { //just sets location for the pause. FIX!
					if(isPlaying){
						clipTime = clip.getMicrosecondPosition();
						clip.stop();
					} else {
						clip.setMicrosecondPosition(clipTime);
						clip.start();
					}
					isPlaying = isPlaying?false:true;
					clickedCoin = true;
				}
				else {
					clickedCoin = false;
				}
				if(isPlaying) { //clicks will only register if game is not paused
					for(Coin x : coins) {
						// checks each coin in the coin array for first instance where the click is within bounds
						if(x.isWithinRange(pointClicked)) {
							x.die(); // Auto increments money variable of player
							clickedCoin = true;
							System.out.println("You have " + money + " coins");
							break;
						} else {
							clickedCoin = false;
						}
					}
					if(!clickedCoin) {
						if(e.getY()>200){
							if(money >= 20) {
								fish.add(new Fish(pointClicked)); //add fish below a certain point
								money-=20;
							}
						} else {
							if(foodNumber > 0) {
								foods.add(new Food(pointClicked));
								foodNumber-=1;
							}
						}
					}
				}

			}
			@Override
			public void mouseClicked(MouseEvent e){}
			@Override
			public void mouseExited(MouseEvent e){}
			@Override
			public void mouseEntered(MouseEvent e){}
			@Override
			public void mousePressed(MouseEvent e){}
		});
		Thread updateThread = new Thread () {
			@Override
			public void run() { //Main game loop
				for(int i=0; i<2; i+=1) {
					//randomPoint = new Point2D.Double(0, 0);
					fish.add(new Fish(new Point2D.Double(rng.nextInt(App.getScreenWidth()/2)+200, rng.nextInt(App.getScreenHeight()/2)+200)));
				}	//bounds: width / 2 && height / 2

				while (!gameOver) { //While not yet game over
					repaint();
					try {
						Thread.sleep(1000 / App.FRAME_RATE); // delay and yield to other threads
					} catch (InterruptedException ex) { }
					// loop thread sleep until game if the game is paused
					while(!isPlaying) { // if paused, sleep indefinitely
						try {
							Thread.sleep(1000 / App.FRAME_RATE); //Pause the game
						} catch (InterruptedException ex) { }
					}
				}
			}
	    };
	    updateThread.start();  // start the thread to run updates

		//start bgm
		try{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getClass().getResource("assets/sounds/bgm/ingame.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
			// clip.loop(Clip.LOOP_CONTINUOUSLY); //IF NEEDED LOOP, MOST LIKELY FOR MENU BGM
		}
		catch(Exception e){}

		Thread timerThread = new Thread () { //thread for timer for game triggers and events (bgm triggers, etc)
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000); // delay and yield to other threads
					} catch (InterruptedException ex) { }
					timer--;
					while(!isPlaying) {
						try {
							Thread.sleep(1000 / App.FRAME_RATE); //Pause the game
						} catch (InterruptedException ex) { }
					}
					// System.out.println(timer);
				}
			}
		};
		timerThread.start();  // start the thread to run updates
	}

	/** Custom painting codes on this JPanel */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  // paint background
		setBackground(clip.getMicrosecondPosition() < SCARY_TIMESTAMP? Color.GREEN: Color.RED);
		Graphics2D g2d = (Graphics2D) g;
		//paint food
		for(int i = 0; i < foods.size(); i++){
			Food current = foods.get(i);
			BufferedImage image = current.getImg();

			transform.setToIdentity();
			transform.translate(current.getPosition().getX() - current.getWidth() / 2, current.getPosition().getY() - current.getHeight() / 2);
    	g2d.drawImage(image, transform, null);
		}
		//paint fish
		for(int i = 0; i < fish.size(); i++){
			Fish current = fish.get(i);
			BufferedImage image = current.getImg();
			transform.setToIdentity();
			transform.translate(current.getPosition().getX() - current.getWidth() / 2, current.getPosition().getY() - current.getHeight() / 2);
			transform.rotate(Math.toRadians(current.getDirection()), current.getWidth() / 2, current.getHeight() / 2); //rotates image based on direction
  		g2d.drawImage(image, transform, null);

			// check if fish is dying, apply red tint
			if(current.getLifespan() <= 8){
				g2d.drawImage(createRedVersion(image, redTintModifier(current.getLifespan())), transform, null);
			}
		}

		// paint coin
		for(int i = 0; i < coins.size(); i++){
			Coin current = coins.get(i);
			BufferedImage image = current.getImg();
			transform.setToIdentity();
			transform.translate(current.getPosition().getX() - current.getWidth() / 2, current.getPosition().getY() - current.getHeight() / 2);
	    	g2d.drawImage(image, transform, null);
		}

		g2d.dispose();
	}

	private BufferedImage createRedVersion(BufferedImage image, float f){
		// create red image
		BufferedImage redVersion = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d2 = (Graphics2D) redVersion.getGraphics();
		g2d2.setColor(Color.RED);
		g2d2.fillRect(0, 0, image.getWidth(), image.getHeight());

		// paint original with composite
		g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, f));
		g2d2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);

		return redVersion;
	}

	private float redTintModifier(int x){
		return 0.3f + (8 - x) * 0.07f;
	}

	public ArrayList<Food> getFoods() {
		return this.foods;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public int getFoodNumber() {
		return this.foodNumber;
	}

	public int addMoney() { //Unsafe. TO UPDATE!
		return this.money++;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public ArrayList<Fish> getFish() {
		return this.fish;
	}

	public ArrayList<Coin> getCoins() {
		return this.coins;
	}

	public ArrayList<Food> getFood() {
		return this.foods;
	}
}

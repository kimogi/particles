package co.nz.kimogi.antcolony;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AntColony extends JFrame
{
	private static final long serialVersionUID = -3079870385792199691L;

	class AntPanel extends JPanel
	{
		private static final long serialVersionUID = -9027962180934835275L;
		public final static int NUM_ANTS = 100;
		public final static int WIDTH = 400;
		public final static int HEIGHT = 400;

		private final static int SZ = 4;

		public LinkedList<Ant> ants = new LinkedList<Ant>();
		public LinkedList<Ant> followers = new LinkedList<Ant>();

		public AntPanel()
		{
			Random rand = new Random();
			ants.addLast(new Ant(0, new Rectangle(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), SZ, SZ), true));
			for (int i = 1; i < NUM_ANTS; i++)
			{
				ants.addLast(new Ant(i, new Rectangle(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), SZ, SZ), false));
			}

			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			setBackground(Color.WHITE);
		}

		private void checkID(int id)
		{
			if (id < 0 || id >= NUM_ANTS)
			{
				throw new IllegalArgumentException("Bad ant id: " + id);
			}
		}

		public synchronized void setAntLocation(Ant ant, int x, int y)
		{
			checkID(ant.id);
			if (x < 0)
			{
				x = (Math.abs(x) / WIDTH + 1) * WIDTH + x;
			}
			if (y < 0)
			{
				y = (Math.abs(y) / HEIGHT + 1) * HEIGHT + y;
			}
			ant.rect.setLocation(x % WIDTH, y % HEIGHT);
		}

		public synchronized void moveAnt(Ant ant, int dx, int dy)
		{
			checkID(ant.id);
			setAntLocation(ant, ant.rect.x + dx, ant.rect.y + dy);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.clearRect(0, 0, WIDTH, HEIGHT);

			Iterator<Ant> iter = ants.iterator();
			while (iter.hasNext())
			{
				Ant ant = iter.next();
				g2.setColor(ant.color);
				g2.fill(ant.rect);
			}
		}
	}

	private AntPanel antPanel = new AntPanel();
	boolean keepRunning = true;
	private Random rand = new Random();

	public AntColony()
	{
		getContentPane().add(antPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private double theta = -Math.PI;
	private Point center = new Point(AntPanel.WIDTH / 2, AntPanel.HEIGHT / 2);
	private int radius = 100;
	private int hookUpRadius = 10;
	private int keepRadius = 5;

	public void updateLoop()
	{
		while (keepRunning)
		{
			Ant leader = antPanel.ants.getFirst();
			theta = theta + Math.PI / 36;
			if (theta > Math.PI)
			{
				theta = -Math.PI;
			}
			leader.rect.x = center.x + (int) (radius * Math.cos(theta));
			leader.rect.y = center.y + (int) (radius * Math.sin(theta));
			leader.color = Color.BLUE;

			Ant measureAnt = antPanel.followers.isEmpty() ? leader : antPanel.followers.getLast();

			Iterator<Ant> iter = antPanel.ants.iterator();
			while (iter.hasNext())
			{
				Ant ant = iter.next();
				if (!ant.isLeader && !ant.isFollower)
				{
					if (calcDistance(ant, measureAnt) > hookUpRadius)
					{
						int dx = rand.nextInt(5) - 2;
						int dy = rand.nextInt(5) - 2;
						antPanel.moveAnt(ant, dx, dy);
						ant.color = Color.RED;
					}
					else
					{
						ant.color = Color.GREEN;
						antPanel.followers.addLast(ant);
						ant.isFollower = true;
					}
				}				
			}

			measureAnt = leader;
			iter = antPanel.followers.iterator();
			while (iter.hasNext())
			{
				Ant follower = iter.next();
				int x2 = follower.rect.x;
				int y2 = follower.rect.y;
				int x1 = measureAnt.rect.x;
				int y1 = measureAnt.rect.y;
				int distance = calcDistance(follower, measureAnt);
			
				if (distance > keepRadius)
				{
					int dx = (int) (x1 - x2) * (distance - keepRadius)/distance;
					int dy = (int) (y1 - y2) * (distance - keepRadius)/distance;
					antPanel.moveAnt(follower, dx, dy);
				}
				measureAnt = follower;
			}
			antPanel.repaint();

			try
			{
				Thread.sleep(50);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private int calcDistance(Ant ant1, Ant ant2)
	{
		return (int) Math.sqrt((ant1.rect.x - ant2.rect.x) * (ant1.rect.x - ant2.rect.x) + (ant1.rect.y - ant2.rect.y) * (ant1.rect.y - ant2.rect.y));
	}

	public void startAnimation()
	{
		Thread animationThread = new Thread(new Runnable()
		{
			public void run()
			{
				updateLoop();
			}
		});
		animationThread.start();
	}

	public static void createAndShowGUI()
	{
		final AntColony antWindow = new AntColony();
		antWindow.pack();
		antWindow.setLocationByPlatform(true);
		antWindow.setVisible(true);

		antWindow.startAnimation();
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});
	}
}
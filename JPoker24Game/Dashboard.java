import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Dashboard implements Runnable{
	private Game24_Backend backend;
	JFrame frame = new JFrame();
	JPanel menu = new JPanel();
	JPanel dashboard = new JPanel();
	JPanel labels = new JPanel();
	JButton btn_profile = new JButton("User Profile");
	JButton btn_game = new JButton("Play Game");
	JButton btn_leader = new JButton("Leader Board");
	JButton btn_logout = new JButton("Logout");
	JLabel label_name = new JLabel();
	JLabel label_wins = new JLabel();
	JLabel label_games = new JLabel();
	JLabel label_time = new JLabel();
	JLabel label_rank = new JLabel();

	String Host = new String();
	String Name = new String();
	public Dashboard(String host, String name){
		Host = host;
		Name = name;
		try {
	        Registry registry = LocateRegistry.getRegistry(host);
	        backend = (Game24_Backend)registry.lookup("Backend");
			
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
	}
	
	public void run(){ 
		menu.setLayout(new GridLayout(1,4));
		menu.add(btn_profile);
		menu.add(btn_game);
		menu.add(btn_leader);
		menu.add(btn_logout);
		btn_logout.addActionListener(new logout());
		btn_leader.addActionListener(new leader());
		btn_game.addActionListener(new play());
		dashboard.add(menu);
		label_name.setText(Name);
		labels.setLayout(new GridLayout(6,1));
		labels.add(label_name);
		labels.add(label_wins);
		labels.add(label_games);
		labels.add(label_time);
		labels.add(label_rank);
		dashboard.add(labels);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(dashboard);
		frame.setTitle("JPoker 24-Game");
		frame.setSize(500, 400);
		frame.setVisible(true);
		new getinfo().execute();
	}
	boolean success = false; 
	private class try_logout extends SwingWorker<Void, Void> {

		protected Void doInBackground() {
			try {
				success = backend.logout(Name);
				} catch (Exception e) {
					System.out.println(e);
				}
			return null;
		}
		
		protected void done() {
			if(success==true) {
				frame.setVisible(false);
		    	SwingUtilities.invokeLater(new Game24(Host));
			}
		}
	}
	
	private class getinfo extends SwingWorker<Void, Void> {
		float result[] = null;
		protected Void doInBackground() {
			try {
				result = backend.getinfo(Name);
				} catch (Exception e) {
					System.out.println(e);
				}
			return null;
		}
		
		protected void done() {
			label_wins.setText("Number of Wins : "+(int)result[0]);
			label_games.setText("Number of Games : "+(int)result[1]);
			label_time.setText("Average Time to Win : "+result[2]+" secs");
			label_rank.setText("Rank #"+(int)result[3]);
		}
	}
	
	public class logout implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			new try_logout().execute();
		}
		
	}
	public class leader implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
	    	SwingUtilities.invokeLater(new LeaderBoard(Host,Name));
		}
		
	}
	public class play implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
	    	SwingUtilities.invokeLater(new PlayGame(Host,Name));
		}
		
	}
}

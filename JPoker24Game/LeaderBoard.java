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


public class LeaderBoard implements Runnable{
	private Game24_Backend backend;
	JFrame frame = new JFrame();
	JPanel menu = new JPanel();
	JPanel dashboard = new JPanel();
	JPanel labels = new JPanel();
	JButton btn_profile = new JButton("User Profile");
	JButton btn_game = new JButton("Play Game");
	JButton btn_leader = new JButton("Leader Board");
	JButton btn_logout = new JButton("Logout");
	JLabel label_name_1 = new JLabel();
	JLabel label_name_2 = new JLabel();
	JLabel label_name_3 = new JLabel();
	JLabel label_name_4 = new JLabel();
	JLabel label_name_5 = new JLabel();
	JLabel label_name_6 = new JLabel();
	JLabel label_name_7 = new JLabel();
	JLabel label_name_8 = new JLabel();
	JLabel label_name_9 = new JLabel();
	JLabel label_name_10 = new JLabel();

	String Host = new String();
	String Name = new String();
	public LeaderBoard(String host,String name){
		Host = host;
		Name = name;
		try {
	        Registry registry = LocateRegistry.getRegistry(host);
	        backend = (Game24_Backend)registry.lookup("Backend");
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
		new getLeaderBoard().execute();
	}

	@Override
	public void run() {
		menu.setLayout(new GridLayout(1,4));
		menu.add(btn_profile);
		menu.add(btn_game);
		menu.add(btn_leader);
		menu.add(btn_logout);
		btn_logout.addActionListener(new logout());
		btn_profile.addActionListener(new dashboard());
		btn_game.addActionListener(new play());
		dashboard.add(menu);
		labels.setLayout(new GridLayout(10,1));
		labels.add(label_name_1);
		labels.add(label_name_2);
		labels.add(label_name_3);
		labels.add(label_name_4);
		labels.add(label_name_5);
		labels.add(label_name_6);
		labels.add(label_name_7);
		labels.add(label_name_8);
		labels.add(label_name_9);
		labels.add(label_name_10);
		dashboard.add(labels);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(dashboard);
		frame.setTitle("JPoker 24-Game");
		frame.setSize(500, 400);
		frame.setVisible(true);		
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
	
	public class logout implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			new try_logout().execute();
		}
		
	}
	public class dashboard implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
	    	SwingUtilities.invokeLater(new Dashboard(Host,Name));
		}
		
	}
	private class getLeaderBoard extends SwingWorker<Void, Void> {
		String result[] = null;
		protected Void doInBackground() {
			try {
				result = backend.getLeaderBoard();
				} catch (Exception e) {
					System.out.println(e);
				}
			return null;
		}
		
		protected void done() {
			label_name_1.setText(result[0]);
			label_name_2.setText(result[1]);
			label_name_3.setText(result[2]);
			label_name_4.setText(result[3]);
			label_name_5.setText(result[4]);
			label_name_6.setText(result[5]);
			label_name_7.setText(result[6]);
			label_name_8.setText(result[7]);
			label_name_9.setText(result[8]);
			label_name_10.setText(result[9]);
		}
	}
	public class play implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
	    	SwingUtilities.invokeLater(new PlayGame(Host,Name));
		}
		
	}
}

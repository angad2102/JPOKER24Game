import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


public class PlayGame implements Runnable{
	private Game24_Backend backend;
	JFrame frame = new JFrame();
	JPanel menu = new JPanel();
	JPanel dashboard = new JPanel();
	JPanel labels = new JPanel();
	JButton btn_profile = new JButton("User Profile");
	JButton btn_game = new JButton("Play Game");
	JButton btn_leader = new JButton("Leader Board");
	JButton btn_logout = new JButton("Logout");
	JButton btn_play = new JButton("New Game");
	JLabel lbl = new JLabel("hello");
	QueueReceiver receiver = null;
	String Players[] = new String[4];
	String Host = new String();
	String Name = new String();
	float wins[] = new float[4];
	float avg[] = new float[4];
	long time;
	public PlayGame(String host,String name){
		Host = host;
		Name = name;
		try {
	        Registry registry = LocateRegistry.getRegistry(host);
	        backend = (Game24_Backend)registry.lookup("Backend");
			
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
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
		btn_leader.addActionListener(new leader());
		dashboard.add(menu);
		btn_play.addActionListener(new new_game());
		dashboard.add(btn_play);
		dashboard.add(lbl);
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
	public class leader implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(false);
	    	SwingUtilities.invokeLater(new LeaderBoard(Host,Name));
		}
		
	}
	
	public class new_game implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			String host = "localhost";
			QueueSender sender = null;
			try {
				sender = new QueueSender(host);
				
			} catch (Exception er) {
				System.err.println("Program aborted");
			} finally {
				if(sender != null) {
					try {
						sender.close();
					} catch (Exception errr) { }
				}
			}
			
		 
			runt();

		}
		
	}

		
		String message; 
		//long time = System.currentTimeMillis();
	    public void runt() {
	    	
	    	String host = "localhost";
			QueueSender sender = null;
			try {
				createJNDIContext();
				lookupConnectionFactory();
				lookupQueue();
				createConnection();
				setListener();
				sender = new QueueSender(host);
				sender.sendMessages("ready "+Name);
				dashboard.remove(btn_play);
				lbl.setText("Waiting for other players to join");

			} catch (NamingException | JMSException e) {
				System.err.println("Program aborted");
			} finally {
				if(sender != null) {
					try {
						sender.close();
					} catch (Exception e) { }
				}
			}    
	}
	JLabel label_Image1 = new JLabel();
	JLabel label_Image2 = new JLabel();
	JLabel label_Image3 = new JLabel();
	JLabel label_Image4 = new JLabel();

	JButton btn_result = new JButton("Done");

	JLabel label_player1 = new JLabel();
	JLabel label_player2 = new JLabel();
	JLabel label_player3 = new JLabel();
	JLabel label_player4 = new JLabel();
	
	JTextField txt_inputbet = new JTextField(20);
	
	JPanel PlayArea = new JPanel();
	Random random = new Random();
	public void startGame(){
		System.out.println("reach");
		lbl.setText("");
		PlayArea.setLayout(new GridLayout(2,4));
		
		ImageIcon Image1 = new ImageIcon("card_"+(random.nextInt(4) + 1)+cards[0]+".gif");
		ImageIcon Image2 = new ImageIcon("card_"+(random.nextInt(4) + 1)+cards[1]+".gif");
		ImageIcon Image3 = new ImageIcon("card_"+(random.nextInt(4) + 1)+cards[2]+".gif");
		ImageIcon Image4 = new ImageIcon("card_"+(random.nextInt(4) + 1)+cards[3]+".gif");

		label_Image1.setIcon(Image1);
		label_Image2.setIcon(Image2);
		label_Image3.setIcon(Image3);
		label_Image4.setIcon(Image4);
		
		PlayArea.add(label_Image1);
		PlayArea.add(label_Image2);
		PlayArea.add(label_Image3);
		PlayArea.add(label_Image4);
		
		label_player1.setText("");
		label_player2.setText("");
		label_player3.setText("");
		label_player4.setText("");
		
		if(!Players[0].equals("null")&&!Players[0].equals(Name))
		{
		label_player1.setText(Players[0]+" wins: "+wins[0]+" Avg: "+avg[0]);	
		}
		
		if(!Players[1].equals("null")&&!Players[1].equals(Name))
		{
		label_player2.setText(Players[1]+" wins: "+wins[1]+" Avg: "+avg[1]);
		
		}
		
		if(!Players[2].equals("null")&&!Players[2].equals(Name))
		{
		label_player3.setText(Players[1]+" wins: "+wins[1]+" Avg: "+avg[1]);
		
		}
		
		if(!Players[3].equals("null")&&!Players[3].equals(Name))
		{
		label_player4.setText(Players[1]+" wins: "+wins[1]+" Avg: "+avg[1]);
		
		}
		PlayArea.add(label_player1);
		PlayArea.add(label_player2);
		PlayArea.add(label_player3);
		PlayArea.add(label_player4);

		dashboard.add(PlayArea);
		lbl.setText("Please use parathesis eg (((5+4)*2)+6)");
		dashboard.add(txt_inputbet);
		btn_result.addActionListener(new send());
		dashboard.add(btn_result);
		frame.setSize(500, 400);
		frame.setVisible(true);
		time = System.currentTimeMillis();
	}
	
	public class send implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String textFieldValue = txt_inputbet.getText();
			
			String host = "localhost";
			QueueSender sender = null;
			try {
				createConnection();
				sender = new QueueSender(host);
				sender.sendMessages("Submit "+Name+" "+Players[0]+" "+Players[1]+" "+Players[2]+" "+Players[3]+" "+textFieldValue+" "+(System.currentTimeMillis()-time));
				//System.out.println(Name+" answer "+textFieldValue);

			} catch (Exception err) {
				System.err.println("Program aborted");
			} finally {
				if(sender != null) {
					try {
						sender.close();
					} catch (Exception er) { }
				}
			}    
		}
		
	}
	
	private Context jndiContext;
	private void createJNDIContext() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}
	
	private TopicConnectionFactory topicConnectionFactory;
	private void lookupConnectionFactory() throws NamingException {

		try {
			topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup("sendmessage");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}
	
	private javax.jms.Topic topic;
	private void lookupQueue() throws NamingException {

		try {
			topic = (javax.jms.Topic)jndiContext.lookup("jms/sendmessage");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}
	
	private TopicConnection connection;
	private void createConnection() throws JMSException {
		try {
			connection = topicConnectionFactory.createTopicConnection();
			connection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}
	private MessageListener topicListener;
	public String cards [] = new String[4];
	public void setListener( ) throws JMSException {
		createSession();
		createReceiver();
		try {

			topicListener = new MessageListener(){
				public void onMessage(Message message) {
					   TextMessage msg = null;
					            
					   try {
						   //System.out.println("reach");
					       if (message instanceof TextMessage) {
					           msg = (TextMessage) message;
					           String m[] = msg.getText().split(Pattern.quote(" "));
					           if(m[0].equals("List"))
					        	   {
					        	   for(int i=0;i<4;i++)
					        		   Players[i]=m[i+1];
					        	   }
					           if(m[0].equals("Wins")){
					        	   for(int i=0;i<4;i++)
					        		   wins[i]=Float.parseFloat(m[i+1]);
					           }
					           if(m[0].equals("AVG")){
					        	   for(int i=0;i<4;i++)
					        		   avg[i]=Float.parseFloat(m[i+1]);
					           }
					           
					           if(m[0].equals("Cards")){
					        	   for(int i=0;i<4;i++) {
					        		   cards[i]=(m[i+1]);
					        	   }
					        	   System.out.println(cards[0]+cards[1]+cards[2]+cards[3]);
					        	   startGame();
					           }
					           
					           if(m[0].equals("Winner")&&m[3].equals(Name)){
					        	   dashboard.remove(PlayArea);
					        	   dashboard.remove(txt_inputbet);
    					       	   dashboard.remove(btn_result);
					        	   if(m[1].equals(Name)){
					        		   lbl.setText("You Won "+m[2]);
					        	   }
					        	   else{
					        		   lbl.setText(m[1]+" Won "+m[2]);
					        	   }
					        	   dashboard.add(btn_play);
					        	   frame.setSize(500, 400);
					       		   frame.setVisible(true);
					           }
					           
					           if(m[0].equals("Incorrect")&&m[2].equals(Name)){
					        	   dashboard.remove(PlayArea);
					        	   dashboard.remove(txt_inputbet);
    					       	   dashboard.remove(btn_result);
					        	   lbl.setText("Game Abandonned due to incorrect answer");
					        	   dashboard.add(btn_play);
					        	   frame.setSize(500, 400);
					       		   frame.setVisible(true);
					           }
					           System.out.println(msg.getText());
					       } else {
					           //System.out.println("Message of wrong type: " +
					             //  message.getClass().getName());
					       }
					   } catch (JMSException e) {
					       System.out.println("JMSException in onMessage(): " + e.toString());
					   } catch (Throwable t) {
					       System.out.println("Exception in onMessage():" + t.getMessage());
					   }
					}
			};
		} catch (Exception e){
			System.out.println("idhar hai");
		}
		

		queueReceiver.setMessageListener((MessageListener) topicListener);

	}

	
	
	private TopicSession session;
	private void createSession() throws JMSException {
		try {
			session = connection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	// Was: QueueReceiver
	private TopicSubscriber queueReceiver;
	
	private void createReceiver() throws JMSException {
		try {
			queueReceiver = session.createSubscriber(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	public void close() {
		if(connection != null) {
			try {
				connection.close();
			} catch (JMSException e) { }
		}
	}

}

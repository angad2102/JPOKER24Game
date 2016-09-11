import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Backend extends UnicastRemoteObject implements Game24_Backend{
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "2102Angad@";
	private static final String DB_NAME = "Assignment1";
	private static Connection conn;

	public static void main(String[] args) {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+
					"/"+DB_NAME+
					"?user="+DB_USER+
					"&password="+DB_PASS);
			System.out.println("Database connection successful.");
			
			Backend app = new Backend();
			System.setSecurityManager(new SecurityManager());
			Naming.rebind("Backend", app);
			System.out.println("Service registered");
			try {
				java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE users SET is_online = 0");					
				int rows = stmt.executeUpdate();
				if(rows > 0) {
					System.out.println("Online Stutus updated");
				} else {
					System.out.println("Cannot update online status");
				}
	            } catch (Exception e){
	            	
	            }	
		} catch(Exception e) {
			System.err.println("Exception thrown: "+e);
		}
	}
	
	
	private Context jndiContext;
	private void createJNDIContext() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			System.out.println("Enter JNDI");
			jndiContext = new InitialContext();
			System.out.println("Exit JNDI");
		} catch (NamingException e) {
			System.out.println("Error JNDI");
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}
	
	private TopicConnectionFactory topicConnectionFactory;
	private void lookupConnectionFactory() throws NamingException {

		try {
			System.out.println("Enter Connection Factory");
			topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup("sendmessage");
			System.out.println("Exit Connection Factory");
		} catch (NamingException e) {
			System.out.println("Error Connection Factory");
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}
	
	private javax.jms.Topic topic;
	private void lookupQueue() throws NamingException {

		try {
			System.out.println("Enter Topic LookUp");
			topic = (javax.jms.Topic)jndiContext.lookup("jms/sendmessage");
			System.out.println("Exit Topic LookUp");
		} catch (NamingException e) {
			System.out.println("Error Topic LookUp");
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}
	
	private TopicConnection connection;
	private void createConnection() throws JMSException {
		try {
			System.out.println("Enter Connection");
			connection = topicConnectionFactory.createTopicConnection();
			connection.start();
			System.out.println("Exit Connection");
		} catch (JMSException e) {
			System.out.println("Error Connection");
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}
	
	public void sendMessages(String msg) throws JMSException {
		createSession();
		createSender();			
		
		TextMessage message = session.createTextMessage(); 
		
			message.setText(msg);
			queueSender.publish(message);
			System.out.println("Sending message "+msg);
		
		// send non-text control message to end
		//queueSender.send(session.createMessage());
	}
	
	
	private TopicSession session;
	private void createSession() throws JMSException {
		try {
			System.out.println("Enter session");
			session = connection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
			System.out.println("Session created");
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	// Was: QueueSender
	private TopicPublisher queueSender;
	private void createSender() throws JMSException {
		try {
			System.out.println("Enter Sender");
			queueSender = session.createPublisher(topic);
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
	
	public Backend() throws RemoteException{
		try {
		createJNDIContext();
		lookupConnectionFactory();
		lookupQueue();
		createConnection();
		
		String host = "localhost";
		//sendmessage("hi");
		createJNDIContext_q();
		lookupConnectionFactory_q();
		lookupQueue_q();
		
		createConnection_q();
		
		new myThread().start();
		}
		catch (Exception e){
			
		}
	
	}
	class myThread extends Thread {
	    public void run() {
	    	try {receiveMessages_q();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				
					try {
						System.out.println("Connection closed");
						close_q();
					} catch (Exception e) { }
				
			}
	    }
	}
	public boolean login(String name,char[] Password) throws RemoteException {
		
	    boolean found=false;
		String password = String.copyValueOf(Password);	
		boolean is_online = false;
		
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT is_online FROM users WHERE name = ?");
			stmt.setString(1, name);		
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				is_online = rs.getBoolean(1);
		}
		catch (Exception e){
			
		}
		
	if(is_online==false) {	
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE name = ? AND password = ?");
			stmt.setString(1, name);	
			stmt.setString(2, password);		
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
					found=true;
		}
		catch (Exception e){
			
		}
	}
		  if(found==true) {
		        try {
		        	java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE users SET is_online = 1 WHERE name = ?");
					stmt.setString(1, name);
					stmt.executeUpdate();
		        }catch (Exception e){
					System.out.println(e);
		        }
		        return true; 
		    }
		  else 
			  return false;
	}
	@Override
	public boolean register(String name, char[] Password) throws RemoteException {
		boolean success=false;
		boolean is_there=false;
		String password = String.copyValueOf(Password);	
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
			stmt.setString(1, name);	
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
					is_there=true;
		}catch (Exception e){
			
		}	
		
		if(is_there==false) {
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, password,is_online) VALUES (?, ?,1)");
			stmt.setString(1, name);
			stmt.setString(2, password);
			stmt.execute();
			
			stmt = conn.prepareStatement("INSERT INTO user_info (name, wins, games, time) VALUES (?, 0, 0, 0)");
			stmt.setString(1, name);
			stmt.execute();
	    	success=true;
		}catch (Exception e){
				System.out.println(e);
	        }
		}
		return success;
	}

	@Override
	public boolean logout(String name) throws RemoteException {
		boolean success=false;
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE users SET is_online = 0 WHERE name = ?");
			stmt.setString(1, name);
					
			int rows = stmt.executeUpdate();
			if(rows > 0) {
				success=true;
			} else {
				success=false;
			}
		}catch (Exception e){
			
		}
		
		return success;
	}
	
	public float[] getinfo(String Name) throws RemoteException{
		float result[] = new float[4];
		try{
			java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info ORDER BY wins ASC");
			ResultSet rs = stmt.executeQuery();
			float rank = 0;
			while(rs.next()){
				rank+=1;
				if(rs.getString(1).equals(Name)){
					result[0] = rs.getFloat(2);
					result[1] = rs.getFloat(3);
					result[2] = rs.getFloat(4);
					result[3] = rank;
					break;
				}
			}
		}catch (Exception e){
			
		}
		return result;
	}

	@Override
	public String[] getLeaderBoard() throws RemoteException {
		String result [] = new String [10];
		try {
			java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info ORDER BY wins DESC");
			ResultSet rs = stmt.executeQuery();
			float rank = 0;
			while(rs.next()){
				result [(int) rank] = "Rank #"+((int)rank+1)+"     "+rs.getString(1)+"      wins: "+rs.getString(2)+"      played: "+rs.getString(3)+"     time: "+rs.getString(4);
				System.out.println(result[(int) rank]);
				rank+=1;
			}
		}catch (Exception e){
			
		}
		return result;
	}
	private Context jndiContext_q;
	private void createJNDIContext_q() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext_q = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}
	
	private ConnectionFactory connectionFactory_q;
	private void lookupConnectionFactory_q() throws NamingException {

		try {
			connectionFactory_q = (ConnectionFactory)jndiContext_q.lookup("jms/TestConnectionFactory");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}
	
	private Queue queue_q;
	private void lookupQueue_q() throws NamingException {

		try {
			queue_q = (Queue)jndiContext_q.lookup("jms/TestQueue");
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}
	
	private javax.jms.Connection connection_q;
	private void createConnection_q() throws JMSException {
		try {
			connection_q = connectionFactory_q.createConnection();
			connection_q.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}
	private void createReceiver_q() throws JMSException {
		try {
			queueReceiver_q = session_q.createConsumer(queue_q);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	private MessageConsumer queueReceiver_q;
	
	public void receiveMessages_q() throws JMSException {
		createSession_q();
		createReceiver_q();
		
		while(true) {
			Message m = queueReceiver_q.receive();
			if(m != null && m instanceof TextMessage) {
				TextMessage textMessage = (TextMessage)m;
				System.out.println("Received message: "+textMessage.getText());
			
			String[] parts = textMessage.getText().split(Pattern.quote(" "));
			
			if(parts[0].equals("ready"))
				newplayer(parts[1]);
			if(parts[0].equals("Submit"))
			{
				String name = parts[1];
				String res = parts[6];
				


				 Stack<String> ops  = new Stack<String>();
			     Stack<Double> vals = new Stack<Double>();
			     int in=0;
			     int out=0;
			        
			     for (int i = 0; i < res.length(); i++){
			    	
		            char set = res.charAt(i);
		            String s;
		            
		            if(set=='Q'||set=='q')
		            	s="12";
		            else if(set=='K'||set=='k')
		            	s="13";
		            else if(set=='J'||set=='J')
		            	s="11";
		            else if(set=='A'||set=='A')
		            	s="1";
		            else if(set=='1'||set=='2'||set=='3'||set=='4'||set=='4'||set=='5'||set=='6'||set=='7'||set=='8'||set=='9'){
		            	if(i<res.length()-1)
		            		if((res.charAt(i+1)=='0'||res.charAt(i+1)=='1'||res.charAt(i+1)=='2'||res.charAt(i+1)=='3')){
		            		s=""+set+res.charAt(i+1);
		            		i++;
		            		}
		            	else{
		            		s=""+set;
		            	}
		            	else{
		            		s=""+set;
		            	}
		            }
		            else
		            	s=""+set;
		            System.out.println(s);
		            if      (s.equals("("))               ;
		            else if (s.equals("+"))    ops.push(s);
		            else if (s.equals("-"))    ops.push(s);
		            else if (s.equals("*"))    ops.push(s);
		            else if (s.equals("/"))    ops.push(s);
		            else if (s.equals("sqrt")) ops.push(s);
		            else if (s.equals(")")) {
		                String op = ops.pop();
		                double v = vals.pop();
		                if      (op.equals("+"))    v = vals.pop() + v;
		                else if (op.equals("-"))    v = vals.pop() - v;
		                else if (op.equals("*"))    v = vals.pop() * v;
		                else if (op.equals("/"))    v = vals.pop() / v;
		                else if (op.equals("sqrt")) v = Math.sqrt(v);
		                vals.push(v);
		                System.out.println(v);
		            }
		            else vals.push(Double.parseDouble(s));
		        }
				//System.out.println(vals.pop());
			     double v = vals.pop();
			     System.out.println(v);
			     if(v==24)
			     {
			    	 sendMessages("Winner "+name+" "+parts[6]+" "+parts[2]);
			    	 sendMessages("Winner "+name+" "+parts[6]+" "+parts[3]);
			    	 sendMessages("Winner "+name+" "+parts[6]+" "+parts[4]);
			    	 sendMessages("Winner "+name+" "+parts[6]+" "+parts[5]);
			    	 String time = parts[7];
			    	 
			    	 float w=0; 
			    	 float g=0;
			    	 float t =0;
			    	 try {
				    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
							stmt.setString(1, name);	
							ResultSet rs = stmt.executeQuery();
							while(rs.next())
									{
										w = rs.getFloat(3);
										g= rs.getFloat(2);
										t=rs.getFloat(4);
										
									}
							
							try {
					        	stmt = conn.prepareStatement("UPDATE user_info SET wins = ?,games = ?,time = ? WHERE name = ?");
					        	stmt.setFloat(1, g+1);
					        	stmt.setFloat(2, w+1);
					        	stmt.setFloat(3, (t+Float.parseFloat(time))/(w+1));
								stmt.setString(4, parts[2]);
								stmt.executeUpdate();
					        }catch (Exception e){
								System.out.println(e);
					        }
						
							
				    	 }catch(Exception e){}
			    	 
			    	 
			    	 
			    	 if(parts[2]!=null&&!parts[2].equals(name)) {
				    	 try {
				    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
							stmt.setString(1, parts[2]);	
							ResultSet rs = stmt.executeQuery();
							while(rs.next())
									{
								w = rs.getFloat(3);
									}
							
							try {
					        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
					        	stmt.setFloat(1, w+1);
								stmt.setString(2, parts[2]);
								stmt.executeUpdate();
					        }catch (Exception e){
								System.out.println(e);
					        }
						
							
				    	 }catch(Exception e){} }
				    	 
				    	if(parts[3]!=null&&!parts[3].equals(name)) {
				    	 try {
					    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
								stmt.setString(1, parts[3]);	
								ResultSet rs = stmt.executeQuery();
								while(rs.next())
										{
									w = rs.getFloat(3);
										}
								
								try {
						        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
						        	stmt.setFloat(1, w+1);
									stmt.setString(2, parts[3]);
									stmt.executeUpdate();
						        }catch (Exception e){
									System.out.println(e);
						        }
							
								
					    	 }catch(Exception e){} }
				    	 
				    	if(parts[4]!=null&&!parts[4].equals(name)) {
				    	 try {
					    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
								stmt.setString(1, parts[4]);	
								ResultSet rs = stmt.executeQuery();
								while(rs.next())
										{
									w = rs.getFloat(3);
										}
								
								try {
						        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
						        	stmt.setFloat(1, w+1);
									stmt.setString(2, parts[4]);
									stmt.executeUpdate();
						        }catch (Exception e){
									System.out.println(e);
						        }
							
								
					    	 }catch(Exception e){} }
				    	 
				    	if(parts[5]!=null&&!parts[5].equals(name)) {
				    	 try {
					    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
								stmt.setString(1, parts[5]);	
								ResultSet rs = stmt.executeQuery();
								while(rs.next())
										{
									w = rs.getFloat(3);
										}
								
								try {
						        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
						        	stmt.setFloat(1, w+1);
									stmt.setString(2, parts[5]);
									stmt.executeUpdate();
						        }catch (Exception e){
									System.out.println(e);
						        }
							
								
					    	 }catch(Exception e){} }
			    	 
 
			     }
			     else{
			    	 sendMessages("Incorrect "+name+" "+parts[2]);
			    	 sendMessages("Incorrect "+name+" "+parts[3]);
			    	 sendMessages("Incorrect "+name+" "+parts[4]);
			    	 sendMessages("Incorrect "+name+" "+parts[5]);
			    	 
			    	int w = 0;
			    	
			    	if(parts[2]!=null) {
			    	 try {
			    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
						stmt.setString(1, parts[2]);	
						ResultSet rs = stmt.executeQuery();
						while(rs.next())
								{
									w = rs.getInt(3);
								}
						
						try {
				        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
				        	stmt.setLong(1, w+1);
							stmt.setString(2, parts[2]);
							stmt.executeUpdate();
				        }catch (Exception e){
							System.out.println(e);
				        }
					
						
			    	 }catch(Exception e){} }
			    	 
			    	if(parts[3]!=null) {
			    	 try {
				    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
							stmt.setString(1, parts[3]);	
							ResultSet rs = stmt.executeQuery();
							while(rs.next())
									{
										w = rs.getInt(3);
									}
							
							try {
					        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
					        	stmt.setLong(1, w+1);
								stmt.setString(2, parts[3]);
								stmt.executeUpdate();
					        }catch (Exception e){
								System.out.println(e);
					        }
						
							
				    	 }catch(Exception e){} }
			    	 
			    	if(parts[4]!=null) {
			    	 try {
				    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
							stmt.setString(1, parts[4]);	
							ResultSet rs = stmt.executeQuery();
							while(rs.next())
									{
										w = rs.getInt(3);
									}
							
							try {
					        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
					        	stmt.setLong(1, w+1);
								stmt.setString(2, parts[4]);
								stmt.executeUpdate();
					        }catch (Exception e){
								System.out.println(e);
					        }
						
							
				    	 }catch(Exception e){} }
			    	 
			    	if(parts[5]!=null) {
			    	 try {
				    	 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
							stmt.setString(1, parts[5]);	
							ResultSet rs = stmt.executeQuery();
							while(rs.next())
									{
										w = rs.getInt(3);
									}
							
							try {
					        	stmt = conn.prepareStatement("UPDATE user_info SET games = ? WHERE name = ?");
					        	stmt.setLong(1, w+1);
								stmt.setString(2, parts[5]);
								stmt.executeUpdate();
					        }catch (Exception e){
								System.out.println(e);
					        }
						
							
				    	 }catch(Exception e){} }
			    	 

			     }
				
			}
			
			}
		}

	}
	
	int n=0;
	String Player[] = new String[4];
	long starttime;
	
	public void newplayer(String name){
		n++;
		Player[n-1]=name;
		System.out.println(n+Player[n-1]);
		if(n==4){
			new startGame(Player).start();
			System.out.println("started because of 4");
			n=0;
			Player = new String[4];
			}
		else if(n==1) {
			starttime=System.currentTimeMillis();
			System.out.println(starttime);
		}
		else if (n==2){
			
		if(System.currentTimeMillis()-starttime>=10000){
				if(n>=2) {
					System.out.println("started because of 2");
					new startGame(Player).start();
					//n=0;
					//Player = new String[4];
				}
			}
		else {
			new initialisetimer().start();
		}
		}
	
	}
	
	class initialisetimer extends Thread {

	    public void run() {
	    	try {
	    		Thread.sleep(10000-(System.currentTimeMillis()-starttime));
	    		if(n>=1)
	    			new startGame(Player).start();
			} catch (Exception e) {

				e.printStackTrace();
			}finally {
				
			}
	    }
	}
	
	class startGame extends Thread {
		String Players [] = new String[4];
		public startGame(String players[]){
			Players = players;
		}
	    public void run() {
	    	try {
	    		String list = new String();
	    		String win = new String();
	    		String avg = new String();
	    		for(int i=0;i<Players.length;i++)
	    		{
	    			list+=Players[i]+" ";
	    			
	    			if(Players[i]!=null) {
		    		java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_info WHERE name = ?");
		    		stmt.setString(1, Players[i]);

					ResultSet rs = stmt.executeQuery();
					while(rs.next()){
							win+= rs.getFloat(2)+" ";
							avg+= rs.getFloat(4)+" ";
							break;
	    		}
	    			}
	    		}
	    		sendMessages("List "+list);
	    		sendMessages("Wins "+win);
	    		sendMessages("AVG "+avg);
	    		final int[] ints = new Random().ints(1, 13).distinct().limit(4).toArray();
	    		
	    		String cards = new String();
	    		
	    		for(int i=0;i<ints.length;i++)
	    			cards+=ints[i]+" ";
				
	    		sendMessages("Cards "+cards);
	    		
	    		
			} catch (Exception e) {

				e.printStackTrace();
			}finally {
				
					try {
						
					} catch (Exception e) { 
						
					}
				
			}
	    }
	}
	
	private Session session_q;
	private void createSession_q() throws JMSException {
		try {
			System.out.println("start");
			session_q = ((javax.jms.Connection) connection_q).createSession(false, Session.AUTO_ACKNOWLEDGE);
			System.out.println("end");
		} catch (JMSException e) {
			System.out.println("error");
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	// Was: QueueReceiver

	
	
	public void close_q() {
		if(connection_q != null) {
			try {
				connection_q.close();
			} catch (JMSException e) { }
		}
	}
	 
}

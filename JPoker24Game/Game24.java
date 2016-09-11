import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Game24 implements Runnable{
	private Game24_Backend backend;
	JFrame frame = new JFrame();
	JPanel Login = new JPanel();
	JPanel Login_buttons = new JPanel();
	JLabel label_login_name=new JLabel();
	JLabel label_password=new JLabel();
	JLabel label_Welcome=new JLabel();
	JButton btn_login = new JButton("Login");
	JButton btn_register = new JButton("Register");
	JTextField txt_login = new JTextField(20);
	JPasswordField txt_password = new JPasswordField(20);
	String Host = new String();
	public Game24(String host){
		Host=host;
		try {
		        Registry registry = LocateRegistry.getRegistry(host);
		        backend = (Game24_Backend)registry.lookup("Backend");

		    } catch(Exception e) {
		        System.err.println("Failed accessing RMI: "+e);
		    }
		
	}
	public void run(){

		label_Welcome.setText("Welcome to Game24. Please Login.");
		Login.add(label_Welcome);
		label_login_name.setText("Login Name");
		Login.add(label_login_name);
		Login.add(txt_login);
		label_password.setText("  Password  ");
		Login.add(label_password);
		Login.add(txt_password);
		Login_buttons.add(btn_login,BorderLayout.WEST);
		Login_buttons.add(btn_register,BorderLayout.EAST);
		Login.add(Login_buttons);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		btn_login.addActionListener(new login_action());
		btn_register.addActionListener(new register_action());
		frame.getContentPane().add(Login);
		frame.setTitle("Login");
		frame.setSize(300, 200);
		frame.setVisible(true);
	}

	public static void main(String[] args){
	    SwingUtilities.invokeLater(new Game24(args[0]));
	}
	
	public class login_action implements ActionListener {
		public void actionPerformed(ActionEvent btn_login) {
			label_Welcome.setText("Welcome to Game24. Please Login.");
			if (txt_login.getText().isEmpty()||txt_password.getPassword().length==0)
				JOptionPane.showMessageDialog(frame, "Login name and password cannot be blank");
			else
				new try_login().execute(); 
		}
	}
	
	public class register_action implements ActionListener {
		public void actionPerformed(ActionEvent btn_register){
		    SwingUtilities.invokeLater(new Register(Host));
			frame.setVisible(false);
		}
	}
	
	public boolean login_try() {
		if (backend!=null){
			try{
				return backend.login(txt_login.getText(),txt_password.getPassword());
			}
			catch (Exception e){
				return false;
			}
		}
		return false;
	}
	boolean result_login;
	private class try_login extends SwingWorker<Void, Void> {

		protected Void doInBackground() {
			result_login=login_try();
			return null;
		}
		protected void done() {
			if(result_login==true) {
			    SwingUtilities.invokeLater(new Dashboard(Host,txt_login.getText()));
				frame.setVisible(false);
			}
			else
				JOptionPane.showMessageDialog(frame, "Problem Please Try Again.");

		}
	}

}

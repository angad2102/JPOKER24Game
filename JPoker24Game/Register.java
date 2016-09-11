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


public class Register implements Runnable{
	private Game24_Backend backend;
	JFrame frame = new JFrame();
	JPanel Register = new JPanel();
	JPanel Register_buttons = new JPanel();
	JLabel label_login_name=new JLabel();
	JLabel label_password=new JLabel();
	JLabel label_confirm_password=new JLabel();
	JLabel label_Welcome=new JLabel();
	JButton btn_register = new JButton("Register");
	JButton btn_cancel = new JButton("Cancel");
	JTextField txt_login = new JTextField(20);
	JPasswordField txt_password = new JPasswordField(20);
	JPasswordField txt_confirm_password = new JPasswordField(20);
	String Host = new String();
	
	public Register(String host){
		Host = host;
		try {
	        Registry registry = LocateRegistry.getRegistry(host);
	        backend = (Game24_Backend)registry.lookup("Backend");
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
	}
	public void run(){
		label_Welcome.setText("Welcome to Game24. Please Register.");
		Register.add(label_Welcome);
		label_login_name.setText("Login Name");
		Register.add(label_login_name);
		Register.add(txt_login);
		label_password.setText("  Password  ");
		Register.add(label_password);
		Register.add(txt_password);
		label_confirm_password.setText("Confirm Password");
		Register.add(label_confirm_password);
		Register.add(txt_confirm_password);
		Register_buttons.add(btn_cancel,BorderLayout.WEST);
		Register_buttons.add(btn_register,BorderLayout.EAST);
		Register.add(Register_buttons);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		btn_cancel.addActionListener(new cancel_action());
		btn_register.addActionListener(new register_action());
		frame.getContentPane().add(Register);
		frame.setTitle("Register");
		frame.setSize(300, 250);
		frame.setVisible(true);
	}

	public class cancel_action implements ActionListener {
		public void actionPerformed(ActionEvent btn_cancel) {
		    SwingUtilities.invokeLater(new Game24(Host));
			frame.setVisible(false);
		}
	}
	
	public class register_action implements ActionListener {
		public void actionPerformed(ActionEvent btn_register){
			if(txt_confirm_password.getText().equals(txt_password.getText())){
				if (txt_confirm_password.getText().isEmpty()||txt_password.getText().isEmpty()||txt_login.getText().isEmpty())
					JOptionPane.showMessageDialog(frame, "No field can be left empty");
				else
					new try_register().execute();
			}
			else {
				JOptionPane.showMessageDialog(frame, "Passwords do not match.");

			}
		}
	}
	
	public boolean register_try() {
		if (backend!=null){
			try{
				return backend.register(txt_login.getText(),txt_password.getPassword());
			}
			catch (Exception e){
				return false;
			}
		}
		return false;
	}
	
	boolean result_register=false;
	
	private class try_register extends SwingWorker<Void, Void> {

		protected Void doInBackground() {
			result_register=register_try();
			return null;
		}
		
		protected void done() {
			if(result_register==true) {
			    SwingUtilities.invokeLater(new Dashboard(Host,txt_login.getText()));
				frame.setVisible(false);
			}
			else
				JOptionPane.showMessageDialog(frame, "Login Name already exists.");
		}
	}
	
}

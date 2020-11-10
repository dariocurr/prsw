package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.formdev.flatlaf.*;

import common.IRenterObserver;
import company.ClientProxy;

//import common.Vehicle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.activation.ActivationGroupID;

public class VehicleRentalGUI {
	
	private JPanel selectRentPanel;
	private JPanel selectReturnPanel;
	private JTabbedPane tabPane;
	private JFrame frame;
	private ClientProxy clientProxy;

	public VehicleRentalGUI(IRenterObserver renter) throws MalformedURLException, RemoteException, NotBoundException {

		try {
		    UIManager.setLookAndFeel( new FlatLightLaf() );
		    if(!FlatDarkLaf.install()) {
		    	System.err.println( "Failed to initialize dark theme" );
		    	
		    }
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		
		this.frame = new JFrame("Vehicles Rental");
		
		this.clientProxy = new ClientProxy();
		
		
		this.tabPane = new JTabbedPane();
		this.selectRentPanel = new RentPanel(renter);
        this.selectReturnPanel = new ReturnPanel(renter);
		
        this.tabPane.addTab("RENT A VEHICLE", this.selectRentPanel);
        this.tabPane.addTab("RETURN A VEHICLE", this.selectReturnPanel);
		this.tabPane.setSelectedIndex(0);
		
		this.frame.add(this.tabPane);
		
		this.tabPane.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent ev) {
				JTabbedPane tab = (JTabbedPane)ev.getSource();
	            if(tab.getSelectedComponent().getClass().toString().equals("class gui.RentPanel")) {
	            	try {
						selectRentPanel = new RentPanel(renter);
					} catch (MalformedURLException | RemoteException | NotBoundException e1) {
						e1.printStackTrace();
					}
	            	
	          
	            	tabPane.remove(0);
					tabPane.insertTab("RENT A VEHICLE", null, selectRentPanel, null, 0);
					//int selectedIndex = tabPane.getSelectedIndex();
					//int nextIndex = selectedIndex == tabPane.getTabCount()-1 ? 0 : selectedIndex+1;
					tabPane.setSelectedIndex(0);
					tabPane.revalidate();
					tabPane.repaint();
	            }
	            
	            if(tab.getSelectedComponent().getClass().toString().equals("class gui.ReturnPanel")) {
	            	try {
						selectReturnPanel = new ReturnPanel(renter);
					} catch (MalformedURLException | RemoteException | NotBoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//frame.remove(tabPane);
					tabPane.remove(1);
					tabPane.addTab("RETURN A VEHICLE", selectReturnPanel);
					//int selectedIndex = tabPane.getSelectedIndex();
					//int nextIndex = selectedIndex == tabPane.getTabCount()-1 ? 0 : selectedIndex+1;
					tabPane.setSelectedIndex(1);
					tabPane.revalidate();
					tabPane.repaint();
					//tabPane.setSelectedComponent(selectReturnPanel);
					//frame.add(tabPane);
	            }
				
			}
		});
		
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setUndecorated(true);
		this.frame.setPreferredSize(new Dimension(720, 500));
		this.frame.setMinimumSize(new Dimension(580, 400));
		this.frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}
	
	public JPanel getSelectReturnPanel() {
		return selectReturnPanel;
	}


	/*public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		new VehicleRentalGUI();
	}*/
}

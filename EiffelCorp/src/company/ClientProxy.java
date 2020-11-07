package company;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.List;

import common.ICarRentalObservable;
import common.IRent;
import common.IRenterObserver;
import common.IVehicle;
import gui.VehicleRentalGUI;

public class ClientProxy {
	ICarRentalObservable carRental;
	
	
	
	public ClientProxy() throws MalformedURLException, RemoteException, NotBoundException {
		Path currentPath = Paths.get("");
		String path = currentPath.toAbsolutePath().toString();
		path = path.substring(0, path.lastIndexOf(File.separator));
		String policy_path = "file:" + File.separator + File.separator + path + File.separator + "EiffelCorp" + File.separator + "src" + File.separator + "company" + File.separator + "sec.policy";
		String codebase_path = "file:" + File.separator + File.separator + path + File.separator + "IfsCars" + File.separator + "bin" + File.separator;
		System.setProperty("java.security.policy", policy_path);
		System.setProperty("java.rmi.server.codebase", codebase_path);
		System.setSecurityManager(new RMISecurityManager());
		carRental = (ICarRentalObservable) Naming.lookup("CarRentalService");
		
	}
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		
		new ClientProxy();
		//VehicleRentalGUI gui = new VehicleRentalGUI();
		
		/*try {
			
			IRenterObserver renter = new Renter("cugino", "antonio", 22, "fafas@ga", "123", true);
			System.out.println("DISPONIBILI");
			for (IVehicle vehicle : carRental.getAvailableVehicles()) {
				System.out.println(vehicle.getModel());
			}
			this.carRental.rentVehicle(renter, carRental.getAvailableVehicles().get(0), "26/01/97" , "28/01/97", "EMP001");
			this.carRental.rentVehicle(renter, carRental.getAvailableVehicles().get(1), "29/01/97" , "30/01/97", "EMP002");
			System.out.println("\nAFFITTATE");
			for (IRent rent : carRental.getRenterRentals(renter)) {
				System.out.println(rent.getVehicle());
			}
			System.out.println("\nDISPONIBILI");
			for (IVehicle vehicle : carRental.getAvailableVehicles()) {
				System.out.println(vehicle.getModel());
			}
			System.out.println("\nLISTA ATTESA");
			for (IVehicle vehicle : carRental.getNotAvailableVehicles().keySet()) {
				System.out.println(vehicle.getModel() + " " + carRental.getNotAvailableVehicles().get(vehicle));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
	}*/
	
	/*
	@Override
	public boolean createRenter(String firstName, String lastName, String email, String password, String discountCode) {
		IRenter renter;
		if (discountCode == "EMP001") {
			renter = new Employee(firstName, lastName, email, password);
		} else {
			renter = new Renter(firstName, lastName, email, password, false);
		}
		this.renters.stream().forEach(v -> System.out.println(v));
		return this.renters.add(renter);*/
	} 
	
	public List<IVehicle> getAvailableVehicles() throws RemoteException {
		return this.carRental.getAvailableVehicles();
	}
	
	public List<IRent> getRenterRentals(IRenterObserver renter) throws RemoteException{
		return this.carRental.getRenterRentals(renter);
	}
	
	
}
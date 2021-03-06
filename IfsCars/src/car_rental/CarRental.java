package car_rental;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import common.ICarRentalObservable;
import common.IRent;
import common.IRenterObserver;
import common.IVehicle;
import common.Vehicle;

/** Class which implements the @ICarRentalObservable interface */
public class CarRental extends UnicastRemoteObject implements ICarRentalObservable {
	
	private List<IVehicle> availableVehicles;
	private Map<IRenterObserver, List<IRent>> rentals;
	private Map<IVehicle, List<IRent>> waitList;

	/** Default constructor used to initialize the list of rents, the waitlist and the list of the available vehicles */
	public CarRental() throws RemoteException {
		this.availableVehicles = CarRental.loadVehiclesFromFile("res" + File.separator + "car_list.json");
		this.rentals = new HashMap<IRenterObserver, List<IRent>>();
		this.waitList = new HashMap<IVehicle, List<IRent>>();
		for (IVehicle vehicle : this.availableVehicles) {
			this.waitList.put(vehicle, new ArrayList<IRent>());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public List<IVehicle> getAvailableVehicles() throws RemoteException {
		return this.availableVehicles;
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<IVehicle, String> getNotAvailableVehicles() throws RemoteException {
		Map<IVehicle, String> notAvailableVehicles = new HashMap<IVehicle, String>();
		for (IVehicle vehicle : this.waitList.keySet()) {
			List<IRent> vehicleWaitList = this.waitList.get(vehicle);
			if (!vehicleWaitList.isEmpty()) {
				notAvailableVehicles.put(vehicle, vehicleWaitList.get(0).getEndDate());
			}
			
		}
		for (List<IRent> renterRentals : this.rentals.values()) {
			for (IRent rent : renterRentals) {
				if (!notAvailableVehicles.containsKey(rent.getVehicle())) {
					notAvailableVehicles.put(rent.getVehicle(), rent.getEndDate());
				}
			}
		}
		return notAvailableVehicles;
	}

	/** {@inheritDoc} */
	@Override
	public IRent rentVehicle(IRenterObserver renter, IVehicle vehicle, String startDate, String endDate, String coupon) throws RemoteException {
		IRent rent = this.createRent(renter, vehicle, startDate, endDate, coupon);
		
		if(waitList.get(vehicle).isEmpty()) {
			this.insertRent(renter, rent);
			this.attach(renter, vehicle, startDate, endDate, coupon);
			this.availableVehicles.get(this.availableVehicles.indexOf(vehicle)).setForSale(false);
			return rent;
		}
		
		return null;
	}
	
	/** {@inheritDoc} */
	@Override
	public void rentVehicle(IRent rent) throws RemoteException {
		Objects.requireNonNull(rent);
		this.insertRent(rent.getRenter(), rent);
	}
	
	/** {@inheritDoc} */
	@Override
	public void returnVehicle(IRent rent) throws RemoteException {
		Objects.requireNonNull(rent);
		this.rentals.get(rent.getRenter()).remove(rent);
		this.notifyObserver(rent.getVehicle());
	}

	/** {@inheritDoc} */
	@Override
	public void returnVehicle(IRent rent, List<String> notes) throws RemoteException {
		Objects.requireNonNull(notes);
		System.out.println(rent.getRenter().getEmail()+" returned the car "+rent.getVehicle()+" with notes added");
		this.returnVehicle(rent);
		rent.getVehicle().getNotes().addAll(notes);
		this.availableVehicles.get(this.availableVehicles.indexOf(rent.getVehicle())).getNotes().addAll(notes);
	}

	/** {@inheritDoc} */
	@Override
	public IRent attach(IRenterObserver renter, IVehicle vehicle, String startDate, String endDate, String coupon) throws RemoteException {
		IRent rent = this.createRent(renter, vehicle, startDate, endDate, coupon);
		this.waitList.get(vehicle).add(rent);
		return rent;
	}

	/** {@inheritDoc} */
	@Override
	public boolean detach(IRent rent) throws RemoteException {
		Objects.requireNonNull(rent);
		return this.waitList.get(rent.getVehicle()).remove(rent);
	}

	/** {@inheritDoc} */
	@Override
	public void notifyObserver(IVehicle vehicle) throws RemoteException {
		Objects.requireNonNull(vehicle);
		List<IRent> vehicleWaitlist = this.waitList.get(vehicle);
		vehicleWaitlist.remove(0);
		if (!vehicleWaitlist.isEmpty()) {
			this.rentVehicle(vehicleWaitlist.get(0));
		}
		else
			this.availableVehicles.get(this.availableVehicles.indexOf(vehicle)).setForSale(true);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<IRent> getRenterRentals(IRenterObserver renter) throws RemoteException {
		Objects.requireNonNull(renter);
		if (this.rentals.get(renter) == null) {
			this.rentals.put(renter, new ArrayList<IRent>());
		}
		return this.rentals.get(renter);
	}
	
	/** {@inheritDoc} */
	public boolean removeVehicle(IVehicle vehicle) throws RemoteException {
		if (this.availableVehicles.contains(vehicle)) {
			this.waitList.remove(vehicle);
			return this.availableVehicles.remove(vehicle);
		} else {
			return false;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean checkCouponCode(String code) throws RemoteException{
		if (code.equalsIgnoreCase("EMP001")) {
			return true;
		} else {
			return false;
		}
		
	}
	
	private IRent createRent(IRenterObserver renter, IVehicle vehicle, String startDate, String endDate, String coupon) throws RemoteException {
		Objects.requireNonNull(renter);
		Objects.requireNonNull(vehicle);
		Objects.requireNonNull(startDate);
		Objects.requireNonNull(endDate);
		Objects.requireNonNull(coupon);
		double discount = 0;
		if (this.checkCouponCode(coupon)) {
			discount = 0.10;
		}
		vehicle.setForSale(true);
		return new Rent(renter, vehicle, startDate, endDate, discount);
	}
	
	private boolean insertRent(IRenterObserver renter, IRent rent) {
		Objects.requireNonNull(renter);
		Objects.requireNonNull(rent);
		if (this.rentals.get(renter) == null) {
			this.rentals.put(renter, new ArrayList<IRent>());
		}
		return this.rentals.get(renter).add(rent);
	}
	

	private static List<IVehicle> loadVehiclesFromFile(String url) {
		JSONParser jsonParser = new JSONParser();
		List<IVehicle> vehiclesList = new ArrayList<>();
		try (FileReader reader = new FileReader(url)) {
            Object obj = jsonParser.parse(reader);
            JSONArray vehicles = (JSONArray) obj;
            for (Object vehicle : vehicles) {
            	vehiclesList.add(parseVehicleObject( (JSONObject) vehicle));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }	
		return vehiclesList;
	}
	
	private static IVehicle parseVehicleObject(JSONObject vehicleObject) {
		String model = (String) vehicleObject.get("model");
		String year = (String) vehicleObject.get("year"); 
		String seats = (String) vehicleObject.get("seats");
		String doors = (String) vehicleObject.get("doors");
		String transmission = (String) vehicleObject.get("transmission");
		String pricePerDay = (String) vehicleObject.get("pricePerDay");
		String price = (String) vehicleObject.get("price");
		String size = (String) vehicleObject.get("size");
		String file_name = (String) vehicleObject.get("fileName");
		IVehicle vehicle = new Vehicle(model, year, Integer.parseInt(seats), Integer.parseInt(doors), transmission, size, Double.parseDouble(pricePerDay), Double.parseDouble(price), file_name);
        return vehicle;
    }
}

package bank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.rpc.ServiceException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.currencysystem.webservices.currencyserver.CurncsrvReturnRate;
import com.currencysystem.webservices.currencyserver.CurrencyServerLocator;
import com.currencysystem.webservices.currencyserver.CurrencyServerSoap;

import common.IBank;
import common.IBankAccount;

/** Class that implements @IBank interface */
public class Bank implements IBank {

	private List<IBankAccount> accounts;
	
	/** Default constructor used to keep all the bank accounts informations */
	public Bank() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(".." + File.separator + "BankService" + File.separator + "bank_accounts_list.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String json = reader.lines().collect(Collectors.joining());
        this.accounts = this.loadBankAccountsFromFile(json);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean makePayment(String accountNumber, double amount, String currency) {
		Objects.requireNonNull(accountNumber);
		Objects.requireNonNull(currency);
		if (currency != "EUR") {
			CurrencyServerSoap currencySystem;
			try {
				currencySystem = new CurrencyServerLocator().getCurrencyServerSoap();
				amount = (double) currencySystem.convert("", currency, "EUR", amount, false, "", CurncsrvReturnRate.curncsrvReturnRateNumber, "", "");
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}

		for (IBankAccount account : this.accounts) {
			if (account.getAccountNumber().equalsIgnoreCase(accountNumber)) {
				if (account.getAmount() >= amount) {
					account.setAmount(account.getAmount() - amount);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public double getExchangeFromEUR(String currency, double amount) {
		Objects.requireNonNull(currency);
		try {
			CurrencyServerSoap currencySystem = new CurrencyServerLocator().getCurrencyServerSoap();
			return (double) currencySystem.convert("", currency, "EUR", amount, false, "", CurncsrvReturnRate.curncsrvReturnRateNumber, "", ""); 
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private static List<IBankAccount> loadBankAccountsFromFile(String json) {
		JSONParser jsonParser = new JSONParser();	
		List<IBankAccount> bankAccountsList = new ArrayList<>();
		Object obj;
		try {
			obj = jsonParser.parse(json);
			JSONArray bankAccounts = (JSONArray) obj;
	        for(Object bankAccount : bankAccounts) {
	        	bankAccountsList.add(parseBankAccountObject( (JSONObject) bankAccount));
	        }
		} catch (ParseException e) {
			System.out.println("ERROR: Reading from bank accounts json");
			e.printStackTrace();
		}

		return bankAccountsList;
	}
	
	private static IBankAccount parseBankAccountObject(JSONObject bankAccountObject) {
		String accountNumber = (String) bankAccountObject.get("accountNumber");
		String amount = (String) bankAccountObject.get("amount"); 
		IBankAccount bankAccount = new BankAccount(accountNumber, Double.parseDouble(amount));
        return bankAccount;
    }
    
	
}

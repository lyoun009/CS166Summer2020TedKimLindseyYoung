/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import java.util.Scanner;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;


	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1  Ted
		System.out.println("Successfully entered AddCustomer function."); // For testing purposes, can delete later!
	}
	
	public static void AddMechanic(MechanicShop esql){//2 Lindsey
		// has basic adding to db functionality
		// still need to implement checking valid parameters part 
		// aka error handling if wrong input
		
			String mechFirstName;
			String mechLastName;
			int mechID;
			int mechExp; 
			
			// Reading in input using Scanner
			Scanner scnr = new Scanner(System.in);

			while(true){
				try{
					System.out.print("Enter the mechanic first name: ");
					mechFirstName = scnr.nextLine();
					if(mechFirstName.length() > 32){
						throw new Exception("First name cannot exceed 32 characters.");
					}
					break;
				}
				catch(Exception ex){
					System.out.println(ex.getMessage());
					System.out.println("Try again.");
				}

			}


			System.out.print("Enter the mechanic last name: ");
			mechLastName = scnr.nextLine();
			System.out.print("Enter the mechanic id: ");
			mechID = scnr.nextInt();
			System.out.print("Enter the mechanic's years of experience: ");
			mechExp = scnr.nextInt();
			
			// Putting values into database: 
			try{
				esql.executeUpdate("INSERT INTO MECHANIC (id, fname, lname, experience) VALUES (" 
				+ mechID + ", '" + mechFirstName + "', '" + mechLastName + "', " + mechExp + ");"  );
				// This will output the newly entered data:
				String test = "SELECT * FROM MECHANIC WHERE id = '" + mechID + "';" ;
				esql.executeQueryAndPrintResult(test);
				
			}
			catch (Exception e){
				System.err.println (e.getMessage());
			}
				

		


	}
	
	public static void AddCar(MechanicShop esql){//3 Lindsey
		String carVin;
		String carMake;
		String carModel;
		int carYear; 
		
		// Reading in input using Scanner
		boolean keepAsking = true;
		Scanner scnr = new Scanner(System.in);

		System.out.print("Enter the car vin: ");
		carVin = scnr.next();
		System.out.print("Enter the car make: ");
		carMake = scnr.next();
		System.out.print("Enter the car model: ");
		carModel = scnr.next();
		System.out.print("Enter the car year: ");
		carYear = scnr.nextInt();
		
		// Putting values into database: 
		try{
			esql.executeUpdate("INSERT INTO CAR (vin, make, model, year) VALUES (" 
			+ carVin + ", '" + carMake + "', '" + carModel + "', " + carYear + ");"  );
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}
			
		
		// Check if values got inputted into DB
		try{
			String test = "SELECT * FROM CAR WHERE vin = '" + carVin + "';" ;
			esql.executeQueryAndPrintResult(test);
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}
		
		
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4 Lindsey
		String userInput;
		Scanner scnr = new Scanner(System.in);

		// ask for user to input the last name, read in input
		System.out.print("Enter in the last name: ");
		userInput = scnr.next();
		int test = -1;
		boolean keepAsking = true;

		// check if exists in table
		try{
			String check = "SELECT * FROM CUSTOMER WHERE lname= '" + userInput + "';" ;
			test = esql.executeQuery(check);
			//System.out.println("Test val: " + test); // for testing, comment out later
			// if test is 1, means that record exists. 0 means doesnt exist in table
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}

		if(test >= 1){ // record exists in table
			// output all clients that match user's input
			try{
				String getLName = "SELECT id, fname, lname FROM CUSTOMER WHERE lname = '" + userInput + "';" ;
				esql.executeQueryAndPrintResult(getLName);
			}
			catch (Exception e){
				System.err.println (e.getMessage());
			}
			
			int custID = -1;

			while(keepAsking){
				boolean notValid = true;
				while(notValid){
					System.out.print("\n Enter in the id of customer you would like to select: ");
					custID = scnr.nextInt();

						//check if selection exists (is valid)
					try{
						test = esql.executeQuery("SELECT id, fname, lname FROM CUSTOMER WHERE lname = '" + userInput + "' AND id = " + custID + ";");
					}
					catch (Exception e){
						System.err.println (e.getMessage());
					}

					if(test == 1){// means its valid customer id
						notValid = false;
					}
					else{ // means its not a valid customer id
						System.out.println("Invalid customer id. Please try again.");
						notValid = true;
					}

				}

				String getCars = "SELECT ownership_id, vin, make, model, year FROM OWNS o, Customer cust, Car cc WHERE o.customer_id = cust.id AND o.car_vin = cc.vin AND cust.id = " + custID + ";" ;
				
				//check if selection exists (is valid)
				try{
					test = esql.executeQuery(getCars);
				}
				catch (Exception e){
					System.err.println (e.getMessage());
				}
				
				System.out.println("Test val: " + test); // for testing, comment out later
				// if test is >= 1, means record(s) exists. 0 means doesnt exist in table

				int ownID;
				if(test >= 1){
					while(true){
						try{
							esql.executeQueryAndPrintResult(getCars);
						}
						catch (Exception e){
							System.err.println (e.getMessage());
						}
						System.out.print("\nEnter in the ownership id of the car you would like to add the service request to: ");
						ownID = scnr.nextInt();
						try{
							test = esql.executeQuery("SELECT ownership_id, vin, make, model, year FROM OWNS o, Customer cust, Car cc WHERE o.customer_id = cust.id AND o.car_vin = cc.vin AND cust.id = " + custID + "AND ownership_id = " + ownID + ";");
						}
						catch (Exception e){
							System.err.println (e.getMessage());
						}
						if(test == 1){//valid car selection
							// Initiate the service request here
							System.out.println("Initiating service request...");
							// TODO: FIXME
							keepAsking = false;
							break;
						}
						else{// invalid car selection
							System.out.println("Invalid car selection. Try again.");
						}
					}

				}
				else{
					System.out.println("Car doesn't exist.");
				}
			
			}

		}
		else{ // record does not exist, option to add customer
			int userChoice;
			System.out.println("Customer does not exist in database.");
			
			while(keepAsking){
				System.out.println("Would you like to add this customer to the database? \n 1. Yes \n 2. No");
				userChoice = scnr.nextInt();
				if(userChoice == 1){ // go to AddCustomer function
					AddCustomer(esql);
					break;
				}
				else if(userChoice == 2){ // Exit back to Main Menu
					System.out.println("Stopped initiating service request... Returning to MAIN MENU.");
					break;
				}
				else{
					System.out.println("Invalid choice. Please try again.");
					keepAsking = true;
				}
			}				
		}		
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5 Ted
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6 Ted
		
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7 Ted
		
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8 Ted
		
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9 Lindsey
		// SQL Code to implement:
		// SELECT cc.make, cc.model, COUNT(*) AS num_serv_requests
		// FROM Car cc, Service_request s
		// WHERE cc.vin = s.car_vin
		// GROUP BY cc.vin
		// ORDER BY num_serv_requests desc
		// LIMIT K;

		int k;
		Scanner scnr = new Scanner(System.in);

		System.out.print("Enter K number of cars: ");
		k = scnr.nextInt();

		String sqlCmd = "SELECT cc.make, cc.model, COUNT(*) AS num_serv_requests FROM CAR cc, Service_request s WHERE cc.vin = s.car_vin GROUP BY cc.vin ORDER BY num_serv_requests desc LIMIT " + k + ";" ;
		
	
		try{
			esql.executeQueryAndPrintResult(sqlCmd);
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}
		
	
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){// 10 Lindsey
		// SELECT cust.fname, cust.lname, SUM (bill) AS total_bill
		// FROM CUSTOMER cust, SERVICE_REQUEST sr, CLOSED_REQUEST cr
		// WHERE cust.id = sr.customer_id
		// AND cr.rid = sr.rid
		// GROUP BY cust.id
		// ORDER BY total_bill desc

		String sqlCmd = "SELECT cust.fname, cust.lname, SUM (bill) AS total_bill FROM CUSTOMER cust, SERVICE_REQUEST sr, CLOSED_REQUEST cr WHERE cust.id = sr.customer_id AND cr.rid = sr.rid GROUP BY cust.id ORDER BY total_bill desc;" ;
		
		try{
			esql.executeQueryAndPrintResult(sqlCmd);
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}

		
	}
	
}
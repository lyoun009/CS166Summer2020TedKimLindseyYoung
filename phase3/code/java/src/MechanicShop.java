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
		try {
			String qs;
			List<List<String>> x;
			int c_id;

		
			qs = String.format("SELECT C.id FROM Customer C");
			x = esql.executeQueryAndReturnResult(qs);
			c_id = x.size() + 1;
			while(true) {
				try { 
					Scanner fnO = new Scanner(System.in);
					System.out.println("Enter customer's first name: ");
					String tfN = fnO.nextLine();
					if (tfN.length() > 32) {
						throw new Exception("First name cannot exceed 32 characters.");
					}
					else if (tfN.length() <= 0) {
						throw new Exception("First name cannot be null.");
					}
				}
				catch (Exception exc) {
					System.err.println(exc.getMessage());
				}
			}

			while(true) {
				try { 
					Scanner lnO = new Scanner(System.in);
					System.out.println("Enter customer's last name: ");
					String tlN = lnO.nextLine();
					if (tlN.length() > 32) {
						throw new Exception("Last name cannot exceed 32 characters.");
					}
					else if (tlN.length() <= 0) {
						throw new Exception("Last name cannot be null.");
					}
				}
				catch (Exception exc) {
					System.err.println(exc.getMessage());
				}
			}

			while(true) {
				try { 
					Scanner pnO = new Scanner(System.in);
					System.out.println("Enter customer's phone number: ");
					String tpN = pnO.nextLine();
					if (tpN.length() > 10) {
						throw new Exception("Phone number cannot exceed 10 characters.");
					}
					else if (tpN.length() <= 0) {
						throw new Exception("Phone number cannot be null.");
					}
				}
				catch (Exception exc) {
					System.err.println(exc.getMessage());
				}
			}

			Scanner aO = new Scanner(System.in);
			System.out.println("Enter customer's address: ");
			String ta = aO.nextLine();

			String sq;
			sq = String.format("INSERT INTO Customer(id, fname, lname, phone, address) VALUES(%d, '%s', '%s', '%s', '%s')", c_id, tfN, tlN, tpN, ta);
			esql.executeUpdate(sq);
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}

	}
	
	public static void AddMechanic(MechanicShop esql){//2 Lindsey
			String mechFirstName;
			String mechLastName;
			int mechID = 0;
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
					else if(mechFirstName.length() <= 0){
						throw new Exception("First name cannot be null.");
					}
					break;
				}
				catch(Exception ex){
					System.out.println(ex.getMessage());
					System.out.println("Please try again.");
				}
			}

			while(true){
				try{
					System.out.print("Enter the mechanic last name: ");
					mechLastName = scnr.nextLine();
					if(mechLastName.length() > 32){
						throw new Exception("Last name cannot exceed 32 characters.");
					}
					else if(mechLastName.length() <= 0){
						throw new Exception("Last name cannot be null.");
					}
					break;
				}
				catch(Exception ex){
					System.out.println(ex.getMessage());
					System.out.println("Please try again.");
				}

			}

			try{
				String qs;
				List<List<String>> x;

				qs = String.format("SELECT M.id FROM Mechanic M");
				x = esql.executeQueryAndReturnResult(qs);
				mechID = x.size() + 1;
			}
			catch(Exception e){
				System.err.println (e.getMessage());
			}

			while(true){
				try{
					System.out.print("Enter the mechanic's years of experience: ");
					mechExp = scnr.nextInt(); 
					if(mechExp < 0){
						throw new Exception("Years of experience cannot be a negative number.");
					}
					break;
				}
				catch(Exception ex){
					System.out.println(ex.getMessage());
					System.out.println("Please try again.");
				}
			}
			// Putting values into database: 
			try{
				System.out.println("\nAdding new mechanic to database:");
				esql.executeUpdate("INSERT INTO MECHANIC (id, fname, lname, experience) VALUES (" 
				+ mechID + ", '" + mechFirstName + "', '" + mechLastName + "', " + mechExp + ");"  );
				// This will output the newly entered data:
				String test = "SELECT * FROM MECHANIC WHERE id = '" + mechID + "';" ;
				esql.executeQueryAndPrintResult(test);
				System.out.println("Done adding mechanic. Returning to MAIN MENU...\n");
				
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
		Scanner scnr = new Scanner(System.in);

		while(true){
			try{
				System.out.print("Enter the car vin: ");
				carVin = scnr.next();
				if(carVin.length() == 0 || carVin.length() > 16){
					throw new Exception("Invalid VIN, can't be null and must be less than 16 characters.");
				}
				
				System.out.print("Enter the car make: ");
				carMake = scnr.next();
				if(carMake.length() == 0 || carMake.length() > 32){
					throw new Exception("Invalid input. Cannot exceed 32 characters!");
				}
				
				System.out.print("Enter the car model: ");
				carModel = scnr.next();
				if(carModel.length() == 0 || carModel.length() > 32){
					throw new Exception("Invalid input. Cannot exceed 32 characters!");
				}
				
				System.out.print("Enter the car year: ");
				carYear = scnr.nextInt();
				if(carYear < 1900 || carYear > 2020){
					throw new Exception("Invalid input. Must be a valid year.");
				}
				
				// input values into db
				System.out.println("\nAdding new car to database:");
				esql.executeUpdate("INSERT INTO CAR (vin, make, model, year) VALUES ('" 
				+ carVin + "', '" + carMake + "', '" + carModel + "', " + carYear + ");"  );

				//show that values are in db
				String test = "SELECT * FROM CAR WHERE vin = '" + carVin + "';" ;
				esql.executeQueryAndPrintResult(test);
				System.out.println("Done adding car. Returning to MAIN MENU...\n");
				break;
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
		}		
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4 Lindsey
		String userInput;
		boolean notValid = true;
		Scanner scnr = new Scanner(System.in);
		String sql = "";

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

			while(notValid){
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

							try{
							// getting rid
								String qs1;
								List<List<String>> x;
								int setRid;
								qs1 = String.format("SELECT s.rid FROM Service_request s");
								x = esql.executeQueryAndReturnResult(qs1);
								setRid = x.size() + 1;
							
							// getting VIN
								String qs2;
								List<List<String>> y;
								String setVin;
								qs2 = String.format("SELECT o.car_vin FROM Owns o WHERE o.ownership_id = '" + ownID + "';");
								y = esql.executeQueryAndReturnResult(qs2);
								setVin = y.toString();
								setVin = setVin.substring(2, setVin.length()-2);
							
							// getting odometer
								int odo = 0;
								System.out.print("Enter odometer reading: ");
								odo = scnr.nextInt();
								if(odo <= 0){
									throw new Exception("Odometer can't be null.");
								}
							// getting complaint 	
								String comp = "";
								System.out.print("Enter complaint: ");
								scnr.nextLine();
								comp = scnr.nextLine();
								if(comp.length() >= 10000){
									throw new Exception("Too long.");
								}								
							
								try{
									System.out.println("\nCreated Service Request:");
									sql = "INSERT INTO SERVICE_REQUEST (rid, customer_id, car_vin, date, odometer, complain) VALUES (" + setRid + ", " 
									+ custID + ", '" + setVin + "', " + "CURRENT_DATE," + odo + ", '" + comp + "');";
									esql.executeUpdate(sql);
									String t = "SELECT * FROM SERVICE_REQUEST WHERE rid = '" + setRid + "';" ;
									esql.executeQueryAndPrintResult(t);
									System.out.println("Done making service request.\n");
									break;
								}
								catch(Exception e){
									System.err.println (e.getMessage());
								}
							
							}
							catch(Exception e){
								System.err.println (e.getMessage());
							}
						}
						else{// invalid car selection
							System.out.println("Invalid car selection. Try again.\n");
						}
					}

				}
				else{
					System.out.println("Car doesn't exist.");
				}
			
			}

		}
		else{ // record does not exist, option to add customer
			char userChoice;
			System.out.println("Customer does not exist in database.");
			
			while(keepAsking){
				System.out.println("Would you like to add this customer to the database? \n 1. Yes \n 2. No");
				userChoice = scnr.next().charAt(0);
				if(userChoice == '1'){ // go to AddCustomer function
					AddCustomer(esql);
					break;
				}
				else if(userChoice == '2'){ // Exit back to Main Menu
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
		try {
			int nRows;
			int rN;
			int mechID;
			int billCost;
			String comments;
			String sq;

			String qs;
			qs = String.format("SELECT B.wid FROM Closed_Request B");
			List<List<String>> y;
			y = esql.executeQueryAndReturnResult(qs);
			int crw = y.size() + 1;

			Scanner tRN = new Scanner(System.in);
			System.out.println("Enter service request number: ");
			rN = tRN.nextInt();

			Scanner tmechID = new Scanner(System.in);
			System.out.println("Enter mechanic's ID: ");
			mechID = tmechID.nextInt();

			Scanner tcomments = new Scanner(System.in);
			System.out.println("Enter any comments regarding the service request for repairs: ");
			comments = tcomments.nextLine();

			Scanner tbillCost = new Scanner(System.in);
			System.out.println("Enter cost to put on the customer's bill: ");
			billCost = tRN.nextInt();

			sq = String.format("SELECT * From Mechanic M WHERE M.id = %d", mechID);
			nRows = esql.executeQuery(sq);
			if (nRows == 0) {
				System.out.println("Invalid mechanic ID. Does not exist.\n");
				return;
			}

			sq = String.format("SELECT * FROM Service_Request S WHERE S.rid = %d", rN);
			nRows = esql.executeQuery(sq);
			if(nRows == 0) {
				System.out.println("Invalid service request ID. Does not exist.\n");
				return;
			}

			sq = String.format("SELECT * FROM Service_Request S WHERE S.rid = %d AND S.date <= CURRENT_DATE", rN);
			nRows = esql.executeQuery(sq);
			if (nRows == 0) {
				System.out.println("Invalid or out of date request date.\n");
				return;
			}

			sq = String.format("INSERT INTO Closed_Request(wid, rid, date, comment, bill) VALUES(%d, %d, %d, CURRENT_DATE, '%s', %d)", crw, rN, mechID, comments, billCost);
			esql.executeQuery(sq);
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6 Ted
		try {
			String sq;
			int nRows;
			sq = "SELECT C.fname, C.lname, A.bill, A.comment, S.date FROM Customer C, Closed_Request A,  Service_Request S WHERE A.bill < 100 AND A.rid = S.rid AND S.customer_id = C.id";
			nRows = esql.executeQueryAndPrintResult(sq);
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7 Ted
		try {
			String sq;
			int nRows;
			sq = "SELECT A.fname, A.lname, A.numCars FROM (SELECT O.customer_id, C.fname, C.lname, COUNT(*) numCars FROM Owns O,Customer C WHERE C.id = O.customer_id GROUP BY O.customer_id, C.fname, C.lname) AS A WHERE numCars > 20";
			nRows = esql.executeQueryAndPrintResult(sq);
		}
		catch(Exception exc){
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8 Ted
		try {
			String sq;
			int nRows;
			sq = "SELECT X.make, X.model, X.year, S.odometer FROM Car X, Service_Request S WHERE S.car_vin = X.vin AND S.odometer < 50000 AND X.year < 1995";
			nRows = esql.executeQueryAndPrintResult(sq);
			
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9 Lindsey
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
		String sqlCmd = "SELECT cust.fname, cust.lname, SUM (bill) AS total_bill FROM CUSTOMER cust, SERVICE_REQUEST sr, CLOSED_REQUEST cr WHERE cust.id = sr.customer_id AND cr.rid = sr.rid GROUP BY cust.id ORDER BY total_bill desc;" ;
		
		try{
			esql.executeQueryAndPrintResult(sqlCmd);
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}
	}
}
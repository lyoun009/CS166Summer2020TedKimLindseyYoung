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

		System.out.print("Enter the mechanic first name: ");
		mechFirstName = scnr.nextLine();
		System.out.print("Enter the mechanic last name: ");
		mechLastName = scnr.nextLine();
		System.out.print("Enter the mechanic id: ");
		mechID = scnr.nextInt();
		System.out.print("Enter the mechanic's years of experience: ");
		mechExp = scnr.nextInt();

		//	Testing code to make sure it is reading in input correctly:
		// System.out.println("First name: " + mechFirstName);
		// System.out.println("Last name: " + mechLastName);
		// System.out.println("Mechanic ID: " + mechID);
		// System.out.println("Years experience: " + mechExp);
		
		// Putting values into database: 
		try{
			esql.executeUpdate("INSERT INTO MECHANIC (id, fname, lname, experience) VALUES (" 
			+ mechID + ", '" + mechFirstName + "', '" + mechLastName + "', " + mechExp + ");"  );
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}
			
		
		// Check if values got inputted into DB
		try{
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
		// SQL Code to implement
		// SELECT cc.make, cc.model, COUNT(*) AS num_serv_requests
		// FROM Car cc, Service_request s
		// WHERE cc.vin = s.car_vin
		// GROUP BY cc.vin
		// ORDER BY num_serv_requests desc
		// LIMIT K;

		int k;
		Scanner scnr = new Scanner(System.in);

		System.out.println("Enter K number of cars: ");
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

		String sqlCmd = "SELECT cust.fname, cust.lname, SUM (bill) AS total_bill FROM CUSTOMER cust, SERVICE_REQUEST sr, CLOSED REQUEST cr WHERE cust.id = sr.customer_id AND cr.rid = sr.rid GROUP BY cust.id ORDER BY total_bill desc;" ;
		
		try{
			esql.executeQueryAndPrintResult(sqlCmd);
		}
		catch (Exception e){
			System.err.println (e.getMessage());
		}

		
	}
	
}
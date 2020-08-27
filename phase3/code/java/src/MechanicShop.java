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
			x = esql.executeQueryAndPrintResult(qs);
			c_id = x.size() + 1;

			Scanner fnO = new Scanner(System.in);
			System.out.println("Enter customer's first name: ");
			String tfN = fnO.nextLine();

			Scanner lnO = new Scanner(System.in);
			System.out.println("Enter customer's last name: ");
			String tlN = lnO.nextLine();

			Scanner pnO = new Scanner(System.in);
			System.out.println("Enter customer's phone number: ");
			String tpN = pnO.nextLine();

			Scanner aO = new Scanner(System.in);
			System.out.println("Enter customer's address: ");
			String ta = aO.nextLine();

			String sq;
			sq = String.format("INSERT INTO Customer(id, fname, lname, phone, address) VALUES(%d, '%s', '%s', '%s', '%s')", c_id, tfN, tlN, tpN, ta);
			esql.executeUpdate();
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2 Lindsey
		
	}
	
	public static void AddCar(MechanicShop esql){//3 Lindsey
		
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4 Lindsey
		
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
			y = esql.executeQueryAndPrintResult(qs);
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
			System.out.println("The number of rows with customers that have bills that cost less than 100 is:" + nRows);
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7 Ted
		try {
			String sq;
			int nRows;
			sq = "SELECT C.fname, C.lname, C.numCars FROM (SELECT O.customer_id, D.fname, D.lname, COUNT(*) C.numCars FROM Owns O, Customer D WHERE D.id = O.customer_id, D.fname, D.lname) AS C WHERE C.numCars > 20";
			nRows = esql.executeQueryAndPrintResult(sq);
			System.out.println("The number of rows with customers that have more than 20 cars is: " + nRows);
		}
		catch(Exception exc){
			System.err.println(exc.getMessage);
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8 Ted
		try {
			String sq;
			int nRows;
			sq = "SELECT X.make, X.model, X.year, S.odometer FROM Car X, Service_Request S WHERE S.car_vin = X.vin AND S.odometer < 50000 AND X.year < 1995";
			nRows = esql.executeQueryAndPrintResult(sq);
			System.out.println("The number of rows with cars that are models from before 1995 with 50000 miles is: " + nRows);
			
		}
		catch(Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9 Lindsey
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){// 10 Lindsey
		//
		
	}
	
}
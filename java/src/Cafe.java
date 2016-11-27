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
import java.lang.Object;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
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
   public int executeQuery (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
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
   }//end executeQuery
   
 /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryGetResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
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
   }//end executeQueryGetResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryCount (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }//end executeQueryCount

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
	if (rs.next())
		return rs.getInt(1);
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
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql, authorisedUser);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql, authorisedUser); break;
                       case 4: UpdateOrder(esql, authorisedUser); break;
                       case 5: ViewOrderHistory(esql, authorisedUser); break;
                       case 6: ViewOrderStatus(esql, authorisedUser); break;
                       case 7: UpdateUserInfo(esql, authorisedUser); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql, authorisedUser); break;
                       case 4: EmployeeUpdateOrder(esql, authorisedUser); break;
                       case 5: ViewCurrentOrder(esql, authorisedUser); break;
                       case 6: ViewOrderStatus(esql, authorisedUser); break;
                       case 7: UpdateUserInfo(esql, authorisedUser); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql, authorisedUser); break;
                       case 4: EmployeeUpdateOrder(esql, authorisedUser); break;
                       case 5: ViewCurrentOrder(esql, authorisedUser); break;
                       case 6: ViewOrderStatus(esql, authorisedUser); break;
                       case 7: ManagerUpdateUserInfo(esql, authorisedUser); break;
                       case 8: UpdateMenu(esql, authorisedUser); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
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

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

//-----------------------TO - DO---------------------------------------
   public static String find_type(Cafe esql, String login){
      //read the username and find out the type of it and return that type
      /*
      String query = "SELECT 
      string userlogin = esql.executeQuery(query).get(0).get(0);
      */
      return "Employee";
   }

   public static void BrowseMenuName(Cafe esql){
      // ask to enter itemName
      // find the info for that item
      // display and exit
      try{  
        String query = "SELECT M.itemname, M.type, M.price, M.description FROM Menu M WHERE M.itemName =";
        System.out.print("\tEnter item name: ");
        String input = in.readLine();
        query += "\'"; 
        query += input;
        query += "\'"; 

        int rowCount = esql.executeQuery(query);
        System.out.println ("\ttotal row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }//end

   public static void BrowseMenuType(Cafe esql){
      try{  
        String query = "SELECT M.itemname, M.type, M.price, M.description FROM Menu M WHERE M.type = ";
        System.out.print("\tEnter item type: ");
        String input = in.readLine();
        query += "\'"; 
        query += input;
        query += "\'"; 

        int rowCount = esql.executeQuery(query);
        System.out.println ("\ttotal row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }//end

   public static Integer AddOrder(Cafe esql, String login){
			Integer order_id = 0;
   		try
   		{
		 		//create new order to get id
		 		String query = String.format("INSERT INTO Orders (login, paid, timestamprecieved, total) VALUES ('%s', 'f', CURRENT_TIMESTAMP, -1)", login);
		 		esql.executeUpdate(query);
		 		
		 		query = "SELECT o.orderid FROM Orders o WHERE o.total = '-1'";
		 		order_id = Integer.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
		 		query = String.format("UPDATE Orders o SET total= '0' WHERE o.orderid = '%s'", order_id);
		 		esql.executeUpdate(query);
		 		
		 		addItemStatus(esql, order_id, login);
		 		
		 		boolean more = true;
		 		
		 		while(more)
		 		{
					System.out.print("\tIs there any other order to make? (Y/N)");
		 			String input = in.readLine();		 		
		 		
			 		if((input.equals("n")) || (input.equals("N")))
			 		{
			 			more = false;
			 			System.out.println("Your order:");
			 			query =  String.format("SELECT i.itemname FROM itemStatus i WHERE i.orderid = '%s'", order_id);
		 				int rowCount = esql.executeQuery(query);
         		System.out.println ("Total Items: " + rowCount);
         		
         		//print order total
         		query =  String.format("SELECT o.total FROM Orders O WHERE O.orderid = '%s'", order_id);
		 				Double total = Double.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
		 				DecimalFormat df = new DecimalFormat("$###,###.##");
		 				df.format(total);
		 				System.out.println("Order total: $" + total);
		 				System.out.println("Order id is: " + order_id);
         		System.out.println("Thank you for your order!");
         					
			 		}
			 		else if ((input.equals("y")) || (input.equals("Y")))
			 		{
			 			addItemStatus(esql, order_id, login);
			 		}
			 		else
			 		{
			 			System.out.println("\tUnrecognized choice");
			 			System.out.println("\tIs there any other order to make? (Y/N)");
			 		}
				}		
		 		
		 	}catch(Exception e)
		 	{
		 		System.err.println(e.getMessage());
		 	}
   		
   		return order_id;
      
   }//end AddOrder

   public static void UpdateOrder(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void EmployeeUpdateOrder(Cafe esq, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderHistory(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateUserInfo(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateMenu(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderStatus(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewCurrentOrder(Cafe esql, String login){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void addItemStatus(Cafe esql, Integer order_id, String login){
   	try{	
      //make item status 
	 		System.out.print("\tPlease enter the item name: ");
	 		String item = in.readLine();
	 		Double new_total = 0.0;
	 		
	 		//check if item exists
	 		String query =  String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", item);
 			int userNum = esql.executeQueryCount(query);
 			
 			if(userNum > 0)
 			{
 				//item name exists, insert
 				String status = "Hasnt started";
 			  query = String.format("INSERT INTO itemStatus VALUES (%s, '%s', CURRENT_TIMESTAMP, '%s')", order_id, item, status);
		 		esql.executeUpdate(query);
		 		
		 		//find the new item price
		 		query = String.format("SELECT M.price FROM Menu M WHERE M.itemName = '%s'", item);
		 		new_total = Double.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
		 		//add to old total price
				query = String.format("SELECT o.total FROM Orders o WHERE o.orderid = '%s'", order_id);
		 		new_total += Double.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
		 		
		 		//add to orders
		 		query = String.format("UPDATE Orders o SET total= '%s' WHERE o.orderid = '%s'", new_total, order_id);
		 		esql.executeUpdate(query);
		 		
		 		//test print the new total
		 		query = String.format("SELECT o.total FROM Orders o WHERE o.orderid = '%s'", order_id);
		 		new_total = Double.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
			}
 			else
 			{
 				System.out.print("\tInvalid name!");		
 			}
	 		
		 }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end addItemStatus

}//end Cafe

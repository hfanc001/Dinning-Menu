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
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql, authorisedUser); break;
                       case 6: ViewOrderStatus(esql); break;
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
                       case 6: ViewOrderStatus(esql); break;
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
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
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
         System.out.print("\tEnter user phone: (can be leave blank)");
         String phone = in.readLine();
         
				 String type="Customer";
				 String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser
   
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
         int userNum = esql.executeQueryCount(query);
				 if (userNum > 0)
				 {
				 	System.out.println("\tLogged in successfully!");
					return login;
				 }
				 else
				 {
				 	System.out.println("\tWrong user login or password");
				 	return null;
				 }
      }catch(Exception e){
        System.err.println (e.getMessage ());
         return null;
      }
   }//end LogIn

   public static String find_type(Cafe esql, String login){	
		 	String type = null;
		 	try{  
         //read the username and find out the type of it and return that type
				String query = String.format("SELECT u.type FROM Users u WHERE u.login='%s'", login);
				type = String.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
      
		 	return type;
		 	//return "Employee";
   }//end find_type

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
   }//end BrowseMenuName

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
   }//end BrowseMenuType

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
		 		
		 		addItemStatus(esql, order_id);
		 		
		 		boolean more = true;
		 		
		 		while(more)
		 		{
					System.out.print("\tIs there any other order to make? (Y/N) ");
		 			String input = in.readLine();		 		
		 		
			 		if((input.equals("n")) || (input.equals("N")))
			 		{
			 			more = false;
			 			System.out.println("\tYour order:");
			 			query =  String.format("SELECT i.itemname FROM itemStatus i WHERE i.orderid = '%s'", order_id);
		 				int rowCount = esql.executeQuery(query);
         					System.out.println ("\tTotal Items: " + rowCount);
         		
				 		//print order total
				 		query =  String.format("SELECT o.total FROM Orders O WHERE O.orderid = '%s'", order_id);
		 				Double total = Double.valueOf(esql.executeQueryGetResult(query).get(0).get(0));
		 				//DecimalFormat df = new DecimalFormat("$###,###.##");
		 				//df.format(total);
		 				System.out.println("\tOrder total: $" + total);
		 				System.out.println("\tOrder id is: " + order_id);
         		System.out.println("\tThank you for your order!");
         					
			 		}
			 		else if ((input.equals("y")) || (input.equals("Y")))
			 		{
			 			addItemStatus(esql, order_id);
			 		}
			 		else
			 		{
			 			System.out.println("\tUnrecognized choice");
			 			System.out.print("\tIs there any other order to make? (Y/N) ");
			 		}
				}		
		 		
		 	}catch(Exception e)
		 	{
		 		System.err.println(e.getMessage());
		 	}
   		
   		return order_id;
      
   }//end AddOrder

   public static void UpdateOrder(Cafe esql){
   	try
   	{
      // ask for order id
      System.out.print("\tPlease enter your order id: ");
      String input = in.readLine();
		 	Integer order_id = Integer.valueOf(input);		 		
		 		
      // check if paid
      String query =  String.format("SELECT o.paid FROM Orders o WHERE o.orderid = '%s'", order_id);
      String paid = esql.executeQueryGetResult(query).get(0).get(0);
		 			
		 	//	if paid, cannot update	
		 	if(paid.equals("t"))
		 	{
		 		System.out.println("\tSorry, the order has been processed");
		 	}
		 	//	if not paid, ask which to update
      //		1. add item
      //		2. delete item
		 	else
		 	{        		
		 		boolean notAnswered = true;
		 		do
		 		{
					System.out.println("Your order:");
		 			query =  String.format("SELECT i.itemname FROM itemStatus i WHERE i.orderid = '%s'", order_id);
		 			int rowCount = esql.executeQuery(query);

			 		System.out.println("\tWhat changes would you like to make?");
			 		System.out.println("\t\t1. Add another item");
			 		System.out.println("\t\t2. Delete an item");
			 		System.out.println("\t\t3. Finish editing");
			 		input = in.readLine();
			 		if(input.equals("1"))
			 		{
			 			addItemStatus(esql, order_id);
			 		}
			 		else if(input.equals("2"))
			 		{
			 			deleteItem(esql, order_id);
			 		}
			 		else if(input.equals("3"))
			 		{
			 			System.out.println("\tThank you for checking your order");
			 			notAnswered = false;
			 		}
			 		else
			 		{
			 			System.out.println("\tUnrecognized choice. Please enter again");
			 		}
			 	}while(notAnswered);
		 	}
    }catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
   }//end UpdateOrder

//-----------------------TO - DO---------------------------------------
   public static void EmployeeUpdateOrder(Cafe esq, String login){
     /*try{
      	//insert your code here
      }catch(Exception e){
         System.err.println (e.getMessage());
     }*/
   }//end

   public static void ViewOrderHistory(Cafe esql, String login){
      try{
      	System.out.println("The login is: " + login);
      	String query = String.format("SELECT * FROM Orders WHERE login='%s' ORDER BY timestamprecieved DESC limit 5", login);
      	if(esql.executeQuery(query) == 0)
      	{
      		System.out.println("\tThere is no past order");
      	}
      }catch(Exception e){
         System.err.println(e.getMessage());
     }
   }//end ViewOrderHistory

   public static void UpdateUserInfo(Cafe esql, String login){
      try{   	
      	boolean done = false;
      	
      	do
      	{
      		//display user info 
      		String query = String.format("SELECT login, password, phonenum, favitems FROM Users WHERE login='%s'", login);
      		esql.executeQuery(query);
      	
	      	System.out.println("\tWhich would you like to update?");
	      	System.out.println("\t\t1. Password");
	      	System.out.println("\t\t2. Phone Number");
	      	System.out.println("\t\t3. Favorite Items");
	      	System.out.println("\t\t9. Nothing");
	      	String input = in.readLine();
	      	if(input.equals("1"))
	      	{
	      		//enter password, cannot be null
			String password = null;
			do
			{
				System.out.print("\tPlease enter your new password: ");
				password = in.readLine();
			}while(password.equals(""));
			query =  String.format("UPDATE Users SET password='%s' WHERE login='%s'", password, login);
			esql.executeUpdate(query);
	      	}
	      	else if(input.equals("2"))
	      	{
	      		//enter phone number, cannot be null
	      		System.out.print("\tPlease enter your new phone number: ");
			String num = in.readLine();
			query =  String.format("UPDATE Users SET phonenum='%s' WHERE login='%s'", num, login);
			esql.executeUpdate(query);
	      	}
	      	else if(input.equals("3"))
	      	{
	      		//enter fav
	      		System.out.print("\tPlease enter your favorite items: ");
			String fav = in.readLine();
			query =  String.format("UPDATE Users SET favitems='%s' WHERE login='%s'", fav, login);
			esql.executeUpdate(query);
	      	}
	      	else if(input.equals("9"))
	      	{
	      		System.out.println("\tThank you for updating your info");
	  		done = true;
	      	}
	      	else
	      	{
	      		System.out.print("\tUnrecognized choice! Please enter again: ");
	      	}
	      	
	 }while(!done);
      }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end UpdateUserInfo

   public static void ManagerUpdateUserInfo(Cafe esql){
      try{   	
      	boolean done = false;
      	
      	do
      	{
      		//display user info 
      		System.out.print("Please enter the login you want to check: ");
      		String login = in.readLine();
      		String query = String.format("SELECT * FROM Users WHERE login='%s'", login);
      		esql.executeQuery(query);
      	
		boolean finishUpdate = false;
		do
		{
		      	System.out.println("\tWhich would you like to update?");
		      	System.out.println("\t\t1. Password");
		      	System.out.println("\t\t2. Phone Number");
		      	System.out.println("\t\t3. Favorite Items");
		      	System.out.println("\t\t4. Type");
		      	System.out.println("\t\t9. Nothing");
		      	String input = in.readLine();
		      	if(input.equals("1"))
		      	{
		      		//enter password, cannot be null
				String password = null;
				do
				{
					System.out.print("\tPlease enter the new password: ");
					password = in.readLine();
				}while(password.equals(""));
				query =  String.format("UPDATE Users SET password='%s' WHERE login='%s'", password, login);
				esql.executeUpdate(query);
		      	}
		      	else if(input.equals("2"))
		      	{
		      		//enter phone number, null
		      		System.out.print("\tPlease enter the new phone number: ");
				String num = in.readLine();
				query =  String.format("UPDATE Users SET phonenum='%s' WHERE login='%s'", num, login);
				esql.executeUpdate(query);
		      	}
		      	else if(input.equals("3"))
		      	{
		      		//enter fav
		      		System.out.print("\tPlease enter the favorite items: ");
				String fav = in.readLine();
				query =  String.format("UPDATE Users SET favitems='%s' WHERE login='%s'", fav, login);
				esql.executeUpdate(query);
		      	}
		      	else if(input.equals("4"))
		      	{
		      		//only give the options for the type
		      		boolean entered = true;
		      		String type = null;
		      		do
		      		{
		      	 		System.out.println("\tPlease enter the type: ");
			      		System.out.println("\t1. Manager");
			      		System.out.println("\t2. Employee");
			      		System.out.println("\t3. Customer");
			      		String ans = in.readLine();
			      		if(ans.equals("1"))
			      		{
			      			type = "Manager";
			      		}
			      		else if(ans.equals("2"))
			      		{
						type = "Employee";
					}
					else if(ans.equals("3"))
					{
						type = "Customer";
					}
					else
					{
						System.out.println("\tUnrecognized choice! Please enter again: ");
						entered = false;
					}
			      	}while(!entered);
		      		
				query =  String.format("UPDATE Users SET type='%s' WHERE login='%s'", type, login);
				esql.executeUpdate(query);
		      	}
		      	else if(input.equals("9"))
		      	{
		      		System.out.println("\tThank you for updating your info");
		  		finishUpdate = true;
		      	}
		      	else
		      	{
		      		System.out.print("\tUnrecognized choice! Please enter again: ");
		      	}
		}while(!finishUpdate);
	      	//display the info again
	      	query = String.format("SELECT * FROM Users WHERE login='%s'", login);
      		esql.executeQuery(query);
      		
      		System.out.print("Is there another user info you want to update?(Y/N) ");
      		boolean updateMore = true;
      		do
      		{
      			String more = in.readLine();
      			if((more.equals("Y")) || (more.equals("y")))
      			{
      				//do nothing
      				updateMore = false;
      			}
      			else if((more.equals("N")) || (more.equals("n")))
      			{	
      				//leave the function
      				done = true;
      				updateMore = false;
      			}
			else
			{
				System.out.print("\tUnrecognized choice! Please enter again: ");
			}
      		}while(updateMore);
      		
	 }while(!done);
      }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end ManagerUpdateUserInfo

   public static void UpdateMenu(Cafe esql){
      try{
      	String query = null;
      	boolean done = false;
      	do
      	{
	      	System.out.println("\tWhich action would you like to take today?");
	      	System.out.println("\t\t1. Add an item");
	      	System.out.println("\t\t2. Delete an item");
	      	System.out.println("\t\t3. Edit an item");
	      	System.out.println("\t\t9. Finished updating");
	      	String input = in.readLine();
	      	
	      	if(input.equals("1"))
		{
			//enter itemname, cannot be null
			String name = null;
			do
			{
				System.out.print("\tPlease enter the name of the item: ");
				name = in.readLine();
			}while(name.equals(""));
			
			//enter type, cannot be null
			String type = null;
			do
			{
				System.out.print("\tPlease enter the type of the item: ");
				type = in.readLine();
			}while(type.equals(""));
		
			//enter price, cannot be null
			String tmp = null;
			do
			{
				System.out.print("\tPlease enter the price of the item: ");
				tmp = in.readLine();
			}while(tmp.equals(""));
			Double price = Double.valueOf(tmp);
			
			System.out.print("\tPlease enter the description of the item: (Press enter to continue if no description)");
			String description = in.readLine();
			System.out.print("\tPlease enter the imageurl of the item: (Press enter to continue if no imageurl)");
			String imageurl = in.readLine();
			
			//perform INSERT 
			query =  String.format("INSERT INTO Menu VALUES ('%s', '%s', %s, '%s', '%s')", name, type, price, description, imageurl);
			esql.executeUpdate(query);	
		}
		else if(input.equals("2"))
		{
			System.out.print("\tPlease enter the name of the item you want to delete: ");
			String name = in.readLine();
			
			//check if item exists
	 		query =  String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", name);
 			int userNum = esql.executeQueryCount(query);
 			
 			if(userNum > 0)
 			{
 				//item name exists, double check if the user really wants to delete it 
 				esql.executeQuery(query);
 				boolean deletion = false;
 				do
 				{
 					System.out.print("\tAre you sure you want to delete this item? ");
	 				String confirm = in.readLine();
	 				if((confirm.equals("Y")) || (confirm.equals("y")))
	 				{
	 					//deletion confirm, delete
	 					query = String.format("DELETE FROM Menu WHERE itemname='%s'", name);
			 			esql.executeUpdate(query);
			 			System.out.println("\tItem Deleted");
			 			deletion = true;
			 		}
			 		else if((confirm.equals("N")) || (confirm.equals("n")))
			 		{
			 			//do nothing
			 			deletion = true;
			 			System.out.println("\tItem Kept");
			 		}
			 		else 
			 		{
			 			System.out.print("\tUnrecognized choice! Please enter again: ");
			 		}
			 	}while(!deletion);
		 		
		 	}
 			else
 			{
 				System.out.println("\tThe item does not exist");		
 			}
		}
		else if(input.equals("3"))
		{
			System.out.print("Please enter the name of the item you want to update: ");
			String name = in.readLine();
			
			//check if item exists
	 		query =  String.format("SELECT * FROM Menu M WHERE M.itemName = '%s'", name);
 			int userNum = esql.executeQueryCount(query);
 			
 			if(userNum > 0)
 			{
 				//item name exists, ask user what the user wants to update 
 				esql.executeQuery(query);
 				String answer = null; 
 				boolean update = false;
 				do
 				{
 					System.out.println("\tWhat would you like to update?");
				      	System.out.println("\t\t1. Type");
				      	System.out.println("\t\t2. Price");
				      	System.out.println("\t\t3. Description");
				      	System.out.println("\t\t4. imageurl");
				      	System.out.println("\t\t5. Nothing");
				      	input = in.readLine();
				      	if(input.equals("1"))
			 		{
			 			String type = null;
						do
						{
							System.out.print("\tPlease enter the new type of the item: ");
			 				type = in.readLine();
						}while(type.equals(""));
						query =  String.format("UPDATE Menu SET type='%s' WHERE itemname='%s'", type, name);
						esql.executeUpdate(query);
			 		}
			 		else if(input.equals("2"))
			 		{
			 			//enter price, cannot be null
						String tmp = null;
						do
						{
							System.out.print("\tPlease enter the new price of the item: ");
							tmp = in.readLine();
						}while(tmp.equals(""));
						Double price = Double.valueOf(tmp);
						query =  String.format("UPDATE Menu SET price=%s WHERE itemname='%s'", price, name);
						esql.executeUpdate(query);
			 		}
			 		else if(input.equals("3"))
			 		{
			 			System.out.print("\tPlease enter the new description of the item: ");
			 			String description = in.readLine();
			 			query =  String.format("UPDATE Menu SET description='%s' WHERE itemname='%s'", description, name);
						esql.executeUpdate(query);
			 		}
			 		else if(input.equals("4"))
			 		{
			 			System.out.print("\tPlease enter the new imageurl of the item: ");
			 			String imageurl = in.readLine();
			 			query =  String.format("UPDATE Menu SET imageurl='%s' WHERE itemname='%s'", imageurl, name);
						esql.executeUpdate(query);
			 		}
			 		else if(input.equals("5"))
			 		{
			 			System.out.print("\tThank you for updating");
			 			update = true;
			 		}
			 		else
			 		{
			 			System.out.print("Unrecognized choice. Please enter again: ");
			 		}
			 	}while(!update);
		 	}
 			else
 			{
 				System.out.println("\tThe item does not exist");		
 			}
		}
		else if(input.equals("9"))
		{
			System.out.println("\tThank you for updating the menu");
			done = true;
		}
		else
		{
			System.out.print("\tUnrecognized choice. Please enter again: ");
		}	
	}while(!done);
      
      
      }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end UpdateMenu

   public static void ViewOrderStatus(Cafe esql){
      try{
      	System.out.print("\tPlease enter your order ID: ");
      	String order_id = in.readLine();
      	
      	//check if the order exists
      	String query =  String.format("SELECT * FROM Orders WHERE orderid = '%s'", order_id);
	int userNum = esql.executeQueryCount(query);
	
	if(userNum > 0)
	{
		esql.executeQuery(query);
		query = String.format("SELECT * FROM itemStatus WHERE orderid='%s'", order_id);
		esql.executeQuery(query);
	}
	else
	{
		System.out.println("\tThe order ID does not exist");
	}
      }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end ViewOrderStatus


//see any order that is unpaid within the past 24 hours
   public static void ViewCurrentOrder(Cafe esql, String login){
      try{
      	
      	String query = String.format("SELECT * FROM Orders WHERE paid='f' AND timestamprecieved >= NOW()-'1 day'::INTERVAL");
      	if(esql.executeQuery(query) == 0)
      	{
      		System.out.println("\tThere is not current order");
      	}
      	
      }catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end ViewCurrentOrder

   public static void addItemStatus(Cafe esql, Integer order_id){
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
   
   
	public static void deleteItem(Cafe esql, Integer order_id){
   	try
   	{	
		System.out.print("\tWhich item would you like to delete? ");
		String item = in.readLine();

		//check if item exists
		String query =  String.format("SELECT * FROM itemStatus i WHERE i.itemName='%s' AND i.orderid='%s'", item, order_id);
			int userNum = esql.executeQueryCount(query);

			if(userNum > 0)
			{
				//item name exists, delete
				query = String.format("DELETE FROM itemStatus WHERE itemname='%s' AND orderid='%s'", item, order_id);
		 		esql.executeUpdate(query);	
				System.out.println("\tDeleted!");
		 	}
		 	else
		 	{
		 		System.out.print("\tThe item is not in your order list");
		 	}
		}catch(Exception e){
         System.err.println (e.getMessage());
     }
   }//end deleteItem

}//end Cafe

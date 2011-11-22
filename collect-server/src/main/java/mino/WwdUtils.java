package mino;

import java.io.*;
import java.sql.*;
public class WwdUtils {

/*****************
**  Asks user to enter a wish list item or 'exit' to exit the loop - returns 
**       the string entered - loop should exit when the string 'exit' is returned
******************/
   public static String getWishItem() {
      BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
      String ans = "";
      try
      {
         while ( ans.length() == 0 ) {
            System.out.println("Enter wish-list item (enter exit to end): ");
            ans = br.readLine();
            if ( ans.length() == 0 ) 
               System.out.print("Nothing entered: ");
         }
      } catch (java.io.IOException e) {
         System.out.println("Could not read response from stdin");	
         }
         return ans;
    }  /**  END  getWishItem  ***/

/***      Check for  WISH_LIST table    ****/
   public static boolean wwdChk4Table (Connection conTst ) throws SQLException {
      boolean chk = true;
      boolean doCreate = false;
      try {
         Statement s = conTst.createStatement();
         s.execute("update WISH_LIST set ENTRY_DATE = CURRENT_TIMESTAMP, WISH_ITEM = 'TEST ENTRY' where 1=3");
      }  catch (SQLException sqle) {
         String theError = (sqle).getSQLState();
         //   System.out.println("  Utils GOT:  " + theError);
         /** If table exists will get -  WARNING 02000: No row was found **/
         if (theError.equals("42X05"))   // Table does not exist
         {  return false;
          }  else if (theError.equals("42X14") || theError.equals("42821"))  {
             System.out.println("WwdChk4Table: Incorrect table definition. Drop table WISH_LIST and rerun this program");
             throw sqle;   
          } else { 
             System.out.println("WwdChk4Table: Unhandled SQLException" );
             throw sqle; 
          }
      }
      //  System.out.println("Just got the warning - table exists OK ");
      return true;
   }  /*** END wwdInitTable  **/


   public static void main  (String[] args) {
   // This method allows stand-alone testing of the getWishItem method
      String answer;
      do {
         answer = getWishItem();
         if (! answer.equals("exit"))  {
            System.out.println ("You said: " + answer);
         }
      } while (! answer.equals("exit")) ;
   }

}
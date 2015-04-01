package message;
import java.sql.*;





public class TestJDBC {

	public static void main(String[] args) {
		new TestJDBC().DBConnection();
	}
	
	public void DBConnection() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		// TODO Auto-generated method stub
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				
				conn = DriverManager.getConnection("jdbc:mysql://localhost/messageboard?" + "user=root&password=gannilaoshi88");
			    stmt = conn.createStatement();
			    rs = stmt.executeQuery("select * from userinformation");
			    while(rs.next()) {
			    	System.out.println(rs.getString("email"));
			    	System.out.println(rs.getString("PASSWORD"));
			    	System.out.println(rs.getString("Nickname"));
			    } 
			} catch (ClassNotFoundException e) {			
			    e.printStackTrace();
			} catch (SQLException ex) {
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("VendorError: " + ex.getErrorCode());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if(rs != null) {
						rs.close();
						rs = null;
					}
					if(stmt != null) {
						stmt.close();
						stmt = null;
					}
					if(conn != null) {
						conn.close();
						conn = null;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	}

}

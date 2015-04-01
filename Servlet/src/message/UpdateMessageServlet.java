package message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpdateMessageServlet extends HttpServlet {


	private int i;


	public UpdateMessageServlet() {
		super();
	}


	public void destroy() {
		super.destroy(); 
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


	}


	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
System.out.println("Connecting~");
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String update = request.getParameter("Update Request");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html, charset=UTF-8");

		OutputStream os = null;
		DataOutputStream dos = null;
		String msg = null;
		
		String selectSql = "SELECT MessageContent, Date, NickName FROM(SELECT * FROM messages ORDER BY MessagesID DESC LIMIT 10) sub ORDER BY MessagesID DESC;";	
				
		try {
			os = response.getOutputStream();
			dos = new DataOutputStream(os);
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/messageboard?" + "user=root&password=gannilaoshi88");
			stmt = conn.prepareStatement(selectSql);
			
			if(update.equals("Received")) {
System.out.println("update: " + update);
				rs = stmt.executeQuery();
				
				if(rs.next()) {
					msg = "success";
					dos.writeUTF(msg);	
					
					do {	
						int i = 1;
						System.out.println(rs.getString(i));
						dos.writeUTF(rs.getString(i++));
						System.out.println(rs.getString(i));
						dos.writeUTF(rs.getString(i++));
						System.out.println(rs.getString(i));
						dos.writeUTF(rs.getString(i++));
						System.out.println(System.currentTimeMillis());
					} while(rs.next());
					
				} else {
					msg = "failed";
					dos.writeUTF(msg);
				}
			} else {
				msg = "failed";
				dos.writeUTF(msg);
			}					
			
			dos.flush();
			dos.close();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(dos != null) {
				dos.close();
			}
		}

	}


	public void init() throws ServletException {
		// Put your code here
	}

}

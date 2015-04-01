package servlet;

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

public class LoginServlet extends HttpServlet {

 
	public LoginServlet() {
		super();
	}

 
	public void destroy() {
		super.destroy(); 
	}
 
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("Servlet is doing task~~!~~~~~~~!~~~");
		this.doPost(request, response);		

	}

 
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
System.out.println("--post--");

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String email = request.getParameter("LoginEmail");
		String password = request.getParameter("LoginPassword");
		
		System.out.println(email);
		System.out.println(password);
		
		// set character encoding of response
		response.setCharacterEncoding("UTF-8");
		// text/html
		response.setContentType("text/html, charset=UTF-8");
		// response
		OutputStream os = null;
		DataOutputStream dos = null;
		String msg = null;
		
		String selectSql = "select Email, password, nickname from userinformation where Email=? and password=?";	
				
		try {
			os = response.getOutputStream();
			dos = new DataOutputStream(os);
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/messageboard?" + "user=root&password=gannilaoshi88");
			stmt = conn.prepareStatement(selectSql);
			
			if(email != null && password != null && email.length() != 0 && password.length() != 0) {
				stmt.setString(1, email);
				stmt.setString(2, password);
				rs = stmt.executeQuery();
				if(rs.next()) {
					msg = "success";
					dos.writeUTF(msg);
					dos.writeUTF(rs.getString("Email"));
					dos.writeUTF(rs.getString("Nickname"));
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
		System.out.println("Servlet is initiazing~~!~~~~~!!");
	}

}

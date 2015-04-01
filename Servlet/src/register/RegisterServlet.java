package register;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;






public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Transfer from get to --post--");
		this.doPost(request, response);	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Connection conn = null;
		PreparedStatement stmt = null;
		
		String email = request.getParameter("Email");
		String password = request.getParameter("Password");
		String nickname = request.getParameter("Nickname");
		
		response.setContentType("text/html, charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		PrintWriter out = response.getWriter();
		String msg = null;
		
		if(email != null && password != null && nickname != null && 
		   email.length() != 0 && password.length() != 0 && nickname.length() != 0) {
			msg = "Register Success";
			
			try {
				// connect MySQL Drive				
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection("jdbc:mysql://localhost/messageboard?" + "user=root&password=gannilaoshi88");
				String insertSql = "insert into userinformation values(?,?,?)";
				stmt = conn.prepareStatement(insertSql);
				stmt.setString(1, email);
				stmt.setString(2, password);
				stmt.setString(3, nickname);
				int status = stmt.executeUpdate();				
				
				if(status != 0) {
					System.out.println("Insert Success");
				} else {
					System.out.println("Insert Failed");
				}
				conn.close();
			} catch (MySQLIntegrityConstraintViolationException icve) {
				icve.printStackTrace();
				msg = "Email is Registered";
			} catch (Exception e) {
				e.printStackTrace();
				msg = "Register Failed";
			}
		} else {
			msg = "Invalid Input";
		}
		
		out.print(msg);
		out.flush();
		out.close();		
	}

}

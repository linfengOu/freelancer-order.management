package com.freelancer.om.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelancer.om.util.DBConnectionManager;
import com.freelancer.om.util.ExceptionMessage;

/**
 * Servlet implementation class Test
 */
@WebServlet("/Test")
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private DBConnectionManager dbcm;

	public void init(ServletConfig config) throws ServletException {
		dbcm = DBConnectionManager.getInstance();
	}

	public void destroy() {
		dbcm.release();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		
		PrintWriter out = response.getWriter();
		
		TestService ts = new TestService(dbcm);
		ObjectMapper mapper = new ObjectMapper();
		
		try {
      out.append(mapper.writeValueAsString(ts.checkCondition()));
      out.append(mapper.writeValueAsString(ts.findOrder(1)));
//		  out.append("id "+ ts.insertRandom());
    } catch (ExceptionMessage e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
		
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

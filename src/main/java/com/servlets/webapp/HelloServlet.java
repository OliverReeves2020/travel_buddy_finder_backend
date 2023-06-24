package com.servlets.webapp;

import java.io.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
//import jakarta.servlet.annotation.*;

@WebServlet(name = "Hello",urlPatterns = "/H")
public class HelloServlet extends HttpServlet {
    private String message;
    //@Override
    //public void init() {
      //  System.out.println("connection");
    //}
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + "message "+ "</h1>");
        out.println("</body></html>");
    }
    @Override
    public void destroy() {
    }
}
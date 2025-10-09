package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class ControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String param = request.getParameter("page");
        String method = request.getMethod();
        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");
        String langs = request.getHeader("Accept-Language");

        //cer 3
        System.out.println("=== New Request ===");
        System.out.println("Method: " + method);
        System.out.println("IP: " + ip);
        System.out.println("User-Agent: " + agent);
        System.out.println("Languages: " + langs);
        System.out.println("Param: " + param);

        log("=== New Request ===");
        log("Method: " + method);
        log("IP: " + ip);
        log("User-Agent: " + agent);
        log("Languages: " + langs);
        log("Param: " + param);

        // Cer 4
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/plain")) {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("Received parameter: " + param);
            return;
        }

        //Cer 2
        if ("cat".equals(param)) {
            request.getRequestDispatcher("page1.html").forward(request, response);
        } else if ("dog".equals(param)) {
            request.getRequestDispatcher("page2.html").forward(request, response);
        } else {
            response.sendRedirect("index.jsp");
        }
    }
}
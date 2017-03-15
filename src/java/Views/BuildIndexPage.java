/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Views;

import Models.SearchEngine;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author brettwalker
 */
public class BuildIndexPage extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullyQualifiedResourcePath = "/Users/brettwalker/workspace/EclipseJava/Lucene-Search-Engine";
        
        SearchEngine se = new SearchEngine();
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Build Status</title>");            
            out.println("</head>");
            out.println("<body style='background-color: #f3f1f1;'>");
            
            //back button
            out.println("<form style='height: 24px; text-align: center;' action=\"MainSearchPage\" method=\"get\">\n" +
                        "  <input style='border: 1px solid #d8d6d6; background-color: white; border-radius: 5px; width: 151px; font-size: 18px;' type=\"submit\" value=\"Back to Search\">\n" +
                        "</form>");
                        
            out.println("<h1 style='text-align: center; font-size 25px;'>Search Engine Indexing Status</h1>");
            
            boolean verbose = false;
            
            if(request.getParameter("verbose") != null && request.getParameter("verbose").equalsIgnoreCase("true"))
                verbose = true;
                       
            out.println("<div style='width: 100%; height: 50vh; overflow: scroll; border: 2px solid black;'>");
            //builds the index
            se.indexCorpus("index", out, verbose);
            out.println("</div>");

            //displays our metrics
            se.printIndexMetrics("index", out);

            out.println("</body>");
            out.println("</html>");
            
            out.flush();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

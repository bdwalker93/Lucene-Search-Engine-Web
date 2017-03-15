package Views;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author brettwalker
 */
public class MainSearchPage extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>INF 141 LUCENE SEARCH ENGINE</title>");            
            out.println("</head>");
            out.println("<body style='background-color: #f3f1f1;'>");
            out.println("<h1 style='text-align: center;'>INF 141: Search Engine</h1>");
            out.println("<h3 style='text-align: center;     font-weight: normal; -webkit-margin-before: -1em;'>Powered by Lucene</h3>");
            
            out.println("<form style='text-align: center; padding-top: 20px' action='ResultsPage' method='get'>\n" +
                        "  <div style='font-size: 19px;'>Search Query:</div>"
                    +   "  <br> <input style='width: 95%; font-size: 20px' type='text' name='query'><br>\n" +
                        "  <input style='border: 1px solid #d8d6d6; background-color: white; border-radius: 5px; width: 151px; font-size: 18px;-webkit-appearance: none;' type='submit' value='Search'>\n" +
                        "</form>");
            
            out.print("<div style='border: 2px solid black; padding: 20px; margin: 100px;'>");
            out.print("<div style='text-align:center; color: red'>***Don't touch these unless you want to rebuild the index***</div>");
            out.println("<form style='text-align: center' action='BuildIndexPage' method='get'>\n" +
                        "<input type='text' name='verbose' value='false' hidden>" +
                        "  <input style='width: 330px;' type='submit' value='Generate Index Quiet'>\n" +
                        "</form>");
            
            out.println("<form style='text-align: center' action='BuildIndexPage' method='get'>\n" +
                        "<input type='text' name='verbose' value='true' hidden>" +
                        "<input style='width: 330px;' type='submit' value='Generate Index Verbose'>\n" +
                        "</form>");
            out.print("</div>");
                    
            out.println("</body>");
            out.println("</html>");
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
//        processRequest(request, response);
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

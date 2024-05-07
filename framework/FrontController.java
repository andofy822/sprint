package controller.front;
import java.io.PrintWriter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
/**
 * FrontController
 */
public class FrontController extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);

        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    public void processRequest(HttpServletRequest request, HttpServletResponse response)throws Exception {
        try 
        {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
        }
        catch(Exception e){
            throw e;
        }
    }
    
}
 
import javax.servlet.*;
import javax.servlet.http.*;

public class titleToJsp extends HttpServlet {

    public void doGet (HttpServletRequest request,
		       HttpServletResponse response) {

	try {
	    // Set the attribute and Forward to hello.jsp
	    request.setAttribute ("servletName", "titleToJsp");
	    getServletConfig().getServletContext().getRequestDispatcher("/peng/jsp/title.jsp").forward(request, response);
	} catch (Exception ex) {
	    ex.printStackTrace ();
	}
    }
}

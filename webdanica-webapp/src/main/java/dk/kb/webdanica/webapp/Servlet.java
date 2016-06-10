package dk.kb.webdanica.webapp;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.antiaction.common.servlet.AutoIncrement;
import com.antiaction.common.servlet.PathMap;
import com.antiaction.common.templateengine.login.LoginTemplateCallback;

public class Servlet extends HttpServlet implements ResourceManagerAbstract, LoginTemplateCallback<User> {

    /**
     * UID.
     */
    private static final long serialVersionUID = -1590306102259729140L;

    /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(Servlet.class.getName());

    public static Environment environment;

    public static PathMap<Resource> pathMap;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
    	try {
            super.init(servletConfig);

            environment = new Environment(getServletContext(), servletConfig);

            pathMap = new PathMap<Resource>();
            
            // takes care that the js, img, and css are loaded by tomcat 
            StaticResource staticResource = new StaticResource();
            staticResource.resources_init(environment);
            staticResource.resources_add(this);
            // takes care of /status/ path pages
            StatusResource statusResource = new StatusResource();
            statusResource.resources_init(environment);
            statusResource.resources_add(this);
            // takes care of the index page (the root page)
            IndexResource indexResource = new IndexResource();
            indexResource.resources_init(environment);
            indexResource.resources_add(this);
            // takes care of /seeds/ path pages
            SeedsResource seedsResource = new SeedsResource();
            seedsResource.resources_init(environment);
            seedsResource.resources_add(this);

            logger.log(Level.INFO, this.getClass().getName() + " initialized.");
    	} catch (Throwable t) {
            logger.log(Level.SEVERE, this.getClass().getName() + " failed to initialize properly.", t);
    	}
    }

    protected AutoIncrement resourceAutoInc = new AutoIncrement();

    public int resource_add(ResourceAbstract resources, String path,
            boolean bSecured) {
        int resource_id = resourceAutoInc.getId();
        Resource resource = new Resource();
        resource.resource_id = resource_id;
        resource.resources = resources;
        resource.bSecured = bSecured;
        pathMap.add(path, resource);
        return resource_id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        if (environment != null) {
            environment.cleanup();
            environment = null;
        }
        logger.log(Level.INFO, this.getClass().getName() + " destroyed.");
        super.destroy();
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        try {
            // debug
            // System.out.println( req.getContextPath() );
            // System.out.println( req.getServletPath() );
            // System.out.println( req.getPathInfo() );
            // System.out.println( req.getRealPath( req.getContextPath() ) );
            // System.out.println( req.getRealPath( req.getPathInfo() ) );

            User current_user = null;

            // If we have a valid session look for an already logged in current
            // user.
            if (session != null) {
                current_user = (User) session.getAttribute("user");
            }

            // Look for cookies in case of no current user in session.
            if (current_user == null && session != null && session.isNew()) {
            	// Note 'this' == LoginTemplateCallback<User>
                current_user = environment.getLoginHandler().loginFromCookie(req, resp, session, this);
                logger.info("current_user:" + current_user);
            }

            String action = req.getParameter("action");

            // Logout, login or administration.
            if (action != null && "logout".compareToIgnoreCase(action) == 0) {
                environment.getLoginHandler().logoff(req, resp, session);
            } else {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() == 0) {
                    pathInfo = "/";
                }

                List<Integer> numerics = new ArrayList<Integer>();
                Resource resource = pathMap.get(pathInfo, numerics);

                if (resource != null) {
                    if (resource.bSecured && current_user == null) {
                    	// Note 'this' == LoginTemplateCallback<User>
                        environment.getLoginHandler().loginFromForm(req, resp, session, this);
                    } else if (!resource.bSecured) {
                        resource.resources.resource_service(this.getServletContext(), current_user, req, resp, resource.resource_id, numerics, pathInfo);
                    } else {
                        // authorized( req, resp, current_user );
                        resource.resources.resource_service(this.getServletContext(), current_user, req, resp, resource.resource_id, numerics, pathInfo);
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, pathInfo);
                }
            }
        } catch (Throwable t) {
        	logger.log(Level.SEVERE, t.toString(), t);
			StringBuilder sb = new StringBuilder();
			sb.append( "<!DOCTYPE html><html lang=\"en\"><head>" );
			sb.append( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
			sb.append( "<title>" );
			sb.append( Integer.toString( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
			sb.append( " Internal server error...</title>" );
			sb.append( "</head><body><h1>" );
			sb.append( Integer.toString( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
			sb.append( " Internal server error..." );
			sb.append( "</h1><pre>" );
			StatusResource.throwable_stacktrace_dump( t, sb );
			sb.append( "</pre></body></html>" );
	        resp.setContentType("text/html; charset=utf-8");
			resp.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
			OutputStream out = resp.getOutputStream();
			out.write( sb.toString().getBytes( "UTF-8" ) );
			out.flush();
			out.close();
            //resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sb.toString());
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    // The methods that implement the LoginTemplateCallback<User> interface
    /////////////////////////////////////////////////////////////////////////

    @Override
    public User validateUserCookie(String token) {
        return null;
    }

    @Override
    public User validateUserCredentials(String id, String password) {
        User current_user = null;
        // FIXME: Temporary hack 
        current_user = User.getAdminByCredentials(id, password);
        logger.info("returned a User for id=" + id);
/*        
        Connection conn = null;
        try {
            conn = environment.dataSource.getConnection();
            current_user = User.getAdminByCredentials(conn, id, password);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, e.toString(), e);
            }
        }
        if (current_user != null) {
            if (!current_user.active) {
            	current_user = null;
                logger.info("User account with id '" + id + "' is not active");
            }
        } else {
            logger.info("No known user '" + id + "' with the given credentials");
        }
        
 */       
        
        return current_user;
    }

    @Override
    public String getTranslated(String text_idstring) {
        return null;
    }

    public static class Resource {

        public int resource_id;

        public ResourceAbstract resources;

        public boolean bSecured;

    }

}

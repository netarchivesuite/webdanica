package dk.kb.webdanica.webapp;

import java.io.IOException;
import java.io.OutputStream;
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

import dk.kb.webdanica.webapp.resources.BlackListResource;
import dk.kb.webdanica.webapp.resources.BlackListsResource;
import dk.kb.webdanica.webapp.resources.CriteriaResultResource;
import dk.kb.webdanica.webapp.resources.CriteriaResultsResource;
import dk.kb.webdanica.webapp.resources.DomainResource;
import dk.kb.webdanica.webapp.resources.HarvestResource;
import dk.kb.webdanica.webapp.resources.HarvestsResource;
import dk.kb.webdanica.webapp.resources.IndexResource;
import dk.kb.webdanica.webapp.resources.IngestLogResource;
import dk.kb.webdanica.webapp.resources.Resource;
import dk.kb.webdanica.webapp.resources.ResourceAbstract;
import dk.kb.webdanica.webapp.resources.ResourceManagerAbstract;
import dk.kb.webdanica.webapp.resources.SeedsResource;
import dk.kb.webdanica.webapp.resources.StaticResource;
import dk.kb.webdanica.webapp.resources.StatusResource;

public class Servlet extends HttpServlet implements ResourceManagerAbstract,
        LoginTemplateCallback<User> {

    /**
     * UID.
     */
    private static final long serialVersionUID = -1590306102259729140L;

    /** Logging mechanism. */
    private static final Logger logger = Logger.getLogger(Servlet.class
            .getName());

    public static Environment environment;

    public static PathMap<Resource> pathMap;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            super.init(servletConfig);

            environment = new Environment(getServletContext(), servletConfig);
            // TODO Initialization seems to fail sometimes, resulting in NPE in
            // the Servlet.doGet method
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
            // takes care of /blacklist path pages
            BlackListResource blackListResource = new BlackListResource();
            blackListResource.resources_init(environment);
            blackListResource.resources_add(this);
            // takes care of /blacklists path pages
            BlackListsResource blackListsResource = new BlackListsResource();
            blackListsResource.resources_init(environment);
            blackListsResource.resources_add(this);

            CriteriaResultResource criteriaResultResource = new CriteriaResultResource();
            criteriaResultResource.resources_init(environment);
            criteriaResultResource.resources_add(this);

            CriteriaResultsResource criteriaResultsResource = new CriteriaResultsResource();
            criteriaResultsResource.resources_init(environment);
            criteriaResultsResource.resources_add(this);

            HarvestResource harvestResource = new HarvestResource();
            harvestResource.resources_init(environment);
            harvestResource.resources_add(this);

            HarvestsResource harvestsResource = new HarvestsResource();
            harvestsResource.resources_init(environment);
            harvestsResource.resources_add(this);

            DomainResource domainResource = new DomainResource();
            domainResource.resources_init(environment);
            domainResource.resources_add(this);

            IngestLogResource ingestResource = new IngestLogResource();
            ingestResource.resources_init(environment);
            ingestResource.resources_add(this);

            logger.log(Level.INFO, this.getClass().getName() + " initialized.");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, this.getClass().getName()
                    + " failed to initialize properly.", t);
        }
    }

    protected AutoIncrement resourceAutoInc = new AutoIncrement();

    public int resource_add(ResourceAbstract resources, String path,
            boolean bSecured) {
        int resource_id = resourceAutoInc.getId();
        Resource resource = new Resource(resource_id, resources, bSecured);
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
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
                current_user = environment.getLoginHandler().loginFromCookie(
                        req, resp, session, this);
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
                // logger.info("Looking for resource to match pathInfo:" +
                // pathInfo);
                List<Integer> numerics = new ArrayList<Integer>();
                Resource resource = pathMap.get(pathInfo, numerics);
                // Hacks for handling access to /blacklist/<uid>/ pages
                if (resource == null
                        && pathInfo
                                .startsWith(BlackListResource.BLACKLIST_PATH)) {
                    resource = pathMap.get(BlackListResource.BLACKLIST_PATH,
                            numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(HarvestResource.HARVEST_PATH)) {
                    resource = pathMap.get(HarvestResource.HARVEST_PATH,
                            numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(HarvestsResource.HARVESTS_PATH)) {
                    resource = pathMap.get(HarvestsResource.HARVESTS_PATH,
                            numerics);
                }

                if (resource == null
                        && pathInfo
                                .startsWith(CriteriaResultResource.CRITERIA_RESULT_PATH)) {
                    resource = pathMap.get(
                            CriteriaResultResource.CRITERIA_RESULT_PATH,
                            numerics);
                }

                if (resource == null
                        && pathInfo
                                .startsWith(CriteriaResultsResource.CRITERIA_RESULTS_PATH)) {
                    resource = pathMap.get(
                            CriteriaResultsResource.CRITERIA_RESULTS_PATH,
                            numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(SeedsResource.SEED_PATH)) {
                    resource = pathMap.get(SeedsResource.SEED_PATH, numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(SeedsResource.SEEDS_PATH)) {
                    resource = pathMap.get(SeedsResource.SEEDS_PATH, numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(DomainResource.DOMAIN_PATH)) {
                    resource = pathMap
                            .get(DomainResource.DOMAIN_PATH, numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(DomainResource.DOMAIN_LIST_PATH)) {
                    resource = pathMap.get(DomainResource.DOMAIN_LIST_PATH,
                            numerics);
                }
                if (resource == null
                        && pathInfo.startsWith(DomainResource.DOMAIN_SEEDS_PATH)) {
                    resource = pathMap.get(DomainResource.DOMAIN_SEEDS_PATH,
                            numerics);
                }
                
                if (resource == null
                        && pathInfo
                                .startsWith(IngestLogResource.INGESTLOG_PATH)) {
                    resource = pathMap.get(IngestLogResource.INGESTLOG_PATH,
                            numerics);
                }
                if (resource == null
                        && pathInfo
                                .startsWith(IngestLogResource.INGESTLOGS_PATH)) {
                    resource = pathMap.get(IngestLogResource.INGESTLOGS_PATH,
                            numerics);
                }

                if (resource != null) {
                    logger.info("Found resource for pathinfo '" + pathInfo
                            + "' in pathMap: " + resource);
                    if (resource.isSecured() && current_user == null) {
                        // Note 'this' == LoginTemplateCallback<User>
                        environment.getLoginHandler().loginFromForm(req, resp,
                                session, this);
                    } else if (!resource.isSecured()) {
                        resource.getResources().resource_service(
                                this.getServletContext(), current_user, req,
                                resp, resource.getId(), numerics, pathInfo);
                    } else {
                        // authorized( req, resp, current_user );
                        resource.getResources().resource_service(
                                this.getServletContext(), current_user, req,
                                resp, resource.getId(), numerics, pathInfo);
                    }
                } else {
                    logger.warning("No resource found for path: " + pathInfo);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, pathInfo);
                }
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.toString(), t);
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang=\"en\"><head>");
            sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            sb.append("<title>");
            sb.append(Integer
                    .toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            sb.append(" Internal server error...</title>");
            sb.append("</head><body><h1>");
            sb.append(Integer
                    .toString(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            sb.append(" Internal server error...");
            sb.append("</h1><pre>");
            StatusResource.throwable_stacktrace_dump(t, sb);
            sb.append("</pre></body></html>");
            resp.setContentType("text/html; charset=utf-8");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            OutputStream out = resp.getOutputStream();
            out.write(sb.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
            // resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            // sb.toString());
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // The methods that implement the LoginTemplateCallback<User> interface
    // ///////////////////////////////////////////////////////////////////////

    @Override
    public User validateUserCookie(String token) {
        return null;
    }

    @Override
    public User validateUserCredentials(String id, String password) {
        User current_user = null;
        // FIXME: Temporary hack: replace with LDAP id/password validation
        current_user = User.getAdminByCredentials(id, password);
        logger.info("returned a User for id=" + id);
        /*
         * Connection conn = null; try { conn =
         * environment.dataSource.getConnection(); current_user =
         * User.getAdminByCredentials(conn, id, password); } catch (SQLException
         * e) { logger.log(Level.SEVERE, e.toString(), e); } finally { try { if
         * (conn != null && !conn.isClosed()) { conn.close(); } } catch
         * (SQLException e) { logger.log(Level.SEVERE, e.toString(), e); } } if
         * (current_user != null) { if (!current_user.active) { current_user =
         * null; logger.info("User account with id '" + id + "' is not active");
         * } } else { logger.info("No known user '" + id +
         * "' with the given credentials"); }
         */

        return current_user;
    }

    @Override
    public String getTranslated(String text_idstring) {
        return null;
    }

}

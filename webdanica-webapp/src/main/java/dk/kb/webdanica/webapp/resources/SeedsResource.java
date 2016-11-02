package dk.kb.webdanica.webapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.html.HtmlEntity;
import com.antiaction.common.templateengine.Template;
import com.antiaction.common.templateengine.TemplateParts;
import com.antiaction.common.templateengine.TemplatePlaceBase;
import com.antiaction.common.templateengine.TemplatePlaceHolder;
import com.antiaction.common.templateengine.TemplatePlaceTag;

import dk.kb.webdanica.datamodel.Seed;
import dk.kb.webdanica.datamodel.SeedsDAO;
import dk.kb.webdanica.datamodel.Status;
import dk.kb.webdanica.webapp.Constants;
import dk.kb.webdanica.webapp.Environment;
import dk.kb.webdanica.webapp.MenuItem;
import dk.kb.webdanica.webapp.Navbar;
import dk.kb.webdanica.webapp.Pagination;
import dk.kb.webdanica.webapp.Servlet;
import dk.kb.webdanica.webapp.User;
import dk.netarkivet.common.utils.I18n;
/*
import dk.netarkivet.dab.webadmin.dao.ArchiveEntry;
import dk.netarkivet.dab.webadmin.dao.GroupedByStatuses;
import dk.netarkivet.dab.webadmin.dao.Permission;
import dk.netarkivet.dab.webadmin.dao.UrlRecord;
import dk.netarkivet.dab.webadmin.dao.UrlRecords;
import dk.netarkivet.dab.webadmin.dao.User;
import dk.netarkivet.dab.webadmin.workflow.PIDWorkThread;
*/
public class SeedsResource implements ResourceAbstract {

	private static final Logger logger = Logger.getLogger(SeedsResource.class.getName());
/*
    protected static final int[] URL_ADD_PERMISSION = {Permission.P_URL_ADD};

    protected static final int[] URL_DELETE_PERMISSION = {Permission.P_URL_DELETE};

    protected static final int[] URL_DECIDE_PERMISSION = {Permission.P_URL_DECIDE};
*/
    protected static final int A_ACCEPT = 1;

    protected static final int A_REJECT = 2;

    protected static final int A_DELETE = 3;

    private Environment environment;

    protected int R_STATUS_LIST = -1;

    protected int R_STATUS_LIST_ID = -1;

    protected int R_STATUS_LIST_ID_DUMP = -1;

    protected int R_URL_WARC_DOWNLOAD = -1;

    public static final String SEEDS_PATH = "/seeds/";
    
    public static final String SEEDS_NUMERIC_PATH = "/seeds/<numeric>/";
    
    public static final String SEEDS_NUMERIC_DUMP_PATH = "/seeds/<numeric>/dump/";
    
    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        R_STATUS_LIST = resourceManager.resource_add(this, SEEDS_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_PATH).isSecure()); 
     
        R_STATUS_LIST_ID = resourceManager.resource_add(this, SEEDS_NUMERIC_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_NUMERIC_PATH).isSecure());
        
        //R_STATUS_LIST_ID_DUMP = resourceManager.resource_add(this, "/seeds/<numeric>/dump/<numeric>/", true);
        R_STATUS_LIST_ID_DUMP = resourceManager.resource_add(this, SEEDS_NUMERIC_DUMP_PATH, 
        		environment.getResourcesMap().getResourceByPath(SEEDS_NUMERIC_DUMP_PATH).isSecure());
        
        //R_URL_WARC_DOWNLOAD = resourceManager.resource_add(this, "/url/warc/<numeric>/", true);
    }

    //private String servicePath;

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
        if (Servlet.environment.getContextPath() == null) {
        	Servlet.environment.setContextPath(req.getContextPath());
        }
        /*
        if (servicePath == null) {
            servicePath = req.getContextPath() + req.getServletPath();
        }
        */
        if (Servlet.environment.getSeedsPath() == null) {
        	Servlet.environment.setSeedsPath(Servlet.environment.getContextPath() + "/seeds/");
        }
        if (Servlet.environment.getSeedPath() == null) {
        	Servlet.environment.setSeedPath(Servlet.environment.getContextPath() + "/seed/");
        }
        
        
        if (resource_id == R_STATUS_LIST || resource_id == R_STATUS_LIST_ID) {
            urls_list(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_LIST_ID_DUMP) {
            // Dump all resources in the given state to screen
            // text/plain; charset=utf-8
            urls_list_dump(dab_user, req, resp, numerics);
        } 
    }

    private void urls_list_dump(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, List<Integer> numerics) throws IOException {
        //UrlRecords urlRecordsInstance = UrlRecords.getInstance(environment.dataSource);
    	SeedsDAO dao = Servlet.environment.getConfig().getDAOFactory().getSeedsDAO();
    	
        int status = 0; //Ordinal for Status.NEW
        if (numerics.size() >= 1) {
            status = numerics.get(0); 
        }
        
        int online_status = 0; // default = alle (alternatives: only_only, offline-only
        if (numerics.size() == 2) {
            online_status = numerics.get(1);
        }
/*
        String sysno = req.getParameter("sysno");
        boolean bSysno = false;
        if (sysno != null) {
        	bSysno = true;
        }
*/
        /*
        
        switch (status) {
        case 0:
        	status = UrlRecord.S_URL_ADDED;
            break;
        case 1:
        	status = UrlRecord.S_URL_REJECTED;
            break;
        case 2:
        	status = UrlRecord.S_URL_ACCEPT_DOMAIN;
            break;
        case 3:
        	status = UrlRecord.S_URL_ACCEPT_URL;
            break;
        case 4:
        	status = UrlRecord.S_URL_ACCEPTED;
            break;
        case 5:
        	status = UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE;
            break;
        case 6:
        	status = UrlRecord.S_URL_IN_REMOTE_ARCHIVE;
            break;
        case 7:
        	status = UrlRecord.S_URL_UNSUPPORTED_FORMAT;
        	break;
        case 8:
        	status = UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE;
        	break;
        case 9:
        	status = UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE;
            break;
        case 10:
        	status = UrlRecord.S_URL_ARCHIVED_LOCALLY;
            break;
        default:
        	status = UrlRecord.S_URL_ADDED;
            break;
        }
        */
/*
        String online_status_text;
        switch (online_status) {
        case 1:
        	online_status_text = "offline";
        	break;
        case 2:
        	online_status_text = "online";
        	break;
        case 0:
        default:
        	online_status_text = "alle";
        	break;
        }
*/        
        /*
        Connection conn = null;
        try {
            conn = environment.dataSource.getConnection();
        } catch (SQLException e) {
            throw new IOException(e);
        }
		*/
        
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/plain; charset=utf-8");
        //resp.setHeader("content-disposition", "attachment; filename=\"url_list_status_"+ status + "_" + online_status_text + ".txt\"");
        resp.setHeader("content-disposition", "attachment; filename=\"url_list_status_"+ status + ".txt\"");
        //logger.info("Using encoding in response: " + resp.getCharacterEncoding());

        // TODO this does not scale
        // Make an iterator
        // FIXME better handling
        List<Seed> urlRecords = null;
        try {
            // This needs to be further limited (e.g. by domain, and tld)
            urlRecords = dao.getSeeds(Status.fromOrdinal(online_status), 10000);  
        } catch (Exception e) {
            
        }

        List<Seed> urlRecordsFiltered = new ArrayList<Seed>(urlRecords.size());
        urlRecordsFiltered = urlRecords;
        Seed urlRecord;

 /*
        if (urlRecords != null) {
            for (int i = 0; i < urlRecords.size(); ++i) {
                urlRecord = urlRecords.get(i);
                switch (status) {
                default:
                    if (urlRecord.status_url == status) {
                        if (online_status == 0 || urlRecord.status_online == online_status) {
                            urlRecordsFiltered.add(urlRecord);
                        }
                    }
                    break;
                }
            }
        }
*/
        StringBuilder sb = new StringBuilder();
        sb.append("##\r\n");
        sb.append("## Liste over alle " + urlRecordsFiltered.size() + " seeds i status "
                + status + "\r\n");
        sb.append("##\r\n");
        
        for (Seed rec: urlRecordsFiltered) {
        /*	if (bSysno) {
                sb.append(rec.sysno);
                sb.append(';');
        	}
        */	
            sb.append(rec.getUrl());
            sb.append("\r\n");
        }

        try {
            out.write(sb.toString().getBytes("utf-8"));
            out.flush();
            out.close();
        } catch (IOException e) {
        	logger.warning("IOException thrown: " + e);
        }
    }
    

    public void urls_list(User dab_user, HttpServletRequest req,
            HttpServletResponse resp, List<Integer> numerics)
            throws IOException {
        String errorStr = null;
        String successStr = null;
        SeedsDAO dao = Servlet.environment.getConfig().getDAOFactory().getSeedsDAO();
        /*
        UrlRecords urlRecordsInstance = UrlRecords
                .getInstance(environment.dataSource);
        */
        
        int status = 0; //Default state shown: Status.NEW
        if (numerics.size() == 1) {
            status = numerics.get(0);
        }

        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && pageStr.length() > 0) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
            	e.printStackTrace();
            }
        }
        String itemsperpageStr = req.getParameter("itemsperpage");
        boolean bShowAll = false;
        int itemsPerPage = 25;
        if (itemsperpageStr != null && itemsperpageStr.length() > 0) {
        	try {
        		itemsPerPage = Integer.parseInt(itemsperpageStr);
        	} catch (NumberFormatException e) {
        		logger.warning("The given value of 'itemsperpage': '" + itemsperpageStr
        				+ "' is not a valid integer!. Using the default: 25"); 
        		itemsPerPage = 25;
        		itemsperpageStr = "25";
        	}
        }

        String actionStr = req.getParameter("action");
        String urlIdStr = req.getParameter("url_id");
/*        
        Integer urlId = null;
        if (urlIdStr != null && urlIdStr.length() > 0) {
        	try {
        		urlId = Integer.parseInt(urlIdStr);
        	} catch (NumberFormatException e) {
        		
        	}
        }
        int action = 0;
        if (actionStr != null && actionStr.length() > 0) {
        	if ("accept".equals(actionStr)) {
        		action = A_ACCEPT;
        	} else if ("reject".equals(actionStr)) {
        		action = A_REJECT;
        	}
        }
*/
        
        
/*
    	boolean bDecidePerm = dab_user.hasAnyPermission(URL_DECIDE_PERMISSION);
    	boolean bDeletePerm = dab_user.hasAnyPermission(URL_DELETE_PERMISSION);
*/
        boolean bDecidePerm = false;
        boolean bDeletePerm = false;
        
        /*
    	Connection conn = null;
        try {
            conn = environment.dataSource.getConnection();
        } catch (SQLException e) {
            throw new IOException(e);
        } */

        
        /*
        
        if ("POST".equals(req.getMethod())) {
            String submitactionStr = req.getParameter("submitaction");
            int submitAction = 0;
            if (submitactionStr != null && submitactionStr.length() > 0) {
                if ("accept".equals(submitactionStr)) {
                	if (dab_user.hasAnyPermission(URL_DECIDE_PERMISSION)) {
                        submitAction = A_ACCEPT;
                	} else {
                    	errorStr = "Du har ikke rettighed til at godkendte urler!";
                	}
                } else if ("reject".equals(submitactionStr)) {
                	if (dab_user.hasAnyPermission(URL_DECIDE_PERMISSION)) {
                        submitAction = A_REJECT;
                	} else {
                    	errorStr = "Du har ikke rettighed til at afvise urler!";
                	}
                } else if ("delete".equals(submitactionStr)) {
                	if (dab_user.hasAnyPermission(URL_DELETE_PERMISSION)) {
                        submitAction = A_DELETE;
                	} else {
                    	errorStr = "Du har ikke rettighed til at slette urler!";
                	}
                }
            }

            if (submitAction > 0) {
            	int succeeded = 0;
            	int failed = 0;
                String[] urlCheckList = req.getParameterValues("url_check_list");
                int url_id;
                UrlRecord urlRecord;
                if (urlCheckList != null && urlCheckList.length > 0) {
                    for (int i = 0; i < urlCheckList.length; ++i) {
                        try {
                            url_id = Integer.parseInt(urlCheckList[i]);
                            urlRecord = urlRecordsInstance.getUrlRecordById(conn,
                                    url_id);
                            if (urlRecord != null) {
                            	switch (submitAction) {
                            	case A_ACCEPT:
                            		if (urlRecord.status_url == UrlRecord.S_URL_ACCEPT_URL) {
                                		urlRecord.status_url = UrlRecord.S_URL_ACCEPTED;
                                		urlRecord.store(conn);
                                		++succeeded;
                            		}
                            		break;
                            	case A_REJECT:
                            		if (urlRecord.status_url == UrlRecord.S_URL_ACCEPT_URL) {
                                		urlRecord.status_url = UrlRecord.S_URL_REJECTED;
                                		urlRecord.reject_reason = UrlRecord.R_URL_MANUAL_REJECT;
                                		urlRecord.store(conn);
                                		++succeeded;
                            		}
                            		break;
                            	case A_DELETE:
                                	if (urlRecordsInstance.delete(conn, urlRecord)) {
                                		++succeeded;
                                	} else {
                                		++failed;
                                	}
                            		break;
                        		default:
                        			break;
                            	}
                            }
                        } catch (NumberFormatException e) {
                        	++failed;
                        }
                    }
                	switch (submitAction) {
                	case A_ACCEPT:
                		// TODO successStr
                		break;
                	case A_REJECT:
                		break;
                	case A_DELETE:
                		break;
            		default:
            			break;
                	}
                }
            }
        }
*/        
        
/*
        if (action > 0 && urlId != null) {
        	UrlRecord urlRecord = urlRecordsInstance.getUrlRecordById(conn, urlId);
        	if (urlRecord != null) {
            	switch (action) {
            	case A_ACCEPT:
                    if (bDecidePerm) {
                		urlRecord.status_url = UrlRecord.S_URL_ACCEPTED;
                		urlRecord.store(conn);
                		successStr = "Url godkendt.";
                    } else {
                    	errorStr = "Du har ikke rettighed til at godkendte urler!";
                    }
            		break;
            	case A_REJECT:
                    if (bDecidePerm) {
                		urlRecord.status_url = UrlRecord.S_URL_REJECTED;
                		urlRecord.reject_reason = UrlRecord.R_URL_MANUAL_REJECT;
                		urlRecord.store(conn);
                		successStr = "Url afvist.";
                    } else {
                    	errorStr = "Du har ikke rettighed til at afvise urler!";
                    }
            		break;
            	}
        	}
        }
*/
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.getTemplateMaster().getTemplate("urls_list.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder userPlace = TemplatePlaceBase.getTemplatePlaceHolder("user");
        TemplatePlaceHolder menuPlace = TemplatePlaceBase.getTemplatePlaceHolder("menu");
        TemplatePlaceHolder statemenuPlace = TemplatePlaceBase.getTemplatePlaceHolder("state_menu");
        TemplatePlaceHolder headingPlace = TemplatePlaceBase.getTemplatePlaceHolder("heading");
        TemplatePlaceHolder actionButtonsPlace = TemplatePlaceBase.getTemplatePlaceHolder("action_buttons");
        TemplatePlaceHolder paginationPlace = TemplatePlaceBase.getTemplatePlaceHolder("pagination");
        TemplatePlaceHolder pagination2Place = TemplatePlaceBase.getTemplatePlaceHolder("pagination2");
        TemplatePlaceTag myformTag = TemplatePlaceTag.getInstance( "form", "myform" );
        TemplatePlaceHolder statusPlace = TemplatePlaceBase.getTemplatePlaceHolder("status");
        TemplatePlaceHolder dumpPlace = TemplatePlaceBase.getTemplatePlaceHolder("dump");
        TemplatePlaceHolder alertPlace = TemplatePlaceBase.getTemplatePlaceHolder("alert");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(userPlace);
        placeHolders.add(menuPlace);
        placeHolders.add(statemenuPlace);
        placeHolders.add(headingPlace);
        placeHolders.add(actionButtonsPlace);
        placeHolders.add(paginationPlace);
        placeHolders.add(pagination2Place);
        // placeHolders.add( myformTag );
        placeHolders.add(statusPlace);
        placeHolders.add(dumpPlace);
        placeHolders.add(alertPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        boolean bShowReason = false;
        boolean bShowPid = true;
        boolean bShowAcceptReject = false;
        boolean bShowArchiveUrl = false;

/*
        int statusState = UrlRecord.S_URL_ADDED;
        switch (status) {
        case 0:
            statusState = UrlRecord.S_URL_ADDED;
            bShowPid = false;
            break;
        case 1:
            statusState = UrlRecord.S_URL_REJECTED;
            bShowReason = true;
            bShowPid = false;
            break;
        case 2:
            statusState = UrlRecord.S_URL_ACCEPT_DOMAIN;
            bShowPid = false;
            break;
        case 3:
            statusState = UrlRecord.S_URL_ACCEPT_URL;
            bShowPid = false;
            bShowAcceptReject = true;
            break;
        case 4:
            statusState = UrlRecord.S_URL_ACCEPTED;
            break;
        case 5:
            statusState = UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE;
            break;
        case 6:
            statusState = UrlRecord.S_URL_IN_REMOTE_ARCHIVE;
            break;
        case 7:
        	statusState = UrlRecord.S_URL_UNSUPPORTED_FORMAT;
        	break;
        case 8:
            statusState = UrlRecord.S_URL_FETCHED_FROM_REMOTE_ARCHIVE;
        	break;
        case 9:
            statusState = UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE;
            bShowArchiveUrl = true;
            break;
        case 10:
            statusState = UrlRecord.S_URL_ARCHIVED_LOCALLY;
            break;
        default:
            statusState = UrlRecord.S_URL_ADDED;
            break;
        }
*/
        StringBuilder statemenuSb = new StringBuilder();
        String heading = buildStatemenu(statemenuSb, status, dao);

    	/*
        actionButtonsSb
        .append("<button type=\"submit\" name=\"submit\" value=\"accept\" class=\"btn btn-success\"><i class=\"icon-white icon-thumbs-up\"></i> Godkend</button>\n");
        actionButtonsSb
        .append("<button type=\"submit\" name=\"submit\" value=\"reject\" class=\"btn btn-inverse\"><i class=\"icon-white icon-thumbs-down\"></i> Afvis</button>\n");
        */

        /*
         * Menu.
         */

        StringBuilder menuSb = new StringBuilder();
/*
        if (dab_user.hasAnyPermission(URL_ADD_PERMISSION)) {
        	menuSb.append("<div class=\"well sidebar-nav\">\n");
        	menuSb.append("<ul class=\"nav nav-list\">\n");
        	menuSb.append("<li class=\"nav-header\">Valgmuligheder</li>\n");

            menuSb.append("<li id=\"state_1\"");
            menuSb.append("><a href=\"");
            menuSb.append(Servlet.environment.seedsPath);
            menuSb.append("add/\">Opret URL</a></li>\n");

            menuSb.append("<li id=\"state_2\"");
            menuSb.append("><a href=\"");
            menuSb.append(Servlet.environment.seedsPath);
            menuSb.append("upload/\">Upload fil med URL'er</a></li>\n");

        	menuSb.append("</ul>\n");
        	menuSb.append("</div><!--/.well -->\n");
        }
*/
        
        /*
         * Action buttons.
         */

        StringBuilder actionButtonsSb = new StringBuilder();

        if (bDeletePerm || (bShowAcceptReject && bDecidePerm)) {
            actionButtonsSb.append("<a href=\"#\" class=\"btn\" onClick=\"select_all(document.myform.url_check_list); return false;\">Vælg alle</a>\n");
            actionButtonsSb.append("<a href=\"#\" class=\"btn\" onClick=\"deselect_all(document.myform.url_check_list); return false;\">Fravælg alle</a>\n");
            if (bShowAcceptReject && bDecidePerm) {
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"accept\" class=\"btn btn-success\"><i class=\"icon-white icon-thumbs-up\"></i> Godkend</button>\n");
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"reject\" class=\"btn btn-inverse\"><i class=\"icon-white icon-thumbs-down\"></i> Afvis</button>\n");
            }
            if (bDeletePerm) {
                actionButtonsSb.append("<button type=\"submit\" name=\"submitaction\" value=\"delete\" class=\"btn btn-danger\"><i class=\"icon-white icon-trash\"></i> Slet</button>\n");
            }
        }

        /*
         * Urls.
         */

        StringBuilder urlListSb = new StringBuilder();

        int ordering = 0;

        //getUrlRecords(ordering);

        // FIXME better handling
        List<Seed> urlRecords = new ArrayList<Seed>();
        Status wantedStatus = Status.fromOrdinal(status);
        try {
            urlRecords = dao.getSeeds(wantedStatus, 10000);
        } catch (Exception e) {
            logger.warning("Exception on retrieving max 10000 seeds with status " + wantedStatus + ": " + e); 
        }
        List<Seed> urlRecordsFiltered = urlRecords;
        Seed urlRecord;
        //urlRecord;
        //List<UrlRecord> urlRecordsFiltered = new ArrayList<UrlRecord>(urlRecords.size());

        //Domain domain;
        
//        if (urlRecords != null) {
//            for (int i = 0; i < urlRecords.size(); ++i) {
//                urlRecord = urlRecords.get(i);
//                switch (status) {
//                /*
//                case 0:
//                    if (urlRecord.status_url == statusState) {
//                    	domain = domainsInstance.getDomainById(conn, urlRecord.domain_id);
//                    	if (domain != null && domain.status != Domain.S_DOMAIN_UNDECIDED) {
//                            urlRecordsFiltered.add(urlRecord);
//                    	}
//                    }
//                	break;
//                case 1:
//                    if (urlRecord.status_url == statusState) {
//                    	domain = domainsInstance.getDomainById(conn, urlRecord.domain_id);
//                    	if (domain != null && domain.status == Domain.S_DOMAIN_UNDECIDED) {
//                            urlRecordsFiltered.add(urlRecord);
//                    	}
//                    }
//                	break;
//                */
//                default:
//                    if (urlRecord.status_url == statusState) {
//                        urlRecordsFiltered.add(urlRecord);
//                    }
//                    break;
//                }
//            }
//        }
        
        // Implementing paging with cassandra
        // https://datastax.github.io/java-driver/manual/paging/
        if (page < 1) {
        	logger.warning("Got negative pagenr '" +  page + "'. Changing it to page=1");
            page = 1;
            
        }
        if (itemsPerPage < 1) {
        	int defaultItemsPerPage = environment.getDefaultItemsPerPage();
        	logger.warning("Got negative itemsPerPage '" +  itemsPerPage + "'. Changing it to itemsPerPage=" + defaultItemsPerPage);
            itemsPerPage = defaultItemsPerPage;
            
        }
        int items = urlRecordsFiltered.size();
        int pages = Pagination.getPages(items, itemsPerPage);
        if (page > pages) {
        	logger.warning("Asked for page " + page + ", but we only have " + pages + ". Set page to maxpage");
            page = pages;
        }
        int fItem = (page - 1) * itemsPerPage;
        int show = itemsPerPage;

        
        urlListSb.append("<table class=\"table table-striped\">\n");
        urlListSb.append("  <thead>\n");
        urlListSb.append("    <tr>\n");
        /*
        if (bDeletePerm || (bShowAcceptReject && bDecidePerm)) {
        	urlListSb.append("      <th style=\"width: 24px;\">&nbsp;</th>\n");
        } */
        
        //urlListSb.append("      <th>Sysno</th>\n");
        urlListSb.append("      <th>url</th>\n");
        if (status == Status.REJECTED.ordinal()) {
            urlListSb.append("      <th>grund</th>\n");
            bShowReason = true;
        }
        if (bShowArchiveUrl) {
            urlListSb.append("      <th>arkiv-url</th>\n");
        }
        /*
        if (bShowPid) {
            urlListSb.append("      <th>pid</th>\n");
        }*/

        urlListSb.append("    </tr>\n");
        urlListSb.append("  </thead>\n");
        urlListSb.append("  <tbody>\n");

        while (fItem < urlRecordsFiltered.size() && show > 0) {
            urlRecord = urlRecordsFiltered.get(fItem);
            urlListSb.append("<tr>");
            /*
            if (bDeletePerm || (bShowAcceptReject && bDecidePerm)) {
            	urlListSb.append("<td>");
            	urlListSb.append("<input type=\"checkbox\" name=\"");
            	urlListSb.append("url_check_list");
            	urlListSb.append("\" value=\"");
            	urlListSb.append(urlRecord.id);
            	urlListSb.append("\" />");
                urlListSb.append("</td>");
            }
            */
            urlListSb.append("<td>");
            urlListSb.append("<a href=\"");
            //urlListSb.append(Servlet.environment.getSeedsPath());
            
            urlListSb.append(urlRecord.getUrl());
            urlListSb.append("\">");
            urlListSb.append(makeEllipsis(urlRecord.getUrl(), 120));
 
            urlListSb.append("</a>");
            urlListSb.append("</td>");
            urlListSb.append("<td>");
            
            /*
            switch (urlRecord.status_online) {
            case 0:
            	break;
            case UrlRecord.O_URL_NO_LONGER_ONLINE:
                urlListSb.append("<a title=\"");
                urlListSb.append(urlRecord.url);
                urlListSb.append("\" href=\"");
                urlListSb.append(urlRecord.url);
                // <i class=\"icon-white icon-question-sign\"></i>
                urlListSb.append("\" class=\"btn btn-warning btn-mini active\">Offline</a>");
                urlListSb.append("&nbsp;");
                break;
            case UrlRecord.O_URL_STILL_ONLINE:
                urlListSb.append("<a title=\"");
                urlListSb.append(urlRecord.url);
                urlListSb.append("\" href=\"");
                urlListSb.append(urlRecord.url);
                // <i class=\"icon-white icon-thumbs-up\"></i>
                urlListSb.append("\" class=\"btn btn-success btn-mini active\">Online</a>");
                urlListSb.append("&nbsp;");
                break;
            case UrlRecord.O_URL_CHECK_TIMEOUT:
            	urlListSb.append("<a title=\"");
                urlListSb.append(urlRecord.url);
                urlListSb.append("\" href=\"");
                urlListSb.append(urlRecord.url);
                // <i class=\"icon-white icon-thumbs-up\"></i>
                urlListSb.append("\" class=\"btn btn-danger btn-mini active\">?Online?</a>");
                urlListSb.append("&nbsp;");
                break;
            default:
            }
            */
            if (bShowReason) {
            /*	
            urlListSb.append("<a title=\"" + urlRecord.getStatusReason() + "\" href=\"");
            urlListSb.append(urlRecord.getStatusReason());
            urlListSb.append("\">");
            urlListSb.append(urlRecord.getStatusReason());
            urlListSb.append("</a>");
            */
            	urlListSb.append("<b>" + urlRecord.getStatusReason() + "</b>");	
            }
            /*
            if (statusState == UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE) {
            	urlListSb.append("&nbsp;");
            	urlListSb.append("<a href=\"");
            	urlListSb.append(Servlet.environment.seedPath);
            	urlListSb.append("warc/");
            	urlListSb.append(urlRecord.id);
            	urlListSb.append("/\" class=\"btn btn-info btn-mini\"><i class=\"icon-white icon-list\"></i> Download WARC</a>");
            } 
            */
            
            /*
            if (bShowAcceptReject && bDecidePerm) {
            	urlListSb.append("&nbsp;");
            	urlListSb.append("<a href=\"");
            	urlListSb.append("?page=" + page + "&itemsperpage=" + itemsperpageStr + "&action=accept&url_id="+urlRecord.id);
            	urlListSb.append("\" class=\"btn btn-success btn-mini active\"><i class=\"icon-white icon-thumbs-up\"></i> Godkend</a>\n");
            	urlListSb.append("<a href=\"");
            	urlListSb.append("?page=" + page + "&itemsperpage=" + itemsperpageStr + "&action=reject&url_id="+urlRecord.id);
            	urlListSb.append("\" class=\"btn btn-inverse btn-mini active\"><i class=\"icon-white icon-thumbs-down\"></i> Afvis</a>\n");
            }
            */
            urlListSb.append("</td>");
            /*
            if (bShowReason) {
                urlListSb.append("<td>");
                switch (urlRecord.reject_reason) {
                case UrlRecord.R_URL_IS_HTML_DOCUMENT:
                	urlListSb.append("HTML dokument");
                	break;
                case UrlRecord.R_URL_BELONGS_TO_REJECTED_DOMAIN:
                	urlListSb.append("Afvist domæne");
                	break;
                case UrlRecord.R_URL_MANUAL_REJECT:
                	urlListSb.append("Afvist URL");
                	break;
                case UrlRecord.R_URL_BLACKLISTED:
                	urlListSb.append("URL blacklistet");
                	break;
            	default:
            		urlListSb.append("Ukendt grund (" + urlRecord.reject_reason + ")");
            		break;
                }
                urlListSb.append("</td>");
            }
            */
            
            /*
            
            if (bShowArchiveUrl) {
                urlListSb.append("<td>");
                urlListSb.append("<a title=\"" + urlRecord.archivalUrl + "\" href=\"");
                urlListSb.append(urlRecord.archivalUrl);
                urlListSb.append("\">");
                urlListSb.append(makeEllipsis(urlRecord.archivalUrl, 120));
                urlListSb.append("</a>");
                urlListSb.append("</td>");
            }
            */
            
            /*
            if (bShowPid) {
                urlListSb.append("<td>");
                switch (urlRecord.status_pid) {
                case PIDWorkThread.S_PID_NONE:
                    // Don't show the PID, if we haven't registered it yet, but only
                    // '-'
                    urlListSb.append("&nbsp;-&nbsp;");
                    break;
                case PIDWorkThread.S_PID_ORIGINAL:
                    urlListSb.append("<a title=\"");
                    urlListSb.append(urlRecord.pid);
                    urlListSb.append("\" href=\"");
                    urlListSb.append("http://hdl.handle.net/");
                    urlListSb.append(environment.pid.handlePrefix);
                    urlListSb.append("/");
                    urlListSb.append(urlRecord.pid);
                    urlListSb.append("\" class=\"btn btn-success btn-mini active\">&nbsp;PID</a>");
                    break;
                case PIDWorkThread.S_PID_ARCHIVED:
                    urlListSb.append("<a title=\"");
                    urlListSb.append(urlRecord.pid);
                    urlListSb.append("\" href=\"");
                    urlListSb.append("http://hdl.handle.net/");
                    urlListSb.append(environment.pid.handlePrefix);
                    urlListSb.append("/");
                    urlListSb.append(urlRecord.pid);
                    urlListSb.append("\" class=\"btn btn-primary btn-mini active\">&nbsp;PID</a>");
                    break;
                default:
                }
                urlListSb.append("</td>");
            }
            */
            urlListSb.append("</tr>\n");
            ++fItem;
            --show;
        }

        urlListSb.append("  </tbody>\n");
        urlListSb.append("</table>\n");

        /*
         * Dump.
         */

        StringBuilder dumpSb = new StringBuilder();
        /*
        if ((statusState == UrlRecord.S_URL_NOT_IN_REMOTE_ARCHIVE || 
        		statusState == UrlRecord.S_URL_IN_REMOTE_ARCHIVE_BUT_NOT_ACCESSABLE) && bDecidePerm) {
            dumpSb.append("<a href=\"");
            dumpSb.append(Servlet.environment.seedsPath);
            dumpSb.append(status);
            dumpSb.append("/dump/2/\" class=\"btn btn-info\"><i class=\"icon-white icon-list\"></i> Gem liste (stadig online)</a>");
            dumpSb.append("&nbsp;");
            dumpSb.append("<a href=\"");
            dumpSb.append(Servlet.environment.seedsPath);
            dumpSb.append(status);
            dumpSb.append("/dump/1/\" class=\"btn btn-info\"><i class=\"icon-white icon-list\"></i> Gem liste (offline)</a>");
        }
        */

        /*
         * Places.
         */

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME).toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities(Constants.WEBAPP_NAME + Constants.SPACE + environment.getVersion()).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(Navbar.getNavbar(Navbar.N_URLS));
        }

        if (userPlace != null) {
            userPlace.setText(Navbar.getUserHref(dab_user));
        }

        if (menuPlace != null) {
            menuPlace.setText(menuSb.toString());
        }

        if (statemenuPlace != null) {
            statemenuPlace.setText(statemenuSb.toString());
        }

        if (headingPlace != null) {
            headingPlace.setText(heading);
        }

        if (actionButtonsPlace != null) {
            actionButtonsPlace.setText(actionButtonsSb.toString());
        }

        if (paginationPlace != null) {
            paginationPlace.setText(Pagination.getPagination(page, itemsPerPage, pages, bShowAll));
        }

        if (pagination2Place != null) {
            pagination2Place.setText(Pagination.getPagination(page, itemsPerPage, pages, bShowAll));
        }

        if (dumpPlace != null) {
            dumpPlace.setText(dumpSb.toString());
        }


        if ( myformTag != null && myformTag.htmlItem != null ) {
        	myformTag.htmlItem.setAttribute( "action", "?page=" + page + "&itemsperpage=" + itemsperpageStr );
        }

        /*
         * if ( contentPlace != null ) { contentPlace.setText( sb.toString() );
         * }
         */
        if (statusPlace != null) {
            statusPlace.setText(urlListSb.toString());
        }

        if (alertPlace != null) {
            StringBuilder alertSb = new StringBuilder();
            if (errorStr != null) {
                alertSb.append("<div class=\"row-fluid\">");
                alertSb.append("<div class=\"span12 bgcolor\">");
                alertSb.append("<div class=\"alert alert-error\">");
                alertSb.append("<a href=\"#\" class=\"close\" data-dismiss=\"alert\">x</a>");
                alertSb.append(errorStr);
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertPlace.setText(alertSb.toString());
            }
            if (successStr != null) {
                alertSb.append("<div class=\"row-fluid\">");
                alertSb.append("<div class=\"span12 bgcolor\">");
                alertSb.append("<div class=\"alert alert-success\">");
                alertSb.append("<a href=\"#\" class=\"close\" data-dismiss=\"alert\">x</a>");
                alertSb.append(successStr);
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertSb.append("</div>");
                alertPlace.setText(alertSb.toString());
            }
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }

        /*
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
        */
    }

    /**
     * Truncate a string to max-length plus " .." if string exceeds maxlength.
     * @param orgString
     *            A string
     * @param maxLength
     *            The max length the string must fill
     * @return the original String if the string is less than maxLength long,
     *         otherwise the substring of length maxLength of the original
     *         String followed by " .."
     */
    private String makeEllipsis(final String orgString, final int maxLength) {
        String resultString = orgString;
        if (orgString.length() > maxLength) {
            resultString = orgString.substring(0, maxLength - 1) + " ..";
        }
        return resultString;
    }

    public static String buildStatemenu(StringBuilder statemenuSb, int status, SeedsDAO dao) {
        /*
         * State menu.
         */

        /*
        int pid_registreret = 0;
        try {
            pid_registreret = Postgresql.getCountRecordsByPIDStatus(conn,
                    UrlRecord.S_URL_ACCEPTED, PID.S_PID_ORIGINAL);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
        */

        // FIXME better handling
        List<MenuItem> menuStatesArr =null;
        try {
            menuStatesArr = makemenuArray(dao);
        } catch (Exception e) {
        }

        String heading = "N/A";

        for (MenuItem item:  menuStatesArr) {
            // javascript:switchToState(0)
            statemenuSb.append("<li id=\"state_");
            statemenuSb.append(item.getOrdinalState()); 
            statemenuSb.append("\"");
            if (status == item.getOrdinalState()) {
                heading =  item.getLongHeaderDescription();
                statemenuSb.append(" class=\"active\"");
            }
            statemenuSb.append("><a href=\"");
            statemenuSb.append(Servlet.environment.getSeedsPath());
            statemenuSb.append(item.getOrdinalState());
            statemenuSb.append("/\">");
            statemenuSb.append(item.getShortHeaderDescription());
            statemenuSb.append(" (");
            statemenuSb.append(item.getCount());
            statemenuSb.append(")</a></li>");
        }

        return heading;
    }

	private static List<MenuItem> makemenuArray(SeedsDAO dao) throws Exception {
		List<MenuItem> result = new ArrayList<MenuItem>();
		I18n i18n = new I18n(dk.kb.webdanica.Constants.WEBDANICA_TRANSLATION_BUNDLE);
		Locale locDa = new Locale("da");
		for (int i=0; i <= Status.getMaxValidOrdinal(); i++) {
			Long count = dao.getSeedsCount(Status.fromOrdinal(i));
			result.add(new MenuItem(i, count, locDa, i18n));
		}
	    return result;
    }

}

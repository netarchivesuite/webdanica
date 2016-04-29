/*
 * Created on 13/09/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
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

public class StatusResource implements ResourceAbstract {

    private static final Logger logger = Logger.getLogger(StatusResource.class.getName());

    private Environment environment;

    protected int R_STATUS = -1;

    protected int R_STATUS_PROPS = -1;

    protected int R_STATUS_DEP = -1;

    protected int R_STATUS_THREADS = -1;

    protected int R_STATUS_PROGRESS = -1;

    protected int R_STATUS_LOG = -1;

    protected int R_STATUS_HEALTHY = -1;

    protected int R_STATUS_SQL_QUERY = -1;

    @Override
    public void resources_init(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        R_STATUS = resourceManager.resource_add(this, "/status/", false);
        R_STATUS_PROPS = resourceManager.resource_add(this, "/status/props/", false);
        R_STATUS_DEP = resourceManager.resource_add(this, "/status/dep/", false);
        R_STATUS_THREADS = resourceManager.resource_add(this, "/status/threads/", false);
        R_STATUS_PROGRESS = resourceManager.resource_add(this, "/status/progress/", false);
        R_STATUS_LOG = resourceManager.resource_add(this, "/status/log/", false);
        R_STATUS_HEALTHY = resourceManager.resource_add(this, "/status/healthy/", false);
        R_STATUS_SQL_QUERY = resourceManager.resource_add(this, "/status/sqlquery/", true);
    }

    //private String servicePath;

    @Override
    public void resource_service(ServletContext servletContext, User dab_user, HttpServletRequest req, HttpServletResponse resp, int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
        if (Servlet.environment.contextPath == null) {
        	Servlet.environment.contextPath = req.getContextPath();
        }
        /*
        if (servicePath == null) {
            servicePath = req.getContextPath() + req.getServletPath();
        }
        */
        if (resource_id == R_STATUS) {
            status(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_PROPS) {
            status_props(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_DEP) {
            status_dependencies(servletContext, dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_THREADS) {
            status_threads(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_PROGRESS) {
            status_progress(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_LOG) {
        	status_log(dab_user, req, resp, numerics);
        } else if (resource_id == R_STATUS_HEALTHY) {
        	status_healthy(dab_user, req, resp, numerics, pathInfo);
        } else if (resource_id == R_STATUS_SQL_QUERY) {
        	status_sql_query(dab_user, req, resp, numerics, pathInfo);
        }
    }

    public void status(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(-1));
        }

        if (contentPlace != null) {
            // contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_props(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\r\n");
        sb.append("System properties:");
        sb.append("\r\n");
        sb.append("\r\n");

        Properties props = System.getProperties();
        Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
        Entry<Object, Object> entry;
        while (iter.hasNext()) {
        	entry = iter.next();
        	sb.append(entry.getKey());
        	sb.append('=');
        	sb.append(entry.getValue());
        	sb.append("\r\n");
        }

        sb.append("\r\n");
        sb.append("Servlet properties:");
        sb.append("\r\n");
        sb.append("\r\n");

        ServletConfig servletConfig = Servlet.environment.servletConfig;
        @SuppressWarnings("rawtypes")
		Enumeration enumeration = servletConfig.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
        	String key = (String)enumeration.nextElement();
        	if (key.toLowerCase().indexOf("password") == -1) {
        		sb.append(key);
        		sb.append('=');
        		sb.append(servletConfig.getInitParameter(key));
            	sb.append("\r\n");
        	}
        }

        sb.append("</pre>\r\n");

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_PROPS));
        }

        if (contentPlace != null) {
            contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_dependencies(ServletContext servletContext, User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss Z");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getDefault());

        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");

        String path = servletContext.getRealPath("/WEB-INF/lib/");
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
        	File[] files = file.listFiles();
        	if (files != null) {
        		Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						return o1.getName().compareTo(o2.getName());
					}
        		});
            	for (int i=0; i<files.length; ++i) {
            		sb.append("<tr>\n");
            		sb.append("<td>\n");
            		sb.append(files[i].getName());
            		sb.append("</td>\n");
            		sb.append("<td>\n");
            		sb.append(dateFormat.format(files[i].lastModified()));
            		sb.append("</td>\n");
            		sb.append("<td>\n");
            		sb.append(files[i].length());
            		sb.append("</td>\n");
            		sb.append("</tr>\n");
            	}
        	}
        }

        sb.append("</table>\n");

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_DEP));
        }

        if (contentPlace != null) {
            contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_threads(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        String restartThreadName = req.getParameter("restart");
        // TODO maybe
        /*
        if (restartThreadName != null && restartThreadName.length() > 0) {
        	environment.monitoring.restartThread(restartThreadName);
        }
        */

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        StringBuilder sb = new StringBuilder();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getDefault());

        WorkThreadAbstract[] workThreads = new WorkThreadAbstract[] {
        		//environment.monitoring,
        		//environment.workflow,
        		//environment.lookup,
        		//environment.pid,
        		//environment.alive,
        		//environment.fetch,
        		//environment.wayback,
        		//environment.archive
        };

        sb.append("<table style=\"border: 1px solid black; width: 100%;\">\n");
        sb.append("<tr>\n");
        sb.append("<td>WorkThread</td>\n");
        sb.append("<td>.bRunning</td>\n");
        sb.append("<td>.bExit</td>\n");
        sb.append("<td>.bRun</td>\n");
        sb.append("<td>.lastRun</td>\n");
        sb.append("<td>.nextRun</td>\n");
        sb.append("<td>.lastWorkRun</td>\n");
        sb.append("</tr>\n");
        for (int i=0; i<workThreads.length; ++i) {
            workthread_status_row(dateFormat, workThreads[i], sb);
        }
        sb.append("</table>\n");

        sb.append("<br />\n");

        sb.append("<pre>\n");

        for (int i=0; i<workThreads.length; ++i) {
        	thread_stacktrace_dump(workThreads[i], sb);
        }

    	sb.append("</pre>\n");

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_THREADS));
        }

        if (contentPlace != null) {
            contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    /*
    public static void workthread_status(DateFormat dateFormat, WorkThreadAbstract workThread, StringBuilder sb) {
    	String name = workThread.thread.getName();
    	sb.append(name);
        sb.append(".bRunning=");
        sb.append(workThread.bRunning);
    	sb.append("<br />\n");

    	sb.append(name);
        sb.append(".bExit=");
        sb.append(workThread.bExit);
    	sb.append("<br />\n");

    	sb.append(name);
        sb.append(".bRun=");
        sb.append(workThread.bRun);
    	sb.append("<br />\n");

    	sb.append(name);
        sb.append(".lastRun=");
        sb.append(dateFormat.format(workThread.lastRun));
    	sb.append("<br />\n");

    	sb.append(name);
        sb.append(".nextRun=");
        sb.append(dateFormat.format(workThread.nextRun));
    	sb.append("<br />\n");

    	sb.append(name);
        sb.append(".lastWorkRun=");
        sb.append(dateFormat.format(workThread.lastWorkRun));
    	sb.append("<br />\n");
    }
    */

    public static void workthread_status_row(DateFormat dateFormat, WorkThreadAbstract workThread, StringBuilder sb) {
    	String name = workThread.thread.getName();
    	sb.append("<tr>\n");

    	sb.append("<td>");
    	sb.append(name);
    	sb.append("</td>\n");

    	if (workThread.bRunning) {
        	sb.append("<td style=\"background-color: green;\">");
    	} else {
        	sb.append("<td style=\"background-color: red;\">");
    	}
        sb.append(workThread.bRunning);
        if (!workThread.bRunning) {
            sb.append(" &nbsp;(<a href =\"?restart=" + workThread.getClass().getName() + "\">restart</a>)");
        }
    	sb.append("</td>\n");

    	if (workThread.bExit) {
        	sb.append("<td style=\"background-color: red;\">");
    	} else {
        	sb.append("<td>");
    	}
        sb.append(workThread.bExit);
    	sb.append("</td>\n");

    	if (workThread.bRun) {
        	sb.append("<td style=\"background-color: blue;\">");
    	} else {
        	sb.append("<td>");
    	}
        sb.append(workThread.bRun);
    	sb.append("</td>\n");

    	sb.append("<td>");
        sb.append(dateFormat.format(workThread.lastRun));
    	sb.append("</td>\n");

    	sb.append("<td>");
        sb.append(dateFormat.format(workThread.nextRun));
    	sb.append("</td>\n");

    	sb.append("<td>");
        sb.append(dateFormat.format(workThread.lastWorkRun));
    	sb.append("</td>\n");

    	sb.append("</tr>\n");
    }

    public static void thread_stacktrace_dump(WorkThreadAbstract workThread, StringBuilder sb) {
    	Class<?> clz = workThread.getClass();
    	Thread thread = workThread.thread;
    	if (thread.isAlive()) {
        	sb.append("Stack trace for ");
        	sb.append(clz.getName());
        	sb.append("\n");
        	stacktrace_dump(thread.getStackTrace(), sb);
    	} else {
        	sb.append(clz.getName());
        	sb.append(" is not running.");
        	sb.append("\n");
        	Throwable t = workThread.exitCausedBy;
        	if (t != null) {
        		sb.append("Caused by: ");
        		throwable_stacktrace_dump(t, sb);
        	}
    	}
    	sb.append("\n");
    }

    public static void stacktrace_dump(StackTraceElement[] stackTraceElementArr, StringBuilder sb) {
    	StackTraceElement stackTraceElement;
    	String fileName;
    	if (stackTraceElementArr != null && stackTraceElementArr.length > 0) {
    		for (int i=0; i<stackTraceElementArr.length; ++i) {
    			stackTraceElement = stackTraceElementArr[i];
    	        sb.append("\tat ");
    			sb.append(stackTraceElement.getClassName());
    	        sb.append(".");
    			sb.append(stackTraceElement.getMethodName());
    	        sb.append("(");
    	        fileName = stackTraceElement.getFileName();
    	        if (fileName != null) {
        			sb.append(fileName);
        	        sb.append(":");
        			sb.append(stackTraceElement.getLineNumber());
    	        } else {
    	        	sb.append("Unknown source");
    	        }
    	        sb.append(")");
    	    	sb.append("\n");
    		}
    	}
    }

    public static void throwable_stacktrace_dump(Throwable t, StringBuilder sb) {
    	String message;
    	if (t != null) {
    		sb.append(t.getClass().getName());
    		message = t.getMessage();
    		if (message != null) {
    			sb.append(": ");
        		sb.append(t.getMessage());
    		}
    		sb.append("\n");
    		stacktrace_dump(t.getStackTrace(), sb);
    		while ((t = t.getCause()) != null) {
    			sb.append("caused by ");
        		sb.append(t.getClass().getName());
        		message = t.getMessage();
        		if (message != null) {
        			sb.append(": ");
            		sb.append(t.getMessage());
        		}
        		sb.append("\n");
        		stacktrace_dump(t.getStackTrace(), sb);
    		}
    	}
    }

    public void status_progress(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        StringBuilder sb = new StringBuilder();
        sb.append("<pre>\n");

        DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getDefault());

        WorkThreadAbstract[] workThreads = new WorkThreadAbstract[] {
        		//environment.monitoring,
        		//environment.workflow,
        		//environment.lookup,
        		//environment.pid,
        		//environment.alive,
        		//environment.fetch,
        		//environment.wayback,
        		//environment.archive
        };

        List<WorkProgress> progressHistory = new ArrayList<WorkProgress>();

        WorkProgress progress;
        for (int i=0; i<workThreads.length; ++i) {
        	sb.append(workThreads[i].thread.getName());
    		sb.append(".queue: ");
        	sb.append(workThreads[i].getQueueSize());
    		sb.append("\n");
        	progress = workThreads[i].progress;
        	if (progress != null) {
        		sb.append(progress.threadName);
        		sb.append(".progress: ");
        		sb.append(dateFormat.format(progress.started));
        		if (progress.stopped != 0) {
            		sb.append(" ");
            		sb.append(dateFormat.format(progress.stopped));
        		}
        		sb.append(" ");
            	sb.append(progress.item);
        		sb.append("/");
            	sb.append(progress.items);
            	if (progress.bFailed) {
            		sb.append(" FAILED!");
            	}
        		sb.append("\n");
        	}
        	synchronized (workThreads[i].progressHistory) {
        		progressHistory.addAll(workThreads[i].progressHistory);
        	}
        }

        sb.append("<hr />");

        Collections.sort(progressHistory, new Comparator<WorkProgress>() {
			@Override
			public int compare(WorkProgress o1, WorkProgress o2) {
				return Long.signum(o1.started - o2.started);
			}
        });

        for (int i=0; i<progressHistory.size(); ++i) {
        	progress = progressHistory.get(i);
    		sb.append(progress.threadName);
    		sb.append(": ");
    		sb.append(dateFormat.format(progress.started));
    		if (progress.stopped != 0) {
        		sb.append(" ");
        		sb.append(dateFormat.format(progress.stopped));
    		}
    		sb.append(" ");
        	sb.append(progress.item);
    		sb.append("/");
        	sb.append(progress.items);
        	if (progress.bFailed) {
        		sb.append(" FAILED!");
        	}
    		sb.append("\n");
        }

        sb.append("</pre>\n");

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_PROGRESS));
        }

        if (contentPlace != null) {
            contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_log(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_master.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder contentPlace = TemplatePlaceBase.getTemplatePlaceHolder("content");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(contentPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        DateFormat dateFormat = new SimpleDateFormat("[dd/MMM/yyyy HH:mm:ss Z]");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getDefault());

        StringBuilder sb = new StringBuilder();
        sb.append("<pre>");
        Iterator<LogRecord> iter = Servlet.environment.logRecords.iterator();
        LogRecord record;
        while (iter.hasNext()) {
        	record = iter.next();
            sb.append(dateFormat.format(record.getMillis()));
        	sb.append(" ");
        	sb.append(record.getSourceClassName());
        	sb.append(" ");
        	sb.append(record.getSourceMethodName());
        	sb.append("<br />\n");
        	sb.append(record.getLevel());
        	sb.append(": ");
        	sb.append(record.getMessage());
        	sb.append("<br />\n");
        	throwable_stacktrace_dump(record.getThrown(), sb);
        }
        sb.append("</pre>");

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_LOG));
        }

        if (contentPlace != null) {
            contentPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_healthy(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics, String pathInfo) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/plain; charset=utf-8");

        Caching.caching_disable_headers(resp);

        StringBuilder sb = new StringBuilder();

        // TODO maybe
        /*
        if (environment.monitoring.thread.isAlive() && environment.monitoring.bHealthy) {
        	sb.append("true");
        } else {
        	sb.append("false");
        }
        */

        try {
        	out.write(sb.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (IOException e) {
        }
    }

    public void status_sql_query(User dab_user, HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics, String pathInfo) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType("text/html; charset=utf-8");

        Caching.caching_disable_headers(resp);

        Template template = environment.templateMaster.getTemplate("status_sqlquery.html");

        TemplatePlaceHolder titlePlace = TemplatePlaceBase.getTemplatePlaceHolder("title");
        TemplatePlaceHolder appnamePlace = TemplatePlaceBase.getTemplatePlaceHolder("appname");
        TemplatePlaceHolder navbarPlace = TemplatePlaceBase.getTemplatePlaceHolder("navbar");
        TemplatePlaceHolder sqlPlace = TemplatePlaceBase.getTemplatePlaceHolder("sql");
        TemplatePlaceHolder resultPlace = TemplatePlaceBase.getTemplatePlaceHolder("result");

        List<TemplatePlaceBase> placeHolders = new ArrayList<TemplatePlaceBase>();
        placeHolders.add(titlePlace);
        placeHolders.add(appnamePlace);
        placeHolders.add(navbarPlace);
        placeHolders.add(sqlPlace);
        placeHolders.add(resultPlace);

        TemplateParts templateParts = template.filterTemplate(placeHolders, resp.getCharacterEncoding());

        Connection conn = null;
        try {
            conn = environment.dataSource.getConnection();
        } catch (SQLException e) {
            throw new IOException(e);
        }

        StringBuilder sb = new StringBuilder();

        String sqlStr = req.getParameter("sql");
        if (sqlStr != null) {
        	sqlStr = sqlStr.trim();
        	if (sqlStr.length() > 0) {
            	Statement stm = null;
            	ResultSet rs = null;
            	try {
                	stm = conn.createStatement();
                	rs = stm.executeQuery(sqlStr);
                	if (rs != null) {
                		ResultSetMetaData rsmd = rs.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        sb.append("<table>");
                        sb.append("<tr>");
                        for (int i=1; i<=columnCount; ++i) {
                            sb.append("<td><b>");
                            sb.append(rsmd.getColumnName(i));
                            sb.append("</td>");
                        }
                        sb.append("</tr>");
                        while (rs.next()) {
                        	sb.append("<tr>");
                        	for (int i=1; i<=columnCount; ++i) {
                                sb.append("<td>");
                                sb.append(rs.getString(i));
                                sb.append("</td>");
                        	}
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                        rs.close();
                        rs = null;
                	}
                	stm.close();
                	stm = null;
            	} catch (SQLException e) {
            		sb.append("<pre>");
        			StatusResource.throwable_stacktrace_dump( e, sb );
            		sb.append("</pre>");
            	} finally {
            		if (rs != null) {
                        try {
							rs.close();
						} catch (SQLException e) {
						}
                        rs = null;
            		}
            		if (stm != null) {
                    	try {
							stm.close();
						} catch (SQLException e) {
						}
                    	stm = null;
            		}
            	}
        	}
        }

        if (titlePlace != null) {
            titlePlace.setText(HtmlEntity.encodeHtmlEntities("DAB").toString());
        }

        if (appnamePlace != null) {
            appnamePlace.setText(HtmlEntity.encodeHtmlEntities("DAB " + environment.version).toString());
        }

        if (navbarPlace != null) {
            navbarPlace.setText(StatusBar.getStatusbar(StatusBar.N_LOG));
        }

        if (sqlPlace != null && sqlStr != null) {
        	sqlPlace.setText( sqlStr );
        }

        if (resultPlace != null) {
        	resultPlace.setText( sb.toString() );
        }

        try {
            for (int i = 0; i < templateParts.parts.size(); ++i) {
                out.write(templateParts.parts.get(i).getBytes());
            }
            out.flush();
            out.close();
        } catch (IOException e) {
        }

        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.toString(), e);
        }
    }

}

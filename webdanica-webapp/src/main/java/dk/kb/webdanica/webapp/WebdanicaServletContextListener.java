package dk.kb.webdanica.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebdanicaServletContextListener 
               implements ServletContextListener{
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("ServletContextListener destroyed");
	}

	@Override
	//Run this before web application is started
    public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("ServletContextListener started");	    
    }

}
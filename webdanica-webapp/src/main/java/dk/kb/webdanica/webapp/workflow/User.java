package dk.kb.webdanica.webapp.workflow;

import javax.servlet.http.HttpServletRequest;

import com.antiaction.common.templateengine.login.LoginTemplateUser;

/**
 * User class. 
 * @deprecated Should not be needed
 *
 */
public class User implements LoginTemplateUser{

	@Override
    public String get_cookie_token(HttpServletRequest arg0) {
	    // TODO Auto-generated method stub
	    return null;
    }
	
}

package dk.kb.webdanica.webapp;

import javax.servlet.http.HttpServletRequest;

import com.antiaction.common.templateengine.login.LoginTemplateUser;

public class User implements LoginTemplateUser {

	public String id;

	public String username;

	public boolean active = false;

	public static User getAdminByCredentials(String id, String password) {	
		return getDefaultUser();
	}

	@Override
	public String get_cookie_token(HttpServletRequest req) {
		return null; // Not needed to further implement at the moment
	}
	
	public static User getDefaultUser() {
		User u = new User();
		u.active=true;
		u.id = "nn@domain.org";
		u.username="nn";
		return u;
	}		
}

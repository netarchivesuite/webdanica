package dk.kb.webdanica.webapp;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import com.antiaction.common.templateengine.login.LoginTemplateUser;

public class User implements LoginTemplateUser {

	public String id;

	public String username;

	public boolean active = false;

	public static User getAdminByCredentials(Connection conn, String id, String password) {
		return null;
	}

	@Override
	public String get_cookie_token(HttpServletRequest req) {
		return null;
	}

}

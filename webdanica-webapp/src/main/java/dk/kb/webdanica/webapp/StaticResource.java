/*
 * Created on 15/03/2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package dk.kb.webdanica.webapp;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.antiaction.common.filter.Caching;

public class StaticResource implements ResourceAbstract {

    @Override
    public void resources_init(Environment environment) {
        // argument ignored
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        resourceManager.resource_add(this, "/css/*", false);
        resourceManager.resource_add(this, "/img/*", false);
        resourceManager.resource_add(this, "/js/*", false);
    }

    @Override
    public void resource_service(ServletContext servletContext, User dab_user,
    		HttpServletRequest req, HttpServletResponse resp,
    		int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(servletContext.getRealPath(req.getPathInfo()), "r");
        byte[] buffer = new byte[(int) raf.length()];
        raf.readFully(buffer);
        raf.close();

        Caching.caching_disable_headers(resp);

        resp.setContentLength(buffer.length);
        String contentType = servletContext.getMimeType(pathInfo);
        if (contentType != null) {
            resp.setContentType(contentType);
        }

        ServletOutputStream out = resp.getOutputStream();

        try {
            out.write(buffer);
            out.flush();
            out.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

}

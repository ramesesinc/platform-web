/*
 * AnubisStartupServlet.java
 *
 * Created on June 27, 2012, 6:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.ContentUtil;
import com.rameses.anubis.ProjectResolver;
import java.io.InputStream;
import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Elmo
 *
 * The key is finding where the anubis host is. It will look for an entry
 * in the server.conf properties named 'anubis.hosts'. If none found, 
 * it will use the workspace + "/anubis.hosts" file. See AnubisWebServer.
 */

public class AnubisStartupServlet extends HttpServlet {
    
    private InputStream findHostsConf(String url) {
        try {
            url = ContentUtil.replaceSysProperty(url);
            URL u = new URL(url);
            return u.openStream();
        } catch(Exception e) {
            return null;
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            ServletContext appContext = config.getServletContext();
            is = findHostsConf( System.getProperty(AnubisWebServer.ANUBIS_HOSTS) );
            
            if (is == null)
                throw new ServletException("Anubis startup failed. Cannot find the anubis.hosts file");
            
            
            ProjectResolver resolver = new ProjectResolver(is);
            appContext.setAttribute( ProjectResolver.class.getName(), resolver );
        } catch(ServletException se) {
            throw se;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new ServletException(ex.getMessage(), ex);
        } finally {
            try { is.close(); } catch(Exception e) {;}
        }
    }
}

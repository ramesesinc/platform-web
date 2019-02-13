/*
 * CmsResourceServlet.java
 *
 * Created on June 28, 2012, 8:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.ContentUtil;
import com.rameses.anubis.Project;
import com.rameses.anubis.Theme;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 * Pattern: /themes/<theme-name>/<theme-page>
 */
public class AnubisThemeServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        
        String pathInfo = hreq.getPathInfo();
        String path = hreq.getServletPath() + pathInfo;
        
        Project project = ctx.getProject();
        String urlPath = project.getUrl() + path;
        
        String themeName = pathInfo.substring(1,  pathInfo.indexOf("/",1));
        String resName = pathInfo.substring( pathInfo.indexOf("/",1)+1 );
        
        
        ServletContext app =  config.getServletContext();
        String mimeType = app.getMimeType( pathInfo );
        
        InputStream is = null;
        try {
            String systemUrl = ctx.getSystemUrl();
            List<String> list = new ArrayList();
            Theme theme = project.getDefaultTheme();

            if( theme !=null ) {
                list.add( theme.getUrl()+resName );
                list.add( theme.getProvider()+resName );
            }
            list.add( systemUrl +"/theme/"+  resName);
            is = ContentUtil.getResources((String[])list.toArray(new String[]{}), resName);
            if (is != null) ResponseUtil.write(hreq,hres,mimeType,is);
        } 
        catch(Exception e) {
            System.out.println("error theme resource " + e.getMessage());
        } 
        finally {
            try { is.close(); } catch(Exception ign){;}
        }
    }
}

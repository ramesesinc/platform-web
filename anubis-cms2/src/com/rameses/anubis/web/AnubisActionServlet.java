/*
 * CmsPostAction.java
 *
 * Created on July 10, 2012, 10:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.ActionCommand;
import com.rameses.anubis.ActionManager;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.Project;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Elmo
 */
public class AnubisActionServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        //check first if post method. If get, do not continue
        /*
        if(hreq.getMethod().equalsIgnoreCase("get")) {
            ResponseUtil.writetErr( hreq, hres, new Exception("GET method is not supported for actions"), null );
            return;
        }
        */ 
         
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Map params = RequestUtil.buildRequestParams( hreq );
        ctx.setParams( params );
        
        
        ServletContext app = config.getServletContext();
        Project project = ctx.getProject();
        
        try {
            String path = hreq.getPathInfo();
            if (path == null || path.trim().length() == 0)
                throw new Exception("path info is required");
            
            ActionManager manager = project.getActionManager();
            ActionCommand command = manager.getActionCommand( path );
            
            Object o = command.execute( params );
            if(o==null) 
                throw new Exception("An outcome must be specified in action " + path);
            if ( !command.getResult().isEmpty() ) {
                HttpSession sess = hreq.getSession();
                sess.setAttribute("PARAMS", command.getResult() );
            }
            String outcome = o.toString();
            if(!outcome.startsWith("/")) outcome = "/"+outcome;
            
            redirect( hres, outcome, hreq.getContextPath() );
        
        } catch(Exception e) {
            ResponseUtil.writetErr( hreq, hres, e, null );
        }
    }
    
    private void redirect(HttpServletResponse hres, String pathInfo, String contextPath ) throws Exception {
        if ( contextPath == null || contextPath.length() == 0 ) {
            hres.sendRedirect( pathInfo ); 
        } else if ( pathInfo.startsWith( contextPath )) {
            hres.sendRedirect( pathInfo );
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append( contextPath ).append( pathInfo );
            hres.sendRedirect( sb.toString() ); 
        }
    }        
}

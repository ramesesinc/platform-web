package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.Module;
import com.rameses.anubis.PageMapperResult;
import com.rameses.anubis.Project;
import com.rameses.anubis.ProjectResolver;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public abstract class AbstractAnubisServlet extends HttpServlet {
    
    private final static String PROJECT_RELOAD_KEY = "/project.r";
    protected ServletConfig config;
    
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }
    
    public void destroy() {
        this.config = null;
    }
    
    protected abstract void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception;
    
    protected final void doPost(HttpServletRequest hreq, HttpServletResponse hres) throws ServletException, IOException {
        process(hreq, hres);
    }
    protected final void doGet(HttpServletRequest hreq, HttpServletResponse hres) throws ServletException, IOException {
        process(hreq, hres);
    }
    
    protected final void process(HttpServletRequest hreq, HttpServletResponse hres) throws ServletException, IOException {
        ServletContext app = config.getServletContext();

        boolean isMultipart = ServletFileUpload.isMultipartContent(hreq);
        if( isMultipart ) {
            hreq = new MultipartRequest(hreq,app);
        }
        
        WebAnubisContext wctx = new WebAnubisContext(app, hreq, hres);
        wctx.setAttribute("contextPath", hreq.getContextPath()); 
        AnubisContext.setContext(wctx);
        
        try {
            ProjectResolver resolver = (ProjectResolver)app.getAttribute( ProjectResolver.class.getName() );            
            Project project =  resolver.getProject(); 
            wctx.setProject( project );
            
            String pathInfo = hreq.getPathInfo();
            if( pathInfo !=null ) {
                
                //check if we need to reload
                if (PROJECT_RELOAD_KEY.equals(pathInfo)) {
                    resolver.removeProject();
                    redirect( hres, "/", hreq.getContextPath() ); 
                    return;
                }
                //determine the module if any
                //determine also the module. to do this check module path if it exists
                if( pathInfo.indexOf("/",1)>0) {
                    PageMapperResult pm = project.getPermalinkManager().resolve(pathInfo); 
                    String smodule = pm.getModule(); 
                    if ( smodule == null ) {
                        String fpath = pm.getFilePath();
                        fpath = (fpath.startsWith("/") ? fpath.substring(1) : fpath);
                        
                        String[] arr = fpath.split("/"); 
                        String modname = arr[0]; 
                        Module module = project.getModules().get(modname);
                        if( module != null ) smodule = module.getName();
                    }
                    
                    if ( smodule != null ) {
                        Module module = project.getModules().get(smodule);
                        if ( module == null ) 
                            throw new IOException("Module "+ smodule +" does not exist"); 
                        
                        wctx.setModule(module); 
                    }
                }
            }
            
            wctx.setSystemUrl( config.getServletContext().getResource("/default").toString() );
            handle( hreq, hres );
            
            
        } catch(ServletException se) {
            throw se;
        } catch(IOException ioe) {
            throw ioe;
        } catch(Throwable e) {
            throw new ServletException(e.getMessage(), e);
        } finally {
            AnubisContext.removeContext();
        }
    }
    
    private void redirect(HttpServletResponse hres, String pathInfo, String contextPath ) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append((contextPath == null ? "" : contextPath)); 
        sb.append( pathInfo );
        hres.sendRedirect( sb.toString() ); 
    }    
}

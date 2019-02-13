package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.resource.Resource;

public class BasicResourceServlet extends AbstractAnubisServlet  
{    
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception 
    {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        String fullPath = hreq.getServletPath();
        if (hreq.getPathInfo() != null) fullPath = fullPath + hreq.getPathInfo(); 
        
        String projectPath = null;
        String mimetype = null; 
        try { 
            projectPath = (new URL(ctx.getProject().getUrl())).getFile();
            mimetype = config.getServletContext().getMimeType(fullPath);
        } catch(Exception e) {
            //do nothing
        }
        
        String libname = (String) ctx.getProject().get("anubis.lib"); 
        if (libname == null) 
            libname = "/default";
        else if (!libname.startsWith("/")) 
            libname = "/" + libname;
        
        String[] staticDirectories = new String[]
        {
            projectPath, 
            System.getProperty(AnubisWebServer.KEY_LIBRARY_PATH)+libname, 
            System.getProperty(AnubisWebServer.KEY_RESOURCE_PATH)
        };
                
        for (String dir : staticDirectories)
        {
            try 
            {
                Resource base_res = Resource.newResource(dir); 
                Resource file_res = base_res.getResource(fullPath);  
                if (!file_res.getFile().exists()) throw new FileNotFoundException();
                
                long lastModified = file_res.lastModified();
                if ( !ctx.getProject().isCached()) { 
                    lastModified = System.currentTimeMillis();
                }
                
                hres.setHeader("ETag", lastModified+""); // always store the ETag in the header
                
                if (mimetype != null) hres.setContentType(mimetype);

                String previousToken = hreq.getHeader("If-None-Match");
                if (previousToken != null && previousToken.equals(lastModified+"")) 
                {
                    hres.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    // use the same date we sent when we created the ETag the first time through
                    hres.setHeader("Last-Modified", hreq.getHeader("If-Modified-Since")); 
                    return;
                }
                else 
                {
                    file_res.writeTo(hres.getOutputStream(), 0L, file_res.length()); 
                    hres.setStatus(HttpServletResponse.SC_OK);
                    hres.setDateHeader("Last-modified", lastModified);
                    return;
                }
            }
            catch(FileNotFoundException fnfe) {
                //do nothing, look into the next directory
            }
        } 
        
        throw new FileNotFoundException("FILE_NOT_FOUND");        
    }
    
}

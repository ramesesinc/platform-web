/*
 * AnubisResourceServlet.java
 *
 * Created on June 28, 2012, 8:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.ContentUtil;
import com.rameses.anubis.Module;

import com.rameses.anubis.Project;
import com.rameses.anubis.ProjectUtils;
import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 * Pattern: /res/*
 */
public class AnubisResourceServlet extends AbstractAnubisServlet 
{
    private String basePath;
    
    public AnubisResourceServlet() { 
        this("res");
    }
    
    public AnubisResourceServlet(String basePath) {
        this.basePath = basePath; 
    }
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception 
    { 
        AnubisContext ctx = AnubisContext.getCurrentContext();
        
        ServletContext app = config.getServletContext();
        String path = hreq.getPathInfo();
        String mimeType = app.getMimeType( path );
        InputStream is = null; 
             
        try 
        {
            Project project = ctx.getProject(); 
            String[] arr = ProjectUtils.getModuleNameFromFile( path, project );
            if(arr!=null) {
                String modulename = arr[0];
                String pathname = arr[1];
                Module module = project.getModules().get( modulename );
                is = ContentUtil.getResources(  new String[]{
                    module.getUrl() + basePath + pathname,
                    module.getProvider() + basePath + pathname
                }, path);
            }
            else {
                is = ContentUtil.getResources( new String[]{
                    project.getUrl()+ "/" + basePath + path,
                    ctx.getSystemUrl() + "/"+ basePath + path
                }, path);
            }
            
            if (is != null) {
                String smimetype = project.getMimeTypeManager().getMimeType( path );
                if (smimetype != null) mimeType = smimetype;
                                
                //System.out.println("path=" + path + ", mimetype="+ mimeType);
                ResponseUtil.write( hreq, hres, mimeType, is );
            }
        } 
        catch(Exception e) {
            System.out.println("error resource " + e.getMessage()); 
        } 
        finally {
            try { is.close(); } catch(Exception ign){;}
        } 
    }    
}

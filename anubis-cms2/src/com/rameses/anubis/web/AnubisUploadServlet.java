/*
 * AnubisUploadServlet.java
 *
 * Created on March 24, 2013, 8:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.Project;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;

/**
 * @author Elmo
 */
public class AnubisUploadServlet extends AbstractAnubisServlet {
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        MultipartRequest mreq = (MultipartRequest)hreq;
        WebAnubisContext ctx = (WebAnubisContext) AnubisContext.getCurrentContext();
        Project project = ctx.getProject();
        String path = mreq.getPathInfo();
        try {
            //System.out.println("process file....");
            //ProgressListener listener = new ProgressListener() {
            //    public void update(long bytesRead, long pContentLength, int pItems) {
            //        System.out.println("upload bytes read: "+ bytesRead + " length:" + pContentLength + " pItems:" + pItems);
            //    }
            //};
            //mreq.setListener( listener );
            mreq.process(); 
            FileItem fi = null;
            try {
                fi = mreq.getFileParameterMap().values().iterator().next().iterator().next();
            }
            catch(Exception ign) {
            }
            if(fi!=null) {
                Map m = new HashMap();
                m.put("file", fi);
                project.getActionManager().fireActions( path, m );
            }
        }
        catch(Exception ex) {
            System.out.println("Error AnubisUploadServlet " + ex.getMessage());
            throw ex;
        }
    }
    
    
}

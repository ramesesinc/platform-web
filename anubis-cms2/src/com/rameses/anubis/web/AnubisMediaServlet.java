/*
 * AnubisMediaServlet.java
 *
 * Created on April 7, 2014, 4:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.ActionCommand;
import com.rameses.anubis.ActionManager;
import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.Project;
import com.rameses.common.MediaFile;
import com.rameses.io.IOStream;
import com.rameses.util.Base64Cipher;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 */
public class AnubisMediaServlet extends AbstractAnubisServlet 
{    
    private static int DEFAULT_BUFFER_SIZE = 1024*4;   
    
    protected void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        if(!hreq.getMethod().equalsIgnoreCase("get")) {
            ResponseUtil.writetErr( hreq, hres, new Exception("Only GET method is supported for media"), null );
            return;
        }
         
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
            Object result = command.execute( params );
            if ( result instanceof String ) { 
                Base64Cipher base64 = new Base64Cipher(); 
                if ( base64.isEncoded( result.toString() )) {
                    result = base64.decode( result ); 
                }
            } 
            
            MediaFile mf = null;
            if (result instanceof MediaFile) {
                mf = (MediaFile)result; 
            } else if (result instanceof byte[]) {
                mf = new MediaFile();
                mf.setContent((byte[]) result); 
            } else {
                throw new Exception("The action must return a MediaFile or bytes"); 
            }
            hres.setContentType(mf.getContentType()); 
            write(hreq, hres, mf.getInputStream()); 
            
        } catch(Exception e) {
            ResponseUtil.writetErr( hreq, hres, e, null );
        }       
    }
    
    private void write(HttpServletRequest hreq, HttpServletResponse hres, InputStream input) throws Exception 
    {
        IOStream io = new IOStream();
        byte[] bytes = io.toByteArray(input);

        String token = '"' + getMd5Digest(bytes) + '"';
        hres.setHeader("ETag", token); // always store the ETag in the header
        String previousToken = hreq.getHeader("If-None-Match");        
        
        // compare previous token with current one            
        if (previousToken != null && previousToken.equals(token)) 
        { 
            hres.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            // use the same date we sent when we created the ETag the first time through
            hres.setHeader("Last-Modified", hreq.getHeader("If-Modified-Since"));
        } 

        // first time through - set last modified time to now
        else  
        { 
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MILLISECOND, 0);
            Date lastModified = cal.getTime();

            //set the headers
            hres.setBufferSize(DEFAULT_BUFFER_SIZE);
            hres.setDateHeader("Last-Modified", lastModified.getTime());
            hres.setContentLength(bytes.length); 
            io.write(bytes, hres.getOutputStream(), DEFAULT_BUFFER_SIZE);
        }
    }    
    
    private static String getMd5Digest(byte[] bytes) 
    {        
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 cryptographic algorithm is not available.", e);
        }
        
        byte[] raw = md.digest(bytes);
        return new BigInteger(1, raw).toString(16);
    }      

    
}




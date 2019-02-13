/*
 * ResponseUtil.java
 *
 * Created on August 8, 2012, 8:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.MediaInputStream;
import com.rameses.io.StreamUtil;
import com.rameses.util.ExceptionManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 *
 * this utility calls everything regarding the writing of the result including the headers
 */
public class ResponseUtil {
    
    
    private static int DEFAULT_BUFFER_SIZE = 1024*8;
    
    /*
    public static void output(ServletContext app, String mimeType, InputStream is, HttpServletRequest hreq, HttpServletResponse hres) throws ServletException, IOException{
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            if(mimeType==null) mimeType = "text/html";
            hres.setBufferSize(DEFAULT_BUFFER_SIZE);
            hres.setContentType(mimeType);
            //hres.setHeader("Content-Disposition", "inline; filename=\"" + u.getFile() + "\"");
     
            // Open streams.
            input = new BufferedInputStream(is, DEFAULT_BUFFER_SIZE);
     
     
     
            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
     
            output = new BufferedOutputStream(hres.getOutputStream(), DEFAULT_BUFFER_SIZE);
     
            int length;
            int sz = 0;
            while ((length = input.read(buffer)) > 0) {
                sz+=length;
                output.write(buffer, 0, length);
            }
     
            hres.setHeader("Content-Length", String.valueOf(sz));
     
        } finally {
            // Gently close streams.
            try {output.flush();} catch(Exception ign){;}
            try {is.close();} catch(Exception ign){;}
            try {input.close();} catch(Exception ign){;}
            try {output.close();} catch(Exception ign){;}
        }
    }
     */
    public static void write(HttpServletRequest hreq, HttpServletResponse hres, String mimeType, String s ) throws ServletException, IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
        write( hreq, hres, mimeType, bis);
    }
    
    public static void write(HttpServletRequest hreq, HttpServletResponse hres, String mimeType, InputStream is ) throws ServletException, IOException{
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            StreamUtil.write(is,baos);
            byte[] bytes = baos.toByteArray();
            
            if(mimeType==null) mimeType = "text/html";
            hres.setContentType(mimeType);
            
            //hres.setHeader("cache-control", "public");
            //hres.setHeader("cache-control", "max-age: 86400");
            
            String token = '"' + getMd5Digest(bytes) + '"';
            hres.setHeader("ETag", token); // always store the ETag in the header
            String previousToken = hreq.getHeader("If-None-Match");
            if (is instanceof MediaInputStream) {
                previousToken = null; 
            }
            
            if (previousToken != null && previousToken.equals(token)) { // compare previous token with current one
                hres.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                // use the same date we sent when we created the ETag the first time through
                hres.setHeader("Last-Modified", hreq.getHeader("If-Modified-Since"));
            } else  { 		// first time through - set last modified time to now
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                Date lastModified = cal.getTime();
                
                //set the headers
                hres.setBufferSize(DEFAULT_BUFFER_SIZE);
                hres.setDateHeader("Last-Modified", lastModified.getTime());
                hres.setContentLength(bytes.length);
                
                output = new BufferedOutputStream(hres.getOutputStream(), DEFAULT_BUFFER_SIZE);
                for(int i=0; i<bytes.length; i+=DEFAULT_BUFFER_SIZE) {
                    int len = (i+DEFAULT_BUFFER_SIZE < bytes.length) ? DEFAULT_BUFFER_SIZE : (bytes.length -i);
                    output.write(bytes,i,len);
                }
            }
        } finally {
            // Gently close streams.
            try {output.flush();} catch(Exception ign){;}
            try {is.close();} catch(Exception ign){;}
            try {input.close();} catch(Exception ign){;}
            try {output.close();} catch(Exception ign){;}
        }
    }
    
    private static String getMd5Digest(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 cryptographic algorithm is not available.", e);
        }
        byte[] messageDigest = md.digest(bytes);
        BigInteger number = new BigInteger(1, messageDigest);
        // prepend a zero to get a "proper" MD5 hash value
        StringBuffer sb = new StringBuffer('0');
        sb.append(number.toString(16));
        return sb.toString();
    }
    
    public static void writetErr(HttpServletRequest hreq, HttpServletResponse hres, Exception e, String errFile) {
        try {
            if(errFile==null) errFile = "/error";
            if(!errFile.startsWith("/")) errFile = "/"+errFile;
            e = ExceptionManager.getOriginal(e);
            
            Map m = new HashMap();
            m.put("message", e.getMessage());
            m.put("exception", e);
            
            hreq.setAttribute("PARAMS", m );
            RequestDispatcher rd = hreq.getServletContext().getRequestDispatcher(errFile);
            if( rd == null )
                throw new Exception("request dispatcher not found");
            rd.forward(hreq, hres);
            /*
            Project project = AnubisContext.getCurrentContext().getProject();
            File file = project.getFileManager().getFile( "/"+errFile+".pg");
            InputStream is = project.getContentManager().getContent( file, m );
            ResponseUtil.write( hreq, hres, null, is);
            */ 
        } catch(Exception ign) {
            throw new RuntimeException(ign);
        }
    }
    
}

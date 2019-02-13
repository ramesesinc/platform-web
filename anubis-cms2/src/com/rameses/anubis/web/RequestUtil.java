/*
 * RequestUtil.java
 *
 * Created on July 6, 2012, 8:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Elmo
 */
public class RequestUtil {
    
    
    public static Map buildRequestParams(HttpServletRequest hreq) {
        Map params = new HashMap();
        Enumeration e = hreq.getParameterNames();
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            params.put( name, hreq.getParameter(name) );
        }
        
        if( hreq instanceof MultipartRequest ) {
            MultipartRequest mreq = (MultipartRequest) hreq;
            Map fparams = mreq.getFileParameterMap();
            for(Map.Entry<String,List> item : (Set<Map.Entry>) fparams.entrySet()) {
                List l = item.getValue();
                if( l.size() == 1 ) {
                    params.put( item.getKey(), l.get(0) );
                } else {
                    params.put( item.getKey(), l );
                }
            }
        }
        
        //check also if there are attribute in request named params. extract that also
        Map attrs = (Map)hreq.getAttribute("PARAMS");
        if(attrs!=null) {
            params.putAll( attrs );
        }
        
        //we also include request attributes from the session if any.
        //This is an additional method used by actions. To pass values
        //during the redirect
        HttpSession sess = hreq.getSession();
        if(sess!=null) {
            Map aparms = (Map)sess.getAttribute("PARAMS");
            if(aparms !=null ) {
                params.putAll(aparms);
                sess.removeAttribute("PARAMS");
            }
        }
        return params;
    }
    
    public static void debugHeaders(HttpServletRequest hreq) {
        Enumeration<String> e = hreq.getHeaderNames();
        System.out.println("*********** headers ***********");
        while(e.hasMoreElements()) {
            String s = e.nextElement();
            System.out.println(s + "=" + hreq.getHeader(s));
        }
        System.out.println("********* end headers *********");


    }
    
}

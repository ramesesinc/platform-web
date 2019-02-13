/*
 * WebAnubisContext.java
 *
 * Created on July 15, 2012, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.SessionContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 */
public class WebAnubisContext extends AnubisContext {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext context;
    private WebSessionContext session;
    
    WebAnubisContext(ServletContext ctx, HttpServletRequest req, HttpServletResponse res) {
        this.request = req;
        this.response = res;
        this.context = ctx;
        this.session = new WebSessionContext(this.request, this.response);
    }

    public ServletContext getServletContext() { return this.context; }    
    public SessionContext getSession() { return session; }    
    public Object getRequest() { return request; }
    public Object getResponse() { return response; }
    
    void setRequest(HttpServletRequest req) {
        this.request = req;
    }
        
    void setResponse(HttpServletResponse resp) {
        this.response = resp;
    }
}

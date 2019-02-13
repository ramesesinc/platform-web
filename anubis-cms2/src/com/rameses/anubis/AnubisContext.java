/*
 * AnubisContext.java
 *
 * Created on July 15, 2012, 10:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.anubis.web.CmsWebConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Elmo
 */
public abstract class AnubisContext {
    
    
    protected static final ThreadLocal<AnubisContext> threadLocal = new ThreadLocal();
    
    public static void setContext(AnubisContext ctx) {
        threadLocal.set(ctx);
    }
    
    public static void removeContext() {
        threadLocal.remove();
    }
    
    public static AnubisContext getCurrentContext() {
        return threadLocal.get();
    }
    
    
    /***************************************************************************
     * start the code here
     ****************************************************************************/
    private Page currentPage;
    private Map params;
    private Project project;
    private String lang;
    private Module module;
    private Map attributes = new HashMap();
    
    //used for adding information to the services
    private Map env;
    
   
    private Stack<ConnectionContext> connectionContext = new Stack();
    
    public abstract SessionContext getSession();
    public abstract Object getRequest();
    public abstract Object getResponse();
    
    public LocaleSupport getCurrentLocale() {
        if( lang==null) return null;
        return project.getLocaleSupport( lang);
    }
    
    private String systemUrl;
    
    public String getSystemUrl() {
        return systemUrl;
    }
    
    public void setSystemUrl(String systemUrl) {
        this.systemUrl = systemUrl;
    }
    
    public Page getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }
    
    public Map getParams() {
        return params;
    }
    
    public void setParams(Map params) {
        this.params = params;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public void setModule(Module module) {
        this.module = module;
    }
    
    public Module getModule() {
        return module;
    }
    
    public void setConnectionContext(ConnectionContext wctx) {
        connectionContext.push( wctx );
    }
    
    public void removeConnectionContext() {
        connectionContext.pop();
    }
    
    public ConnectionContext getConnectionContext() {
        if(connectionContext.empty()) return null;
        return connectionContext.peek();
    }
    
    public Map getEnv() {
        if(env==null) {
            env = new HashMap();
            if(this.getSession()!=null) {
                env.put(CmsWebConstants.SESSIONID, this.getSession().getSessionid());
            }
            env.put("CLIENTTYPE", "web");
        }
        return env;
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
}

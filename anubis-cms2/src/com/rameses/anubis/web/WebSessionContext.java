/*
 * WebSessionContext.java
 *
 * Created on July 6, 2012, 4:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.web;

import com.rameses.anubis.SessionContext;
import com.rameses.util.Base64Coder;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Elmo
 */
public class WebSessionContext extends SessionContext {
    
    private static String SESSIONID = "SESSIONID";
    /*
    private static String GET_SESSION = "session/getSession";
    private static String HAS_PERMISSION = "session/checkPermission";
    */
    private String sessionid;
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    WebSessionContext(HttpServletRequest req, HttpServletResponse res) {
        this.request = req;
        this.response = res;
        
        //automatically retreieve the session;
        Cookie cookie = CookieUtil.getCookie(SESSIONID,request);
        if (cookie != null) this.sessionid = cookie.getValue();
    }
    
    
    /**
     * Gets the sessionid from the cookie. If the cookie's sessionid does not exist,
     * try to check if you can get the session. if you cant get the session,
     * then destroy the cookie.
     */
    public String getSessionid() { return this.sessionid; }
    
    public String createSession(String sid) {
        //do not create if there is already a sessionid
        this.sessionid = sid;
        if ( this.sessionid == null ) {
            this.sessionid = "SESS" + (new java.rmi.server.UID());
        }
        CookieUtil.addCookie( SESSIONID, sessionid, response );
        return this.sessionid;
    }
    
    public String destroySession() {
        if ( sessionid != null ) {
            CookieUtil.removeCookie(SESSIONID, request, response);
            CookieUtil.disposeAll(request, response); 
        }
        return sessionid;
    }
        
    public boolean checkPermission(String domain, String role, String permission) {
        Map map = new HashMap();
        map.put("domain", domain );
        map.put("role", role );
        map.put("permission", permission );
        //return (Boolean) execute(HAS_PERMISSION, map);
        return true;
    }
    
    public void addCookie(String name, String value) 
    {
        if (name == null || name.length() == 0) return;
        if (value == null || value.length() == 0) return;
        
        try {
            String encvalue = new CookieCipher().encrypt(value); 
            CookieUtil.addCookie(name, encvalue, response);
        } catch(Exception ex) { 
            System.out.println("failed to add cookie for " + name);
        }
    }
    
    public void removeCookie(String name) 
    {
        if (name == null || name.length() == 0) return;
        
        CookieUtil.removeCookie(name, request, response); 
    }   
    
    public String getCookieValue(String name) 
    {
        if (name == null || name.length() == 0) return null;
        
        Cookie cookie = CookieUtil.getCookie(name, request);
        if (cookie == null) return null; 
        
        String encvalue = cookie.getValue(); 
        try {
            return new CookieCipher().decrypt(encvalue);
        } catch(Exception ex) {
            return null; 
        }
    }    
    
    
    /*
    private Object execute(  String action, Map params ) {
        try {
            ActionManager actionManager = AnubisContext.getCurrentContext().getProject().getActionManager();
            return actionManager.getActionCommand(action).execute( params );
        } catch(Exception e) {
            System.out.println("error executing action->"+action+": " +e.getMessage());
            throw new RuntimeException(e);
        }
    }
     */
    
    private class CookieCipher 
    {
        private byte[] key = { 0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79 };
        
        String encrypt(String text) throws Exception 
        {
            if (text == null || text.length() == 0) return text;
            
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(text.getBytes()); 
            return new String(Base64Coder.encode(bytes));
        }

        String decrypt(String text) throws Exception 
        {
            if (text == null || text.length() == 0) return text;
            
            byte[] srcBytes = Base64Coder.decode(text);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(srcBytes); 
            return new String(bytes); 
        } 
    }
}

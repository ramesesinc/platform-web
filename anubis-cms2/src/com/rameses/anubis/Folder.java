/*
 * PageFolder.java
 *
 * Created on July 1, 2012, 8:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class Folder {
    
    private Map meta;
    private List<PermalinkEntry> urlMappings = new ArrayList();
    private List<File> children = new ArrayList();
    private String path;
    
    public Folder(String path, Map info) {
        this.meta = info;
        this.path = path;
    }
    
    public List<File> getChildren() {
        return children;
    }
    
    public Map getMeta() {
        return meta;
    }
    
    public List<File> getAllowedChildren() {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        if(ctx==null || ctx.getSession()==null) return getChildren();
        SessionContext sctx = ctx.getSession();
        List list = new ArrayList();
        for(File f: getChildren()) {
            String domain = f.getDomain();
            String role = f.getRole();
            String permission = f.getPermission();
            boolean checkPerms = (role!=null || permission!=null);
            boolean pass = true;
            if( checkPerms ) {
                pass = sctx.checkPermission(domain, role, permission);
            }
            if(pass) list.add(f);
        }
        return list;
    }

    public String getPath() {
        return path;
    }
    
}

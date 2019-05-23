/*
 * ThemeManager.java
 *
 * Created on June 12, 2012, 9:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Elmo
 */
public class Theme extends HashMap {
    
    private ConfigProperties conf;
    
    private ArrayList<ResourceInfo> resources;
    
    public Theme(String name, String url) {
        conf = ContentUtil.getConf( url + "/theme.conf" );
        super.putAll(conf.getProperties());
        super.put("name",name );
        super.put("url", url);
        this.resources = new ArrayList(); 
    }
    
    public String getName() {
        return (String)super.get("name");
    }
    
    public String getUrl() {
        return (String)super.get("url");
    }
    
    public String getProvider() {
        return  (String)super.get("provider");
    }
    
    public String getLicenseKey() {
        return (String)super.get("licenseKey");
    }
    
    public String getModuleName() {
        return (String)super.get("moduleName"); 
    }
    
    public void addResource( String url, String moduleName ) { 
        ResourceInfo info = new ResourceInfo(); 
        info.moduleName = moduleName; 
        info.url = url; 
        resources.add( info ); 
    }
    
    public List<String> getPaths() {
        ArrayList<String> paths = new ArrayList(); 
        for (ResourceInfo info : resources) {
            paths.add( info.url ); 
        }
        paths.add( getUrl()); 
        return paths;
    }
    
    private class ResourceInfo {
        String url;
        String moduleName;
    }
}

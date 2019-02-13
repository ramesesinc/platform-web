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
import java.util.HashMap;

/**
 *
 * @author Elmo
 */
public final class Module extends HashMap {
    
    private Project project;
        
    private ConfigProperties conf;
    
    
    public Module(String name, String url) {
        conf = ContentUtil.getConf( url + "/module.conf" );
        super.putAll(conf.getProperties());
        super.put("name",name );
        super.put("url", url);
        //fileManage
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
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getDomain() {
        return (String) super.get("domain");
    }
    
    
    /**
     * find the map in the provider, if any.
     * find the module attached. Then consolidate the results
     * the local conf will always override the provider
     */
    /*
    private Map findJsonMap(String filename) {
        Map providerMap = null;
        Map srcMap = null;
        
        if( getProvider()!=null) {
            try {
                providerMap = ContentUtil.getJsonMap( getProvider(), null, filename );
            } catch(Exception ign){;}
        }
        try {
            srcMap = ContentUtil.getJsonMap( getUrl() ,null, filename );
        } catch(Exception ign){;}
        if(providerMap == null && srcMap == null) return null;
        if(providerMap == null) providerMap = new HashMap();
        if(srcMap==null) srcMap = new HashMap();
        providerMap.putAll(srcMap);
        return providerMap;
    }
    */

    
}

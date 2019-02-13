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
public class Theme extends HashMap {
    
    private ConfigProperties conf;
    
    public Theme(String name, String url) {
        conf = ContentUtil.getConf( url + "/theme.conf" );
        super.putAll(conf.getProperties());
        super.put("name",name );
        super.put("url", url);
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
    
}

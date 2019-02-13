/*
 * ProjectSystem.java
 *
 * Created on March 22, 2013, 11:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;

/**
 *
 * @author Elmo
 * a project's system resources. This
 * is determined by the project 
 */
public class ProjectSystem {
    
    private String url;
    private ConfigProperties conf;
    private TemplateManager templateManager = new TemplateManager();
    
    public ProjectSystem(String url) {
        this.url = url;
        //build the conf
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}

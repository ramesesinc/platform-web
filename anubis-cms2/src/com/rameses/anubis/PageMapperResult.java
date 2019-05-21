/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author elmonazareno
 */
public class PageMapperResult {
    
    private String module;
    private Map params;
    private String filePath;
    
    public PageMapperResult(String filePath, Map params) {
        this.params = (params == null ? new HashMap() : params);
        this.filePath = filePath;
        
        String[] arr = this.filePath.split(":"); 
        if ( arr.length > 1 ) {
            module = (arr[0].startsWith("/") ? arr[0].substring(1) : arr[0]); 
            this.filePath = "/"+ module + arr[1]; 
        }
    }

    /**
     * @return the params
     */
    public Map getParams() {
        return params;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }
    
    public String getModule() {
        return module;
    }
}

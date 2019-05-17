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
    private Map params;
    private String filePath;
    
    public PageMapperResult(String filePath, Map params) {
        this.params = (params == null ? new HashMap() : params);
        this.filePath = filePath;
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
}

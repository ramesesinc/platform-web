/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

/**
 *
 * @author elmonazareno
 */
public class PageMapperToken {
    
    private PageMapperTokenType type = PageMapperTokenType.TEXT;
    private String value;

    /**
     * @return the type
     */
    public PageMapperTokenType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(PageMapperTokenType type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getValue() {
        return value;
    }

    /**
     * @param name the name to set
     */
    public void setValue(String name) {
        this.value = name;
    }
    
}

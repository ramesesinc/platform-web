/*
 * PropertyMap.java
 *
 * Created on September 13, 2012, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class PropertyMap extends HashMap {
    
    public PropertyMap(Map m) {
        if(m!=null) super.putAll(m);
    }
    
    public Object get(Object key) {
        Object val = super.get(key);
        if(val!=null && (val instanceof String) && val.toString().contains("$")  ) {
            val = ContentUtil.replaceSysProperty(val.toString());
        }
        return val;
    }
    
}

/*
 * ContentProvider.java
 *
 * Created on March 15, 2013, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public abstract class ContentProvider {

    public abstract String getExt();
    public abstract InputStream getContent(File file, Map params) throws ResourceNotFoundException;

    
}

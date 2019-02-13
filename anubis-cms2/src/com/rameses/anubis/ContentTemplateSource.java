/*
 * ContentTemplateSource.java
 *
 * Created on March 17, 2013, 7:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.InputStream;

/**
 *
 * @author Elmo
 */
public abstract class ContentTemplateSource {
    
    public abstract InputStream getResource(String name) throws ResourceNotFoundException;
    public abstract String getType();
    
}

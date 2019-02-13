/*
 * ContentManager.java
 *
 * Created on March 15, 2013, 8:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.Service;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class ContentManager {
    
    private Map<String, ContentProvider> providers = new Hashtable();
    
    public ContentManager() {
        Iterator<ContentProvider> iter = Service.providers(ContentProvider.class, Thread.currentThread().getContextClassLoader());
        while(iter.hasNext()) {
            ContentProvider cp = iter.next();
            providers.put( cp.getExt(), cp );
        }
    }
    
    public InputStream getContent(File file, Map params) throws ResourceNotFoundException {
        String ext = file.getExt();
        ContentProvider cp = providers.get( ext );
        return cp.getContent( file, params );
    }
    
    public ContentProvider getContentProvider(String ext) {
        return providers.get( ext );
    }
    
}

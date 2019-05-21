/*
 * ContentTemplateCache.java
 *
 * Created on March 17, 2013, 7:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import groovy.text.Template;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class ContentTemplateCache {
    
    private Map<String, ContentTemplate> cache = new HashMap();
    
    public void clear() {
        cache.clear(); 
    }
    
    public ContentTemplate getTemplate(String name, ContentTemplateSource src ) throws ResourceNotFoundException {
        String n = src.getType()+":"+name;
        InputStream is = null;
        try {
            AnubisContext actx = AnubisContext.getCurrentContext();
            boolean cached = actx.getProject().isCached();
            if(!cache.containsKey(n)) {
                is = src.getResource( name );
                Template temp = TemplateParser.getInstance().parse(is);
                ContentTemplate ct = new ContentTemplate(temp);
                if ( cached ) cache.put( n, ct );
                else return ct;
            }
            return cache.get(n);
        } 
        catch(ResourceNotFoundException rnfe) {
            throw rnfe;
        }
        catch(RuntimeException re) {
            throw re; 
        }
        catch(Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        catch(Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
        finally {
            try { is.close(); } catch(Exception ign){;}
        }
    }
    
}

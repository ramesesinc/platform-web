/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wflores 
 */
public class MimeTypeManager {
    
    private Map<String,String> mimetypes = new HashMap();
    
    public MimeTypeManager() {
        mimetypes.put("mpga", "audio/mpeg");
        mimetypes.put("mpega", "audio/mpeg");
        mimetypes.put("mp2", "audio/mpeg");
        mimetypes.put("mp3", "audio/mpeg");
        mimetypes.put("m4a", "audio/mpeg");
        mimetypes.put("oga", "audio/ogg");
        mimetypes.put("ogg", "audio/ogg");
        mimetypes.put("spx", "audio/ogg");
        mimetypes.put("ra", "audio/x-realaudio");
        mimetypes.put("weba", "audio/webm");
        mimetypes.put("3gpp", "video/3gpp"); 
        mimetypes.put("3gp", "video/3gpp"); 
        mimetypes.put("mp4", "video/mp4"); 
        mimetypes.put("mpeg", "video/mpeg"); 
        mimetypes.put("mpg", "video/mpeg"); 
        mimetypes.put("mpe", "video/mpeg"); 
        mimetypes.put("ogv", "video/ogg"); 
        mimetypes.put("ov", "video/quicktime"); 
        mimetypes.put("webm", "video/webm"); 
        mimetypes.put("flv", "video/x-flv");
        mimetypes.put("mng", "video/x-mng");
        mimetypes.put("asf", "video/x-ms-asf");
        mimetypes.put("wmv", "video/x-ms-wmv");
        mimetypes.put("avi", "video/x-msvideo");
        mimetypes.put("m4v", "video/x-m4v");
    }
    
    public void init(ConfigProperties conf) {
        Map map = conf.getProperties("mimetype");
        if( map != null) { 
            for (Object o: map.entrySet()) {
                Map.Entry me = (Map.Entry)o;
                Object key = me.getKey();
                Object val = me.getValue(); 
                if (key != null && val != null) {
                    add( key.toString(), val.toString() ); 
                } 
            }
        }
    }    
    
    void add( String key, String value ) {
        if ( key != null && value != null) {
            mimetypes.put( key, value ); 
        }
    }
        
    public String getMimeType( String path ) {
        if (path == null) return null;
        
        int idx = path.lastIndexOf('.');
        if (idx <= 0) return null; 
        
        try {
            String key = path.substring(idx+1); 
            return mimetypes.get( key ); 
        } catch(Throwable t) {
            return null; 
        } 
    }    
}

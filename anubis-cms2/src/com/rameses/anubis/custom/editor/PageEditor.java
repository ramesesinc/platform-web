/*
 * PageEditor.java
 *
 * Created on March 5, 2013, 7:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis.custom.editor;

import com.rameses.anubis.JsonUtil;
import com.rameses.io.FileUtil;
import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class PageEditor {
    
    /***
     * save two pages the pg and the actual content
     * pg = root/files/
     */
    private String rootUrl;
    
    public PageEditor(String rurl) {
        this.rootUrl = rurl;
    }
    
    public PageEditor() {
    }
    
    public void save( String path, Map info, String content ) throws Exception {
        if(this.rootUrl==null)
            throw new RuntimeException("Root url for PageEditor must not be null");
        if(!path.startsWith("/")) path = "/" + path;
        String pgPath = rootUrl + "/files" + path + ".pg";
        String contentPath = rootUrl + "/content/pages" + path + "/content";
        
        //save the page file part
        URL u = new URL( pgPath );
        File f = new File(u.getFile());
        String fil = JsonUtil.toString( info );
        fil = fil.replaceAll("\\{", "{\n").replaceAll("\\}", "\n}").replaceAll(",",",\n");
        FileUtil.writeBytes( f, fil.getBytes() );
        
        //save the content part
        u = new URL(contentPath);
        File fc = new File( u.getFile() );
        FileUtil.writeBytes( fc, content.getBytes() );
    }
    
    
    public String getRootUrl() {
        return rootUrl;
    }
    
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }
    
    public static void main(String[] args) throws Exception {
        PageEditor pageEditor = new PageEditor("file:///c:/tests/anubis-test");
        Map props = new LinkedHashMap();
        props.put( "title", "My First Page2" );
        props.put( "layout", "My Layout 1" );
        
        pageEditor.save("/contact/info", props, "This is the beginning of the end" );
        
        /*
        URLConnection uc = u.openConnection();
        uc.setDoOutput(true);
        OutputStream os = uc.getOutputStream();
        os.write("hello".getBytes());
        os.close();
         */
    }
    
}

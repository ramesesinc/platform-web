/*
 * FileManager.java
 *
 * Created on March 24, 2013, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.rameses.anubis;

import com.rameses.io.StreamUtil;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class FileManager {

    private Project project;
    private Map<String, File> files = new Hashtable();

    /**
     * Creates a new instance of FileManager
     */
    public FileManager(Project project) {
        this.project = project;
    }
    
    public File getFile(String name) {
        if (!files.containsKey(name)) {
            String moduleName = null; 
            String fileName = name;
            
            AnubisContext actx = AnubisContext.getCurrentContext();
            Module mod = actx.getModule(); 
            if ( mod != null ) {
                moduleName = mod.getName(); 
                String skey = "/"+ moduleName; 
                fileName = name.substring(skey.length()); 
            }            
            
            InputStream inp = findSource(fileName, moduleName);
            Map map = JsonUtil.toMap(StreamUtil.toString(inp));

            map.put("id", name);
            map.put("ext", name.substring(name.lastIndexOf(".") + 1));

            //check if file has items. This is done by checking if folders exist.
            //calculate the parent path
            String parentPath = null; 
            if ( name.lastIndexOf('/') > 0 ) {
                parentPath = name.substring(0, name.lastIndexOf("/"));
            }
            if (parentPath == null || parentPath.trim().length() == 0) {
                parentPath = "/";
            }
            map.put("parentPath", parentPath);
            if (moduleName != null) {
                map.put("module", moduleName);
            }

            //check also if page is secured
            //adjust sort order
            if (!map.containsKey("sortorder")) {
                map.put("sortorder", 0);
            }

            //set path
            String path = name.substring(0, name.lastIndexOf("."));
            map.put("path", path);

            //set secured
            if (!map.containsKey("secured")) {
                boolean secured = false;
                if (project.getSecuredPages() != null && path.matches(project.getSecuredPages())) {
                    secured = true;
                }
                map.put("secured", secured);
            }

            if (!map.containsKey("version")) {
                map.put("version", "1.0");
            }

            if (!map.containsKey("name")) { 
                int idx = 0; 
                if ( name.charAt(0) == '/' ) {
                    idx = 1; 
                }
                map.put("name", name.substring(idx, name.lastIndexOf(".")).replace("/", "-"));
            }

            if (!map.containsKey("hidden")) {
                map.put("hidden", false);
            }

            if (!map.containsKey("fragment")) {
                map.put("fragment", false);
            }

            int startIdx = name.lastIndexOf('/'); 
            int endIdx = name.lastIndexOf('.'); 
            startIdx = (startIdx < 0 ? 0 : startIdx+1); 
            map.put("pagename", name.substring(startIdx, endIdx));

            if (!map.containsKey("hashid")) {
                String hname = (String) map.get("name");
                map.put("hashid", hname);
            }
            files.put(name, new File(map));
        }
        return files.get(name);
    }

    public void clear() {
        files.clear();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" helper methods ">
    
    private String[] buildNames( String name ) {
        ArrayList<String> names = new ArrayList();
        if (name.endsWith(".media")) {
            names.add( name.substring(0, name.lastIndexOf(".media")) +"/info" );
        } else {
            names.add( name +"/info" );
        }
        names.add( name ); 
        return names.toArray(new String[]{}); 
    }

    private InputStream findSource(String name, String moduleName) {
        try {
            ArrayList<String> baseURLs = new ArrayList();
            if (moduleName != null) {
                Module module = project.getModules().get(moduleName);
                baseURLs.add(project.getUrl() + "/" + moduleName);
                if (module != null && module.getUrl() != null) {
                    baseURLs.add(module.getUrl());
                }

            } else {
                AnubisContext ctx = AnubisContext.getCurrentContext();
                baseURLs.add(project.getUrl());
                baseURLs.add(ctx.getSystemUrl());
            }

            ArrayList<String> paths = new ArrayList();
            for ( String basepath : baseURLs ) {
                paths.add(ContentUtil.correctUrlPath(basepath, "files", null));
            }
            
            String[] names = buildNames( name );  
            URL u = new ResourceUtil().findResource(paths.toArray(new String[]{}), names); 
            if ( u != null ) {
                String str = u.toString(); 
                if ( str.endsWith(".pg/info") || str.endsWith(".pg")) { 
                    return u.openStream(); 
                } 
            } 
            
            throw new ResourceNotFoundException("'"+ name +"' resource not found"); 
            
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    // </editor-fold>
}

/*
 * FolderManager.java
 *
 * Created on March 24, 2013, 9:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.anubis.FileDir.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Elmo
 */
public class FolderManager {
    
    private Project project;
    private Map<String, Folder> folders = new Hashtable();
    
    /** Creates a new instance of FolderManager */
    public FolderManager(Project project) {
        this.project = project;
    }
    
    private Set<String> scanFileNames(final String prefixName, final String rootUrl, String fileName) {
        //scan directories here
        final Set<String> items = new LinkedHashSet();
        try {
            String basePath = ContentUtil.correctUrlPath( rootUrl, "files", null );
            URL u = new ResourceUtil().createURL(basePath, fileName); 
            if ( u == null ) return items; 
            
            FileDir.scan( u, new FileFilter() {
                public void handle(FileDir.FileInfo f) {
                    String fname = null; 
                    if ( f.isDir()) {
                        if ( !f.getFileName().endsWith(".pg")) return;
                        if ( f.getSubfile("info") == null ) return; 
                        
                        fname = f.getFileName(); 
                        
                    } else {
                        if (f.getExt()!=null && f.getExt().equals("conf")) return;
                        
                        if (f.getUrl().toString().endsWith("/info")) {
                            String parentPath = f.getParentPath();
                            fname = parentPath.substring( parentPath.lastIndexOf('/')+1);
                        }                                            
                        else if (!f.getFileName().endsWith(".pg")) { 
                            // exit is file is not a pg extension
                            return;
                        } 
                        else {
                            fname = f.getFileName();                             
                        }
                        
                    }
                    
                    if ( fname != null && fname.trim().length() > 0 ) {
                        String pName = prefixName;
                        if(!pName.equals("/")) pName = pName ;
                        if(!pName.endsWith("/")) pName = pName + "/";
                        items.add( pName +  fname );   
                    }
                }
            });
        } catch(Throwable ign) {;}
        
        return items;
    }
    
    public Folder getFolder(String name) {
        return getFolder(name, null);
    }
    
    public Folder getFolder(String name, String moduleName){
        if (!folders.containsKey(name)) {
            AnubisContext ctx = AnubisContext.getCurrentContext(); 
            boolean allowCache = project.isCached();             
            String fileName = name;
            
            Module modu = null;
            if ( moduleName != null ) {
                modu = project.getModules().get( moduleName );
            }
            
            if ( modu != null ) { 
                String str = "/"+ moduleName;
                if ( fileName.startsWith(str)) {
                    fileName = name.substring( str.length());
                }
            }
            
            Set<String> urlNames = new LinkedHashSet();
            //check first if the requested file is from a module, else scan
            //through all files in folders
            if ( modu != null ) {
                for (String s : scanFileNames(fileName, modu.getUrl(), fileName )) {
                    urlNames.add( modu.getName()+":"+ s);
                }
            } 
            else {
                urlNames.addAll( scanFileNames(fileName,project.getUrl(),fileName) );
                urlNames.addAll( scanFileNames(fileName,ctx.getSystemUrl(), fileName) );
                for( Module mod: project.getModules().values()) {
                    for (String s : scanFileNames(fileName, mod.getUrl(), fileName )) {    
                        urlNames.add( mod.getName()+":"+ s);
                    }
                }
            }
            
            Folder folder = new Folder(name, new HashMap());
            for (String s: urlNames) {
                try {
                    String path = s; 
                    if ( path.endsWith("/info")) continue; 
                    if ( path.endsWith("/content")) continue;
                    
                    Module mod = null; 
                    String modname = null; 
                    String fname = path; 
                    if ( path.indexOf(':') > 0) {
                        modname = path.substring(0, path.indexOf(':')); 
                        fname = path.substring( path.indexOf(':')+1); 
                        mod = project.getModules().get(modname);
                    }
                    
                    File f = project.getFileManager().getFile( fname, path, mod );
                    if (!f.isHidden()) folder.getChildren().add( f );
                } 
                catch(Throwable e) {
                    e.printStackTrace();
                }
            }
            Collections.sort( folder.getChildren()); 
            if ( !allowCache ) return folder; 

            folders.put(name, folder);
        }
        return folders.get(name);
    }
    
    
    private class URLInfo {
        private String url;
        private String name; 
        private Module module;
        
        public URLInfo( String name, String url, Module module ) {
            this.name = null; 
            this.url = url; 
            this.module = module;
        }
    }
    public List<File> getFiles( String name ) {
        AnubisContext ctx = AnubisContext.getCurrentContext(); 
        ArrayList<URLInfo> paths = new ArrayList();
        paths.add( new URLInfo(name, project.getUrl(), null));
        paths.add( new URLInfo(name, ctx.getSystemUrl(), null));
        for( Module mod: project.getModules().values()) {
            paths.add( new URLInfo(name, mod.getUrl(), mod)); 
        }
        
        ArrayList<File> files = new ArrayList(); 
        for (URLInfo info : paths) {
            StringBuilder sb = new StringBuilder(); 
            sb.append( info.url +"/files/"+ name ); 
            if ( !sb.toString().endsWith(".pg")) {
                sb.append(".pg");
            }
            sb.append("/info");
            
            URL u = null; 
            try {
                u = new URL(sb.toString()); 
                u.openStream(); 
            } catch(Throwable t) {
                continue; 
            }
            
            StringBuilder pathInfo = new StringBuilder(); 
            if ( info.module != null ) {
                pathInfo.append( info.module.getName() +":"+ name );
            } else {
                pathInfo.append( name ); 
            }
            
            File file = null; 
            try {
                file = project.getFileManager().getFile(name, pathInfo.toString(), info.module); 
            } catch(Throwable t) {
                continue; 
            } 
            
            if ( file != null && !file.isHidden()) {
                files.add( file ); 
            }
        }
        return files; 
    }

    public File findFirstVisibleFile(String s) {
        Folder folder = getFolder(s);
        return findFirstPage(folder);
    }
    
     //finds the first file in the folder that is visible.
    public File findFirstPage(Folder folder) {
        //check first if there is a file named index and use that instead.
        String parent = folder.getPath();
        if(parent.equals("/")) parent = "";
        String index = parent + "index.pg";
        File f = null;
        try {
            f = project.getFileManager().getFile( index );
        }
        catch(Exception ign){;}
        if(f!=null) return f;
        
        if( folder.getChildren().size() > 0) {
            Iterator<File> iter = folder.getChildren().iterator();
            while(iter.hasNext()) {
                File firstFile = iter.next();
                if(!firstFile.isHidden()) {
                    return firstFile;
                }
            }
        }
        return null;
    }
    
    public void clear() {
        folders.clear();
    }
}

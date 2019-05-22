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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
    
    private Set<String> scanFileNames(final String prefixName, String rootUrl, String fileName) {
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
                        
                        if (f.getFileName().equals("info")) {
                            //proceed to next step
                        }                        
                        else if (!f.getFileName().endsWith(".pg")) return;
                        
                        fname = f.getFileName(); 
                    }
                    
                    if ( fname != null ) {
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
        return getFolder(name, false);
    }
    
    public Folder getFolder(String name, boolean scanAll) 
    {
        AnubisContext ctx = AnubisContext.getCurrentContext();        
        if (!folders.containsKey(name)) {
            String moduleName = null;
            String fileName = name;
            Module modu = ctx.getModule();
            
            if (scanAll) {
                //scan everything 
            }
            else if(modu!=null) {
                moduleName = modu.getName(); 
                fileName = name.substring(("/"+modu.getName()).length());
            }
            
            Set<String> urlNames = new LinkedHashSet();
            //check first if the requested file is from a module, else scan
            //through all files in folders
            if(modu!=null && !scanAll) {
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
                    if ( path.endsWith("/info")) {
                        path = path.substring( 0, path.lastIndexOf("/info"));
                    } 
                    
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
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
            Collections.sort( folder.getChildren()  );
            folders.put(name, folder);
        }
        return folders.get(name);
    }
    
    public Folder getFolder(String name, String moduleName) 
    {
        if (moduleName == null || moduleName.length() == 0)
            return getFolder(name); 
                
        AnubisContext ctx = AnubisContext.getCurrentContext();
        String sname = "/" + moduleName + name;        
        if (!folders.containsKey(sname)) 
        {
            Set<String> urlNames = new LinkedHashSet();
            Module mod = project.getModules().get(moduleName);
            for (String s : scanFileNames(name, mod.getUrl(), name )) {
                urlNames.add( mod.getName()+":"+ s);
            }
            
            Folder folder = new Folder(sname, new HashMap());            
            for (String s: urlNames) 
            {
                try 
                {
                    String path = s; 
                    if ( path.endsWith("/info")) {
                        path = path.substring( 0, path.lastIndexOf("/info"));
                    } 
                    
                    String fname = path; 
                    if ( path.indexOf(':') > 0) {
                        fname = path.substring( path.indexOf(':')+1); 
                    }
                    
                    File f = project.getFileManager().getFile( fname, path, mod );
                    if (!f.isHidden()) folder.getChildren().add( f );
                } 
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            Collections.sort( folder.getChildren()  );
            folders.put(name, folder);
        }
        return folders.get(name); 
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

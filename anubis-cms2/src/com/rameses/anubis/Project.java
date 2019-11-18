/*
 * Project.java
 *
 * Created on July 1, 2012, 2:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.anubis.FileDir.FileFilter;
import com.rameses.util.ConfigProperties;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class Project extends HashMap  {
    
    //where all contents are cached
    //TRANSLATED GET TEXT
    private static String IGNORE_LANG_FIELDS = "name|url|defaultTheme";
    
    private ContentTemplateCache templateCache;
    
    private TemplateManager templateManager;
    private ContentManager contentManager;
    private BlockManager blockManager;
    private WidgetManager widgetManager;
    private ServiceManager serviceManager;
    private ActionManager actionManager;
    
    private FileManager fileManager;
    private FolderManager folderManager;
    
    //project specific files. This exists in a project only
    private PermalinkManager permalinkManager;
    private MimeTypeManager mimetypeManager;
    
    private Map<String,Theme> themes;
    private Map<String,Module> modules;
    private Theme defaultTheme;
    private ConfigProperties conf;
    
    /**************************************************************************
     * LOCALE MANAGER
     **************************************************************************/
    private Map<String, LocaleSupport> locales = new Hashtable();

    private String _id;
    private String _url; 
    
    /** Creates a new instance of Project */
    public Project(String id, String url) {
        this._id = id; 
        this._url = url;
        
        conf = ContentUtil.getConf( url + "/project.conf"  );
        super.putAll(conf.getProperties());
        super.put("name", id);
        super.put("url", url);
        init();
    }
    
    public void init() {
        this.templateCache = new ContentTemplateCache();
        this.templateManager = new TemplateManager( this );
        this.contentManager = new ContentManager();
        this.blockManager = new BlockManager();
        this.widgetManager = new WidgetManager();
        this.serviceManager = new ServiceManager();
        this.actionManager = new ActionManager();
        this.fileManager = new FileManager(this);
        this.folderManager = new FolderManager(this);
        //project specific files. This exists in a project only
        this.permalinkManager = new PermalinkManager( this );
        this.mimetypeManager = new MimeTypeManager();
        this.themes = new LinkedHashMap();
        this.modules = new LinkedHashMap();        
        
        mimetypeManager.init( conf ); 
        blockManager.init( conf ); 
        actionManager.init( conf ); 
        
        loadModules();
        loadThemes();

        templateManager.init( conf ); 
        permalinkManager.init( conf ); 
        
        String themeName = (String)super.get("theme");
        if(themeName==null) themeName = "default";
        defaultTheme = themes.get(themeName);
        
        //if there is a secured page, fix it
        /*
        String securedPages = (String)super.get("securedPages");
        if(securedPages!=null) {
        }
         */
    }
    
    private class MetaInfo {
        private String moduleName;
        private String path; 
        
        MetaInfo (String path, String moduleName) {
            this.path = path;
            this.moduleName = moduleName;
        }
    }
    
    private void loadThemes() {
        themes.clear();
        
        ArrayList<MetaInfo> metas = new ArrayList(); 
        metas.add( new MetaInfo( ContentUtil.correctUrlPath( getUrl(), null, "themes" ), null ));
        for (Module mod : getModules().values()) {
            metas.add( new MetaInfo(ContentUtil.correctUrlPath( mod.getUrl(), null, "themes" ), mod.getName()));
        }
        
        final MetaInfo[] mm = new MetaInfo[1];
        for (MetaInfo meta : metas) {
            try { 
                mm[0] = meta;
                FileDir.scan(meta.path, new FileFilter(){
                    public void handle(FileDir.FileInfo f) {
                        if ( !f.isDir()) return; 
                        URL conf = f.getSubfile("theme.conf");
                        Theme theme = themes.get( f.getName()); 
                        if ( theme == null ) {
                            theme = new Theme(f.getName(), f.getUrl().toString());
                            themes.put(theme.getName(), theme);
                        }
                        else {
                            theme.addResource(f.getUrl().toString(), mm[0].moduleName);
                        }
                    }
                });
            } catch(Throwable warn) {
                //do nothing
            }            
        }
        
    }
    
    private void loadModules() {
        modules.clear();
        
        // 
        // 1. load modules from config file 
        // 
        BufferedReader br = null;
        try {
            String text = null; 
            URL url = new URL(getUrl() +"modules.conf");
            br = new BufferedReader(new InputStreamReader( url.openStream()));
            while ((text=br.readLine()) != null) {
                if ( text.trim().length() == 0 ) continue; 
                if ( text.trim().startsWith("#")) continue;
                
                String[] arr = text.split("=");
                String key = arr[0].trim();
                String val = arr[1].trim();                
                
                InputStream inp = null; 
                try {
                    inp = new URL(val +"/module.conf").openStream();
                } catch(Throwable t) {
                    continue; 
                } 
                
                try {
                    URL urlc = new URL( val +"/" ); 
                    
                    Module mod = null; 
                    ConfigProperties props = new ConfigProperties(inp); 
                    String modname = props.get("name"); 
                    if ( modname == null || modname.trim().length() == 0 ) {
                        mod = new Module( modname, urlc.toString()); 
                    }
                    else {
                        mod = new Module( key, urlc.toString()); 
                    }
                    
                    mod.setProject( this ); 
                    modules.put( mod.getName(), mod ); 
                } 
                catch(Throwable t) {
                    System.out.println("failed to load URL -> "+ val);
                    t.printStackTrace(); 
                }
                finally {
                    try{ inp.close(); }catch(Throwable t){;} 
                }
            }
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            try { br.close(); }catch(Throwable t){;}
        }
        
        // 
        // 2. load module folders from "modules" directory 
        //         
        try {
            String[] paths = new String[]{ 
                ContentUtil.correctUrlPath(getUrl(), null, "modules"), 
                ContentUtil.correctUrlPath(getUrl(), null, "modules/ext") 
            }; 
            
            for (String path : paths) {
                FileDir.scan(path, new FileFilter(){
                    public void handle(FileDir.FileInfo f) {
                        String fname = f.getName(); 
                        URL furl  = f.getUrl();

                        try {
                            URL urlc = f.getSubfile("module.conf");
                            if ( urlc == null ) return; 
                            
                            Module module = null; 
                            ConfigProperties props = new ConfigProperties(urlc);
                            String modname = props.get("name"); 
                            if ( modname == null || modname.trim().length() == 0 ) {
                                module = new Module(modname, f.getUrl().toString());
                            }
                            else {
                                module = new Module(fname, f.getUrl().toString());
                            }
                            
                            module.setProject(Project.this);
                            modules.put(module.getName(), module);
                        } 
                        catch(Throwable t) {
                            System.out.println("["+ fname +"] failed to load module ("+ furl +") ");
                            t.printStackTrace(); 
                        }
                    }
                });
            }            
        } 
        catch(Throwable t) {
            t.printStackTrace(); 
        }        
    }
    
    
    public String getName() {
        return (String)super.get("name");
    }
    
    public String getUrl() {
        return (String)super.get("url");
    }
    
    public String getTitle() {
        return (String)get("title");
    }
    
    public Theme getDefaultTheme() {
        return defaultTheme;
    }
    
    public boolean isEditable() {
        if(super.containsKey("editable")) {
            return Boolean.valueOf( super.get("editable")+"");
        }
        boolean editable = false;
        try {
            editable = Boolean.valueOf( super.get("editable") +"" );
        } catch(Exception ign){;}
        super.put("editable", editable);
        return editable;
    }
    
    
    public boolean isCached() {
        String webcached = (System.getProperty("web.cached")+"").toLowerCase();
        if ( webcached.matches("true|false")) {
            return new Boolean( webcached ); 
        } 
        
        if( super.containsKey("cached")) {
            return Boolean.valueOf( super.get("cached")+"" );
        }
        boolean cached = false;
        if(isEditable()) {
            cached = false;
        } else {
            try {
                cached = Boolean.valueOf(super.get("cached")+"");
            } catch(Exception ign){;}
        }
        super.put("cached", cached);
        return cached;
    }
    
    
    
    public LocaleSupport getLocaleSupport(String lang) {
        if(locales.containsKey(lang)) return locales.get(lang);
        LocaleSupport locale = loadLocaleSupport(lang);
        locales.put(lang, locale);
        return locale;
    }
    
    
    /*
    public Object get(Object key) {
        if(! key.toString().matches(IGNORE_LANG_FIELDS)) {
            LocaleSupport locale = AnubisContext.getCurrentContext().getCurrentLocale();
            if(locale!=null) {
                String _key = "project."+key;
                String val = (String)locale.getResourceFile().get(_key);
                if(val!=null && val.trim().length()>0) return val;
            }
        }
        return super.get(key);
    }
     */
    
    public ContentTemplateCache getTemplateCache() {
        return templateCache;
    }
    
    public Map<String, Theme> getThemes() {
        return themes;
    }
    
    public Map<String, Module> getModules() {
        return modules;
    }
    
    protected LocaleSupport loadLocaleSupport(String lang) {
        return new LocaleSupport(lang, this);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Managers">
    public PermalinkManager getPermalinkManager() {
        return permalinkManager;
    }
    
    
    public ContentManager getContentManager() {
        return contentManager;
    }
    
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    public BlockManager getBlockManager() {
        return blockManager;
    }
    
    public WidgetManager getWidgetManager() {
        return widgetManager;
    }
    
    public FileManager getFileManager() {
        return fileManager;
    }
    public FolderManager getFolderManager() {
        return folderManager;
    }
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    
    public ActionManager getActionManager() {
        return actionManager;
    }
   
    public MimeTypeManager getMimeTypeManager() {
        return mimetypeManager; 
    }

   
     // </editor-fold >

    public String getSecuredPages() {
        return (String)super.get("securedPages");
    }

    public String getWelcomePage() {
        return (String)super.get("welcomePage");
    }

    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        super.clear();
        conf.clear(); 

        templateCache.clear();
        serviceManager.clear();
        fileManager.clear();
        folderManager.clear(); 
        
        templateManager.clear();
        blockManager.clear();
        actionManager.clear();
        permalinkManager.clear(); 
    }
}

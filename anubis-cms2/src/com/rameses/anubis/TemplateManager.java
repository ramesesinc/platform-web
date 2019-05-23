/*
 * TemplateManager.java
 *
 * Created on March 7, 2013, 3:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Elmo
 */
public class TemplateManager {
    
    private ArrayList<MappingEntry> mappings = new ArrayList();
    private ArrayList<MappingEntry> fragmentMappings = new ArrayList();
    
    private ThemeTemplateCacheSource templateSource = new ThemeTemplateCacheSource();

    private Project project;
    
    public TemplateManager(Project project) {
        this.project = project;
    }
    
    public void clear() {
        mappings.clear();
        fragmentMappings.clear();
    }
    
    private class MetaConf {
        
        private String path;
        private String moduleName;
        
        public MetaConf( String path, String moduleName ) {
            this.path = path;
            this.moduleName = moduleName;
        }
    }
    
    public void init(ConfigProperties conf) {
        ArrayList<MetaConf> metas = new ArrayList();
        metas.add( new MetaConf( project.getUrl() + "template-mapping.conf", null) ); 
        for (Module mod : project.getModules().values()) {
            metas.add( new MetaConf( mod.getUrl() + "template-mapping.conf", mod.getName())); 
        }
        
        List allList = new ArrayList(); 
        URL url = null;
        for ( MetaConf meta : metas ) {
            Properties props = new Properties(); 
            try {
                url = new URL( meta.path );
                props.load( url.openStream()); 
            } catch(Throwable t) { 
                continue; 
            }
            
            List list = new ArrayList(); 
            for(Map.Entry me : props.entrySet()) { 
                String key = (me.getKey() == null ? "" : me.getKey().toString().trim()); 
                if ( key.trim().length() == 0 ) continue; 
                
                String val = (me.getValue() == null ? "" : me.getValue().toString().trim());
                if ( val.trim().length() == 0 ) continue; 
                
                list.add( new MappingEntry( key, val, meta.moduleName ));
            }
            
            list.addAll( allList );  
            allList = list; 
        }
        mappings.addAll( allList ); 
        
        metas.clear(); 
        metas.add( new MetaConf(project.getUrl() + "fragment-template-mapping.conf", null )); 
        for (Module mod : project.getModules().values()) {
            metas.add( new MetaConf( mod.getUrl() + "fragment-template-mapping.conf", mod.getName() )); 
        }    
        
        allList.clear();
        url = null;
        for ( MetaConf meta : metas ) {
            Properties props = new Properties(); 
            try {
                url = new URL( meta.path );
                props.load( url.openStream()); 
            } catch(Throwable t) {  
                continue; 
            }
            
            List list = new ArrayList(); 
            for(Map.Entry me : props.entrySet()) { 
                String key = (me.getKey() == null ? "" : me.getKey().toString().trim()); 
                if ( key.trim().length() == 0 ) continue; 
                
                String val = (me.getValue() == null ? "" : me.getValue().toString().trim());
                if ( val.trim().length() == 0 ) continue; 
                
                list.add( new MappingEntry( key, val, meta.moduleName ));
            }
            list.addAll( allList ); 
            allList = list;
        }
        fragmentMappings.addAll( allList ); 
        allList.clear(); 
    }
        
    private void renderTemplate(String[] masters, Map pmap, Project project, String moduleName) {
        for(String _master: masters) {
            try {
                if(!_master.startsWith("/")) _master = "/" + _master;
                
                templateSource.moduleName = moduleName;
                ContentTemplate ct = project.getTemplateCache().getTemplate( _master, templateSource );
                String tresult  = ct.render( pmap );
                if(tresult!=null && tresult.trim().length()>0) {
                     pmap.put("content", tresult);
                }
            } catch(Exception e) {
                System.out.println("template ->"+_master + " error loading. "+e.getMessage());
            }
        }
    }
    
    public String applyTemplates( File file, Map pmap ) {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Project project = ctx.getProject();
        String[] masters = null;
        String path = file.getPath();
        
        // the path that needs to be match with template mapping is Request.PathInfo
        if ( ctx.getRequest() instanceof HttpServletRequest ) {
            path = ((HttpServletRequest) ctx.getRequest()).getPathInfo(); 
        }

        String result = (String) pmap.get("content");
        
        //if fragment, find the templates for fragments and apply it first
        if(file.isFragment() ) {
            String moduleName = null; 
            for(MappingEntry m: fragmentMappings ) {
                if( m.matches( path )) {
                    masters = m.getTemplates();
                    moduleName = m.getModuleName();
                    break;
                }
            }    
            if(masters==null) masters=new String[]{"fragment"};
            renderTemplate( masters, pmap, project, moduleName );
        }
        
        boolean ajaxRequest = false;
        if(ctx.getAttribute("ajaxRequest")!=null) {
            ajaxRequest = Boolean.valueOf(ctx.getAttribute("ajaxRequest")+"");
        }
        if(!ajaxRequest) {
            masters = null;
            
            String moduleName = null;             
            for(MappingEntry m: mappings ) {
                if( m.matches( path )) {
                    masters = m.getTemplates();
                    moduleName = m.getModuleName(); 
                    break;
                }
            }
            if(masters==null) masters = new String[]{"default"};
            renderTemplate( masters, pmap, project, moduleName );
        }
        return (String)pmap.get("content");
    }
    
    private class ThemeTemplateCacheSource extends ContentTemplateSource {
        
        private String moduleName;
        
        public String getType() {
            return "templates";
        }
        
        public InputStream getResource(String name) throws ResourceNotFoundException{
            AnubisContext ctx = AnubisContext.getCurrentContext();
            Project project = ctx.getProject();
            Theme theme = project.getDefaultTheme();
            String themeName = (theme == null ? null : theme.getName()); 
            if ( themeName == null ) themeName = "default";
            
            ArrayList<String> paths = new ArrayList(); 
            Module mod = (moduleName == null ? null : project.getModules().get( moduleName )); 
            if ( mod != null ) {
                paths.add( mod.getUrl() +"/themes/"+ themeName +"/templates/"+ name ); 
            }
            if ( theme !=null ) {
                for (String path : theme.getPaths()) {
                    paths.add( path +"/templates/"+ name );
                }
            }
            
            paths.add( project.getUrl() +"/themes/"+ themeName +"/templates/"+ name ); 
            paths.add( ctx.getSystemUrl() +"/theme/templates"+ name ); 
            
            return ContentUtil.getResources( paths.toArray(new String[]{}), name);             
        }
    }
}

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
    
    public void init(ConfigProperties conf) {
        ArrayList<String> urls = new ArrayList();
        urls.add( project.getUrl() + "template-mapping.conf" ); 
        for (Module mod : project.getModules().values()) {
            urls.add( mod.getUrl() + "template-mapping.conf" ); 
        }
        
        List allList = new ArrayList(); 
        URL url = null;
        for ( String path : urls ) {
            Properties props = new Properties(); 
            try {
                url = new URL( path );
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
                
                list.add( new MappingEntry( key, val ));
            }
            
            list.addAll( allList );  
            allList = list; 
        }
        mappings.addAll( allList ); 
        
        urls.clear(); 
        urls.add( project.getUrl() + "fragment-template-mapping.conf" ); 
        for (Module mod : project.getModules().values()) {
            urls.add( mod.getUrl() + "fragment-template-mapping.conf" ); 
        }    
        
        allList.clear();
        url = null;
        for ( String path : urls ) {
            Properties props = new Properties(); 
            try {
                url = new URL( path );
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
                
                list.add( new MappingEntry( key, val ));
            }
            list.addAll( allList ); 
            allList = list;
        }
        fragmentMappings.addAll( allList ); 
        allList.clear(); 
    }
        
    private void renderTemplate(String[] masters, Map pmap, Project project) {
        for(String _master: masters) {
            try {
                if(!_master.startsWith("/")) _master = "/" + _master;
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
            for(MappingEntry m: fragmentMappings ) {
                if( m.matches( path )) {
                    masters = m.getTemplates();
                    break;
                }
            }    
            if(masters==null) masters=new String[]{"fragment"};
            renderTemplate( masters, pmap, project );
        }
        
        boolean ajaxRequest = false;
        if(ctx.getAttribute("ajaxRequest")!=null) {
            ajaxRequest = Boolean.valueOf(ctx.getAttribute("ajaxRequest")+"");
        }
        if(!ajaxRequest) {
            masters = null;
            for(MappingEntry m: mappings ) {
                if( m.matches( path )) {
                    masters = m.getTemplates();
                    break;
                }
            }
            if(masters==null) masters = new String[]{"default"};
            renderTemplate( masters, pmap, project );
        }
        return (String)pmap.get("content");
    }
    
    private class ThemeTemplateCacheSource extends ContentTemplateSource {
        public String getType() {
            return "templates";
        }
        public InputStream getResource(String name) throws ResourceNotFoundException{
            AnubisContext ctx = AnubisContext.getCurrentContext();
            Project project = ctx.getProject();
            final Theme theme = project.getDefaultTheme();
            if(theme!=null) {
                return ContentUtil.getResources( new String[]{
                    theme.getUrl()+"/templates/"+name,
                    theme.getProvider()+"/templates/"+name,
                    ctx.getSystemUrl()+"/theme/templates" +name 
                },name);
            } else {
                return ContentUtil.getResources( new String[]{
                    ctx.getSystemUrl()+"/theme/templates"+name
                },name);
            }
        }
    }
}

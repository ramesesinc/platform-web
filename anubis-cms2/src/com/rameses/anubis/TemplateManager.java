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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Elmo
 */
public class TemplateManager {
    
    private List<MappingEntry> mappings = new ArrayList();
    private List<MappingEntry> fragmentMappings = new ArrayList();
    
    private ThemeTemplateCacheSource templateSource = new ThemeTemplateCacheSource();
    
    public void init(ConfigProperties conf) {
        Map masters = conf.getProperties( "template-mapping" );
        if(masters!=null) {
            //load master template mapping
            for(Object o: masters.entrySet()) {
                Map.Entry me = (Map.Entry)o;
                mappings.add( new MappingEntry(me.getKey()+"", me.getValue()+"" ));
            }
        }
        
        masters = conf.getProperties( "fragment-template-mapping" );
        if(masters!=null) {
            //load master template mapping
            for(Object o: masters.entrySet()) {
                Map.Entry me = (Map.Entry)o;
                fragmentMappings.add( new MappingEntry(me.getKey()+"", me.getValue()+"" ));
            }
        }
        
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

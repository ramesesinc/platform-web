/*
 * PageContentProvider.java
 *
 * Created on March 15, 2013, 8:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Elmo
 */
public class PageContentProvider extends ContentProvider {
    
    private PageContentCacheSource contentSource = new PageContentCacheSource();
    
    
    public String getExt() {
        return "pg";
    }
    
    public InputStream getContent(File file, Map params)  throws ResourceNotFoundException{
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Project project = ctx.getProject();
        Module mod = ctx.getModule();
        
        Page page = new Page(file);
        ctx.setCurrentPage( page );
        ContentMap pmap = new ContentMap();
        if ( params != null ) pmap.putAll( params ); 
        
        String result  = "";        
        try {
            ContentTemplate ct = project.getTemplateCache().getTemplate( file.getPath(), contentSource );
            result = ct.render( pmap  );
            pmap.put("content", result );
            result = project.getTemplateManager().applyTemplates( file, pmap );
            return new ByteArrayInputStream(result.getBytes());
        } catch(ResourceNotFoundException rnfe) {
            throw rnfe;
        }
    }
    
    
    
    
    //SOURCE OF THE CONTENT
    private class PageContentCacheSource extends ContentTemplateSource {
        public String getType() {
            return "content";
        }
        public InputStream getResource(String name) throws ResourceNotFoundException{
            AnubisContext ctx = AnubisContext.getCurrentContext();
            ArrayList<String> basePaths = new ArrayList(); 
            String fname = name; 
            
            if ( ctx.getModule() != null ) { 
                String[] arr = ProjectUtils.getModuleNameFromFile( name, ctx.getProject() );
                Module module = ctx.getModule(); 
                fname = arr[1];        
                basePaths.add( module.getUrl() );
                if ( module.getProvider() != null ) {
                    basePaths.add( module.getProvider() );
                }
            } else {
                basePaths.add( ctx.getProject().getUrl());
                basePaths.add( ctx.getSystemUrl());
            }
            
            ArrayList<String> paths = new ArrayList();
            for ( String basepath : basePaths ) {
                paths.add(ContentUtil.correctUrlPath(basepath, "files", null));
                paths.add(ContentUtil.correctUrlPath(basepath, "content", null));
            }
            
            String[] names = new String[]{
                fname +".pg/content",
                fname +"/content"
            };
            
            URL u = new ResourceUtil().findResource(paths.toArray(new String[]{}), names); 
            if ( u != null && !u.toString().endsWith("/content")) u = null; 
            if ( u == null ) throw new ResourceNotFoundException("'"+ name +"' content resource not found"); 
            
            try {
                return u.openStream(); 
            } catch (RuntimeException re) {
                throw re;
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    
    
}

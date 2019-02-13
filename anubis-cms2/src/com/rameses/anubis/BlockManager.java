/*
 * BlockManager.java
 *
 * Created on March 19, 2013, 5:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.util.ConfigProperties;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class BlockManager {
    
    private List<MappingEntry> mappings = new ArrayList();
    private BlockCacheSource blockSource = new BlockCacheSource();
    private GlobalBlockCacheSource globalBlockSource = new GlobalBlockCacheSource();
    
    public void init(ConfigProperties conf) {
        Map map = conf.getProperties( "block-mapping" );
        if(map!=null) {
            //load master template mapping
            for(Object o: map.entrySet()) {
                Map.Entry me = (Map.Entry)o;
                mappings.add( new MappingEntry(me.getKey()+"", me.getValue()+"") );
            }
        }
    }
    
    public String getBlockContent(String name, ContentMap m) throws Exception {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Project project = ctx.getProject();
        File file = ctx.getCurrentPage().getFile();
        String blockname = file.getPath() + "/" + name;
        ContentTemplate ct = null;
        if ( m == null ) { 
            m = new ContentMap();
        } 
        
        try {
            ct = project.getTemplateCache().getTemplate(blockname, blockSource);
            return ct.render( m );
        } catch (ResourceNotFoundException ex) {
            //do nothing 
        } 
        
        //check the mappings
        for( MappingEntry me: mappings) {
            if(me.matches(blockname)) {
                try {
                    String blockName = me.getTemplates()[0];
                    ct = project.getTemplateCache().getTemplate(blockName, globalBlockSource );
                    return ct.render( m );
                } catch(ResourceNotFoundException rnfe){
                    //do nothing 
                } 
            }
        }
        
        //if not found in mapping, we find in global blocks
        try {
            ct = project.getTemplateCache().getTemplate(name, globalBlockSource );
            return ct.render( m );
        } catch(ResourceNotFoundException rnfe) {
            //do nothing 
        } 
        
        return "";
    }
    
    private class BlockCacheSource extends ContentTemplateSource {
        public String getType() {
            return "blocks";
        }
        public InputStream getResource(String name) throws ResourceNotFoundException { 
            AnubisContext ctx = AnubisContext.getCurrentContext();
            ArrayList<String> basePaths = new ArrayList(); 
            String fname = name; 

            Module module = ctx.getModule(); 
            if ( module != null ) { 
                fname = ProjectUtils.correctModuleFilePath( name, module );
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
            
            String[] names = new String[]{ fname };
            URL u = new ResourceUtil().findResource(paths.toArray(new String[]{}), names); 
            if ( u == null ) throw new ResourceNotFoundException("'"+ fname +"' content block not found"); 
            
            try {
                return u.openStream();                
            } catch (RuntimeException re) {
                throw re;
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage(), e);
            } 
        }
    }
    
    private class GlobalBlockCacheSource extends ContentTemplateSource {
        public String getType() {
            return "global-blocks";
        }
        public InputStream getResource(String name) throws ResourceNotFoundException {
            AnubisContext ctx = AnubisContext.getCurrentContext();
            ArrayList<String> basePaths = new ArrayList(); 
            String fname = name; 

            Module module = ctx.getModule(); 
            if ( module != null ) { 
                fname = ProjectUtils.correctModuleFilePath( name, module );
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
                paths.add(ContentUtil.correctUrlPath(basepath, "blocks", null));
            }
            
            String[] names = new String[]{ fname };
            URL u = new ResourceUtil().findResource(paths.toArray(new String[]{}), names); 
            if ( u == null ) throw new ResourceNotFoundException("'"+ fname +"' global block not found"); 
            
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

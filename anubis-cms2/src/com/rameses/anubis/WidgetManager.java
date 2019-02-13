/*
 * WidgetManager.java
 *
 * Created on March 19, 2013, 5:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Elmo
 */
public class WidgetManager {
    
    private WidgetContentSource contentSource = new WidgetContentSource();
    private WidgetStyleSource styleSource = new WidgetStyleSource();
    
    public String getWidgetContent( String name, Map options )  throws ResourceNotFoundException{
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Project project = ctx.getProject();
        ContentMap pmap = new ContentMap();
        pmap.put("STYLE", new WidgetStyle(name) );
        pmap.put("OPTIONS", options );
        
        ctx.setConnectionContext( new ConnectionContext(name) );
        try {
            ContentTemplate ct = project.getTemplateCache().getTemplate( name, contentSource );
            return ct.render( pmap );
        } catch(ResourceNotFoundException rnfe) {
            throw rnfe;
        } catch(RuntimeException e) {
            throw e;
        } finally {
            ctx.removeConnectionContext();
        }
    }
    
    
    private class WidgetStyle {
        private String name;
        
        public WidgetStyle(String name) {
            this.name = name;
        }
        public String render(String style, Object data, Map options) throws ResourceNotFoundException {
            Project project = AnubisContext.getCurrentContext().getProject();
            Map map = new ContentMap();
            map.put("DATA", data);
            map.put("OPTIONS", options);
            String wname = name + "/styles/"+style;
            ContentTemplate ct = project.getTemplateCache().getTemplate( wname, styleSource );
            return ct.render( map );
        }
    }
    
    
    /**
     * we do not allow widgets to be overridden ala classloader
     * heirarchy is -> system, project, module.provider, module
     */
    private class WidgetContentSource extends ContentTemplateSource {
        public String getType() {
            return "widget";
        }
        public InputStream getResource(String name)  throws ResourceNotFoundException{
            AnubisContext ctx = AnubisContext.getCurrentContext();
            InputStream is = null;
            
            //if there is a module name, then its already sure
            if(name.indexOf(":")>0) {
                String[] arr = name.split(":");
                Module module = ctx.getProject().getModules().get(arr[0]);
                if(module == null)
                    throw new ResourceNotFoundException("Module " + arr[0] + " does not exist");
                is = ContentUtil.getResources( new String[]{
                    module.getProvider()+"/widgets/"+arr[1]+"/code",
                    module.getUrl()+"/widgets/"+arr[1]+"/code"
                }, arr[1]);
                ctx.getConnectionContext().setModule( module );
                return is;
            } else {
                Module m = ctx.getModule();
                if(m!=null) {
                    try {
                        is = ContentUtil.getResources( new String[]{
                            m.getProvider() +"/widgets/"+name+"/code",
                            m.getUrl() +"/widgets/"+name+"/code"}, name );
                        if(is!=null) {
                            ctx.getConnectionContext().setModule( m );
                            return is;
                        }
                    } catch(ResourceNotFoundException rnfe){;} catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
                
                //we have to check if its in the module or if its in the global
                return ContentUtil.getResources( new String[]{
                    ctx.getSystemUrl()+"/widgets/"+name+"/code",
                    ctx.getProject().getUrl()+"/widgets/"+name+"/code"
                }, name);
               
            }
        }
    }
    
    private class WidgetStyleSource extends ContentTemplateSource {
        public String getType() {
            return "widgetstyle";
        }
        public InputStream getResource(String name)  throws ResourceNotFoundException{
            AnubisContext ctx = AnubisContext.getCurrentContext();
            if(name.indexOf(":")>0) {
                String prefix = name.substring(0, name.lastIndexOf("/"));
                name = name.substring( name.lastIndexOf("/")+1 );
                String[] arr = name.split(":");
                Module module = ctx.getProject().getModules().get(arr[0]);
                if(module == null)
                    throw new ResourceNotFoundException("Module " + arr[0] + " does not exist");
                return ContentUtil.getResources( new String[]{
                    module.getUrl()+"widgets/"+ prefix + "/" + arr[1],
                    module.getProvider()+"widgets/"+ prefix + "/" + arr[1]
                }, arr[1]);
            } else {
                List<String> list = new ArrayList();
                Module m = ctx.getModule();
                if(m!=null) {
                    list.add( m.getUrl() +"/widgets/"+name );
                    list.add( m.getProvider() +"/widgets/"+name );
                }
                list.add( ctx.getProject().getUrl()+"/widgets/"+name );
                list.add( ctx.getSystemUrl()+"/widgets/"+name );
                return ContentUtil.getResources( (String[])list.toArray(new String[]{}), name );
            }
            
        }
    }
}

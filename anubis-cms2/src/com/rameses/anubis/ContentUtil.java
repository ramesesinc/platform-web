/*
 * ContentUtil.java
 *
 * Created on July 4, 2012, 10:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.anubis;

import com.rameses.io.StreamUtil;
import com.rameses.util.ConfigProperties;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Elmo
 */
public class ContentUtil {
    
    public static String correctUrlPath(String head, String context, String tail) {        
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[]{
            (head == null ? "" : head), 
            (context == null ? "" : context), 
            (tail == null ? "" : tail)
        };
        for ( String str : arr ) {
            if ( str.length() <= 0 ) continue; 
            if ( sb.length() > 0 ) sb.append("/");
            
            StringBuilder buff = new StringBuilder( str );
            if ( buff.length() > 0 && buff.charAt(0) == '/' ) buff.deleteCharAt(0); 
            if ( buff.length() > 0 && buff.charAt(buff.length()-1) == '/') buff.deleteCharAt(buff.length()-1); 
            
            sb.append( buff ); 
        } 
        return sb.toString();
    }
    
    public static boolean fileExists( String path1, String path2, String path3 ) {
        InputStream is = null;
        try {
            is = findResource(path1, path2,path3);
            return (is!=null);
        } catch(Exception e) {
            //do nothing
            return false;
        } finally {
            try {is.close();} catch(Exception ign){;}
        }
    }
    
    public static InputStream findResource( String path1, String path2, String path3 ) {
        try {
            String path = correctUrlPath(path1,path2,path3);
            InputStream is = null;
            is  = new URL(path).openStream();
            return is;
        } catch(FileNotFoundException fe) {
            return null;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String[] tokenizePage(String name) {
        if(name.indexOf("/", 1 ) > 0 ) {
            String[] str = new String[2];
            str[0] = name.substring(1, name.indexOf("/",1));
            str[1] = name.substring( name.indexOf("/",1) );
            return str;
        }
        return null;
    }
    
    
    /**
     * locates a file, opens the inputstream then run the Json utility.
     * if file does not exist, null is returned
     */
    public static Map getJsonMap(URL u) {
        InputStream is = null;
        try {
            is = u.openStream();
            if(is==null) return null;
            return JsonUtil.toMap( StreamUtil.toString(is) );
        } catch(FileNotFoundException fe) {
            return null;
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {is.close(); } catch(Exception ign){;}
        }
    }
    
    public static Map getJsonMap(InputStream is) {
        try {
            if(is==null) return null;
            return JsonUtil.toMap( StreamUtil.toString(is) );
        } 
        catch(RuntimeException re) {
            throw re; 
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        } 
        finally {
            try {is.close(); } catch(Exception ign){;}
        }
    }
    
    public static Map getJsonMap(String path1, String path2, String path3) {
        InputStream is = null;
        try {
            is = findResource(path1, path2, path3);
            if(is==null) return null;
            return JsonUtil.toMap( StreamUtil.toString(is) );
        } 
        catch(RuntimeException re) {
            throw re; 
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        } 
        finally {
            try {is.close(); } catch(Exception ign){;}
        }
    }
     
    public static Map getProperties(URL u) {
        InputStream is = null;
        try {
            is = u.openStream();
            if(is==null) return null;
            Properties props = new Properties();
            props.load(is);
            return props;
        } 
        catch(RuntimeException re) {
            throw re; 
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        } 
        finally {
            try {is.close(); } catch(Exception ign){;}
        }
    }
    public static String replaceSysProperty(String name) {
        AnubisContext ctx = AnubisContext.getCurrentContext();
        Project proj = null;
        if ( ctx != null ) { 
            proj = ctx.getProject();
        } 
        Pattern p = Pattern.compile("\\$\\{.*?\\}");
        Matcher m = p.matcher(name);
        StringBuffer sb = new StringBuffer();
        String s1 = name;
        int start = 0;
        while(m.find()) {
            sb.append( s1.substring(start, m.start()) );
            String s = m.group();
            s = s.replaceAll("\\$|\\{|\\}","");
            
            String value = null;
            if( proj!=null ) value = (String)proj.get(s);
            if (value==null) value = System.getProperty(s);
            if (value==null) value = "";
            
            sb.append(value); 
            start = m.end(); 
        }
        if( start < s1.length()  ) sb.append( s1.substring(start));
        return sb.toString();
    }
    
    
    public static InputStream getResources(String[] urlPaths, String contentName)  throws ResourceNotFoundException{
        for(String s: urlPaths) {
            try {
                URL u = new URL(s);
                if(u!=null) {
                    return u.openStream();
                }
            } catch(Throwable t) {
                //
            }
        }
        throw new ResourceNotFoundException(contentName + " not found");
    }
    
    /**
     * This finds the first applicable file and parses the json from a list of locations.
     */
    
    public static Map findJsonResource(String[] urlPaths, String contentName)  throws Exception{
        InputStream is = null;
        for(String s: urlPaths) {
            try {
                URL u = new URL(s);
                if(u!=null) {
                    is = u.openStream();
                    if(is!=null) break;
                }
            } 
            catch(Throwable ign) {;}
        }
        
        if ( is == null ) {
            throw new ResourceNotFoundException(contentName + " not found");
        }

        try { 
            String str = StreamUtil.toString( is ); 
            return JsonUtil.toMap( new Util().resolveValue( str )); 
        } 
        catch(RuntimeException re) {
            throw re; 
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        } 
        finally {
            try { is.close(); } catch(Throwable t){;}
        }
    }
     
    public static ConfigProperties getConf(String urlPath) {
        try {
            return new ConfigProperties(new URL(urlPath));
        }
        catch(RuntimeException re) {
            throw re; 
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class Util {

        public String resolveValue( String str ) {
            Project proj = null; 
            try { 
                AnubisContext ctx = AnubisContext.getCurrentContext();
                proj = ctx.getProject(); 
            } 
            catch(Throwable t) {;} 
            
            int startidx = 0;
            StringBuilder buff = new StringBuilder();
            while ( true ) {
                int idx0 = str.indexOf("${", startidx); 
                if ( idx0 < 0 ) break; 

                int idx1 = str.indexOf("}", idx0); 
                if ( idx1 < 0 ) break; 

                buff.append(str.substring(startidx, idx0)); 

                String skey = str.substring(idx0+2, idx1); 
                Object objval = (proj == null ? null : proj.get(skey)); 
                if (objval == null) objval = System.getProperty( skey ); 
                if (objval == null) objval = System.getenv( skey ); 

                if (objval == null) { 
                    buff.append(str.substring(idx0, idx1+1)); 
                } else { 
                    buff.append( objval ); 
                } 

                startidx = idx1 + 1; 
            } 

            if ( startidx < str.length()) {  
                buff.append(str.substring(startidx)); 
            } 
            return buff.toString(); 
        }
    }
}

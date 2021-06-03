/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.anubis.web;

import com.rameses.util.Encoder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author wflores
 */
class XmlParser {
    
    private ImportResultInfo result;
    
    public XmlParser() {
    }

    public void parse( File file ) {
        parse( file.toURI() ); 
    }
    
    public void parse( URI uri ) {
        try {
            parse( uri, uri.toURL().openStream());
        }
        catch(RuntimeException re) {
            throw re; 
        }
        catch(Throwable t) {
            throw new RuntimeException(t.getMessage(),  t); 
        }
    }
    
    public void parse( URI uri, byte[] bytes ) {
        parse( uri, new ByteArrayInputStream( bytes )); 
    }
    
    public void parse( URI uri, InputStream inp ) {
        try {
            result = null; 
            
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            HandlerImpl handler = new HandlerImpl( uri );
            parser.parse( inp, handler );
            result = handler.result; 
        }
        catch(RuntimeException re) {
            throw re; 
        }
        catch(Throwable t) {
            throw new RuntimeException(t.getMessage(),  t); 
        }
        finally {
            try { inp.close(); }catch(Throwable t){;} 
        }
    }
    
    public String getText() {
        return (result == null ? null : result.text); 
    }
    
    
    class ImportResultInfo {
        String text; 
        String text_env;
        String text_mod; 
    }
    
    class HandlerImpl extends DefaultHandler {

        int level; 
        File treePath;
        StringBuilder buffer; 
        
        File sourceFile;
        File sourceDir;
        
        StringBuilder buffEnv;
        StringBuilder buffMod;
        
        ImportResultInfo result;
        
        String envid;
        ArrayList<String> importEnvs; 
        
        public HandlerImpl( URI uri ) {
            super();
            
            importEnvs = new ArrayList();
            sourceFile = new File( uri ); 
            sourceDir = sourceFile.getParentFile(); 
        }
        
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            
            this.envid = Encoder.MD5.encode( new java.rmi.server.UID().toString()); 
            
            this.level = 0;
            this.treePath = new File("root"); 
            this.buffer = new StringBuilder();
            
            this.buffEnv = new StringBuilder();
            this.buffMod = new StringBuilder();
            this.result = null; 
        }

        protected void endDocument( ImportResultInfo info ) {
            this.result = info;
        }
        
        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            
            try {
                String search_name = "{"+ envid +"}";
                ImportResultInfo info = new ImportResultInfo();
                info.text = buffer.toString().replace( search_name, join( importEnvs )); 

                buffEnv.append( join( importEnvs )); 
                info.text_env = buffEnv.toString(); 
                
                info.text_mod = buffMod.toString(); 
                endDocument( info ); 
            }
            finally { 
                this.buffer.delete(0, this.buffer.length()); 
                this.buffEnv.delete(0, this.buffEnv.length()); 
                this.buffMod.delete(0, this.buffMod.length()); 
                this.buffer = null; 
                this.buffEnv = null; 
                this.buffMod = null; 
                this.treePath = null; 
                this.level = 0;
            } 
        }
        
        protected ImportResultInfo processImport( File file ) {
            if ( file == null ) { return null; } 
            
            XmlParser parser = new XmlParser(); 
            parser.parse( file.toURI() ); 
            return parser.result; 
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            super.startElement(uri, localName, qName, attrs);
            
            treePath = new File( treePath, qName ); 
            level += 1; 
            
            if ( treePath.toString().equals("root\\app\\modules\\import")) {
                String sval = attrs.getValue("file"); 
                if ( sval == null || sval.trim().length() == 0 ) {
                    return; 
                }
                
                File file = new File( sourceDir, sval ); 
                if ( !file.exists() || !file.isFile())  {
                    return; 
                }
                
                if ( file.toString().equals( sourceFile.toString())) {
                    return; 
                }
                
                ImportResultInfo info = processImport( file ); 
                if ( info != null && info.text_mod != null ) { 
                    buffer.append( info.text_mod ); 
                }
                if ( info != null && info.text_env != null ) { 
                    importEnvs.add( info.text_env ); 
                }
                return; 
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append( createdPad( level )); 
            sb.append("<").append( qName ); 
            for (int i=0; i<attrs.getLength(); i++) {
                sb.append(" "+ attrs.getQName(i)+"=\""+ attrs.getValue(i) +"\"");  
            }
            sb.append(">"); 
            
            buffer.append( sb ); 
            
            boolean is_module_path = treePath.toString().equals("root\\app\\modules\\module"); 
            if ( is_module_path ) {
                buffMod.append( sb ); 
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            boolean is_env_path = treePath.toString().equals("root\\app\\env"); 
            boolean is_import_path = treePath.toString().equals("root\\app\\modules\\import"); 
            boolean is_module_path = treePath.toString().equals("root\\app\\modules\\module"); 
            
            treePath = treePath.getParentFile(); 
            level -= 1; 
            
            if ( is_env_path ) {
                buffer.append("{").append( this.envid ).append("}\n"); 
            }
            
            if ( !is_import_path ) {
                buffer.append(" </").append( qName ).append(">"); 
            }
            
            if ( is_module_path ) {
                buffMod.append(" </").append( qName ).append(">"); 
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            
            boolean is_import_path = treePath.toString().equals("root\\app\\modules\\import"); 
            if ( !is_import_path ) {
                buffer.append(ch, start, length);
            }
            
            boolean is_env_path = treePath.toString().equals("root\\app\\env"); 
            boolean is_mod_path = treePath.toString().equals("root\\app\\modules"); 
            if ( is_env_path ) {
                buffEnv.append(ch, start, length);
            }
            if ( is_mod_path ) {
                buffMod.append(ch, start, length);
            }            
        }

        String createdPad( int level ) {
            StringBuilder sb = new StringBuilder();
            int len = Math.max( level - 1, 0 );
            for (int i=0; i<len; i++) {
                sb.append("   "); 
            }
            return sb.toString(); 
        }
        
        String join( List<String> list ) {
            StringBuilder sb = new StringBuilder();
            for ( String str : list ) {
                if ( str != null ) {
                    sb.append( str ); 
                }
            }
            return sb.toString(); 
        }
    }
    
}

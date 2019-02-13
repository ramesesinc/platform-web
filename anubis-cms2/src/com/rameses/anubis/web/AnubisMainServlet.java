package com.rameses.anubis.web;

import com.rameses.anubis.AnubisContext;
import com.rameses.anubis.File;
import com.rameses.anubis.PermalinkManager;
import com.rameses.anubis.Project;
import com.rameses.anubis.SessionContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AnubisMainServlet extends AbstractAnubisServlet {
    
    protected final void handle(HttpServletRequest hreq, HttpServletResponse hres) throws Exception {
        AnubisContext actx = AnubisContext.getCurrentContext();
        
        String fullPath = hreq.getPathInfo();
        ServletContext app = config.getServletContext();
        
        //run the CMS file
        Project project = actx.getProject();
        Map params = RequestUtil.buildRequestParams( hreq );
        actx.setParams( params );
        
        String mimeType = app.getMimeType( fullPath );
        String ext = CmsWebConstants.PAGE_FILE_EXT;
        if ( mimeType == null ) {
            mimeType = "text/html";
            
        } else {
            //if mimetype is not null then automatically we consider it as media.
            ext = CmsWebConstants.MEDIA_FILE_EXT;
        }
        
        if (fullPath.equals("/")) {
            if(project.getWelcomePage()!=null) { 
                redirect( hres, project.getWelcomePage(), hreq.getContextPath());
            }
            else {
                File ff = project.getFolderManager().findFirstVisibleFile("/");
                if ( ff != null ) fullPath = ff.getPath(); 
                
                redirect( hres, fullPath, hreq.getContextPath());
            }
            return;
            
        } else {
            if(fullPath.endsWith("/")) fullPath = fullPath.substring(0, fullPath.length()-1);
        }
        
        String filename = null;
        if ( fullPath.endsWith(".pg") || fullPath.endsWith(".media")) { 
            filename = fullPath; 
        } else {
            filename = fullPath + ext; 
        }
        
        boolean is_media = false;
        if ( filename.endsWith(".media")) {
            is_media = true; 
            String mediaType = app.getMimeType( filename.substring(0, filename.lastIndexOf(".media")) ); 
            if ( mediaType != null ) mimeType = mediaType; 
        } 

        SessionContext ctx = actx.getSession();
        
        //FIND the associated file in permalink. Bypass if its a fragment request. We assume
        //it is a fragment if requested via ajax. Requests thru ajax headers are marked
        //with : X-Requested-With=XMLHttpRequest
        
        boolean ajaxRequest = false;
        String requestWith = hreq.getHeader("X-Requested-With");
        if( requestWith !=null && requestWith.equalsIgnoreCase("XMLHttpRequest") ) {
            ajaxRequest = true;
            actx.setAttribute( "ajaxRequest", true );
        }
        
        File file = null; 
        try {
            //check if filename matches permalinks. if secured,
            //use secured permalinks instead
            PermalinkManager permalink = project.getPermalinkManager();
            String resolvedName = permalink.resolveName( fullPath, params  );
            if ( resolvedName != null ) filename = resolvedName;
            
            file = project.getFileManager().getFile( filename );
        } 
        catch(com.rameses.anubis.FileNotFoundException fe) { 
            hres.setStatus( HttpServletResponse.SC_NOT_FOUND ); 
            File f = getErrorFile( HttpServletResponse.SC_NOT_FOUND ); 
            if ( f != null ) { 
                handleError(hreq, hres, mimeType, f, fe); 
                return; 
            }       
            throw new ServletException( fe.getMessage(), fe ); 
        } 
        catch(Throwable e) {  
            hres.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            File f = getErrorFile( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ); 
            if ( f != null ) {
                handleError(hreq, hres, mimeType, f, e); 
                return; 
            }

            if ( e instanceof RuntimeException ) {
                throw (RuntimeException) e; 
            } else {
                throw new ServletException(e.getMessage(), e); 
            }
        }
            
        if (file.getHref() != null) {
            redirect( hres, file.getHref(), hreq.getContextPath() );
            return;
        }
        
        boolean secured = false;
        if(file.isSecured()) {
            secured = true;
        }
        
        if ( is_media ) {
            Object objval = file.get("mimeType"); 
            String strval = (objval == null ? "" : objval.toString().trim()); 
            if (strval.length() > 0) mimeType = strval; 
        }
        
        
        //set authenicated as true if there is sessionid
        boolean allow_access = true;
        if (secured && ctx.getSessionid()==null) allow_access = false;
        if (!allow_access) {
            String path = CmsWebConstants.LOGIN_PAGE_PATH;
            String requestPath = hreq.getRequestURI();
            String qry = hreq.getQueryString();
            if (qry!=null && qry.trim().length()>0){
                requestPath += "?"+ qry;
            }
            
            if (ajaxRequest) {
                hres.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                //hres.sendRedirect(path  + "?target=" + URLEncoder.encode(requestPath));
                //ResponseUtil.write( hreq, hres, mimeType, is);
                //hres.sendError(  )
                String referer = hreq.getHeader("Referer");
                //remove the server part
                referer = referer + "#"+ file.getHashid();
                String redirect = path  + "?target=" + URLEncoder.encode(referer);
                ResponseUtil.write( hreq, hres, mimeType, redirect);
            } 
            else {
                //file = project.getFileManager().getFile( "/reload.pg" );
                //InputStream is = project.getContentManager().getContent(file, params);
                //ResponseUtil.write( hreq, hres, mimeType, is);
                redirect( hres, path + "?target=" + URLEncoder.encode(requestPath), hreq.getContextPath() );
            }
        } 
        else {
            boolean authorized = true;
            String domain = file.getDomain();
            String role = file.getRole();
            String permission = file.getPermission();
            
            if (role!=null || permission!=null) {
                authorized = ctx.checkPermission(domain, role, permission);
            }
            
            //if no permission, redirect to a non-authorized page
            if ( !authorized ) {
                hres.setStatus(HttpServletResponse.SC_FORBIDDEN);
                File f = getErrorFile( HttpServletResponse.SC_FORBIDDEN ); 
                if ( f != null ) {
                    handleError(hreq, hres, mimeType, f, new Exception("Forbidden")); 
                }                 
                return;
            } 
            
            try { 
                hres.setStatus(HttpServletResponse.SC_OK);

                //fire all actions first before rendering the page.
                project.getActionManager().fireActions( fullPath, params );
                if( params.containsKey("redirect") ) {
                    String redirect = params.get("redirect").toString();
                    String queryParams = null;
                    int pidx = redirect.indexOf("?");
                    if( params.containsKey("error")) {
                        Map merr = new HashMap();
                        merr.put("error", params.get("error"));
                        hreq.getSession().setAttribute("PARAMS", merr);
                    }
                    if(  pidx > 0 ) {
                        queryParams = redirect.substring(pidx+1);
                        redirect = redirect.substring(0, pidx);
                    }
                    //this is to avoid cyclic redirects...
                    if(!fullPath.equals(redirect) && !fullPath.endsWith(redirect)) {
                        if(queryParams !=null) redirect += "?" + queryParams;
                        redirect( hres, redirect, hreq.getContextPath() ); 
                        return;
                    }
                }
                InputStream inp = project.getContentManager().getContent(file,params);
                ResponseUtil.write( hreq, hres, mimeType, inp);
            } 
            catch(Throwable e) { 
                hres.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
                File f = getErrorFile( HttpServletResponse.SC_INTERNAL_SERVER_ERROR ); 
                if ( f != null ) {
                    handleError(hreq, hres, mimeType, f, e); 
                    return; 
                }
                
                if ( e instanceof RuntimeException ) {
                    throw (RuntimeException) e; 
                } else {
                    throw new ServletException(e.getMessage(), e); 
                }                
            } 
        }
    }
    
    private File getErrorFile( int status ) {
        AnubisContext actx = AnubisContext.getCurrentContext();
        Project project = actx.getProject();
        
        File file = null; 
        if ( status == HttpServletResponse.SC_FORBIDDEN ) { 
            file = findFile( project, "/403.pg" ); 
        } 
        else if ( status == HttpServletResponse.SC_NOT_FOUND ) {
            file = findFile( project, "/404.pg" );
        }
        else if ( status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) {
            file = findFile( project, "/500.pg" );
        }
        
        if ( file == null ) {
            file = findFile( project, "/error.pg" );
        } 
        return file; 
    }
    private File findFile( Project project, String filepath ) { 
        try {
            return project.getFileManager().getFile( filepath );
        } catch (Exception e) {
            return null; 
        }
    }
    private void handleError( HttpServletRequest hreq, HttpServletResponse hres, String mimeType, File targetFile, Throwable error ) throws Exception { 
        AnubisContext actx = AnubisContext.getCurrentContext();
        Project project = actx.getProject();
        Map params = actx.getParams();
        Map map = new HashMap(); 
        if ( params != null ) { 
            map.putAll( params );
        } 
        map.put("ERROR", error); 

        InputStream inp = project.getContentManager().getContent( targetFile, map );
        if ( inp == null ) { 
            inp = new ByteArrayInputStream("".getBytes()); 
        } 
        ResponseUtil.write( hreq, hres, mimeType, inp );
    } 
    
    private void redirect(HttpServletResponse hres, String pathInfo, String contextPath ) throws Exception { 
        if ( contextPath == null || contextPath.length() == 0 ) {
            hres.sendRedirect( pathInfo ); 
        } else if ( pathInfo.startsWith( contextPath )) {
            hres.sendRedirect( pathInfo );
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append( contextPath ).append( pathInfo );
            hres.sendRedirect( sb.toString() ); 
        }
    }
}


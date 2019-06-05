function RemoteProxy(name, connection, module, is_remote) {
	this.name = name;
	this.module = module;
    this.connection = connection;
	this.is_remote = is_remote; 
	
	var convertResult = function( result ) {
        if (result==null) return null;

        var datatype = (typeof result); 
        if ( datatype == 'string') { 
        	result = unescape( result ); 
        	try {
        		if ( result.indexOf('[') == 0 && result.lastIndexOf(']') > 1 ) {
        			return JSON.parse(result); 
        		} 

        		var idx0 = result.indexOf('{'); 
				var idx1 = result.lastIndexOf('}'); 
				if ( idx0 >= 0 && idx1 > idx0 ) { 
					result = result.substring(idx0, idx1+1); 
				} 
        		//return JSON.parse( result ); 
        		return JSON.parse(result); 
        	} 
        	catch(e) { 
        		console.log( e ); 
        	} 
		} 
        return eval(result);
	} 

	var guid = function() { 
		function s4() { return ''+ Math.floor(Math.random()*1000000001); } 
		return s4() +'-'+ s4() +'-'+ s4(); 
	} 

	var getDomain = function() { 
		var href = window.location.href;
		href = href.substring( href.indexOf('://') + 3);
		href = href.split('/')[0];
		return href.split(':')[0];
	} 

	this.invoke = function( action, args, handler ) {
        var urlaction = '/js-invoke'+ (this.module? '/'+this.module:'');
        urlaction += '/'+ this.connection +'/'+ this.name +'.'+ action; 
        var err = null;	
		var data = []; 
		if( args ) { 
			if (args.length == 0 || !args[0]) {
				//do nothing
			} else { 
				var _args = [(args? args[0]: null)];
				data.push('args=' + encodeURIComponent(JSON.stringify( _args ))); 
			} 
		}
		data = data.join('&');

		if ( this.is_remote ) { 
			// this is a remote connection 
			var tokenid = guid(); 
			var _args = (args? args[0]: null);
			_args.tokenid = tokenid; 

			data = 'args=' + encodeURIComponent(JSON.stringify([ _args ])); 

			var has_received_result = false; 
			var ws = new WebSocket('ws://'+ getDomain() +':9001/gdx-notifier/subscribe/'+ tokenid);
			ws.onopen = function() {} 
			ws.onclose = function() {} 
			ws.onerror = function ( evt ) { 
				console.log( evt ); 
			} 

			ws.onmessage = function ( evt ) { 
				if ( has_received_result == true ) { 
					// do nothing 
					return; 
				} 

				var data = evt.data; 
				var datatype = (typeof data); 
				if ( datatype == 'string') { 
					var idx0 = data.indexOf('{'); 
					var idx1 = data.lastIndexOf('}'); 
					if ( idx0 >= 0 && idx1 > idx0 ) { 
						data = data.substring(idx0, idx1+1); 
					} 
					data = JSON.parse( data ); 
					if ( data.stackTrace ) {
						data.status = 'error'; 
					}
				} 
				else {
					var newdata = {result: data} 
					data = newdata;  
				}
				
				var stat = 'success'; 
				if ( data.status == 'error' ) {
					stat = data.status; 
					data.result = new Error(data.message); 
				}
				else if ((typeof data) == 'object') { 
					var svc = Service.lookup('CacheService');  
					data.result = svc.get({ key: data.tokenid, autoremove: true, charset:'UTF8' }); 
				} 

				if ( handler ) {
					handler( data, stat ); 
				}
			} 

			$.ajax({ 
				type    : "POST", 		
				url     : urlaction,
				data    : data, 
				async   : true,	
				success : function( data ) { 
					// do nothing 
				}, 
				error   : function( xhr ) { 
					has_received_result = true; 
					if ( handler ) {
						handler( new Error(xhr.responseText), 'error' ); 
					}
				} 
			}); 
		} 
		else {
			// this is a default connection 
			if (handler == null) { 
				var result = $.ajax({
					type  : "POST", 				
					url   : urlaction, 
					data  : data, 
					async : false, 
					error : function( xhr ) { 
						err = xhr.responseText; 
					} 				
				}).responseText;

				if ( err!=null ) {
					throw new Error(err);
				}
				return convertResult( result );
			}
			else {
				$.ajax({ 
					type    : "POST", 	
					url     : urlaction, 
					data    : data, 
					async   : true,	
					success : function( data ) { 
						var r = convertResult(data);
						handler(r); 
					},
					error   : function( xhr ) { 
						handler( null, new Error(xhr.responseText) ); 
					} 				
				});
			}
		}
	}
};

var Service = new function() {
	this.debug = false;
	this.services = {}
	this.module = null;

	var guid = function() { 
		function s4() { return ''+ Math.floor(Math.random()*1000000001); } 
		return s4() +'-'+ s4() +'-'+ s4(); 
	} 

	this.lookup = function(name, connection, mod) { 
		var is_remote = (name.indexOf(':') > 0); 

        var module = this.module;
        if(connection==null) connection = "default";
        if( mod ) module = mod;

		if (this.debug == true && window.console) 
			window.console.log('Service_lookup: name='+name + ', module='+module + ', connection=' + connection); 
	
		if ( this.services[name]==null ) {
			if (this.debug == true && window.console) 
				window.console.log('Service_lookup: module='+module); 
			
			var urlaction =  '/js-proxy' + (module? '/'+module: '');
			urlaction += '/' + connection + '/' + name + ".js";
			urlaction += '?' + guid();
			
            if (this.debug == true && window.console) 
				window.console.log('Service_lookup: urlaction='+urlaction); 
			
			var err = null;
			var params = {
				type: "GET", async: false, url: urlaction,
	            error: function( xhr ) { err = xhr.responseText } 
			} 
			var result = $.ajax( params ).responseText;
			if ( err != null ) throw new Error( err );

			if (this.debug == true && window.console) 
				window.console.log('Service_lookup: result='+result); 
			
			var func = eval( '(' + result + ')' );	
                            
			var svc = new func( new RemoteProxy(name, connection, module, is_remote) );
			this.services[name] = svc;
		}
		return this.services[name];
	} 
};
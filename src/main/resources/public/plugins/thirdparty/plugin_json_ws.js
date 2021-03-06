(function()
{
	var jsonWebSocketDatasource = function(settings, updateCallback)
	{
		var self = this;
		var currentSettings = settings;
		var ws;
		
		var onOpen=function()
		{
			console.info("WebSocket(%s) Opened",currentSettings.url);
		}
		
		var onClose=function()
		{
			console.info("WebSocket Closed");
		}
		
		var onMessage=function(event)
		{
			var data=event.data;
			
			console.info("WebSocket received %s",data);
			
			var objdata=JSON.parse(data);
			
			if(typeof objdata == "object")
			{
				updateCallback(objdata);
			}
			else
			{
				updateCallback(data);
			}
			
		}
		
		function createWebSocket()
		{
			if(ws) ws.close();
			
			var url=currentSettings.url;
			ws=new WebSocket(url);
			
			ws.onopen=onOpen;
			ws.onclose=onClose;
			ws.onmessage=onMessage;

//			//create queries
//			$.ajax({
//				url: "http://localhost:4567/api/queries/register",
//				dataType: "JSONP",
//				type: "POST",
//				data: currentSettings.queries
//			});
		}
		
		createWebSocket();

		this.updateNow = function()
		{
			createWebSocket();
		}

		this.onDispose = function()
		{
			ws.close();
		}

		this.onSettingsChanged = function(newSettings)
		{
			currentSettings = newSettings;
			
			createWebSocket();
		}
	};

	freeboard.loadDatasourcePlugin({
		type_name  : "iot_ws_stream",
		display_name : "IoT Datastore Stream",
		description : "Streaming datasource from your IoT Datastore",
		settings   : [
			{
				name        : "url",
				display_name: "URL",
				type        : "text",
				"default_value": "ws://localhost:4567/events"
			},
			{
				name: "queries",
				display_name: "Queries",
				type: "array",
				settings: [
					{
						name: "sparql",
						display_name: "SPARQL queries",
						type: "text"
					}
				]
			}
		],
		newInstance: function(settings, newInstanceCallback, updateCallback)
		{
			newInstanceCallback( new jsonWebSocketDatasource(settings, updateCallback));
		}
	});
}());
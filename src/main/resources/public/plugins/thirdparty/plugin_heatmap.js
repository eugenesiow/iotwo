// Best to encapsulate your plugin in a closure, although not required.
(function()
{
	// ## A Widget Plugin
	//
	// -------------------
	// ### Widget Definition
	//
	// -------------------
	// **freeboard.loadWidgetPlugin(definition)** tells freeboard that we are giving it a widget plugin. It expects an object with the following:
	freeboard.loadWidgetPlugin({
		// Same stuff here as with datasource plugin.
		"type_name"   : "heatmap",
		"display_name": "Time-series Heatmap Plugin",
        "description" : "",
		// **external_scripts** : Any external scripts that should be loaded before the plugin instance is created.
		"external_scripts": [
			"http://d3js.org/d3.v3.min.js", "js/epoch.js"
		],
		// **fill_size** : If this is set to true, the widget will fill be allowed to fill the entire space given it, otherwise it will contain an automatic padding of around 10 pixels around it.
		"fill_size" : false,
		"settings"    : [
			{
				"name"        : "chart_id",
				"display_name": "Chart Id",
				"type"        : "text"
			},
			{
				"name"        : "label",
				"display_name": "Label",
				"type"        : "calculated"
			},
			{
				"name"        : "value",
				"display_name": "Value",
				"type"        : "calculated"
			}
		],
		// Same as with datasource plugin, but there is no updateCallback parameter in this case.
		newInstance   : function(settings, newInstanceCallback)
		{
			newInstanceCallback(new HeatMapPlugin(settings));
		}
	});
	
	freeboard.addStyle('.tw-barchart',
		'height:280px;width:100%;');
	freeboard.addStyle('.epoch','font-family: "Helvetica Neue", Helvetica, Arial, sans-serif; font-size: 12pt;');
	freeboard.addStyle('.axis path','fill: transparent; stroke: #d0d0d0;');
	freeboard.addStyle('.axis path','fill: transparent; stroke: #d0d0d0;');
	freeboard.addStyle('.axis .tick text','fill: #d0d0d0;font-size: 9pt;');
	freeboard.addStyle('.bar.category1','fill: #909CFF;');
	freeboard.addStyle('.category1 .bucket','fill: #1f77b4;');
	freeboard.addStyle('.category2 .bucket','fill: #2ca02c;');
	freeboard.addStyle('.category3 .bucket','fill: #d62728;');
	freeboard.addStyle('.category4 .bucket','fill: #8c564b;');
	freeboard.addStyle('.category5 .bucket','fill: #7f7f7f;');

	// ### Widget Implementation
	//
	// -------------------
	// Here we implement the actual widget plugin. We pass in the settings;
	var HeatMapPlugin = function(settings)
	{
		var self = this;
		var currentSettings = settings;
		var displayElement = $('<div class="tw-display"></div>');
        var chartElement = $('<div id="'+currentSettings.chart_id+'" class="tw-barchart tw-td"></div>');
        var labelObj = [];
        var chart;
        
        function updateValueSizing()
		{
			chart.option('width',$(displayElement).width());
		}

		// **render(containerElement)** (required) : A public function we must implement that will be called when freeboard wants us to render the contents of our widget. The container element is the DIV that will surround the widget.
		self.render = function(element)
		{
			$(element).empty();

			$(displayElement)
				.append($('<div class="tw-tr"></div>').append(chartElement));

			$(element).append(displayElement);
			
			chart = $('#'+currentSettings.chart_id).epoch({ 
				type: 'time.heatmap',
				ticks: {left:4, bottom: 20},
				buckets: 5,
				bucketRange: [0,20],
				axes: ['bottom', 'left'],
				margins: {top:10,bottom:25,left:80,right:10},
//				tickFormats: {left:function(d) {return d.split("_")[1];}},
				data: [{"label":currentSettings.chart_id,"values":[{time:Math.floor(Date.now() / 1000),histogram:{"kitchen_corner":0,"livingroom_corner":0,"master_corner":0,"bedroom_corner":0,"basement_corner":0}}]}]
			});

//			updateValueSizing();
		}

		// **getHeight()** (required) : A public function we must implement that will be called when freeboard wants to know how big we expect to be when we render, and returns a height. This function will be called any time a user updates their settings (including the first time they create the widget).
		//
		// Note here that the height is not in pixels, but in blocks. A block in freeboard is currently defined as a rectangle that is fixed at 300 pixels wide and around 45 pixels multiplied by the value you return here.
		//
		// Blocks of different sizes may be supported in the future.
		self.getHeight = function()
		{
			return 5;
		}

		// **onSettingsChanged(newSettings)** (required) : A public function we must implement that will be called when a user makes a change to the settings.
		self.onSettingsChanged = function(newSettings)
		{
			// Normally we'd update our text element with the value we defined in the user settings above (the_text), but there is a special case for settings that are of type **"calculated"** -- see below.
			currentSettings = newSettings;
		}

		// **onCalculatedValueChanged(settingName, newValue)** (required) : A public function we must implement that will be called when a calculated value changes. Since calculated values can change at any time (like when a datasource is updated) we handle them in a special callback function here.
		self.onCalculatedValueChanged = function(settingName, newValue)
		{
			if(settingName == "label")
			{
				labelObj = newValue;
			} else if(settingName == "value") {
				
				var chartData = [{time:Math.floor(Date.now() / 1000),histogram:{}}];
				if(newValue.constructor == Array) {
					for(var i=0;i<labelObj.length;i++) {
						chartData[0].histogram[labelObj[i]] = newValue[i];
					}
				} else {
					chartData[0].histogram[labelObj] = newValue;
				}
				chart.push(chartData);
				updateValueSizing();
			}
			
		}

		// **onDispose()** (required) : Same as with datasource plugins.
		self.onDispose = function()
		{
		}
		
		this.onSettingsChanged(settings);
	}
}());
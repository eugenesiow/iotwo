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
		"type_name"   : "barchart",
		"display_name": "Horizontal Bar Chart Plugin",
        "description" : "A horizontal bar chart",
		// **external_scripts** : Any external scripts that should be loaded before the plugin instance is created.
		"external_scripts": [
			"http://d3js.org/d3.v3.min.js", "js/epoch.js"
		],
		// **fill_size** : If this is set to true, the widget will fill be allowed to fill the entire space given it, otherwise it will contain an automatic padding of around 10 pixels around it.
		"fill_size" : false,
		"settings"    : [
			{
				"name"        : "title",
				"display_name": "Title",
				"type"        : "text"
			},
			{
				"name"        : "chart_id",
				"display_name": "Chart Id",
				"type"        : "text"
			},
			{
				"name"        : "bar_label",
				"display_name": "Bar Label",
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
			newInstanceCallback(new BarChartPlugin(settings));
		}
	});
	
	freeboard.addStyle('.tw-barchart',
		'height:280px;width:100%;');
	freeboard.addStyle('.epoch','font-family: "Helvetica Neue", Helvetica, Arial, sans-serif; font-size: 12pt;');
	freeboard.addStyle('.axis path','fill: transparent; stroke: #d0d0d0;');
	freeboard.addStyle('.axis path','fill: transparent; stroke: #d0d0d0;');
	freeboard.addStyle('.axis .tick text','fill: #d0d0d0;font-size: 9pt;');
	freeboard.addStyle('.bar.category1','fill: #909CFF;');

	// ### Widget Implementation
	//
	// -------------------
	// Here we implement the actual widget plugin. We pass in the settings;
	var BarChartPlugin = function(settings)
	{
		var self = this;
		var currentSettings = settings;
		var displayElement = $('<div class="tw-display"></div>');
		var titleElement = $('<h2 class="section-title tw-title tw-td"></h2>');
        var chartElement = $('<div id="'+currentSettings.chart_id+'" class="tw-barchart tw-td"></div>');
        var barLabel;
        var barData = [];
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
				.append($('<div class="tw-tr"></div>').append(titleElement))
				.append($('<div class="tw-tr"></div>').append(chartElement));

			$(element).append(displayElement);
			
			chart = $('#'+currentSettings.chart_id).epoch({ 
				type: 'bar',
				orientation: 'horizontal',
				ticks: {left:20},
				margins: {top:10,bottom:25,left:80,right:10},
				tickFormats: {left:function(d) {return d.split("_")[1];}},
				width: 590
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

			var shouldDisplayTitle = (!_.isUndefined(newSettings.title) && newSettings.title != "");

			if(shouldDisplayTitle)
			{
				titleElement.html((_.isUndefined(newSettings.title) ? "" : newSettings.title));
				titleElement.attr("style", null);
			}
			else
			{
				titleElement.empty();
				titleElement.hide();
			}
		}

		// **onCalculatedValueChanged(settingName, newValue)** (required) : A public function we must implement that will be called when a calculated value changes. Since calculated values can change at any time (like when a datasource is updated) we handle them in a special callback function here.
		self.onCalculatedValueChanged = function(settingName, newValue)
		{
			if(settingName == "bar_label")
			{
				barLabel = newValue;
			} else if(settingName == "value") {
				if(newValue.constructor == Array) {
					for(var i=0;i<barLabel.length;i++) {
						barData[barLabel[i]] = newValue[i];
					}
				} else {
					barData[barLabel] = newValue;
				}
				var chartData = [{"label":currentSettings.chart_id,"values":[]}];
//				var max = 0;
				for(var i in barData) {
					var row = {x:i,y:barData[i]};
					chartData[0].values.push(row);
//					if(barData[i]>max)
//						max = barData[i];
				}
				chart.update(chartData);
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
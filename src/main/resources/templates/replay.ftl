<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>IoT Personal Datastore</title>

    <!-- Bootstrap Core CSS -->
    <link href="/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="/bower_components/metisMenu/dist/metisMenu.min.css" rel="stylesheet">
    
    <!-- Dropzone -->
    <link rel="stylesheet" href="/bower_components/dropzone/dist/min/dropzone.min.css">

    <!-- Custom CSS -->
    <link href="/css/sb-admin-2.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="/bower_components/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body>

    <div id="wrapper">

        <!-- Navigation -->
        <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/">IoT Personal Datastore</a>
            </div>
            <!-- /.navbar-header -->

            <#include "navbar.ftl">

            <#include "sidebar.ftl">
        </nav>

        <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Sensor Replay</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-12 toolbar">
				    <div class="btn-group" data-toggle="buttons-checkbox">
        				<a class="btn btn btn-default" id="new-replay-btn" data-toggle="collapse" href="#new-replay">
							<span class="glyphicon glyphicon-file" aria-hidden="true"></span> New
						</a>
    				</div>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row collapse toolbar" id="new-replay">
                <div class="panel panel-default">
                	<div class="panel-heading">Add new replay</div>
                	<div class="panel-body" id="new-replay-body">
                    	<form action="/sensors/replay/upload" class="dropzone toolbar" id="dataUploadDropzone"></form>
                    </div>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
	            <div class="table-responsive">
		            <table class="table table-striped table-bordered table-hover">
		                <thead>
		                    <tr>
		                        <th class="col-md-1">#</th>
		                        <th>URI</th>
		                        <th>Name</th>
		                        <th>Source</th>
		                        <th class="col-md-1">Model</th>
		                        <th class="col-md-1">Mapping</th>
		                        <th class="col-md-1">Rate</th>
		                        <th class="col-md-1">Action</th>
		                    </tr>
		                </thead>
		                <tbody id="replay-rows">
		                    
		                </tbody>
		            </table>
		        </div>
		        <!-- /.table-responsive -->
	        </div>
	        <!-- /.row -->
        </div>
        <!-- /#page-wrapper -->

    </div>
    <!-- /#wrapper -->

    <!-- jQuery -->
    <script src="/bower_components/jquery/dist/jquery.min.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>

    <!-- Metis Menu Plugin JavaScript -->
    <script src="/bower_components/metisMenu/dist/metisMenu.min.js"></script>

    <!-- Dropzone -->
    <script src="/bower_components/dropzone/dist/min/dropzone.min.js"></script>
    
    <!-- Custom Theme JavaScript -->
    <script src="/js/sb-admin-2.js"></script>
    
    <script>
    	$( document ).ready(function() {
    		loadData();
    	});
    
    	Dropzone.options.dataUploadDropzone = {
    		dictDefaultMessage: "Drop CSV files here or click to upload",
    		acceptedFiles: ".csv",
    		init: function() {
 		    	this.on("success", function(file,msg) { 
 		    		var myDropZone = this;
					$('#dataUploadDropzone').delay(1000).hide(0, function() {
						myDropZone.removeFile(file);
						var dataObj = JSON.parse(msg);
						$('#new-replay-body').append(createForm(dataObj));
					});
					
 		    	});
 		  	}
   		};
    	
    	function loadData() {
    		$.get('/api/sensors/replay', function(rawData) {
    			var data = JSON.parse(rawData);
    			var colHeaderList = ["uri","name","source","model","mapping","rate"];
    			var replayRows = $('#replay-rows');
    			for(var i in data) {
    				var tableRow = $('<tr>',{class:'replay-row'});
    				tableRow.data(data);
    				tableRow.append('<td class="replay-row-index">'+(parseInt(i)+1)+'</td>');
    				for(var j in colHeaderList) {
    					var colName = colHeaderList[j];
    					var colVal = data[i][colName];
    					var okCol = $('<td class="replay-row-'+colName+'"><i class="glyphicon glyphicon-ok"></i></td>');
    					var missCol = $('<td class="replay-row-'+colName+'"><i class="glyphicon glyphicon-remove"></i></td>');
    					if(colName=="source") {
    						tableRow.append(okCol);
    					} else if(colName=="mapping") {
    						if(colVal==undefined) {
    							tableRow.append(missCol);
    						} else {
    							tableRow.append(okCol);
    						}
    					} else if(colName=="model") {
    						tableRow.append(okCol);
    					} else {
    						tableRow.append('<td class="replay-row-'+colName+'">'+colVal+'</td>');
    					}
    				}
    				tableRow.append('<td class="replay-row-action"><i class="glyphicon glyphicon-play"></i></td>');
    				replayRows.append(tableRow);
    			}
    		});
    	}
    	
    	function createForm(dataObj) {
    		var row = $('<div>',{class:'panel-body new-replay-form'});
    		
    		//create header
    		var header = $('<form>',{class:'form-horizontal'});
    		var grp = $('<div>',{class:'form-group'});
    		var uriInputGrp = $('<div>',{class:'col-xs-5'});
    		uriInputGrp.append('<label>URI:</label>');
    		uriInputGrp.append('<input type="text" class="form-control new-input-uri" value="'+dataObj.uri+'">');
    		grp.append(uriInputGrp);
    		var nameInputGrp = $('<div>',{class:'col-xs-2'});
    		nameInputGrp.append('<label>Name:</label>');
    		nameInputGrp.append('<input type="text" class="form-control new-input-name" value="'+dataObj.tableName+'">');
    		grp.append(nameInputGrp);
    		var rateInputGrp = $('<div>',{class:'col-xs-2'});
    		rateInputGrp.append('<label>Rate (/s):</label>');
    		rateInputGrp.append('<input type="text" class="form-control new-input-rate" value="1">');
    		grp.append(rateInputGrp);
    		header.append(grp);
    		row.append(header);
    		
    		//create col editor
    		var coledit = $('<form>',{class:'form-horizontal'});
    		coledit.append('<div class="form-group"><div class="col-xs-2"><label>Columns:</label></div></div>');
    		var cols = dataObj.sample;
    		for(var key in cols) {
    			var keyGrp = $('<div>',{class:'form-group new-model-col'});
    			keyGrp.append('<div class="col-xs-2"><input class="form-control new-model-col-name" value="'+cols[key].name+'"></div>');
    			keyGrp.append('<div class="col-xs-2"><input class="form-control new-model-col-type" value="'+cols[key].type+'"></div>');
    			keyGrp.append('<label>E.g. '+cols[key].eg+'</label>');
    			coledit.append(keyGrp);
    		}
    		
    		var addBtn = $('<button>',{class:'btn btn-primary',text:'Add'});
    		addBtn.on('click',function(e) {
    			e.preventDefault();
    			
    			var dataReplay = {};
    			dataReplay.uri = uriInputGrp.find('.new-input-uri').val();
    			dataReplay.name = nameInputGrp.find('.new-input-name').val();
    			dataReplay.rate = rateInputGrp.find('.new-input-rate').val();
    			dataReplay.source = dataObj.actualPath;
    			var model = [];
    			coledit.find('.new-model-col').each(function() {
    				var modelCol = {};
    				modelCol.name = $(this).find('.new-model-col-name').val();
    				modelCol.type = $(this).find('.new-model-col-type').val();
    				model.push(modelCol);
    			});
    			dataReplay.model = JSON.stringify(model);
    			$.post('/api/sensors/replay',JSON.stringify(dataReplay), function(data) {
    				console.log(data);
    			},"text");
    			
    			row.remove();
    			if($('.new-replay-form').length==0) {
    				$('#dataUploadDropzone').show();
    				$('#new-replay-btn').click();
    			}
    		});
    		
    		coledit.append(addBtn);
    		
    		row.append(coledit);
    		
    		return row;
    	}
    </script>

</body>

</html>

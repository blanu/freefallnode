var fs=require('fs');
var request = require('request');
var url=require('url');

var base='https://github.com/blanu/embiggen/raw/master/views/';

var runView=function(dbName, data, resp)
{
    var view=require('./'+dbName);
    console.log(view);

    var result=view.map(data);
    console.log('result: '+result.toString());

    resp.write_html(result.toString());
}

var fetchView=function(dbName, data, resp)
{
	fs.open(dbName, 'r', function(err, fd)
	{
	    if(err)
	    {
		request({uri:base+dbName}, function (error, response, body)
		{
		    if (!error && response.statusCode == 200)
		    {
			fs.open(dbName, 'w', function(err, fd)
			{
			    var buff=new Buffer(body.length);
			    buff.write(body);
			    fs.write(fd, buff, 0, buff.length, null, function()
                            {
				fs.close(fd, function() {
				    runView(dbName, data, resp);
				});
			    });	 
   			});
		    }
		})
	    }	    
	    else
	    {
		fs.close(fd, function() {
		    runView(dbName, data, resp);
		});
	    }
	});	
}

var handler = {
    GET: function (dbName)
    { 
	var resp=this.res;

        var dataUrl=url.parse(this.req.url, true).query.data;

	request({uri:dataUrl}, function (error, response, data)
	{
	    if (!error && response.statusCode == 200)
	    {
		console.log('data: '+data);
		fetchView(dbName, JSON.parse(data), resp);
	    }
	});
    }
};

exports.routes = [
    '/:dbName', handler
];

var Request = require("ringo/webapp/request").Request
var logging = require("google/appengine/logging")

exports.app = function (request) {
    return exports[request.method](request);
}

exports.GET = function (request) {
    logging.info("view get...");
    var params = new Request(request).params
    logging.info("params: "+params);

    return {
        status: 200,
        headers: {"Content-Type": "application/json"},
        body: ["{\"key\": \"test\", \"value\": "+JSON.stringify(params)+"}"]
    };
}

exports.POST = function (request) {
    logging.info("view post...");
    var params = new Request(request).params
    logging.info("params: "+params);

    return {
        status: 200,
        headers: {"Content-Type": "application/json"},
        body: ["{\"key\": \"test\", \"value\": "+JSON.stringify(params)+"}"]
    };
}

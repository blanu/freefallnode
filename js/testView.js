var Request = require("ringo/webapp/request").Request
var logging = require("google/appengine/logging")

exports.app = function (request) {
    return exports[request.method](request);
}

exports.POST = function (request) {
    logging.debug("view post...");
    var params = new Request(request).params

    return {
        status: 200,
        headers: {"Content-Type": "application/json"},
        body: ["[1,2,3,7,8,9]"]
    };
}
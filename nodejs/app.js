var nozzle = require('./nozzle');

var viewMap=require('./viewMap');

var viewApp = nozzle.app(viewMap.routes);
nozzle.mount(viewApp, '/viewMap');

nozzle.listen(8080);


// Nozzle - a framework for web apps targeting Node.js
// http://github.com/fictorial/nozzle

var http = require('http'),
    sys = require('sys'),
    url_parse = require('url').parse,
    fs = require('fs'),
    path = require('path'),
    querystring = require('querystring'),
    Buffer = require('buffer').Buffer,
    cookie = require('./vendor/cookie-node'),
    mustache = require('./vendor/mustache').Mustache,
    mime = require('./vendor/mime');

exports.__defineSetter__('cookie_secret', function (value) {
  cookie.secret = value;
});

// We want template loading to be atomic so this does block.  
// Generally, an app loads templates once at startup so this matters little.  
//
// Name your templates like so: 
// 
//     foobar.mu            for a template
//     foobar.partial.mu    for a partial
//
// Note that once a template is loaded, the file is watched and reloaded
// whenever the file changes on disk.
//
// General Note: mustache.js does not memoize/cache templates (cf. haml-js).

var templates = {};
var partials  = {};

exports.configure_templates = function (root, autoreload) {
  if (autoreload === undefined) 
    autoreload = true;

  fs.readdirSync(root).forEach(function (filename) {
    var full_path = root + '/' + filename;

    var stat = fs.statSync(full_path);
    if (!stat.isFile()) return;

    var is_partial = true;
    var template_name = filename.replace('.partial.mu', '');
    if (template_name == filename) {
      is_partial = false;
      template_name = filename.replace('.mu', '');
      if (template_name == filename)
        return;
    }

    function load_template_file() {
      var contents = fs.readFileSync(full_path);
      if (!contents || contents.length == 0) return;
      (is_partial ? partials : templates)[template_name] = contents;
    }
    
    load_template_file(full_path);

    if (autoreload) {
      fs.watchFile(full_path, function (curr, prev) {
        if (curr.mtime.toString() != prev.mtime.toString()) {
          sys.log('template file ' + full_path + ' changed! reloading..');
          load_template_file();
        }
      });
    }
  });
};

exports.render = function (template_name, view, callback) {
  return mustache.to_html(templates[template_name], view, partials, callback);
};

http.ServerResponse.prototype.write_html = function (body, code, headers) {
  var h = headers || {};

  if (!h['Content-Type'])   h['Content-Type']   = 'text/html;charset=utf-8';
  if (!h['Content-Length']) h['Content-Length'] = body.length;

  this.writeHead(code || 200, h);
  this.write(body, 'utf8');
  this.end();
};

http.ServerResponse.prototype.write_json = function (object, code, headers) {
  var json = JSON.stringify(object);

  var h = headers || {};

  if (!h['Content-Type'])   h['Content-Type']   = 'text/html;charset=utf-8';
  if (!h['Content-Length']) h['Content-Length'] = json.length;

  this.writeHead(code || 200, h);
  this.write(json, 'utf8');
  this.end();
};

http.ServerResponse.prototype.write_not_found = function (body, headers) {
  var h = headers || {};

  if (!h['Content-Type'])   h['Content-Type']   = 'text/html;charset=utf-8';
  if (!h['Content-Length']) h['Content-Length'] = body ? body.length : 0;

  this.writeHead(404, h);
  if (body) this.write(body, 'utf8');
  this.end();
};

http.ServerResponse.prototype.redirect = function (location) {
  this.writeHead(302, { Location: location });
  this.end();
};

var apps = {}; 

var server = exports.server = http.createServer(function (request, response) {
  try {
    var url = url_parse(request.url, true),
        prefixes = Object.keys(apps),
        app, 
        prefix; 

    for (var i=0, n=prefixes.length; i<n; ++i) {
      prefix = prefixes[i];
      if (url.pathname.match(new RegExp('^' + prefix))) {
        app = apps[prefix];
        break;
      }
    }

    if (!app) {
      response.write_not_found();
      return;
    }

    if (prefix != '/')
      url.pathname = url.pathname.replace(prefix, '');

    app.handle_request(url, { req: request, res: response });
  } catch (e) {
    sys.error(e.toString());
    response.write_html('<h1>Internal Server Error</h1><pre>' + e.toString() + '</pre>', 500); 
  }
});

function App(routes) {
  this.routes = routes;
}

var no_options = {};

App.prototype.handle_request = function (url, ctx) {
  var pathname = url.pathname,
      req = ctx.req,
      res = ctx.res,
      method = req.method,
      route, match, handler_fn;

  function invoke_handler (captures) {
    var output = handler_fn.apply(ctx, captures);

    switch (typeof output) {
      case 'string': res.write_html(output); break;
      case 'object': res.write_json(output); break;
    }

    // NB: if something else was returned, the handler is in charge 
    // of responding to the client.
  };

  for (var i=0, n=this.routes.length; i<n; ++i) {
    route = this.routes[i];
    match = pathname.match(route.regex),
    handler_fn = route.handler[method];

    if (match && typeof handler_fn == 'function') {
      var captures = match.splice(1, match.length - 1).map(function (x) {
        return x ? unescape(x) : undefined;
      });

      // Load entire PUT/POST bodies and parse as form data or JSON unless 
      // we are told to leave the request alone.  The handler will need to
      // load and parse the request body.

      if ((method == 'POST' || method == 'PUT') && !route.handler[method].unparsed) {
        req.setEncoding('utf8');

        var body = "";
        req.addListener('data', function (chunk) { body += chunk; });

        req.addListener('end', function () {
          switch (req.headers['content-type']) {
            case 'application/x-www-form-urlencoded':
              req.params = querystring.parseQuery(body);
              break;

            case 'application/json':
              try {
                req.params = req.body = JSON.parse(unescape(body));
              } catch (e) {
                res.write_html(e.toString(), 400);
                return;
              }
              break;

            default:
              req.params = req.body = body;
              break;
          }

          invoke_handler(captures);
        });
      } else {
        req.params = url.query;
        invoke_handler(captures);
      }

      return;   
    }
  }

  res.write_not_found();
};

exports.listen = function (port, ip) {
  server.listen(port || 8080, ip || '127.0.0.1');
};

exports.app = function (user_routes) {
  if (!Array.isArray(user_routes) || user_routes.length % 2)
    throw new Error('pass pattern-handler pairs');
  var routes = [];
  for (var i = 0, n = user_routes.length; i < n; i += 2) 
    routes.push(make_route(user_routes[i], user_routes[i + 1]));
  return new App(routes);
};

var named_param_regex = /\/:(\w+)/g;

function make_route(pattern, handler) {
  var route = { handler: handler };
  pattern = pattern.replace(named_param_regex, '(?:/([^\/]+)|/)');
  route.regex = new RegExp('^' + pattern + '$');
  return route;
}

exports.mount = function (an_app, path_prefix) {
  apps[path_prefix] = an_app;
  return exports;
};

function StaticFileServerApp(root) {
  this.root = root;
  this.root_regex = new RegExp('^' + root);
}

StaticFileServerApp.prototype.handle_request = function (url, ctx) {
  var filename = path.normalize(this.root + url.pathname);

  // Stop someone from peeking around outside the root directory via '..'

  if (!this.root_regex.test(filename)) {
    ctx.res.write_html('', 403);
    return;
  }

  var content_type = mime.mime_type_of(filename),
      encoding = /^text/.test(content_type) ? 'utf8' : 'binary';

  // Load the entire file and write it to the client.
  // Er, this is why you want something that uses sendfile(2).
  // Node seems to have it but it's undocumented (on purpose?).

  fs.readFile(filename, encoding, function (err, contents) {
    if (err) {
      ctx.res.write_not_found('failed to read ' + url.pathname);
      return;
    }

    var headers = [ [ 'Content-Type' , content_type ]
                  , [ 'Content-Length', Buffer.byteLength(contents) ]
                  , [ 'Cache-Control', 'public' ]
                  ];

    ctx.res.writeHead(200, headers);
    ctx.res.write(contents, encoding);
    ctx.res.end();
  });
};

exports.static_files_app = function (directory) {
  return new StaticFileServerApp(directory);
};


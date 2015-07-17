var arenaapi = require('./arenaapi.js');

// Parameters could be hard coded, from command line, etc
var url = process.env.ARENA_API_URL;
var email = process.env.ARENA_API_EMAIL;
var password = process.env.ARENA_API_PASSWORD;
var workspaceID = process.env.ARENA_API_WORKSPACEID;
var log = process.env.ARENA_API_LOG;

arenaapi.url = url;
arenaapi.log = '1TtYy'.indexOf(log[0]) != -1;

var args = workspaceID ? {email: email, password: password, workspaceId: workspaceID} : {email: email, password: password};

arenaapi.login({}, args, function(statusCode, errors, result) {
  if (errors == null) {
    arenaapi.getItemCategories({}, function(statusCode, errors, result) {
      console.log(errors || result);
      arenaapi.logout({}, function(statusCode, errors, result) {
      });
    });
  } else
    console.log(errors);
});

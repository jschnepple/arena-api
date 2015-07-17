// allnodep - Make a call to all of the Arena API methods, no external dependencies

var arenaapi = require('./arenaapi');

// Parameters could be hard coded, from command line, etc
var url = process.env.ARENA_API_URL;
var email = process.env.ARENA_API_EMAIL;
var password = process.env.ARENA_API_PASSWORD;
var workspaceID = process.env.ARENA_API_WORKSPACEID;
var log = process.env.ARENA_API_LOG;

var findGUID = function(name, objects) {
  for (var i = 0; i < objects.length; ++i)
    if (objects[i].name == name) return objects[i].guid;
  return null;
};

var aa = arenaapi;
aa.url = url;
aa.log = true;  // this is the only output!

var files;
var assemblies;
var addedFile;
var createdItem;
var fileCategories;
var apiName;
var nfGUID;
var nf;
var categories;
var numberFormat;
var numberFormats;
	
var noErrors = function(errors) {
  if (errors && aa.sessionID != null)
    aa.logout({}, function(statusCode, errors, result) {});
  return errors == null;
};

// define the wrapper functions

var logout = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.logout({}, function(statusCode, errors, result) {});
  }
};

var deleteItem = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.deleteItem({guid: createdItem.guid}, function(statusCode, errors, result) {
      logout(statusCode, errors, result)
    });
  }
};

var deleteItemFile = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.deleteItemFile({guid: createdItem.guid, fileguid: addedFile.guid}, function(statusCode, errors, result) {
      deleteItem(statusCode, errors, result)
    });
  }
};

var getItemRelationships = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemRelationships({guid: assemblies[0].guid}, function(statusCode, errors, result) {
      deleteItemFile(statusCode, errors, result)
    });
  }
};		

var getItemRequirements = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemRequirements({guid: assemblies[0].guid}, function(statusCode, errors, result) {
      getItemRelationships(statusCode, errors, result)
    });
  }
};		

var getItemRevisions = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemRevisions({guid: assemblies[0].guid}, function(statusCode, errors, result) {
      getItemRequirements(statusCode, errors, result)
    });
  }
};		

var getItemFileContent = function(statusCode, errors, result) {
  files = result.results;
  if (noErrors(errors)) {
    aa.getItemFileContent({guid: assemblies[0].guid, fileguid: files[0].guid}, function(statusCode, errors, result) {
      getItemRevisions(statusCode, errors, result)
    });
  }
};

var getItemFiles = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemFiles({guid: assemblies[0].guid}, function(statusCode, errors, result) {
      getItemFileContent(statusCode, errors, result)
    });
  }
};	

var getItemBOM = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemBOM({guid: assemblies[0].guid}, function(statusCode, errors, result) {
      getItemFiles(statusCode, errors, result)
    });
  }
};	

var getItem = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    assemblies = result.results;
    aa.getItem({guid: assemblies[0].guid, includeEmptyAdditionalAttributes: true}, function(statusCode, errors, result) {
      getItemBOM(statusCode, errors, result)
    });
  }
};	

var getItems = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItems({'category.guid': findGUID('Subassembly', categories), limit:10}, function(statusCode, errors, result) {
      getItem(statusCode, errors, result)
    });
  }
};

var getItems2 = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    addedFile = result;
    aa.getItems({'category.guid': findGUID('Cable', categories), limit: 5}, function(statusCode, errors, result) {
      getItems(statusCode, errors, result)
    });
  }
};		

var addItemFile = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.addItemFile({guid: createdItem.guid}, {'category.guid': findGUID('Image', fileCategories), storageMethod: 0, edition: '1', name: 'apiTest.txt', title: 'Just a test file from the API'}, function(statusCode, errors, result) {
      getItems2(statusCode, errors, result)
    });
  }
};	

var updateItem = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    createdItem = result;
    aa.updateItem({guid: createdItem.guid}, {description: 'Updated description'}, function(statusCode, errors, result) {
      addItemFile(statusCode, errors, result)
    });
  }
};	

var createItem = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    fileCategories = result.results;
    apiName = numberFormat.fields[0].apiName;
    nfGUID = numberFormat.guid;
    nf = {guid: nfGUID, fields: [{"apiName": apiName, "value":"APITEST-0001"}]};
    aa.createItem({}, {'category.guid': findGUID('Cable', categories), numberFormat: nf, name: 'API Test ' + new Date().toISOString(), uom: 'each'}, function(statusCode, errors, result) {
      updateItem(statusCode, errors, result)
    });
  }
};

var getFileCategories = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getFileCategories({}, function(statusCode, errors, result) {
      createItem(statusCode, errors, result)
    });
  }
};	

var getItemAttributes = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemAttributes({searchableOnly: true}, function(statusCode, errors, result) {
      getFileCategories(statusCode, errors, result)
    });
  }
};	

var getCategoryAttributes = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    categories = result.results;
    aa.getCategoryAttributes({guid: result.results[result.results.length - 1].guid, includePossibleValues: true}, function(statusCode, errors, result) {
      getItemAttributes(statusCode, errors, result)
    });
  }
};	

var getItemCategories = function(statusCode, errors, result) {
  numberFormat = result;
  if (noErrors(errors)) {
    aa.getItemCategories({}, function(statusCode, errors, result) {
      getCategoryAttributes(statusCode, errors, result)
    });
  }
};

var getItemNumberFormat = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    numberFormats = result.results;
    aa.getItemNumberFormat({guid: findGUID('Basic Item Number', numberFormats)}, function(statusCode, errors, result) {
      getItemCategories(statusCode, errors, result)
    });
  }
};

var getItemNumberFormats = function(statusCode, errors, result) {
  if (noErrors(errors)) {
    aa.getItemNumberFormats({}, function(statusCode, errors, result) {
      getItemNumberFormat(statusCode, errors, result)
    });
  }
};

	
var args = workspaceID ? {email: email, password: password, workspaceId: workspaceID} : {email: email, password: password};

aa.login({}, args, function(statusCode, errors, result) {
  getItemNumberFormats(statusCode, errors, result)
});

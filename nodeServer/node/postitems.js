var arenaapi = require('./arenaapi.js');
console.warn("in postitems.js");
// Parameters could be hard coded, from command line, etc
var url = process.env.ARENA_API_URL;
var email = process.env.ARENA_API_EMAIL;
var password = process.env.ARENA_API_PASSWORD;
var workspaceID = process.env.ARENA_API_WORKSPACEID;
var log = process.env.ARENA_API_LOG;

arenaapi.url = url;
arenaapi.log = '1TtYy'.indexOf(log[0]) != -1;
console.warn("URL = ", url);
handleFileSelect();
var args = workspaceID ? {email: email, password: password, workspaceId: workspaceID} : {email: email, password: password};

arenaapi.login({}, args, function(statusCode, errors, result) {
  if (errors == null) {
    arenaapi.getItems({}, function(statusCode, errors, result) {
      console.log(errors || result);
      arenaapi.logout({}, function(statusCode, errors, result) {
      });
    });
  } else
    console.warn(errors);
});

    // This will parse a delimited string into an array of
    // arrays. The default delimiter is the comma, but this
    // can be overriden in the second argument.
    function CSVToArray( strData, strDelimiter ){
        // Check to see if the delimiter is defined. If not,
        // then default to comma.
        strDelimiter = (strDelimiter || ',');

        // Create a regular expression to parse the CSV values.
        var objPattern = new RegExp(
            (
                // Delimiters.
                "(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +

                // Quoted fields.
                "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +

                // Standard fields.
                "([^\"\\" + strDelimiter + "\\r\\n]*))"
            ),
            "gi"
            );


        // Create an array to hold our data. Give the array
        // a default empty first row.
        var arrData = [[]];

        // Create an array to hold our individual pattern
        // matching groups.
        var arrMatches = null;


        // Keep looping over the regular expression matches
        // until we can no longer find a match.
        while (arrMatches = objPattern.exec( strData )){

            // Get the delimiter that was found.
            var strMatchedDelimiter = arrMatches[ 1 ];

            // Check to see if the given delimiter has a length
            // (is not the start of string) and if it matches
            // field delimiter. If id does not, then we know
            // that this delimiter is a row delimiter.
            if (
                strMatchedDelimiter.length &&
                (strMatchedDelimiter != strDelimiter)
                ){

                // Since we have reached a new row of data,
                // add an empty row to our data array.
                arrData.push( [] );

            }


            // Now that we have our delimiter out of the way,
            // let's check to see which kind of value we
            // captured (quoted or unquoted).
            if (arrMatches[ 2 ]){

                // We found a quoted value. When we capture
                // this value, unescape any double quotes.
                var strMatchedValue = arrMatches[ 2 ].replace(
                    new RegExp( "\"\"", "g" ),
                    "\""
                    );

            } else {

                // We found a non-quoted value.
                var strMatchedValue = arrMatches[ 3 ];

            }


            // Now that we have our value string, let's add
            // it to the data array.
            arrData[ arrData.length - 1 ].push( strMatchedValue );
        }

        // Return the parsed data.
        return( arrData );
    }

 function handleFileSelect(){
  var file = document.getElementById("the_file").files[0];
  var reader = new FileReader();
  var link_reg = /(http:\/\/|https:\/\/)/i;

  reader.onload = function(file) {
    //console.log("RESULTS = ", file.target.result);
    
              var reformat = CSVToArray(String(file.target.result));
              console.log("reformat = ", reformat);
              var content = file.target.result;
              var rows = file.target.result.split(/[\r\n|\n]+/);
              console.log("# rows = ", reformat.length);
              console.log("# columns = ", reformat[0].length);
              var table = document.createElement('table');
              
              for (var i = 0; i < reformat.length; i++){
                var tr = document.createElement('tr');
                var arr = rows[i].split(',');

                for (var j = 0; j < reformat[0].length; j++){
                  if (i==0)
                    var td = document.createElement('th');
                  else
                    var td = document.createElement('td');

                  if( link_reg.test(reformat[i][j]) ){
                    var a = document.createElement('a');
                    a.href = reformat[i][j];
                    a.target = "_blank";
                    a.innerHTML = reformat[i][j];
                    td.appendChild(a);
                  }else{
                    td.innerHTML = reformat[i][j];
                  }
                  tr.appendChild(td);
                }

                table.appendChild(tr);
              }

              document.getElementById('list').appendChild(table);
          };
  reader.readAsText(file);
 }
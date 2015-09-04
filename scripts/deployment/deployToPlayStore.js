/**
 * The script that deploys to Google Play store.
 * Currently it just demonstrates how to use the Request module to access REST APIs for 
 * https://developers.google.com/android-publisher/
 */
var request = require('request');
request('http://www.google.com', function (error, response, body) {
  if (!error && response.statusCode == 200) {
    console.log(body) // Show the HTML for the Google homepage.
  }
})
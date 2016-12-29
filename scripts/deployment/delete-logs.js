/**
 * delete all *.html files
 */
var fs = require('fs');
//var glob = require('glob');

require('glob').glob("*.html", function (err, files) {
	console.log(err);
	console.log(files);
	
	// fs.unlink(files, function (err) {
	// 	if (err) throw err;
	// 	console.log('successfully deleted.');
	// });	
});

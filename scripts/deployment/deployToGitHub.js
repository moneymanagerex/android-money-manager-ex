/**
 * Trying to create a release and upload a file to GitHub.
 * Sample script on how to upload a release to GitHub.
 * Gulp plugin: 
 * https://www.npmjs.com/package/gulp-upload
 * GitHub APIs:
 * https://developer.github.com/v3/repos/releases/#create-a-release
 * 
 */

var oauth_token = "";

// get parameters
var program = require('commander');
program
  .version('0.0.1')
  .option('-t, --token', 'OAuth token')
  .option('-P, --pineapple', 'Add pineapple')
  .option('-b, --bbq-sauce', 'Add bbq sauce')
  .option('-c, --cheese [type]', 'Add the specified type of cheese [marble]', 'marble')
  .parse(process.argv);
if (program.token) {
	oauth_token = program.token;
} else {
	console.log("you must pass token in the -t parameter");
	process.exit(1);
}

// parameters
//var args = process.argv.slice(2);
//console.log(args);


var GitHubApi = require("github");
var github = new GitHubApi({
	version: "3.0.0",
	debug: true,
	protocol: "https"
});

console.log("Authorization");

// Authorize with GitHub
// GET https://github.com/login/oauth/authorize

github.authenticate({
	type: "oauth",
	token: oauth_token
});

// todo: create a release
// POST /repos/:owner/:repo/releases

// todo: get these values from outside:
var version = "2.23.2";
var description = "test only";

var owner = "moneymanagerex";
var repo = "android-money-manager-ex";

github.releases.createRelease({
	owner: owner,
	repo: repo,
	tag_name: version,
	name: version
}, function (err, result) {
	console.log(err);
	console.log(result);
});


// todo: upload the binary files
// POST https://<upload_url>/repos/:owner/:repo/releases/:id/assets?name=foo.zip

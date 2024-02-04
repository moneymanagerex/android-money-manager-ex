/*global gulp */

// include gulp
var gulp = require('gulp');
var rename = require('gulp-rename');

gulp.task('copy', function () {
    console.log("Copy is demonstrating the capability to copy the file to the same directory and rename the file in the process. The output is 'copy.md'.");
    gulp.src('./ReadMe.md')
        .pipe(rename('copy.md'))
        .pipe(gulp.dest('.'));
});

gulp.task('default', function () {
    console.log('Please check ReadMe.md file for instructions or run one of the following commands' +
        ' to execute a task:');
    console.log('copy => copy files')
});

gulp.task('deploy', function () {
    console.log("The deployment script goes here...");
});
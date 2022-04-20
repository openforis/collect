'use strict'

var gulp = require('gulp')
var concat = require('gulp-concat')
const sass = require('gulp-sass')(require('sass'))

function buildStyles() {
  return gulp
    .src('./scss/style.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(concat('style.css'))
    .pipe(gulp.dest('./public/css'))
    .pipe(sass({ outputStyle: 'compressed' }))
    .pipe(concat('style.min.css'))
    .pipe(gulp.dest('./public/css'))
}

exports.buildStyles = buildStyles
exports.watch = function () {
  gulp.watch('./scss/**/*.scss', ['sass'])
}

var exec = cordova.require("cordova/exec");
var PLUGIN_NAME = "PanoramaFunction";

var PanoramaFunction = {
    start: function(onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'start', []);
 }
};

module.exports = PanoramaFunction;
var exec = cordova.require("cordova/exec");
var PLUGIN_NAME = "PanoramaFunction";

var PanoramaFunction = {
    start: function(name, onSuccess, onError) {
    exec(onSuccess, onError, PLUGIN_NAME, 'start', [name]);
 }
};

module.exports = PanoramaFunction;
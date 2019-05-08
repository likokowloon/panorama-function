import Foundation

@objc(PanoramaFunction) class PanoramaFunction : CDVPlugin {
    var pictureUri:String = ""

    @objc(start:) func start(_ command: CDVInvokedUrlCommand) {        
        print("exec from plugin")
        let v : ViewController = ViewController()
        self.viewController.addChild(v)
        self.viewController.view.addSubview(v.view)

        var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: pictureUri)
        commandDelegate.send(pluginResult, callbackId:command.callbackId)
    }
}

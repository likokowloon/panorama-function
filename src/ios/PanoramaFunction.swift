import Foundation

@objc(PanoramaFunction) class PanoramaFunction : CDVPlugin {

    @objc(start:) func start(_ command: CDVInvokedUrlCommand) {        
        print("exec from plugin")
        let v : ViewController = ViewController()
        self.viewController.addChildViewController(v)
        self.viewController.view.addSubview(v.view)
        var path = command.arguments[0] as! String
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: path)  
        commandDelegate.sendPluginResult(pluginResult, callbackId:command.callbackId) 
    }
}

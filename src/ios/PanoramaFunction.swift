import Foundation

@objc(PanoramaFunction) class PanoramaFunction : CDVPlugin {

    @objc(start:) func start(_ command: CDVInvokedUrlCommand) {        
        print("exec from plugin")
        let v : ViewController = ViewController()
        self.viewController.addChildViewController(v)
        self.viewController.view.addSubview(v.view)
    }
}

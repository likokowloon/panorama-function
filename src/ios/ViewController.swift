//
//  ViewController.swift
//  PanoLiteSwift
//
//  Created by Ephrem Beaino on 5/3/17.
//  Copyright © 2017 dermandar. All rights reserved.
//

import UIKit
import AssetsLibrary


class ViewController: UIViewController, MonitorDelegate, UIGestureRecognizerDelegate {
    let TXT_HOLD_DEVICE_VERTICAL:String = NSLocalizedString("Hold your device vertically", comment:"")
    let TXT_MOVE_LEFT_RIGHT:String = NSLocalizedString("Rotate left or right or tap to restart", comment:"")
    let TXT_KEEP_MOVING:String =  NSLocalizedString("Tap to finish when ready or continue rotating", comment:"")
    let TXT_TAP_TO_START:String = NSLocalizedString("Tap anywhere to start", comment:"")
    
    let TMP_DIR:String = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    
    var shooterView : ShooterView? = nil;
    var tapRecognizer : UITapGestureRecognizer? = nil;
    var activityInd : UIActivityIndicatorView? = nil;
    var infoView : PLITInfoView? = nil;
    var numShots : NSInteger? = 0;
    
    override func loadView() {
        Monitor.instance().delegate=self;
        
        var frame:CGRect = UIScreen.main.bounds
        if(!UIApplication.shared.isStatusBarHidden){
            frame.origin.y=UIApplication.shared.statusBarFrame.size.height
            frame.size.height-=UIApplication.shared.statusBarFrame.size.height
        }
        let aView:UIView = UIView.init(frame: frame)
        aView.backgroundColor=UIColor.black
        aView.autoresizingMask=[.flexibleWidth, .flexibleHeight]
        self.view=aView
        
        self.shooterView = ShooterView.init(frame: frame)
        aView.addSubview(shooterView!);
        
        var infoViewFrame:CGRect;
        var infoViewSubFrame:CGRect;
        if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiom.phone){
            infoViewFrame = CGRect(x: 15.0, y: (frame.size.height-100.0)/2.0, width: frame.size.width-30.0, height: 100.0);
            infoViewSubFrame = (infoViewFrame.insetBy(dx: 35.0,dy: 0.0)).offsetBy(dx: 35.0,dy: frame.size.height-infoViewFrame.origin.y-infoViewFrame.size.height-20.0)
        }else{
            infoViewFrame=CGRect(x: frame.size.width/2.0-200.0, y: (frame.size.height-100.0)/2.0, width: 400.0, height: 100.0)
            infoViewSubFrame=infoViewFrame.offsetBy(dx: 0.0, dy: frame.size.height-infoViewFrame.origin.y-infoViewFrame.size.height-20.0)
        }
        
        self.tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(ViewController.userTapped))
        tapRecognizer?.numberOfTapsRequired=1;
        tapRecognizer?.delegate=self
        shooterView?.addGestureRecognizer(tapRecognizer!)
        
        self.activityInd = UIActivityIndicatorView(style: .whiteLarge)
        activityInd?.hidesWhenStopped=true
        activityInd?.stopAnimating()
        activityInd?.center=aView.center
        aView.addSubview(activityInd!)
        
        self.infoView = PLITInfoView(frame: infoViewFrame)
        infoView?.setSubFrame(infoViewSubFrame)
        infoView?.setText(TXT_HOLD_DEVICE_VERTICAL)
        shooterView?.insertSubview(infoView!, at: 1)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        shooterView?.isHidden=false
        self.restart(nil)
        NotificationCenter.default.addObserver(self, selector: #selector(ViewController.willEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
        super.viewDidAppear(animated)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        shooterView?.isHidden=true
        NotificationCenter.default.removeObserver(self)
        super.viewDidDisappear(animated)
    }
    
    @objc func willEnterForeground(_ notification : NSNotification){
        shooterView?.isHidden=false
        self.restart(nil)
    }

    @objc func userTapped(gs: UIGestureRecognizer){
        if(numShots==1){
            self.restart(gs)
        }else if(numShots!>1){
            self.stop(gs)
        }else{
            self.start(gs)
        }
    }
    
    func start(_ sender : Any?){
        Monitor.instance().startShooting()
        self.numShots=0;
        
        infoView?.setText(TXT_MOVE_LEFT_RIGHT)
        infoView?.switchToSubFrame()
    }
    func restart(_ sender : Any?){
        Monitor.instance().restart()
        self.numShots = -1
    }
    func stop(_ sender : Any?){
        if(numShots!>1){
            Monitor.instance().finishShooting()
        }
    }
    
    func takingPhoto() {
        let camView : UIView = self.view
        camView.backgroundColor=UIColor.white
        camView.alpha=0.1;
        UIView.animate(withDuration: 0.6, animations: { 
            camView.alpha = 1.0
        }) { (_ finished:Bool) in
            self.view.window?.backgroundColor=UIColor.white;
        }
    }
    
    func photoTaken() {
        numShots!+=1;
        if(numShots!==2){
            infoView?.setText(TXT_KEEP_MOVING);
        }
    }
    
    func shootingCompleted() {
        shooterView?.isHidden=true
        activityInd?.startAnimating()
    }
    
    func stitchingCompleted(_ dict: [AnyHashable : Any]!) {
        self.savePanorama()
    }
    
    func deviceVerticalityChanged(_ isVertical: NSNumber!) {
        tapRecognizer?.isEnabled=isVertical.boolValue
        if(isVertical.boolValue){
            if(!Monitor.instance().isShooting){
                infoView?.setText(TXT_TAP_TO_START)
                infoView?.switchToMainFrame()
            }else{
                if(numShots==1){
                    infoView?.setText(TXT_MOVE_LEFT_RIGHT)
                }else{
                    infoView?.setText(TXT_KEEP_MOVING)
                }
                infoView?.switchToSubFrame()
            }
        }else{
            infoView?.setText(TXT_HOLD_DEVICE_VERTICAL)
            infoView?.switchToMainFrame()
        }
    }
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        var array:[UIView]! = shooterView?.flashControls as! [UIView]
        for i in 0..<array.count{
            let v:UIView = array![i]
            if(v==touch.view){
                return false
            }
        }
        array = shooterView?.exposureControls as! [UIView]
        for i in 0..<array.count{
            let v:UIView = array![i]
            if(v==touch.view){
                return false
            }
        }
        return true;
    }
    
    func savePanorama(){
        try! FileManager.default.createDirectory(atPath: TMP_DIR, withIntermediateDirectories: true, attributes: nil)
        let ename:String = TMP_DIR.appending("/equi.jpeg")
        Monitor.instance().genEqui(at: ename, withHeight: 800, andWidth: 0, andMaxWidth: 0)
        
        let library:ALAssetsLibrary = ALAssetsLibrary();
        library.writeImageData(toSavedPhotosAlbum: NSData(contentsOfFile: ename) as Data?, metadata: nil) { (url : URL?,error : Error?) in
            if(url != nil){
                UIAlertView(title: nil, message: "Image saved to camera roll", delegate: nil, cancelButtonTitle: "OK").show()
            }else if(error != nil){
                let errorNS = error! as NSError
                if(errorNS.code == ALAssetsLibraryAccessUserDeniedError || errorNS.code == ALAssetsLibraryAccessGloballyDeniedError){
                    UIAlertView(title: "Error", message: "Permission needed to access camera roll", delegate: nil, cancelButtonTitle: "OK").show()
                }
            }
            
            self.shooterView?.isHidden=false
            self.activityInd?.stopAnimating()
            self.restart(nil)
        }
    }
    override var supportedInterfaceOrientations : UIInterfaceOrientationMask {
        get { return UIInterfaceOrientationMask.portrait }
    }
    override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation{
        get{return UIInterfaceOrientation.portrait}
    }
    override var shouldAutorotate: Bool{
        get{return false}
    }

}


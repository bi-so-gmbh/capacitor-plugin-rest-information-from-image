import Foundation
import AVFAudio
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(RestInformationPlugin)
public class RestInformationPlugin: CAPPlugin, CameraViewControllerDelegate {
    public static let SETTINGS = "settings"
    public static let REQUEST = "request"
    private var call: CAPPluginCall?
    private var settings: ScannerSettings!
    private var httpRequest: HttpRequest!
    private var player: AVAudioPlayer?
    
    @objc func scan(_ call: CAPPluginCall) {
        // TODO: get settings and request
        let options = call.jsObjectRepresentation
        self.call = call
        
        if (options.isEmpty || !options.keys.contains(RestInformationPlugin.REQUEST)) {
            call.reject(ErrorMessages.REQUIRED_DATA_MISSING)
            return
        }
        
        settings = ScannerSettings(options: options[RestInformationPlugin.SETTINGS] as? [String:Any])
        
        guard let scanRequest = options[RestInformationPlugin.REQUEST] as? [String:Any]
        else {
            call.reject(ErrorMessages.REQUEST_INVALID)
            return
        }
        httpRequest = HttpRequest(request: scanRequest)
        if (httpRequest == nil) {
            call.reject(ErrorMessages.INVALID_URL)
            return
        }
        
        DispatchQueue.main.async {
            let cameraViewController = CameraViewController(settings: self.settings, request: self.httpRequest)
            cameraViewController.delegate = self
            self.bridge!.viewController!.present(cameraViewController, animated: true)
        }
    }
    
    func onComplete(_ result: [String: Any]) {
        weak var weakSelf = self
        DispatchQueue.main.sync {
            guard weakSelf != nil else {
                return
            }
            self.bridge!.viewController!.dismiss(animated: true)
        }
        if (result.isEmpty) {
            self.call!.reject(ErrorMessages.EMPTY_RESPONSE)
            return
        }
        
        if (!result.keys.contains(ImageCaptureListener.ERROR)) {
            if (settings.vibrateOnSuccess) {
                AudioServicesPlayAlertSoundWithCompletion(SystemSoundID(kSystemSoundID_Vibrate)) { }
            }
            if (settings.beepOnSuccess) {
                if let audioData = NSDataAsset(name: "beep")?.data {
                    do {
                        try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
                        try AVAudioSession.sharedInstance().setActive(true)
                        
                        player = try AVAudioPlayer(data: audioData)
                        
                        if let unwrappedPlayer = player {
                            unwrappedPlayer.play()
                        }
                    } catch let error {
                        print(error.localizedDescription)
                    }
                }
            }
        }
        
        if (result.keys.contains(ImageCaptureListener.STATUS) && result[ImageCaptureListener.STATUS] as! Int == 200) {
            self.call!.resolve(result)
        } else if (result.keys.contains(ImageCaptureListener.ERROR)) {
            if (result.keys.contains(ImageCaptureListener.STATUS)) {
                self.call!.reject(result[ImageCaptureListener.ERROR] as! String, result[ImageCaptureListener.STATUS] as? String)
            } else {
                self.call!.reject(result[ImageCaptureListener.ERROR] as! String)
            }
        } else {
            self.call!.reject(ErrorMessages.UNKNOWN_ERROR)
        }
    }
    
    func onError(_ error: String) {
        self.bridge!.viewController!.dismiss(animated: false)
        self.call!.reject(error)
    }
}

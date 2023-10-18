import Foundation
import AVFAudio
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(RestInformationPlugin)
public class RestInformationPlugin: CAPPlugin, CameraViewControllerDelegate {
    private var call: CAPPluginCall?
    private var settings: ScannerSettings!
    private var httpRequest: HttpRequest!
    private var player: AVAudioPlayer?

    @objc func scan(_ call: CAPPluginCall) {
        // TODO: get settings and request
        let options = call.jsObjectRepresentation
        self.call = call
        
        settings = ScannerSettings(options: options["settings"] as! [String : Any])
        httpRequest = HttpRequest(request: options["request"] as! [String:Any])
        
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
            self.call!.reject("EMPTY")
            return
        }
        
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
        
        if (result.keys.contains("status") && result["status"] as! Int == 200) {
            self.call!.resolve(result)
        } else {
            if(result.keys.contains("status")) {
                self.call!.reject(result["error"] as! String, result["status"] as? String)
            } else {
                self.call!.reject(result["error"] as! String)
            }
        }
    }
    
    func onError(_ error: String) {
        self.bridge!.viewController!.dismiss(animated: false)
        self.call!.reject(error)
    }
}

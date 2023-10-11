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
        print(options)
        self.call = call
        
        settings = ScannerSettings(options: options["settings"] as! [String : Any])
        httpRequest = HttpRequest(request: options["request"] as! [String:Any])
        
        print(httpRequest!)
        
        DispatchQueue.main.async {
            let cameraViewController = CameraViewController(settings: self.settings, request: self.httpRequest)
            cameraViewController.delegate = self
            self.bridge!.viewController!.present(cameraViewController, animated: true)
        }
    }

    // TODO: update for serial
    func onComplete(_ result: [String: Any]) {
        weak var weakSelf = self
        DispatchQueue.main.sync {
            guard weakSelf != nil else {
                return
            }
            self.bridge!.viewController!.dismiss(animated: true)
        }
        if (result.isEmpty) {
            self.call!.reject("NO_BARCODE")
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
        var output = JSObject()
        //output.updateValue(resultBarcodes, forKey: "barcodes")
        self.call!.resolve(output)
    }
    
    func onError(_ error: String) {
        print("error occurred")
        self.bridge!.viewController!.dismiss(animated: false)
        print("controller dismissed")
        self.call!.reject(error)
    }
}

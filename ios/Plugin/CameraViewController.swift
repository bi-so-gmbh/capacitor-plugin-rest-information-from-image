import UIKit
import SwiftUI
import AVFoundation

protocol CameraViewControllerDelegate: NSObjectProtocol {
    func onComplete(_ result: [String: Any])
    func onError(_ error: String)
}

class CameraViewController: UIViewController, RestDataListener {
    weak var delegate: CameraViewControllerDelegate?
    var captureDelegate : AVCapturePhotoCaptureDelegate?
    private let captureSession = AVCaptureSession()
    private let sessionQueue = DispatchQueue(label: "sessionQueue")
    private var previewLayer = AVCaptureVideoPreviewLayer()
    private var settings: ScannerSettings
    private var httpRequest: HttpRequest
    private var cameraOverlay: CameraOverlay!
    private var torchButton: UIButton?
    private var finishedAlready: Bool = false // ensure we only actually finish once
    private var spinner: UIActivityIndicatorView?
    private let stillImageOutput = AVCapturePhotoOutput()
    
    func onRestData(_ data: [String: Any]) {
        finishWithResult(data)
    }
    
    init(settings: ScannerSettings, request: HttpRequest) {
        self.settings = settings
        self.httpRequest = request
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        self.settings = ScannerSettings()
        self.httpRequest = HttpRequest()
        super.init(coder: coder)
    }
    
    override func viewDidLoad() {
        cameraOverlay = CameraOverlay(settings: settings, parentView: view)
        //barcodeAnalyzer = BarcodeAnalyzer(settings: settings, barcodesListener: self, cameraOverlay: cameraOverlay)
        
        if AVCaptureDevice.authorizationStatus(for: .video) == .authorized {
            setupCaptureSession()
            setupUI()
            setOrientation()
        } else {
            AVCaptureDevice.requestAccess(for: .video, completionHandler: { (authorized) in
                DispatchQueue.main.async {
                    if authorized {
                        self.setupCaptureSession()
                        self.setupUI()
                        self.setOrientation()
                    } else {
                        self.finishWithError("NO_CAMERA_PERMISSION")
                    }
                }
            })
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        startSession()
        // slight delay so the camera has time to load before we show the modal
        while (!captureSession.isRunning) {
            usleep(100)
        }
        usleep(150000)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        stopSession()
    }
    
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)
        setOrientation()
    }
    
    private func setOrientation() {
        previewLayer.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        
        switch UIDevice.current.orientation {
        case UIDeviceOrientation.portraitUpsideDown:
            previewLayer.connection?.videoOrientation = .portraitUpsideDown
        case UIDeviceOrientation.landscapeLeft:
            previewLayer.connection?.videoOrientation = .landscapeRight
        case UIDeviceOrientation.landscapeRight:
            previewLayer.connection?.videoOrientation = .landscapeLeft
        case UIDeviceOrientation.portrait:
            previewLayer.connection?.videoOrientation = .portrait
        default:
            break
        }
    }
    
    private func setupCaptureSession() {
        guard let videoDevice = captureDevice() else {
            finishWithError("NO_CAMERA")
            return
            
        }
        guard let videoDeviceInput = try? AVCaptureDeviceInput(device: videoDevice) else {
            finishWithError("NO_CAMERA")
            return
        }
        guard captureSession.canAddInput(videoDeviceInput) else { return }
        
        captureSession.addInput(videoDeviceInput)
        
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.backgroundColor = UIColor.black.cgColor
        previewLayer.frame = view.bounds
        previewLayer.videoGravity = AVLayerVideoGravity.resizeAspectFill
        
        previewLayer.connection?.videoOrientation = .portrait
        
        guard captureSession.canAddOutput(stillImageOutput) else {
            finishWithError("CANT_TAKE_PICTURE")
            return
        }
        captureSession.addOutput(stillImageOutput)
        
    }
    
    private func setupUI() {
        view.layer.addSublayer(previewLayer)
        cameraOverlay.setPreviewLayer(previewLayer)
        view.bringSubviewToFront(cameraOverlay)
        
        torchButton = createTorchButton()
        let gesture = UITapGestureRecognizer(target: self, action:  #selector (self.onTouch (_:)))
        view.addGestureRecognizer(gesture)
        
        spinner = UIActivityIndicatorView(style: .large)
        spinner!.translatesAutoresizingMaskIntoConstraints = false
        spinner!.startAnimating()
        spinner!.isHidden = true
        spinner!.color = settings.loadingCircleUIColor
        view.addSubview(spinner!)
        
        spinner!.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        spinner!.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
    }
    
    @objc func onTouch(_ sender:UITapGestureRecognizer){
        spinner!.isHidden = false
        let photoSettings : AVCapturePhotoSettings = AVCapturePhotoSettings(format: [AVVideoCodecKey:AVVideoCodecType.jpeg])

        captureDelegate = ImageCaptureListener(httpRequest: httpRequest, restDataListener: self)
        self.stillImageOutput.capturePhoto(with: photoSettings, delegate: captureDelegate!)
    }
    
    private func createTorchButton() -> UIButton? {
        if let device = AVCaptureDevice.default(for: AVMediaType.video) {
            if device.hasTorch {
                let torchButton = UIButton(type: UIButton.ButtonType.custom)
                torchButton.backgroundColor = UIColor.white
                torchButton.layer.cornerRadius = 25
                torchButton.imageEdgeInsets = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
                torchButton.tintColor = UIColor.black
                torchButton.alpha = 0.5
                torchButton.addTarget(self, action: #selector(toggleFlash), for: UIControl.Event.touchUpInside)
                if let image = UIImage(named: "flashlight")
                {
                    torchButton.setImage(image, for: UIControl.State.normal)
                }
                view.addSubview(torchButton)
                torchButton.translatesAutoresizingMaskIntoConstraints = false
                NSLayoutConstraint.activate([
                    torchButton.bottomAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.bottomAnchor, constant: -20),
                    torchButton.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -20),
                    torchButton.widthAnchor.constraint(equalToConstant: 50),
                    torchButton.heightAnchor.constraint(equalToConstant: 50)
                ])
                return torchButton
            }
        }
        return nil
    }
    
    @objc
    private func toggleFlash() {
        guard let device = AVCaptureDevice.default(for: AVMediaType.video) else { return }
        guard device.hasTorch else { print("Torch isn't available"); return }
        
        do {
            try device.lockForConfiguration()
            
            if (device.torchMode == AVCaptureDevice.TorchMode.on) {
                device.torchMode = AVCaptureDevice.TorchMode.off
                torchButton?.alpha = 0.5
            } else {
                do {
                    try device.setTorchModeOn(level: 1.0)
                    torchButton?.alpha = 1
                } catch {
                    print(error)
                }
            }
            
            device.unlockForConfiguration()
        } catch {
            print(error)
        }
    }
    
    private func captureDevice() -> AVCaptureDevice? {
        if #available(iOS 10.0, *) {
            let discoverySession = AVCaptureDevice.DiscoverySession(
                deviceTypes: [.builtInWideAngleCamera],
                mediaType: .video,
                position: .unspecified
            )
            return discoverySession.devices.first { $0.position == .back }
        }
        return nil
    }
    
    private func startSession() {
        weak var weakSelf = self
        sessionQueue.async {
            guard let strongSelf = weakSelf else {return}
            strongSelf.captureSession.startRunning()
        }
    }
    
    private func stopSession() {
        weak var weakSelf = self
        sessionQueue.async {
            guard let strongSelf = weakSelf else {return}
            strongSelf.captureSession.stopRunning()
        }
    }
    
    private func finishWithError(_ error: String) {
        if (!finishedAlready) {
            finishedAlready = true
            delegate?.onError(error)
        }
    }
    
    private func finishWithResult(_ result: [String: Any]){
        if (!finishedAlready) {
            finishedAlready = true
            delegate?.onComplete(result)
        }
    }
}

import AVFoundation
import CoreVideo
import UIKit

class CameraOverlay: UIView {
    private(set) var scanArea: CGRect
    private var settings: ScannerSettings
    private(set) var previewLayer: AVCaptureVideoPreviewLayer!

    init(settings: ScannerSettings, parentView: UIView) {
        self.scanArea = Utils.calculateCGRect(height: parentView.bounds.height, width: parentView.bounds.width, scaleFactor: settings.detectorSize, aspectRatio: settings.aspectRatioF)
        self.settings = settings

        super.init(frame: .zero)

        parentView.addSubview(self)
        NSLayoutConstraint.activate([
            self.topAnchor.constraint(equalTo: parentView.topAnchor),
            self.leadingAnchor.constraint(equalTo: parentView.leadingAnchor),
            self.trailingAnchor.constraint(equalTo: parentView.trailingAnchor),
            self.bottomAnchor.constraint(equalTo: parentView.bottomAnchor)
        ])

        self.translatesAutoresizingMaskIntoConstraints = false
        self.contentMode = UIView.ContentMode.scaleAspectFill
        self.isOpaque = false
        self.backgroundColor = UIColor.clear
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        self.setNeedsDisplay()
    }

    override func draw(_ rect: CGRect) {
        super.draw(rect)
        self.scanArea = Utils.calculateCGRect(height: bounds.height, width: bounds.width, scaleFactor: settings.detectorSize, aspectRatio: settings.aspectRatioF)
        if let context = UIGraphicsGetCurrentContext() {
            drawScanArea(context: context)
        }
    }

    func setPreviewLayer(_ previewLayer: AVCaptureVideoPreviewLayer) {
        self.previewLayer = previewLayer
    }

    private func drawScanArea(context: CGContext) {
        if settings.drawFocusBackground {
            drawFocusBackground(context: context, color: settings.focusBackgroundUIColor, radius: settings.focusRectBorderRadius)
        }
        if settings.drawFocusLine {
            drawFocusLine(context: context, color: settings.focusLineUIColor, thickness: settings.focusLineThickness)
        }
        if settings.drawFocusRect {
            drawScanAreaOutline(context: context, color: settings.focusRectUIColor, thickness: settings.focusRectBorderThickness, radius: settings.focusRectBorderRadius)
        }
    }

    private func drawFocusLine(context: CGContext, color: UIColor, thickness: Int) {
        context.setLineWidth(CGFloat(thickness))
        context.setStrokeColor(color.cgColor)
        context.beginPath()
        context.move(to: CGPoint(x: scanArea.minX, y: CGFloat(bounds.height/2)))
        context.addLine(to: CGPoint(x: scanArea.maxX, y: CGFloat(bounds.height/2)))
        context.strokePath()
        context.saveGState()
    }

    private func drawScanAreaOutline(context: CGContext, color: UIColor, thickness: Int, radius: Int) {
        let rounded = UIBezierPath(roundedRect: scanArea, cornerRadius: CGFloat(radius))
        context.addPath(rounded.cgPath)
        context.setLineWidth(CGFloat(thickness))
        context.setStrokeColor(color.cgColor)
        context.strokePath()
        context.saveGState()
    }

    private func drawFocusBackground(context: CGContext, color: UIColor, radius: Int) {
        let rounded = UIBezierPath(roundedRect: scanArea, cornerRadius: CGFloat(radius))
        color.setFill()
        context.fill(bounds)
        context.saveGState()
        context.setBlendMode(.destinationOut)
        context.addPath(rounded.cgPath)
        context.fillPath()
        context.saveGState()
        context.setBlendMode(.normal)
    }
}

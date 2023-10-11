import AVFoundation
import UIKit

class ImageCaptureListener: NSObject, AVCapturePhotoCaptureDelegate {
    
    let httpRequest: HttpRequest
    
    init(httpRequest: HttpRequest) {
        print("imageCaptureListener init")
        self.httpRequest = httpRequest
    }
    
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        print("got a picture")
        if let error {
            print("Error processing photo: \(error.localizedDescription)")
            return
        }
        guard let imageData = photo.fileDataRepresentation() else { return }
        
        
        let image = UIImage(data: imageData)
        let base64 = image?.jpegData(compressionQuality: 0.75)?.base64EncodedString() ?? "error"
        print(base64.prefix(100))
        
        var request = URLRequest(url: httpRequest.url)
        request.httpMethod = "POST"
        for (key, value) in httpRequest.headers {
            request.setValue(value, forHTTPHeaderField: key)
        }
        
        var body = httpRequest.body
        body[httpRequest.base64Key] = base64
        body[httpRequest.imageTypeKey] = "jpeg"
        
        let bodyData = try? JSONSerialization.data(
            withJSONObject: body,
            options: []
        )
        print(bodyData!)
        
        request.httpBody = bodyData!
        
        // Create the HTTP request
        let session = URLSession.shared
        let task = session.dataTask(with: request) { (data, response, error) in

            if let error = error {
                print(error.localizedDescription)
            } else if let data = data {
                print(data)
            } else {
                print("unexpected error")
            }
        }
        
        task.resume()
    }
}

import AVFoundation
import UIKit

class ImageCaptureListener: NSObject, AVCapturePhotoCaptureDelegate {
    public static let ERROR = "error"
    public static let STATUS = "status"
    let httpRequest: HttpRequest
    let restDataListener : RestDataListener
    var runningTask : Task<Void, Never>?
    
    init(httpRequest: HttpRequest, restDataListener: RestDataListener) {
        self.httpRequest = httpRequest
        self.restDataListener = restDataListener
    }
    
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        if error != nil {
            self.restDataListener.onRestData([ImageCaptureListener.ERROR:ErrorMessages.CAMERA_ERROR])
            return
        }
        guard let imageData = photo.fileDataRepresentation() else {
            self.restDataListener.onRestData([ImageCaptureListener.ERROR:ErrorMessages.CAMERA_ERROR])
            return
        }
        
        var image = UIImage(data: imageData)
        image = Utils.rotateImage(image: image!, orientation: Utils.imageOrientation(fromDevicePosition: .back))
        if let base64 = image?.jpegData(compressionQuality: 0.75)?.base64EncodedString() {
            runningTask = Task {
                let result = await doPOSTRequest(base64Image: base64)
                self.restDataListener.onRestData(result)
            }
        } else {
            self.restDataListener.onRestData([ImageCaptureListener.ERROR:ErrorMessages.CAMERA_ERROR])
        }
    }
    
    private func doPOSTRequest(base64Image:String) async -> [String:Any] {
        var request = URLRequest(url: httpRequest.url)
        request.httpMethod = "POST"
        for (key, value) in httpRequest.headers {
            request.setValue(value, forHTTPHeaderField: key)
        }
        
        var body = httpRequest.body
        body[httpRequest.base64Key] = base64Image
        body[httpRequest.imageTypeKey] = "jpeg"
        
        guard let bodyData = try? JSONSerialization.data(
            withJSONObject: body,
            options: []
        ) else {
            return [ImageCaptureListener.ERROR:ErrorMessages.JSON_ERROR]
        }
        
        request.httpBody = bodyData
        
        var result:[String:Any] = [:]
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode)
            else {
                return responseToJson(data: data)
            }
            result = responseToJson(data: data)
            result[ImageCaptureListener.STATUS] = 200
        } catch let error {
            if (error.localizedDescription == "cancelled") {
                result[ImageCaptureListener.ERROR] = ErrorMessages.CANCELLED
            } else {
                result[ImageCaptureListener.ERROR] = error.localizedDescription
                print("Post Request Error: \(error.localizedDescription)")
            }
        }
        
        return result
    }
    
    private func responseToJson(data:Data?) -> [String:Any] {
        guard let responseData = data else {
            print("nil Data received from the server")
            return [ImageCaptureListener.ERROR: ErrorMessages.EMPTY_RESPONSE]
        }
        
        do {
            if let jsonResponse = try JSONSerialization.jsonObject(with: responseData, options: .mutableContainers) as? [String: Any] {
                return jsonResponse
            } else {
                print("data maybe corrupted or in wrong format")
                return [ImageCaptureListener.ERROR:ErrorMessages.JSON_ERROR]
            }
        } catch let error {
            print(error.localizedDescription)
            return [ImageCaptureListener.ERROR:ErrorMessages.JSON_ERROR]
        }
    }
}

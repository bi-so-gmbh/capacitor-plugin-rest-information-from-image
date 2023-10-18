import AVFoundation
import UIKit

class ImageCaptureListener: NSObject, AVCapturePhotoCaptureDelegate {
    
    let httpRequest: HttpRequest
    let restDataListener : RestDataListener
    
    init(httpRequest: HttpRequest, restDataListener: RestDataListener) {
        self.httpRequest = httpRequest
        self.restDataListener = restDataListener
    }
    
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        if let error {
            print("Error processing photo: \(error.localizedDescription)")
            return
        }
        guard let imageData = photo.fileDataRepresentation() else { return }
        
        var image = UIImage(data: imageData)
        image = Utils.rotateImage(image: image!, orientation: Utils.imageOrientation(fromDevicePosition: .back))
        let base64 = image!.jpegData(compressionQuality: 0.75)?.base64EncodedString() ?? "error"
        
        Task.init {
            let result = await doPOSTRequest(base64Image: base64)
            self.restDataListener.onRestData(result)
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
        
        let bodyData = try? JSONSerialization.data(
            withJSONObject: body,
            options: []
        )
        
        request.httpBody = bodyData!
        
        var result:[String:Any] = [:]
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode)
            else {
                print("Invalid Response received from the server")
                return responseToJson(data: data)
            }
            result = responseToJson(data: data)
            result["status"] = 200
        } catch let error {
            result["error"] = error.localizedDescription
            print("Post Request Error: \(error.localizedDescription)")
        }
        
        return result
    }
    
    private func responseToJson(data:Data?) -> [String:Any] {
        guard let responseData = data else {
            print("nil Data received from the server")
            return ["error": "no response"]
        }
        
        do {
            if let jsonResponse = try JSONSerialization.jsonObject(with: responseData, options: .mutableContainers) as? [String: Any] {
                return jsonResponse
            } else {
                print("data maybe corrupted or in wrong format")
                return ["error":"response unreadable"]
            }
        } catch let error {
            print(error.localizedDescription)
            return ["error":error.localizedDescription]
        }
    }
}

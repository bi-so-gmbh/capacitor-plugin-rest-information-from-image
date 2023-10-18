import Foundation
class HttpRequest: CustomDebugStringConvertible {
    var debugDescription: String {
        var des: String = "\(type(of: self)) {"
        for child in Mirror(reflecting: self).children {
            if let propName = child.label {
                des += "\n\t\(propName): \(child.value)"
            }
        }
        
        return des + "\n}"
    }
    
    public private(set) var url: URL
    public private(set) var headers: [String:String]
    public private(set) var body: [String:Any]
    public private(set) var base64Key: String = "fileBase64"
    public private(set) var imageTypeKey: String = "imageType"
    
    init?(request:[String:Any]) {
        guard let urlString = request["url"] as? String else {return nil}
        guard let url = URL(string: urlString) else {return nil}
        self.url = url
        
        if let headers = request["headers"] as? [String:String] {
            self.headers = headers
        } else {
            self.headers = [:]
        }
        if let body = request["body"] as? [String:Any] {
            self.body = body
        } else {
            self.body = [:]
        }

        if let temp = request["base64Key"] as? String {
            base64Key = temp
        }
        if let temp = request["imageTypeKey"] as? String {
            imageTypeKey = temp
        }
    }
}

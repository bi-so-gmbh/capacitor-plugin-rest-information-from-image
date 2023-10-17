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
    
    init(request:[String:Any]) {
        url = URL(string: request["url"] as! String)!
        headers = request["headers"] as! [String:String]
        body = request["body"] as! [String:Any]
        if let temp = request["base64Key"] as? String {
            base64Key = temp
        }
        if let temp = request["imageTypeKey"] as? String {
            imageTypeKey = temp
        }
    }
    
    convenience init() {
        self.init(request: [:])
    }
}

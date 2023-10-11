import ObjectiveC

protocol RestDataListener: NSObjectProtocol {
    func onRestData(_ data: [String: Any])
}


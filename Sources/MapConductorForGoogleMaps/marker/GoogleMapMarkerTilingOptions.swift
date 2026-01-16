import Foundation

public struct GoogleMapMarkerTilingOptions: Equatable, Hashable, Sendable {
    public var enabled: Bool
    public var minMarkerCount: Int

    public init(
        enabled: Bool = true,
        minMarkerCount: Int = 2000
    ) {
        self.enabled = enabled
        self.minMarkerCount = minMarkerCount
    }

    public static let disabled: GoogleMapMarkerTilingOptions = .init(enabled: false)
}


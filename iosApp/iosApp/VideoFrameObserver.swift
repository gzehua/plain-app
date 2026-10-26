import AVFoundation
import PlainShared

final class VideoFrameObserver: NSObject, IosVideoFrameObserver {
    func observe(layer: AVPlayerLayer, onReady: @escaping () -> Void) -> any IosVideoFrameObservation {
        VideoFrameObservation(layer: layer, onReady: onReady)
    }
}

private final class VideoFrameObservation: NSObject, IosVideoFrameObservation {
    private var observation: NSKeyValueObservation?

    init(layer: AVPlayerLayer, onReady: @escaping () -> Void) {
        super.init()
        observation = layer.observe(\.isReadyForDisplay, options: [.initial, .new]) { layer, _ in
            if layer.isReadyForDisplay { onReady() }
        }
    }

    func close() {
        observation?.invalidate()
        observation = nil
    }
}

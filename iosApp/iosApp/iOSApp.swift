import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
  private nonisolated let backgroundManager = BackgroundManager()
  
  var body: some Scene {
    WindowGroup {
      ContentView()
    }
    .backgroundTask(.appRefresh("com.example.community_app.refresh")) {
      await withCheckedContinuation { continuation in
        backgroundManager.performBackgroundFetch { _ in
          continuation.resume()
        }
      }
    }
  }
}

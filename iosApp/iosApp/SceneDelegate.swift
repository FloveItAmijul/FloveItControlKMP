//
//  SceneDelegate.swift
//  iosApp
//
//  Created by Debashis Das on 08/05/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit
import SwiftUI

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene,
               willConnectTo session: UISceneSession,
               options connectionOptions: UIScene.ConnectionOptions) {

        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)

        let hostingController = TransparentHostingController(rootView: ContentView())

        // Transparent background + extended layout
        hostingController.view.backgroundColor = .clear
        hostingController.edgesForExtendedLayout = [.all]
        hostingController.modalPresentationCapturesStatusBarAppearance = true

        window.rootViewController = hostingController
        self.window = window
        window.makeKeyAndVisible()
    }

}

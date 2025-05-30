//
//  TransparentHostingController.swift
//  iosApp
//
//  Created by Debashis Das on 08/05/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

class TransparentHostingController<Content: View>: UIHostingController<Content> {
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent // Match your Android light/dark style
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
    }
}

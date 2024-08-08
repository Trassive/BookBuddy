package com.example.bookbuddy.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.FragmentState
import org.readium.r2.navigator.epub.EpubDefaults
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun LibraryFragmentWrapper(
    fragmentFactory: FragmentFactory,
    createFragment: () -> Fragment,
    publication: Publication,
    initialLocator: Locator?,
    fragmentState: FragmentState
) {
    val navigatorFactory = EpubNavigatorFactory(
        publication = publication,
        configuration = EpubNavigatorFactory.Configuration(
            defaults = EpubDefaults(
                pageMargins = 1.4
            )
        )
    )

}

// Usage

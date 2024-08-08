package com.example.bookbuddy.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment

@Composable
fun ReaderContent(fragmenFactory: FragmentFactory){
    AndroidView(
        factory = {context->
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                fragmentManager.fragmentFactory = fragmenFactory
                val fragment = fragmentManager.fragmentFactory.instantiate(
                    context.classLoader,
                    EpubNavigatorFragment::class.java.name
                )
                fragmentManager.beginTransaction()
                    .replace(id,fragment)
                    .commit()
            }
        }
    )
}
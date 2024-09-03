package com.example.bookbuddy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.strictmode.FragmentStrictMode
import androidx.navigation.compose.rememberNavController
import com.example.bookbuddy.ui.EbookReaderApp
import com.example.compose.BookBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FragmentStrictMode.defaultPolicy =
            FragmentStrictMode.Policy.Builder()
                .penaltyLog()
                .detectRetainInstanceUsage()
                .detectWrongFragmentContainer()
                .detectFragmentReuse()
                .build()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookBuddyTheme {
                EbookReaderApp(
                        navController = rememberNavController(),
                        modifier = Modifier.fillMaxSize()
                    )

            }
        }
    }
}


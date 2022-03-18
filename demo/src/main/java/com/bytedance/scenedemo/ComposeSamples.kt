package com.bytedance.scenedemo

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.appcompattheme.AppCompatTheme

class ComposeSamples : ComposeScene() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireComposeView().setContent {
            AppCompatTheme { // or AppCompatTheme
                Greeting()
            }
        }
    }

    @Composable
    private fun Greeting() {
        Text(
            text = "我是测试文字",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
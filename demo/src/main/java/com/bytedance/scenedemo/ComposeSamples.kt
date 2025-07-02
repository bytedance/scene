package com.bytedance.scenedemo

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.appcompattheme.AppCompatTheme

class ComposeSamples : ComposeScene() {
    @Composable
    override fun Content() {
        AppCompatTheme { // or AppCompatTheme
            Greeting()
        }
    }

    @Composable
    private fun Greeting() {
        Text(
            text = "Test",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
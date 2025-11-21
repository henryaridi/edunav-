package com.example.edunav.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.edunav.R

@Composable
fun AppBackground(
    @DrawableRes resId: Int = R.drawable.tech,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "App Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}
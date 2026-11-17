package com.respondo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.respondo.app.ui.RespondoApp
import com.respondo.app.ui.theme.RespondoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RespondoTheme {
                RespondoApp()
            }
        }
    }
}

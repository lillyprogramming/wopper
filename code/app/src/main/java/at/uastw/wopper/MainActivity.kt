package at.uastw.wopper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import at.uastw.wopper.ui.WopperApp
import at.uastw.wopper.ui.theme.WopperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WopperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WopperApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

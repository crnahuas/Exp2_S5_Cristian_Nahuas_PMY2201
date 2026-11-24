package cl.duoc.veterinaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.veterinaria.ui.ClinicaApp
import cl.duoc.veterinaria.ui.theme.VeterinariaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VeterinariaTheme { ClinicaApp() } }
    }
}

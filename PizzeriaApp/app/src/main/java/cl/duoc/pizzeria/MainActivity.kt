package cl.duoc.pizzeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeria.ui.PizzeriaApp
import cl.duoc.pizzeria.ui.theme.PizzeriaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PizzeriaAppTheme { PizzeriaApp() } }
    }
}

@Composable
fun Greetings(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.bienvenida),
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun GreetingPreview() {
    PizzeriaAppTheme { Surface { Greetings(Modifier.padding(24.dp)) } }
}

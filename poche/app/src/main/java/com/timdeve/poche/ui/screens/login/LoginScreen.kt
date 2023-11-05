package com.timdeve.poche.ui.screens.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdeve.poche.network.LoginRequest
import com.timdeve.poche.ui.theme.PocheTheme

@ExperimentalMaterial3Api
@Composable
fun LoginScreen(
    login: (LoginRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Login",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall.copy(
                color = colorScheme.onBackground,
            ),
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "Username",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorScheme.onBackground,
            ),
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = { username = it },
//            placeholder = { Text(text = "e.g. Hexamine") },
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorScheme.onBackground,
            ),
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
//            placeholder = { Text(text = "e.g. Hexamine") },
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = {login(LoginRequest(username, password))},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun LoginScreenPreview() {
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.surfaceColorAtElevation(2.dp),
        ) {
            LoginScreen({})
        }
    }
}

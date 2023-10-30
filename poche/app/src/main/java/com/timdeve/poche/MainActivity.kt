package com.timdeve.poche

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.timdeve.poche.ui.screens.HomeScreen
import com.timdeve.poche.ui.screens.StoriesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timdeve.poche.ui.theme.PocheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocheTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val storiesViewModel: StoriesViewModel = viewModel()
                    HomeScreen(storiesUiState = storiesViewModel.storiesUiState)
                }
            }
        }
    }
}

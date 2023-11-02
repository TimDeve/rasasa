package com.timdeve.poche.ui.screens.article

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.model.Article
import com.timdeve.poche.ui.theme.PocheTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    articleUiState: ArticleUiState,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.primary,
                ),
                title = {
                    Surface(navigateUp, color = Color.Transparent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Back Arrow",
//                                modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp)
                            )
                            Text("Back to stories", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
//                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
    ) {
        when (articleUiState) {
            is ArticleUiState.Loading -> Text(text = "Loading")
            is ArticleUiState.Success -> Success(
                articleUiState.article,
                Modifier.padding(it)
            )

            is ArticleUiState.Error -> Text(text = "Error")
        }
    }
}

@Composable
@Preview
fun ArticleScreenSuccessPreview() {
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.surfaceColorAtElevation(2.dp),
        ) {
            ArticleScreen(
                articleUiState = ArticleUiState.Success(
                    Article(
                        readable = true,
                    )
                ),
                {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Success(article: Article, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Is readable: ${article.readable}")
        Text(text = article.url)
        Text(text = article.title)
        Text(text = article.byline ?: "")
        Text(text = article.content)
    }
}

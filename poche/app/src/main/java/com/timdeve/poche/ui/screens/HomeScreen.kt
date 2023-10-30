package com.timdeve.poche.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdeve.poche.R
import com.timdeve.poche.model.Story
import com.timdeve.poche.ui.theme.PocheTheme

@Composable
fun HomeScreen(
    storiesUiState: StoriesUiState, modifier: Modifier = Modifier
) {
    Scaffold {
        when (storiesUiState) {
            is StoriesUiState.Loading -> LoadingScreen(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            )

            is StoriesUiState.Success -> ResultScreen(
                storiesUiState.stories, modifier = modifier
                    .fillMaxWidth()
                    .padding(it)
            )

            is StoriesUiState.Error -> ErrorScreen(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewHomeScreen() {
    val stories = listOf(
        Story(
            id = 0,
            feedId = 0,
            title = "Something is happening!",
            url = "",
            isRead = false,
            publishedDate = "",
            content = "",
        ),
        Story(
            id = 1,
            feedId = 0,
            title = "And something else!",
            url = "",
            isRead = false,
            publishedDate = "",
            content = "",
        )
    )
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(storiesUiState = StoriesUiState.Success(stories))
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ResultScreen(stories: List<Story>, modifier: Modifier = Modifier) {
    LazyColumn {
        items(stories) { story ->
            Row(modifier = Modifier.padding(4.dp)) {
                val link = buildAnnotatedString { append(story.title) }
                val uriHandler = LocalUriHandler.current
                ClickableText(text = link, onClick = { uriHandler.openUri(story.url) })
            }
        }
    }
}

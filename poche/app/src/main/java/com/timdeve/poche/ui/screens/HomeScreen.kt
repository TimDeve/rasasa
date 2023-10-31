package com.timdeve.poche.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdeve.poche.R
import com.timdeve.poche.model.Story
import com.timdeve.poche.model.genRandomStories
import com.timdeve.poche.ui.theme.PocheTheme
import com.timdeve.poche.ui.theme.Typography

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    storiesUiState: StoriesUiState, modifier: Modifier = Modifier
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = enterAlwaysScrollBehavior(appBarState)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("All stories")
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(color = Color.Transparent),
    ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewHomeScreen() {
    val stories = genRandomStories()
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.surfaceColorAtElevation(2.dp),
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
            color = colorScheme.surfaceVariant,
            trackColor = colorScheme.secondary,
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
fun StoryItem(story: Story) {
    Row(modifier = Modifier.padding(8.dp)) {
        val uriHandler = LocalUriHandler.current
        Column {
            ClickableText(
                text = buildAnnotatedString { append(story.title) },
                style = Typography.labelLarge.copy(
                    color = colorScheme.onBackground,
                ),
                onClick = { uriHandler.openUri(story.url) }
            )
            ClickableText(
                text = buildAnnotatedString { append(story.feedId.toString()) },
                style = Typography.bodyMedium.copy(
                    color = colorScheme.onBackground,
                ),
                onClick = { uriHandler.openUri(story.url) },
            )
        }
    }
}

@Composable
fun ResultScreen(stories: List<Story>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.background(color = colorScheme.surfaceColorAtElevation(1.dp))) {
        items(stories) { story ->
            StoryItem(story)
        }
    }
}

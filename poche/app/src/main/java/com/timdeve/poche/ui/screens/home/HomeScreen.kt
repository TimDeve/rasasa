package com.timdeve.poche.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdeve.poche.R
import com.timdeve.poche.model.Story
import com.timdeve.poche.model.genRandomStories
import com.timdeve.poche.ui.theme.PocheTheme
import com.timdeve.poche.ui.theme.Typography

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    storiesUiState: StoriesUiState,
    getStories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = enterAlwaysScrollBehavior(appBarState)
    val bottomBarHeight = LocalDensity.current.run {
        (64.dp.toPx() + (appBarState.heightOffset)).toDp()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = storiesUiState is StoriesUiState.Loading,
        onRefresh = getStories
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.primary,
                ),
                title = { Text("All stories") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.primary,
                modifier = Modifier
                    .height(bottomBarHeight)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar",
                )
            }
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(color = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(color = colorScheme.surfaceColorAtElevation(1.dp))
                .pullRefresh(pullRefreshState)
        ) {
            when (storiesUiState) {
                is StoriesUiState.Loading -> ResultScreen(
                    storiesUiState.stories, modifier = modifier
                        .fillMaxWidth()
                )

                is StoriesUiState.Success -> ResultScreen(
                    storiesUiState.stories, modifier = modifier
                        .fillMaxWidth()
                )

                is StoriesUiState.Error -> ErrorScreen(
                    modifier = modifier
                        .fillMaxSize()
                )
            }
            PullRefreshIndicator(
                refreshing = storiesUiState is StoriesUiState.Loading,
                state = pullRefreshState,
                modifier = modifier.align(Alignment.TopCenter),
                backgroundColor = colorScheme.surfaceVariant
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
            HomeScreen(
                storiesUiState = StoriesUiState.Success(stories),
                getStories = {}
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
fun PreviewHomeScreenLoading() {
    PocheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.surfaceColorAtElevation(2.dp),
        ) {
            HomeScreen(storiesUiState = StoriesUiState.Loading(emptyList()), getStories = {})
        }
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row {
                Text(
                    text = stringResource(R.string.loading_failed),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun StoryItem(story: Story) {
    Row(
        modifier = Modifier
            .padding(PaddingValues(16.dp, 12.dp))
            .fillMaxWidth()
    ) {
        val uriHandler = LocalUriHandler.current
        Column {
            ClickableText(
                text = buildAnnotatedString {
                    append("Feed Name (")
                    append(story.feedId.toString())
                    append(")")
                },
                style = Typography.labelLarge.copy(
                    color = colorScheme.onBackground,
                ),
                onClick = { uriHandler.openUri(story.url) },
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 2.dp)
            )
            ClickableText(
                text = buildAnnotatedString { append(story.title) },
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
    LazyColumn {
        items(stories) { story ->
            StoryItem(story)
            Divider(
                color = colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(PaddingValues(16.dp, 0.dp))
                    .height(1.dp)
                    .fillMaxWidth()
            )
        }
    }
}

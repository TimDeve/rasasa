package com.timdeve.poche.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.BaseWrapper
import com.timdeve.poche.PocheNavigate
import com.timdeve.poche.R
import com.timdeve.poche.model.Feed
import com.timdeve.poche.model.Story
import com.timdeve.poche.model.genFeeds
import com.timdeve.poche.model.genStories
import com.timdeve.poche.ui.screens.feedlists.FeedsUiState
import com.timdeve.poche.ui.theme.Typography

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    screenTitle: String,
    storiesUiState: StoriesUiState,
    getStories: () -> Unit,
    feedsUiState: FeedsUiState,
    getFeedsAndFeedLists: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = enterAlwaysScrollBehavior(appBarState)
    val bottomBarHeight = LocalDensity.current.run {
        (((64.dp.toPx() + (appBarState.heightOffset)) / 100) * 46.dp.toPx()).toDp()
    }

    val refreshing =
        storiesUiState is StoriesUiState.Loading || feedsUiState is FeedsUiState.Loading

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            getFeedsAndFeedLists()
            getStories()
        },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.primary,
                ),
                title = { Text(screenTitle) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(PocheNavigate.article("http://example.com"))
            }) {
                Icon(Icons.Filled.Favorite, contentDescription = "More")
            }
        },
        bottomBar = { BottomBar(bottomBarHeight) },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(color = Color.Transparent),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                storiesUiState is StoriesUiState.Loading &&
                        feedsUiState is FeedsUiState.Loading -> ResultScreen(
                    storiesUiState.stories, feedsUiState.feeds, navController, modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Success &&
                        feedsUiState is FeedsUiState.Success -> ResultScreen(
                    storiesUiState.stories, feedsUiState.feeds, navController, modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Loading &&
                        feedsUiState is FeedsUiState.Success -> ResultScreen(
                    storiesUiState.stories, feedsUiState.feeds, navController, modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Success &&
                        feedsUiState is FeedsUiState.Loading -> ResultScreen(
                    storiesUiState.stories, feedsUiState.feeds, navController, modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Error
                        || feedsUiState is FeedsUiState.Error -> ErrorScreen(
                    modifier = modifier
                        .fillMaxSize()
                )
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = modifier.align(Alignment.TopCenter),
                backgroundColor = colorScheme.surfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun BottomBarFull() {
    BottomBar(bottomBarHeight = 80.dp)
}

@Preview
@Composable
fun BottomBarHalf() {
    BottomBar(bottomBarHeight = 40.dp)
}

@Composable
fun BottomBar(bottomBarHeight: Dp) {
    BottomAppBar(
        containerColor = colorScheme.primaryContainer,
        contentColor = colorScheme.primary,
        modifier = Modifier
            .height(bottomBarHeight)
    ) {
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                    )
                    Text("Home")
                }
            },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Lists",
                    )
                    Text("Lists")
                }
            },
            selected = false,
            onClick = {}
        )
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
    val stories = genStories()
    val (feeds, feedLists) = genFeeds()
    BaseWrapper {
        HomeScreen(
            screenTitle = "All stories",
            storiesUiState = StoriesUiState.Success(stories),
            getStories = {},
            feedsUiState = FeedsUiState.Success(feeds, feedLists),
            getFeedsAndFeedLists = {},
            navController = rememberNavController(),
        )
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
    BaseWrapper {
        HomeScreen(
            screenTitle = "All stories",
            storiesUiState = StoriesUiState.Loading(emptyList()),
            getStories = {},
            feedsUiState = FeedsUiState.Loading(emptyMap(), emptyList()),
            getFeedsAndFeedLists = {},
            navController = rememberNavController(),
        )
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
fun StoryItem(story: Story, feeds: Map<Int, Feed>, onClick: () -> Unit = {}) {
    Surface(
        onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Box(Modifier.padding(PaddingValues(16.dp, 12.dp))) {
            Column {
                Text(
                    text = story.title,
                    style = Typography.labelLarge.copy(
                        color = colorScheme.onSurface,
                    ),
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 4.dp)
                )
                Text(
                    text = feeds[story.feedId]?.name ?: "Feed id '${story.feedId}' Not Found",
                    style = Typography.bodyMedium.copy(
                        color = colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
fun ResultScreen(
    stories: List<Story>,
    feeds: Map<Int, Feed>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(stories) { story ->
            StoryItem(
                story,
                feeds,
                onClick = { navController.navigate(PocheNavigate.article(story.url)) }
            )
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

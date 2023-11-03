package com.timdeve.poche.ui.screens.stories

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.timdeve.poche.ui.shared.BottomBar
import com.timdeve.poche.ui.theme.Typography
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMaterial3Api
@Composable
fun StoriesScreen(
    screenTitle: String,
    storiesUiState: StoriesUiState,
    getStories: () -> Unit,
    markStoryAsRead: (index: Int) -> Unit,
    showReadStories: Boolean,
    toggleReadStories: () -> Unit,
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
                    containerColor = Color.Transparent,
                    titleContentColor = colorScheme.onSurface,
                ),
                title = { Text(screenTitle) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = toggleReadStories,
                containerColor = colorScheme.surfaceColorAtElevation(48.dp),
            ) {
                Icon(
                    painterResource(
                        if (showReadStories) R.drawable.visibility_off_fill else R.drawable.visibility_fill
                    ), contentDescription = "Toggle read stories"
                )
            }
        },
        bottomBar = { BottomBar(bottomBarHeight, navController) },
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
                    storiesUiState.stories,
                    feedsUiState.feeds,
                    markStoryAsRead,
                    navController,
                    modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Success &&
                        feedsUiState is FeedsUiState.Success -> ResultScreen(
                    storiesUiState.stories,
                    feedsUiState.feeds,
                    markStoryAsRead,
                    navController,
                    modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Loading &&
                        feedsUiState is FeedsUiState.Success -> ResultScreen(
                    storiesUiState.stories,
                    feedsUiState.feeds,
                    markStoryAsRead,
                    navController,
                    modifier = modifier
                        .fillMaxWidth()
                )

                storiesUiState is StoriesUiState.Success &&
                        feedsUiState is FeedsUiState.Loading -> ResultScreen(
                    storiesUiState.stories,
                    feedsUiState.feeds,
                    markStoryAsRead,
                    navController,
                    modifier = modifier
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
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-64).dp),
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
fun PreviewStoriesScreen() {
    val stories = genStories()
    val (feeds, feedLists) = genFeeds()
    BaseWrapper {
        StoriesScreen(
            screenTitle = "All stories",
            storiesUiState = StoriesUiState.Success(stories),
            getStories = {},
            markStoryAsRead = {},
            showReadStories = false,
            toggleReadStories = {},
            feedsUiState = FeedsUiState.Success(feeds, feedLists),
            getFeedsAndFeedLists = {},
            navController = rememberNavController(),
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(name = "Light Mode")
//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Dark Mode"
//)
//@Composable
//fun PreviewStoriesScreenLoading() {
//    BaseWrapper {
//        StoriesScreen(
//            screenTitle = "All stories",
//            storiesUiState = StoriesUiState.Loading(emptyList()),
//            getStories = {},
//            feedsUiState = FeedsUiState.Loading(emptyMap(), emptyList()),
//            getFeedsAndFeedLists = {},
//            navController = rememberNavController(),
//        )
//    }
//}

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
fun linkSharer(url: String): () -> Unit {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current
    return {
        context.startActivity(shareIntent)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryItem(
    story: Story,
    feeds: Map<Int, Feed>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val shareLink = linkSharer(story.url)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = shareLink,
            ),
        color = Color.Transparent,
    ) {
        Box(Modifier.padding(PaddingValues(16.dp, 12.dp))) {
            Column {
                Text(
                    text = story.title,
                    style = Typography.labelLarge.copy(
                        color = if (story.isRead) colorScheme.tertiary else colorScheme.onSurface,
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
    markStoryAsRead: (index: Int) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var highestIndex by remember { mutableIntStateOf(Int.MIN_VALUE) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it > highestIndex }
            .collect {
                // Offset by one so the one outside of the screen is targeted
                val targetIndex = it - 1
                if (targetIndex >= 0) {
                    highestIndex = targetIndex
                    markStoryAsRead(targetIndex)
                }
            }
    }
    LazyColumn(state = listState, modifier = modifier) {
        itemsIndexed(stories) { i, story ->
            StoryItem(
                story,
                feeds,
                onClick = { navController.navigate(PocheNavigate.article(story.url)) },
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

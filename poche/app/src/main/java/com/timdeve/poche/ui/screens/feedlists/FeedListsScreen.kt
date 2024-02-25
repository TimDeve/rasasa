package com.timdeve.poche.ui.screens.article

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.BaseWrapper
import com.timdeve.poche.PocheNavigate
import com.timdeve.poche.model.genFeeds
import com.timdeve.poche.persistence.Feed
import com.timdeve.poche.persistence.FeedList
import com.timdeve.poche.persistence.fromModel
import com.timdeve.poche.ui.screens.feedlists.FeedsUiState
import com.timdeve.poche.ui.shared.BottomBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListsScreen(
    feedsUiState: FeedsUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val refreshing = feedsUiState is FeedsUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
//            getFeedsAndFeedLists()
//            getStories()
        },
    )
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorScheme.onSurface,
                ),
                title = { Text("Feed Lists") },
//                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = { BottomBar(modifier.height(72.dp), navController) },
        modifier = Modifier
//            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(color = Color.Transparent),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
//                .pullRefresh(pullRefreshState)
        ) {
            when (feedsUiState) {
                is FeedsUiState.Loading -> Text("Loading")
                FeedsUiState.Error -> Text("Error")
                is FeedsUiState.Success -> Success(feedsUiState.feedLists, navController)
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

@Composable
fun Success(
    feedLists: Map<Long, FeedList>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val feedListsList = feedLists.entries.sortedBy { it.value.name }.map { it.value }
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        items(feedListsList) { list ->
            Surface(
                color = colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .clickable { navController.navigate(PocheNavigate.stories(list.id)) }
            ) {
                Text(
                    text = list.name,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
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
fun PreviewStoriesScreen() {
    val (feeds, feedLists) = genFeeds()

    BaseWrapper {
        FeedListsScreen(
            feedsUiState = FeedsUiState.Success(
                feeds.mapValues { Feed.fromModel(it.value) },
                feedLists.mapValues { FeedList.fromModel(it.value) },
            ),
            navController = rememberNavController(),
        )
    }
}

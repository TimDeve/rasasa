package com.timdeve.poche.ui.screens.article

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdeve.poche.BaseWrapper
import com.timdeve.poche.model.genArticle
import com.timdeve.poche.persistence.Article
import com.timdeve.poche.persistence.fromModel
import com.timdeve.poche.ui.shared.HtmlContent
import com.timdeve.poche.ui.shared.linkOpener
import com.timdeve.poche.ui.shared.linkSharer
import com.timdeve.poche.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    articleUiState: ArticleUiState,
    navigateUp: () -> Unit,
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = enterAlwaysScrollBehavior(appBarState)
    val refreshing = articleUiState is ArticleUiState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {},
    )
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorScheme.onSurface,
                ),
                title = {
                    Surface(
                        navigateUp,
                        color = Color.Transparent,
                        shape = CircleShape,
                        modifier = Modifier.offset(x = (-8).dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Back Arrow",
                            )
                            Text(
                                "Back",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            when (articleUiState) {
                is ArticleUiState.Loading -> Unit
                is ArticleUiState.Success -> {
                    if (articleUiState.article != null) {
                        Success(articleUiState.article)
                    } else {
                        NoArticle()
                    }
                }

                is ArticleUiState.Error -> Failure()
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-64).dp),
                backgroundColor = colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Preview
fun ArticleScreenSuccessPreview() {
    BaseWrapper {
        ArticleScreen(
            ArticleUiState.Success(Article.fromModel(genArticle())),
            {}
        )
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Preview(showSystemUi = true)
fun ArticleScreenLoadingPreview() {
    BaseWrapper {
        ArticleScreen(
            ArticleUiState.Loading,
            {}
        )
    }
}

@Composable
fun NoArticle(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("No article available", color = colorScheme.onSurface)
    }
}

@Composable
fun Failure(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("An error happened", color = colorScheme.onSurface)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Success(article: Article, modifier: Modifier = Modifier) {
    val shareLink = linkSharer(article.url)
    val openLink = linkOpener(article.url)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (article.readable) {
            Text(
                text = article.title,
                style = Typography.displaySmall.copy(
                    color = colorScheme.onSurface,
                ),
                modifier = Modifier
                    .padding(PaddingValues(top = 0.dp, bottom = 12.dp))
                    .combinedClickable(
                        onClick = openLink,
                        onLongClick = shareLink,
                    ),
            )
            // TODO: Replace with a domain only text
            // Text(text = article.url)
            if (article.byline.orEmpty().isNotEmpty()) {
                HtmlContent(
                    "<h5>" + article.byline.orEmpty() + "</h5>",
                )
            }
            HtmlContent(article.content)
        } else {
            Text("Article is not readable", color = colorScheme.onSurface)
        }
    }
}


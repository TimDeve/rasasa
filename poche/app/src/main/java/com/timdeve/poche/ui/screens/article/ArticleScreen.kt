package com.timdeve.poche.ui.screens.article

import android.content.res.Configuration
import android.text.Html
import android.text.method.LinkMovementMethod
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.textview.MaterialTextView
import com.timdeve.poche.BaseWrapper
import com.timdeve.poche.model.Article
import com.timdeve.poche.model.genArticle
import com.timdeve.poche.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    articleUiState: ArticleUiState,
    navigateUp: () -> Unit,
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = enterAlwaysScrollBehavior(appBarState)
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
                                "Back to stories",
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
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Preview
fun ArticleScreenSuccessPreview() {
    BaseWrapper {
        ArticleScreen(
            ArticleUiState.Success(genArticle()),
            {}
        )
    }
}

@Composable
fun Success(article: Article, modifier: Modifier = Modifier) {
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
                modifier = Modifier.padding(PaddingValues(top = 0.dp, bottom = 12.dp))
            )
            // TODO: Replace with a domain only text
            // Text(text = article.url)
            if (article.byline.orEmpty().isNotEmpty()) {
                Content(
                    "<h5>" + article.byline.orEmpty() + "</h5>",
                )
            }
            Content(article.content)
        } else {
            Text("Article is not readable")
        }
    }
}

@Composable
fun Content(content: String, modifier: Modifier = Modifier) {
    val spannedText = Html.fromHtml(content, 0)
    val textColor = colorScheme.onSurface
    val linkColor = colorScheme.tertiary
    AndroidView(
        modifier = modifier,
        factory = {
            MaterialTextView(it).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor.toArgb())
                setLinkTextColor(linkColor.toArgb())
            }
        },
        update = {
            it.text = spannedText
        }
    )
}

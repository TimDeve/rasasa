@file:UseSerializers(URLSerializer::class)
package com.timdeve.poche.model

import com.timdeve.poche.network.URLSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.net.URL

@Serializable
data class Article(
    val url: URL,
    val readable: Boolean,
    val title: String = "",
    val byline: String? = "",
    val content: String = "",
)

private const val fakeArticleContent = """
<h2>H2 Type title</h2>
<p>
    A paragraph <i>wow</i> this is just some great html.
</p>
<p>
    Another paragraph with a 
    <a href="http://example.com">Link</a> in the middle of it.
    This one is longer and run for more than one line. 
    Maybe even three lines.
</p>
<h3>H3 Type title</h3>
<p>Small paragraph with <em>emphasised</em> text</p>
<h4>H4 Type title</h4>
<p>Small paragraph with <strong>strong</strong> text</p>
<h5>H5 Type title</h5>
<pre><code>
console.log("Hello World")
</code></pre>
<h6>H6 Type title</h6>
<p>Unordered list</p>
<ul>
    <li>Cat</li>
    <li>Dog</li>
    <li>Rabbit</li>
</ul>
<p>Ordered list</p>
<ol>
    <li>One</li>
    <li>Two</li>
    <li>Three</li>
</ol>
"""

fun genArticle(): Article {
    return Article(
        url = URL("http://example.com"),
        readable = true,
        title = "A most fantastic day",
        byline = "By me, a writer",
        content = fakeArticleContent,
    )
}
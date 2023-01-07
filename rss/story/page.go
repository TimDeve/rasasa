package story

import (
	"fmt"
	"net/url"

	md "github.com/JohannesKaufmann/html-to-markdown"
	"github.com/TimDeve/rasasa/rss/api"
	"github.com/TimDeve/rasasa/rss/messages"
	"github.com/TimDeve/rasasa/rss/shared"
	"github.com/charmbracelet/bubbles/viewport"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/glamour"
	"github.com/charmbracelet/lipgloss"
)

var converter = md.NewConverter("", true, nil)

type Model struct {
	viewport viewport.Model
	story    api.Story
	article  api.Article
	width    int
	loading  bool
}

func backgroundStyle() string {
	if lipgloss.HasDarkBackground() {
		return "dark"
	}

	return "light"
}

func mdRenderer(width int) *glamour.TermRenderer {
	r, _ := glamour.NewTermRenderer(
		glamour.WithWordWrap(width),
		glamour.WithStandardStyle(backgroundStyle()),
	)
	return r
}

func InitModel() Model {
	return Model{
		viewport: viewport.New(80, 20),
		loading:  true,
	}
}

func Open(m Model, win tea.WindowSizeMsg) Model {
	m.viewport.Width = shared.Min(win.Width, shared.MaxWidth)
	m.viewport.Height = win.Height
	m.width = shared.Min(win.Width, shared.MaxWidth)

	return m
}

func Update(m Model, msg tea.Msg) (Model, tea.Cmd) {
	var (
		cmd  tea.Cmd
		cmds []tea.Cmd
	)

	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.viewport.Width = shared.Min(msg.Width, shared.MaxWidth)
		m.viewport.Height = msg.Height
		m.width = shared.Min(msg.Width, shared.MaxWidth)
		m.viewport.SetContent(loadedView(m))

	case tea.KeyMsg:
		if msg.Type == tea.KeyBackspace {
			cmds = append(cmds, messages.Cmd(messages.OpenStoriesPage{}))
		}

	case messages.LoadStory:
		m.loading = true
		m.story = msg.Story
		m.viewport.SetContent(loadingView(m))
		cmds = append(cmds, fetchArticle(msg.Story.Url))

	case messages.ArticleFetched:
		m.loading = false
		m.article = msg.Article
		m.viewport.SetContent(loadedView(m))
	}

	m.viewport, cmd = m.viewport.Update(msg)
	cmds = append(cmds, cmd)

	return m, tea.Batch(cmds...)
}

func fetchArticle(url string) tea.Cmd {
	return func() tea.Msg {
		a, err := api.FetchArticle(url)

		if err != nil {
			return messages.ErrMsg(err)
		}

		return messages.ArticleFetched{Article: a}
	}
}

func View(m Model) string {
	return m.viewport.View()
}

func render(m Model, body string) string {
	r, err := glamour.NewTermRenderer(
		glamour.WithWordWrap(m.width),
		glamour.WithStandardStyle(backgroundStyle()),
	)

	if err != nil {
		return fmt.Sprintf("There was a error when creating markdown renderer: %e", err)
	}

	mdToRender := "# " + m.story.Title + "\n"
	mdToRender += "`" + urlDomain(m.story.Url) + "`\n\n"

	mdToRender += body

	md, err := r.Render(mdToRender)
	if err != nil {
		return fmt.Sprintf("Could not render article because of error: %e", err)
	}
	return md
}

func loadingView(m Model) string {
	return render(m, "Loading...")
}

func loadedView(m Model) string {
	var body string

	if m.article.Readable {
		if m.article.Byline != nil {
			body += *m.article.Byline + "\n\n"
		}

		if m.article.Content != nil {
			markdown, err := converter.ConvertString(*m.article.Content)
			if err != nil {
				body += "Can't read this article"
			}
			body += markdown
		}
	} else {
		body += "Can't read this article"
	}

	return render(m, body)
}

func urlDomain(ustr string) string {
	u, err := url.Parse(ustr)
	if err != nil {
		return "Can't parse URL"
	}

	return u.Host
}

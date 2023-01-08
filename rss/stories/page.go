package stories

import (
	"fmt"
	"io"

	"github.com/TimDeve/rasasa/rss/api"
	"github.com/TimDeve/rasasa/rss/messages"
	"github.com/TimDeve/rasasa/rss/shared"
	"github.com/charmbracelet/bubbles/key"
	"github.com/charmbracelet/bubbles/list"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

const topMargin = 1

var (
	pageStyle         = lipgloss.NewStyle().MarginTop(topMargin)
	titleStyle        = lipgloss.NewStyle().MarginLeft(2).Reverse(true).Padding(0, 2)
	itemStyle         = lipgloss.NewStyle().PaddingLeft(4)
	selectedItemStyle = lipgloss.NewStyle().PaddingLeft(4).Foreground(lipgloss.Color("3"))
	paginationStyle   = list.DefaultStyles().PaginationStyle.PaddingLeft(4)
	helpStyle         = list.DefaultStyles().HelpStyle.PaddingLeft(4).PaddingBottom(1)

	extraKeybindings = []key.Binding{
		key.NewBinding(
			key.WithKeys("ENTER"),
			key.WithHelp("ENTER", "open story"),
		),
		key.NewBinding(
			key.WithKeys("f"),
			key.WithHelp("f", "open lists"),
		),
	}
)

type Model struct {
	stories list.Model
	list    *api.List
	loading bool
}

func InitModel() Model {
	return Model{
		stories: makeList(),
		loading: true,
	}
}

type storyItemDelegate struct{}

func (d storyItemDelegate) Height() int                               { return 1 }
func (d storyItemDelegate) Spacing() int                              { return 0 }
func (d storyItemDelegate) Update(msg tea.Msg, m *list.Model) tea.Cmd { return nil }
func (d storyItemDelegate) Render(w io.Writer, m list.Model, index int, listItem list.Item) {
	i, ok := listItem.(api.Story)
	if !ok {
		return
	}

	str := fmt.Sprintf("%d. %s", index+1, i.Title)

	fn := itemStyle.Render
	if index == m.Index() {
		fn = func(s string) string {
			return selectedItemStyle.Render(s)
		}
	}

	fmt.Fprint(w, fn(str))
}

func listHeight(viewportHeight int) int {
	return viewportHeight - topMargin
}

func makeList() list.Model {
	l := list.New(nil, storyItemDelegate{}, shared.MaxWidth, 10)
	l.Title = "Rasasa"
	l.SetShowStatusBar(false)
	l.SetFilteringEnabled(false)
	l.Styles.Title = titleStyle
	l.Styles.PaginationStyle = paginationStyle
	l.Styles.HelpStyle = helpStyle
	l.AdditionalFullHelpKeys = func() []key.Binding { return extraKeybindings }
	l.AdditionalShortHelpKeys = func() []key.Binding { return extraKeybindings }
	return l
}

func Open(m Model, win tea.WindowSizeMsg, f *api.List) Model {
	m.stories.SetHeight(listHeight(win.Height))
	m.stories.SetWidth(win.Width)
	if f != nil {
		m.stories.Title = fmt.Sprintf("Rasasa - %s", f.Name)
	} else {
		m.stories.Title = "Rasasa"
	}
	m.list = f

	return m
}

func Update(m Model, msg tea.Msg) (Model, tea.Cmd) {
	switch msg := msg.(type) {

	case tea.WindowSizeMsg:
		m.stories.SetHeight(listHeight(msg.Height))
		m.stories.SetWidth(shared.Min(msg.Width, shared.MaxWidth))

	case tea.KeyMsg:
		switch msg.Type {
		case tea.KeyEnter:
			item := m.stories.SelectedItem().(api.Story)
			return m, messages.Cmd(messages.OpenStoryPage{Story: item})
		case tea.KeyRunes:
			switch string(msg.Runes) {
			case "f":
				return m, messages.Cmd(messages.OpenListsPage{})
			}
		}

	case messages.LoginSuccessful:
		return m, fetchStories(nil)

	case messages.OpenStoriesPage:
		return m, fetchStories(msg.List)

	case messages.StoriesFetched:
		m.stories.SetItems(shared.ToListItem(msg))
		return m, nil
	}

	var cmd tea.Cmd
	m.stories, cmd = m.stories.Update(msg)
	return m, cmd
}

func fetchStories(f *api.List) tea.Cmd {
	return func() tea.Msg {
		sx, err := api.FetchStories(f)

		if err != nil {
			return messages.ErrMsg{Err: err}
		}

		return messages.StoriesFetched(sx)
	}
}

func View(m Model) string {
	return pageStyle.Render(m.stories.View())
}

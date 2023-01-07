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

var (
	titleStyle        = lipgloss.NewStyle().MarginLeft(2).Reverse(true).Padding(0, 2)
	itemStyle         = lipgloss.NewStyle().PaddingLeft(4)
	selectedItemStyle = lipgloss.NewStyle().PaddingLeft(4).Foreground(lipgloss.Color("3"))
	paginationStyle   = list.DefaultStyles().PaginationStyle.PaddingLeft(4)
	helpStyle         = list.DefaultStyles().HelpStyle.PaddingLeft(4).PaddingBottom(1)

	extraKeybindings = []key.Binding{
		key.NewBinding(
			key.WithKeys("\n"),
			key.WithHelp("ENTER", "open story"),
		),
	}
)

type Model struct {
	stories list.Model
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

func makeList() list.Model {
	l := list.New(nil, storyItemDelegate{}, shared.MaxWidth, 0)
	l.Title = "Rasasa"
	l.SetShowStatusBar(false)
	l.SetFilteringEnabled(false)
	l.Styles.Title = titleStyle
	l.Styles.PaginationStyle = paginationStyle
	l.Styles.HelpStyle = helpStyle
	return l
}

func Update(m Model, msg tea.Msg) (Model, tea.Cmd) {
	switch msg := msg.(type) {

	case tea.WindowSizeMsg:
		m.stories.SetHeight(msg.Height - 2)
		m.stories.SetWidth(shared.Min(msg.Width, shared.MaxWidth))

	case tea.KeyMsg:
		if msg.Type == tea.KeyEnter {
			item := m.stories.SelectedItem().(api.Story)
			return m, messages.Cmd(messages.OpenStoryPage{Story: item})
		}

	case messages.LoginSuccessful:
		return m, fetchStories

	case messages.StoryFetched:
		m.stories.SetItems(storiesToListItem(msg))
		return m, nil
	}

	var cmd tea.Cmd
	m.stories, cmd = m.stories.Update(msg)
	return m, cmd
}

func fetchStories() tea.Msg {
	sx, err := api.FetchStories()

	if err != nil {
		return messages.ErrMsg(err)
	}

	return messages.StoryFetched(sx)
}

func View(m Model) string {
	return "\n" + m.stories.View()
}

func storiesToListItem(sx []api.Story) []list.Item {
	titles := make([]list.Item, 0, len(sx))
	for _, s := range sx {
		titles = append(titles, s)
	}
	return titles
}

package lists

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
			key.WithHelp("ENTER", "pick list"),
		),
	}
)

type Model struct {
	lists   list.Model
	loading bool
}

func InitModel() Model {
	return Model{
		lists:   makeList(),
		loading: true,
	}
}

type listItemDelegate struct{}

func (d listItemDelegate) Height() int                               { return 1 }
func (d listItemDelegate) Spacing() int                              { return 0 }
func (d listItemDelegate) Update(msg tea.Msg, m *list.Model) tea.Cmd { return nil }
func (d listItemDelegate) Render(w io.Writer, m list.Model, index int, listItem list.Item) {
	i, ok := listItem.(api.List)
	if !ok {
		return
	}

	str := fmt.Sprintf("%d. %s", index+1, i.Name)

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
	l := list.New(shared.ToListItem([]api.List{{Name: "My list"}}), listItemDelegate{}, shared.MaxWidth, 10)
	l.Title = "Lists"
	l.SetShowStatusBar(false)
	l.SetFilteringEnabled(false)
	l.Styles.Title = titleStyle
	l.Styles.PaginationStyle = paginationStyle
	l.Styles.HelpStyle = helpStyle
	l.AdditionalFullHelpKeys = func() []key.Binding { return extraKeybindings }
	l.AdditionalShortHelpKeys = func() []key.Binding { return extraKeybindings }
	return l
}

func Open(m Model, win tea.WindowSizeMsg) Model {
	m.lists.SetHeight(listHeight(win.Height))
	m.lists.SetWidth(win.Width)

	return m
}

func Update(m Model, msg tea.Msg) (Model, tea.Cmd) {
	var (
		cmd  tea.Cmd
		cmds []tea.Cmd
	)

	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.lists.SetHeight(listHeight(msg.Height))
		m.lists.SetWidth(shared.Min(msg.Width, shared.MaxWidth))

	case tea.KeyMsg:
		switch msg.Type {
		case tea.KeyBackspace:
			cmds = append(cmds, messages.Cmd(messages.OpenStoriesPage{}))
		case tea.KeyEnter:
			item, ok := m.lists.SelectedItem().(api.List)
			if ok == false {
				break
			}
			cmds = append(cmds, messages.Cmd(messages.OpenStoriesPage{List: &item}))
		}

	case messages.OpenListsPage:
		cmds = append(cmds, fetchLists)

	case messages.ListsFetched:
		m.lists.SetItems(shared.ToListItem(msg.Lists))
	}

	m.lists, cmd = m.lists.Update(msg)
	cmds = append(cmds, cmd)

	return m, tea.Batch(cmds...)
}

func View(m Model) string {
	return pageStyle.Render(m.lists.View())
}

func fetchLists() tea.Msg {
	fxs, err := api.FetchLists()

	if err != nil {
		return messages.ErrMsg{Err: err}
	}

	return messages.ListsFetched{Lists: fxs}
}

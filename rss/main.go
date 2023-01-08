package main

import (
	"fmt"
	"log"
	"os"

	"github.com/TimDeve/rasasa/rss/api"
	"github.com/TimDeve/rasasa/rss/messages"
	"github.com/TimDeve/rasasa/rss/shared"
	"github.com/TimDeve/rasasa/rss/stories"
	"github.com/TimDeve/rasasa/rss/story"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

type httpState struct {
	loading bool
	body    string
	status  int
	err     error
}

type model struct {
	currentPage shared.Page
	storiesPage stories.Model
	storyPage   story.Model

	win tea.WindowSizeMsg

	failure error
	exiting bool
}

func main() {
	if _, err := tea.NewProgram(model{
		storiesPage: stories.InitModel(),
		storyPage:   story.InitModel(),
	},
		tea.WithAltScreen(),
		tea.WithMouseCellMotion(),
	).Run(); err != nil {
		fmt.Printf("Uh oh, there was an error: %v\n", err)
		os.Exit(1)
	}
}

func (m model) Init() tea.Cmd {
	return login
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	var cmd tea.Cmd

	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.win = msg

	case messages.ErrMsg:
		m.failure = msg
		return m, tea.Quit

	case messages.OpenStoryPage:
		m.currentPage = shared.StoryPage

		m.storyPage = story.Open(m.storyPage, m.win)

		return m, messages.Cmd(messages.LoadStory{Story: msg.Story})

	case messages.OpenStoriesPage:
		m.currentPage = shared.StoriesPage
		return m, nil

	case tea.KeyMsg:
		if msg.Type == tea.KeyCtrlC {
			m.exiting = true
			return m, tea.Quit
		}
	}

	switch m.currentPage {
	case shared.StoriesPage:
		m.storiesPage, cmd = stories.Update(m.storiesPage, msg)
		return m, cmd

	case shared.StoryPage:
		m.storyPage, cmd = story.Update(m.storyPage, msg)
		return m, cmd
	}

	return m, nil
}

func center(m model, source string) string {
	if m.win.Width > shared.MaxWidth {
		toMaxWidth := lipgloss.NewStyle().MaxWidth(shared.MaxWidth).PaddingRight(shared.MaxWidth)
		centered := lipgloss.NewStyle().Width(m.win.Width).Align(lipgloss.Center)
		return centered.Render(toMaxWidth.Render(source))
	} else {
		return source
	}
}

func (m model) View() string {
	if m.failure != nil {
		return fmt.Sprintf("\nUnexpected errror: %v\n\n", m.failure)
	}

	if m.exiting {
		return "See you later!\n"
	}

	switch m.currentPage {
	case shared.StoriesPage:
		return center(m, stories.View(m.storiesPage))

	case shared.StoryPage:
		return center(m, story.View(m.storyPage))

	default:
		log.Fatal("Page not handled")
	}

	return "\n"
}

func login() tea.Msg {
	err := api.Login()
	if err != nil {
		return messages.ErrMsg(err)
	}

	return messages.LoginSuccessful{}
}

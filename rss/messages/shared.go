package messages

import tea "github.com/charmbracelet/bubbletea"

type ErrMsg struct{ Err error }

func Cmd(m tea.Msg) tea.Cmd {
	return func() tea.Msg { return m }
}

func Err(e error) tea.Cmd {
	return Cmd(ErrMsg{e})
}

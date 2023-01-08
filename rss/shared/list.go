package shared

import "github.com/charmbracelet/bubbles/list"

func ToListItem[T list.Item](xs []T) []list.Item {
	items := make([]list.Item, 0, len(xs))
	for _, x := range xs {
		items = append(items, x)
	}
	return items
}

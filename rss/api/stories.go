package api

import (
	"encoding/json"
	"fmt"
	"net/url"
	"strconv"
)

type Story struct {
	Id      int32  `json:"id"`
	ListId  int32  `json:"listId"`
	Title   string `json:"title"`
	Url     string `json:"url"`
	IsRead  bool   `json:"isRead"`
	Content string `json:"content"`
	//published_date time.Time `json:"publishedDate"`
}

func (s Story) FilterValue() string { return s.Title }

func FetchStories(f *List) ([]Story, error) {
	query := ""
	if f != nil {
		query = "?listId=" + url.QueryEscape(strconv.Itoa(int(f.Id)))
	}
	res, err := client.Get(V0("stories" + query))
	if err != nil {
		return nil, fmt.Errorf("Could not fetch stories: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return nil, fmt.Errorf("Stories fetch responded with status: %d", res.StatusCode)
	}

	sx := struct {
		Stories []Story `json:"stories"`
	}{}
	err = json.NewDecoder(res.Body).Decode(&sx)
	if err != nil {
		return nil, err
	}

	return sx.Stories, nil
}

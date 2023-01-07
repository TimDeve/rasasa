package api

import (
	"encoding/json"
	"fmt"
	"net/url"
)

type Article struct {
	Readable bool    `json:"readable"`
	Title    *string `json:"title"`
	Byline   *string `json:"byline"`
	Content  *string `json:"content"`
}

func FetchArticle(u string) (Article, error) {
	res, err := client.Get(V0("read?page=" + url.QueryEscape(u)))
	if err != nil {
		return Article{}, fmt.Errorf("Could not fetch article: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return Article{}, fmt.Errorf("Article fetch responded with status: %d", res.StatusCode)
	}

	a := Article{}
	err = json.NewDecoder(res.Body).Decode(&a)
	if err != nil {
		return Article{}, err
	}

	return a, nil
}

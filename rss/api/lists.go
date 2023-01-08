package api

import (
	"encoding/json"
	"fmt"
)

type List struct {
	Id   int32  `json:"id"`
	Name string `json:"name"`
	Url  string `json:"url"`
}

func (f List) FilterValue() string { return f.Name }

func FetchLists() ([]List, error) {
	res, err := client.Get(V0("lists"))
	if err != nil {
		return nil, fmt.Errorf("Could not fetch lists: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return nil, fmt.Errorf("Stories fetch responded with status: %d", res.StatusCode)
	}

	fx := struct {
		Lists []List `json:"lists"`
	}{}
	err = json.NewDecoder(res.Body).Decode(&fx)
	if err != nil {
		return nil, err
	}

	return fx.Lists, nil
}

package api

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/http/cookiejar"
	"os"
	"time"

	"golang.org/x/net/publicsuffix"
)

var jar cookiejar.Jar
var client http.Client

const baseUrlEnvVariable = "RASASA_RSS_BASE_URL"

var baseUrl string

func init() {
	baseUrl = os.Getenv(baseUrlEnvVariable)
	if baseUrl == "" {
		log.Fatal(fmt.Sprintf("$%s is not set", baseUrlEnvVariable))
	}

	jar, err := cookiejar.New(&cookiejar.Options{PublicSuffixList: publicsuffix.List})
	if err != nil {
		log.Fatal(err)
	}

	client = http.Client{Timeout: 10 * time.Second, Jar: jar}
}

func PostJson(url string, body any) (*http.Response, error) {
	marshalled, err := json.Marshal(body)
	if err != nil {
		return nil, err
	}

	req, err := http.NewRequest("POST", url, bytes.NewReader(marshalled))
	if err != nil {
		return nil, err
	}

	req.Header.Set("Content-Type", "application/json")

	return client.Do(req)
}

func V0(resource string) string {
	return baseUrl + "/api/v0/" + resource
}

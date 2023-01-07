package api

import (
	"fmt"
	"log"
	"os"
)

var (
	username string
	password string
)

const (
	usernameEnvVar = "RASASA_RSS_USER"
	passwordEnvVar = "RASASA_RSS_PASS"
)

func init() {
	username = os.Getenv(usernameEnvVar)
	if username == "" {
		log.Fatal(fmt.Sprintf("$%s is not set", usernameEnvVar))
	}

	password = os.Getenv(passwordEnvVar)
	if password == "" {
		log.Fatal(fmt.Sprintf("$%s is not set", passwordEnvVar))
	}
}

func Login() error {
	res, err := PostJson(V0("login"), map[string]string{
		"username": username,
		"password": password,
	})

	if err != nil {
		return err
	}

	if res.StatusCode != 200 {
		return fmt.Errorf("Login responded with status: %d", res.StatusCode)
	}

	return nil
}

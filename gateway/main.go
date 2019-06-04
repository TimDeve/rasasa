package main

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"

	"github.com/go-session/redis"
	"github.com/go-session/session"
	"github.com/gorilla/mux"
	"github.com/urfave/negroni"
)

var adminUsername = os.Getenv("RASASA_USER")
var adminPassword = os.Getenv("RASASA_PASS")

var adminLogin = login{
	Username: &adminUsername,
	Password: &adminPassword,
}

var rasasaServer = "http://localhost:8091"
var readServer = "http://localhost:8092"

func main() {
	initSession()

	r := mux.NewRouter()

	r.HandleFunc("/api/v0/login", loginHandler).Methods("POST")

	r.HandleFunc("/api/{proxyPath:v0/read.*}", restrictedProxy(readServer))
	r.HandleFunc("/api/{proxyPath:.*}", restrictedProxy(rasasaServer))

	r.PathPrefix("/").HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "public/index.html")
	})

	n := negroni.Classic()
	n.UseHandler(r)

	fmt.Println("Listening on localhost:8090")
	http.ListenAndServe("localhost:8090", n)
}

func loginHandler(wr http.ResponseWriter, req *http.Request) {
	store, err := session.Start(context.Background(), wr, req)
	if err != nil {
		http.Error(wr, http.StatusText(http.StatusInternalServerError), http.StatusInternalServerError)
	}

	d := json.NewDecoder(req.Body)
	d.DisallowUnknownFields()

	l := login{}
	err = d.Decode(&l)
	if err != nil {
		http.Error(wr, err.Error(), http.StatusBadRequest)
		return
	}

	if !l.Equal(adminLogin) {
		http.Error(wr, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
		return
	}

	store.Set("authenticated", true)
	if store.Save() != nil {
		http.Error(wr, http.StatusText(http.StatusInternalServerError), http.StatusInternalServerError)
	}
	fmt.Fprint(wr, http.StatusText(http.StatusOK))
}

func proxy(target string) func(http.ResponseWriter, *http.Request) {
	url, err := url.Parse(target)
	if err != nil {
		panic(err)
	}

	proxy := httputil.NewSingleHostReverseProxy(url)

	return func(wr http.ResponseWriter, req *http.Request) {
		req.URL.Path = mux.Vars(req)["proxyPath"]
		proxy.ServeHTTP(wr, req)
	}
}

func restricted(handler func(http.ResponseWriter, *http.Request)) func(http.ResponseWriter, *http.Request) {
	return func(wr http.ResponseWriter, req *http.Request) {
		store, err := session.Start(context.Background(), wr, req)
		if err != nil {
			http.Error(wr, http.StatusText(http.StatusInternalServerError), http.StatusInternalServerError)
		}

		isAuthenticated, ok := store.Get("authenticated")
		if !ok || !isAuthenticated.(bool) {
			http.Error(wr, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
			return
		}
		handler(wr, req)
	}
}

func restrictedProxy(target string) func(http.ResponseWriter, *http.Request) {
	return restricted(proxy(target))
}

func initSession() {
	session.InitManager(
		session.SetStore(redis.NewRedisStore(&redis.Options{
			Addr: os.Getenv("REDIS_URL"),
			DB:   1,
		})),
	)
}

type login struct {
	Username *string `json:"username"`
	Password *string `json:"password"`
}

func (aLogin login) Equal(anotherLogin login) bool {
	return *aLogin.Username == *anotherLogin.Username && *aLogin.Password == *anotherLogin.Password
}

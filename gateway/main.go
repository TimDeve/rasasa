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

var rasasaServer = os.Getenv("SERVER_URL")
var readServer = os.Getenv("READ_URL")

func main() {
	initSession()

	r := mux.NewRouter()

	r.HandleFunc("/api/v0/login", loginHandler).Methods("POST")

	r.HandleFunc("/api/{proxyPath:v0/read.*}", restrictedProxy(readServer))
	r.HandleFunc("/api/{proxyPath:.*}", restrictedProxy(rasasaServer))

	r.PathPrefix("/").HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "public/index.html")
	})

	n := negroni.New()
	n.Use(negroni.NewRecovery())
	n.Use(noCacheStatic(negroni.NewStatic(http.Dir("public"))))
	n.Use(negroni.NewLogger())
	n.UseHandler(r)

	serve(n)
}

func loginHandler(wr http.ResponseWriter, req *http.Request) {
	store, err := session.Start(context.Background(), wr, req)
	if err != nil {
		http.Error(wr, err.Error(), http.StatusInternalServerError)
		return
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

func restricted(handler http.HandlerFunc) http.HandlerFunc {
	return func(wr http.ResponseWriter, req *http.Request) {
		store, err := session.Start(context.Background(), wr, req)
		if err != nil {
			http.Error(wr, err.Error(), http.StatusInternalServerError)
			return
		}

		isAuthenticated, ok := store.Get("authenticated")
		if ok && isAuthenticated.(bool) {
			handler(wr, req)
			return
		}

		http.Error(wr, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
	}
}

func restrictedProxy(target string) http.HandlerFunc {
	return restricted(proxy(target))
}

func noCacheStatic(s *negroni.Static) negroni.HandlerFunc {
	return negroni.HandlerFunc(func(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
		w.Header().Set("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate")
		w.Header().Set("Pragma", "no-cache")
		w.Header().Set("Expires", "0")
		s.ServeHTTP(w, r, next)
	})
}

func serve(handler http.Handler) {
	gatewayURL, err := url.Parse(os.Getenv("GATEWAY_URL"))
	if err != nil {
		panic(err)
	}
	fmt.Println("Listening on " + gatewayURL.Host)
	http.ListenAndServe(gatewayURL.Host, handler)
}

func initSession() {
	redisOptions := redis.Options{DB: 1}
	redisURL, err := url.Parse(os.Getenv("REDIS_URL"))
	if err == nil {
		redisOptions.Addr = redisURL.Host
		pass, ok := redisURL.User.Password()
		if ok {
			redisOptions.Password = pass
		}
	}

	aMonthInSeconds := 60 * 60 * 24 * 30

	session.InitManager(
		session.SetExpired(int64(aMonthInSeconds)),
		session.SetCookieLifeTime(aMonthInSeconds),
		session.SetStore(redis.NewRedisStore(&redisOptions)))
}

type login struct {
	Username *string `json:"username"`
	Password *string `json:"password"`
}

func (aLogin login) Equal(anotherLogin login) bool {
	return *aLogin.Username == *anotherLogin.Username && *aLogin.Password == *anotherLogin.Password
}

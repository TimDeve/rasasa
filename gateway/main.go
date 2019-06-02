package main

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/gorilla/mux"
	"github.com/urfave/negroni"
)

var rasasaServer = "http://localhost:8091"
var readServer = "http://localhost:8092"

func main() {
	r := mux.NewRouter()

	r.HandleFunc("/api/{proxyPath:v0/read.*}", proxy(readServer))
	r.HandleFunc("/api/{proxyPath:.*}", proxy(rasasaServer))

	r.PathPrefix("/").HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "public/index.html")
	})

	n := negroni.Classic()
	n.UseHandler(r)

	fmt.Println("Listening on 8090")
	http.ListenAndServe(":8090", n)
}

func proxy(target string) func(http.ResponseWriter, *http.Request) {
	url, err := url.Parse(target)
	if err != nil {
		panic(err)
	}

	proxy := httputil.NewSingleHostReverseProxy(url)

	return func(w http.ResponseWriter, r *http.Request) {
		r.URL.Path = mux.Vars(r)["proxyPath"]
		proxy.ServeHTTP(w, r)
	}
}

package shared

import (
	"log"
	"os"
)

const logfileEnvVar = "RASASA_RSS_LOGFILE"

func init() {
	file := os.Getenv(logfileEnvVar)

	if file != "" {
		f, err := os.OpenFile(file, os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
		if err != nil {
			log.Fatalf("Could not open $%s: %v", logfileEnvVar, err)
		}

		log.SetOutput(f)
	}
}

package config

import (
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

var (
	ServerURL = "http://localhost:8080"
	APIKey    = ""
	TokenFile = ""
)

func LoadToken(tokenFile string) {
	if tokenFile == "" {
		home, _ := os.UserHomeDir()
		tokenFile = filepath.Join(home, ".xunbao", "merchant", "token")
	}

	data, err := os.ReadFile(tokenFile)
	if err != nil {
		return
	}
	APIKey = strings.TrimSpace(string(data))
}

// NewClient returns an HTTP client that bypasses system proxy
func NewClient() *http.Client {
	return &http.Client{
		Transport: &http.Transport{
			Proxy: nil,
		},
	}
}

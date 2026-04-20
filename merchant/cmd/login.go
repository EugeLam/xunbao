package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/merchant/pkg/config"
	"github.com/xunbao/merchant/pkg/i18n"
)

var loginCmd = &cobra.Command{
	Use:   "login",
	Short: "Login as merchant",
	Run:   runLogin,
}

var loginArgs struct {
	email    string
	password string
}

func init() {
	loginCmd.Flags().StringVar(&loginArgs.email, "email", "", "Email address")
	loginCmd.Flags().StringVar(&loginArgs.password, "password", "", "Password")
	loginCmd.MarkFlagRequired("email")
	loginCmd.MarkFlagRequired("password")
	rootCmd.AddCommand(loginCmd)
}

func runLogin(cmd *cobra.Command, args []string) {
	reqBody := map[string]string{
		"email":    loginArgs.email,
		"password": loginArgs.password,
	}
	body, _ := json.Marshal(reqBody)

	client := config.NewClient()
	resp, err := client.Post(config.ServerURL+"/api/v1/auth/login", "application/json", bytes.NewBuffer(body))
	if err != nil {
		fmt.Fprintf(os.Stderr, i18n.T("cli.request_failed", err.Error())+"\n")
		os.Exit(1)
	}
	defer resp.Body.Close()

	var result map[string]interface{}
	json.NewDecoder(resp.Body).Decode(&result)

	if resp.StatusCode != http.StatusOK {
		code := int(result["code"].(float64))
		fmt.Fprintf(os.Stderr, i18n.T("cli.login_failed", i18n.TError(code))+"\n")
		os.Exit(1)
	}

	data := result["data"].(map[string]interface{})
	accessToken := data["accessToken"].(string)

	config.APIKey = accessToken

	home, _ := os.UserHomeDir()
	tokenDir := fmt.Sprintf("%s/.xunbao/merchant", home)
	os.MkdirAll(tokenDir, 0755)
	tokenFile := fmt.Sprintf("%s/token", tokenDir)
	os.WriteFile(tokenFile, []byte(accessToken), 0600)

	fmt.Println(i18n.T("cli.login_success"))
	fmt.Printf(i18n.T("cli.token_saved", tokenFile)+"\n")
}

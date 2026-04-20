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

var registerCmd = &cobra.Command{
	Use:   "register",
	Short: "Register as merchant",
	Run:   runRegister,
}

var registerArgs struct {
	email    string
	password string
	name     string
}

func init() {
	registerCmd.Flags().StringVar(&registerArgs.email, "email", "", "Email address")
	registerCmd.Flags().StringVar(&registerArgs.password, "password", "", "Password")
	registerCmd.Flags().StringVar(&registerArgs.name, "name", "", "Merchant name")
	registerCmd.MarkFlagRequired("email")
	registerCmd.MarkFlagRequired("password")
	rootCmd.AddCommand(registerCmd)
}

func runRegister(cmd *cobra.Command, args []string) {
	reqBody := map[string]interface{}{
		"email":         registerArgs.email,
		"password":      registerArgs.password,
		"role":         "MERCHANT",
		"merchantName":  registerArgs.name,
	}
	body, _ := json.Marshal(reqBody)

	client := config.NewClient()
	resp, err := client.Post(config.ServerURL+"/api/v1/auth/register", "application/json", bytes.NewBuffer(body))
	if err != nil {
		fmt.Fprintf(os.Stderr, i18n.T("cli.request_failed", err.Error())+"\n")
		os.Exit(1)
	}
	defer resp.Body.Close()

	var result map[string]interface{}
	json.NewDecoder(resp.Body).Decode(&result)

	if resp.StatusCode != http.StatusOK {
		code := int(result["code"].(float64))
		fmt.Fprintf(os.Stderr, "%s\n", i18n.TError(code))
		os.Exit(1)
	}

	fmt.Println(i18n.T("cli.login_success"))
	fmt.Println("You can now login with: merchant login --email " + registerArgs.email + " --password <password>")
}

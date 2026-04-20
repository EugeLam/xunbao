package address

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/buyer/pkg/config"
	"github.com/xunbao/buyer/pkg/i18n"
)

var AddressCmd = &cobra.Command{
	Use:   "address",
	Short: "Manage addresses",
}

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "Add an address",
	Run:   runAdd,
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List addresses",
	Run:   runList,
}

var setDefaultCmd = &cobra.Command{
	Use:   "set-default",
	Short: "Set default address",
	Run:   runSetDefault,
}

var deleteCmd = &cobra.Command{
	Use:   "delete",
	Short: "Delete an address",
	Run:   runDelete,
}

var addArgs struct {
	name         string
	phone        string
	province     string
	city         string
	district     string
	detail       string
	isDefault    bool
}

var defaultArgs struct {
	id int64
}

var deleteArgs struct {
	id int64
}

func init() {
	addCmd.Flags().StringVar(&addArgs.name, "name", "", "Receiver name (required)")
	addCmd.Flags().StringVar(&addArgs.phone, "phone", "", "Phone number (required)")
	addCmd.Flags().StringVar(&addArgs.province, "province", "", "Province (required)")
	addCmd.Flags().StringVar(&addArgs.city, "city", "", "City (required)")
	addCmd.Flags().StringVar(&addArgs.district, "district", "", "District")
	addCmd.Flags().StringVar(&addArgs.detail, "detail", "", "Detail address (required)")
	addCmd.Flags().BoolVar(&addArgs.isDefault, "default", false, "Set as default")
	addCmd.MarkFlagRequired("name")
	addCmd.MarkFlagRequired("phone")
	addCmd.MarkFlagRequired("province")
	addCmd.MarkFlagRequired("city")
	addCmd.MarkFlagRequired("detail")

	setDefaultCmd.Flags().Int64Var(&defaultArgs.id, "id", 0, "Address ID")
	setDefaultCmd.MarkFlagRequired("id")

	deleteCmd.Flags().Int64Var(&deleteArgs.id, "id", 0, "Address ID")
	deleteCmd.MarkFlagRequired("id")

	AddressCmd.AddCommand(addCmd, listCmd, setDefaultCmd, deleteCmd)
}

func runAdd(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"receiverName":  addArgs.name,
		"phone":         addArgs.phone,
		"province":      addArgs.province,
		"city":          addArgs.city,
		"district":      addArgs.district,
		"detailAddress": addArgs.detail,
		"isDefault":     addArgs.isDefault,
	}
	body, _ := json.Marshal(reqBody)

	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/addresses", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+config.APIKey)

	client := config.NewClient()
	resp, err := client.Do(req)
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

	fmt.Println("Address added!")
}

func runList(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	req, _ := http.NewRequest("GET", config.ServerURL+"/api/v1/addresses", nil)
	req.Header.Set("Authorization", "Bearer "+config.APIKey)

	client := config.NewClient()
	resp, err := client.Do(req)
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

	data, _ := json.MarshalIndent(result["data"], "", "  ")
	fmt.Println(string(data))
}

func runSetDefault(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/addresses/%d/default", config.ServerURL, defaultArgs.id)
	req, _ := http.NewRequest("PUT", url, nil)
	req.Header.Set("Authorization", "Bearer "+config.APIKey)

	client := config.NewClient()
	resp, err := client.Do(req)
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

	fmt.Println("Default address set!")
}

func runDelete(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/addresses/%d", config.ServerURL, deleteArgs.id)
	req, _ := http.NewRequest("DELETE", url, nil)
	req.Header.Set("Authorization", "Bearer "+config.APIKey)

	client := config.NewClient()
	resp, err := client.Do(req)
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

	fmt.Println("Address deleted!")
}

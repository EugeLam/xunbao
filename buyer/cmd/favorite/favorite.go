package favorite

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

var FavoriteCmd = &cobra.Command{
	Use:   "favorite",
	Short: "Manage favorites",
}

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "Add to favorites",
	Run:   runAdd,
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List favorites",
	Run:   runList,
}

var removeCmd = &cobra.Command{
	Use:   "remove",
	Short: "Remove from favorites",
	Run:   runRemove,
}

var addArgs struct {
	productID int64
}

var removeArgs struct {
	productID int64
}

func init() {
	addCmd.Flags().Int64Var(&addArgs.productID, "product-id", 0, "Product ID")
	addCmd.MarkFlagRequired("product-id")

	removeCmd.Flags().Int64Var(&removeArgs.productID, "product-id", 0, "Product ID")
	removeCmd.MarkFlagRequired("product-id")

	FavoriteCmd.AddCommand(addCmd, listCmd, removeCmd)
}

func runAdd(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"productId": addArgs.productID,
	}
	body, _ := json.Marshal(reqBody)

	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/favorites", bytes.NewBuffer(body))
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

	fmt.Println(i18n.T("cli.added_to_favorites"))
}

func runList(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	req, _ := http.NewRequest("GET", config.ServerURL+"/api/v1/favorites", nil)
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

func runRemove(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/favorites/%d", config.ServerURL, removeArgs.productID)
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

	fmt.Println(i18n.T("cli.removed_from_favorites"))
}

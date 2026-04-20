package order

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

var OrderCmd = &cobra.Command{
	Use:   "order",
	Short: "Manage orders",
}

var createCmd = &cobra.Command{
	Use:   "create",
	Short: "Create an order",
	Run:   runCreate,
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List orders",
	Run:   runList,
}

var viewCmd = &cobra.Command{
	Use:   "view",
	Short: "View order details",
	Run:   runView,
}

var createArgs struct {
	addressID int64
	items     string
}

var listArgs struct {
	page int
	size int
}

var viewArgs struct {
	id int64
}

func init() {
	createCmd.Flags().Int64Var(&createArgs.addressID, "address-id", 0, "Address ID (required)")
	createCmd.Flags().StringVar(&createArgs.items, "items", "", "Order items JSON array (required if cart is empty)")
	createCmd.MarkFlagRequired("address-id")

	listCmd.Flags().IntVar(&listArgs.page, "page", 0, "Page number")
	listCmd.Flags().IntVar(&listArgs.size, "size", 20, "Page size")

	viewCmd.Flags().Int64Var(&viewArgs.id, "id", 0, "Order ID")
	viewCmd.MarkFlagRequired("id")

	OrderCmd.AddCommand(createCmd, listCmd, viewCmd)
}

func runCreate(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	var items []map[string]interface{}
	if createArgs.items != "" {
		json.Unmarshal([]byte(createArgs.items), &items)
	}

	reqBody := map[string]interface{}{
		"addressId": createArgs.addressID,
		"items":     items,
	}
	body, _ := json.Marshal(reqBody)

	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/orders", bytes.NewBuffer(body))
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

	data := result["data"].(map[string]interface{})
	fmt.Printf("Order created! ID: %.0f\n", data["id"])
}

func runList(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/orders?page=%d&size=%d", config.ServerURL, listArgs.page, listArgs.size)
	req, _ := http.NewRequest("GET", url, nil)
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

func runView(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/orders/%d", config.ServerURL, viewArgs.id)
	req, _ := http.NewRequest("GET", url, nil)
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

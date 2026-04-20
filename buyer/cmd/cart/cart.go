package cart

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

var CartCmd = &cobra.Command{
	Use:   "cart",
	Short: "Manage cart",
}

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "Add to cart",
	Run:   runAdd,
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List cart items",
	Run:   runList,
}

var updateCmd = &cobra.Command{
	Use:   "update",
	Short: "Update cart item quantity",
	Run:   runUpdate,
}

var removeCmd = &cobra.Command{
	Use:   "remove",
	Short: "Remove from cart",
	Run:   runRemove,
}

var clearCmd = &cobra.Command{
	Use:   "clear",
	Short: "Clear cart",
	Run:   runClear,
}

var addArgs struct {
	productID int64
	variantID int64
	quantity  int
}

var updateArgs struct {
	id       int64
	quantity int
}

var removeArgs struct {
	id int64
}

func init() {
	addCmd.Flags().Int64Var(&addArgs.productID, "product-id", 0, "Product ID")
	addCmd.Flags().Int64Var(&addArgs.variantID, "variant-id", 0, "Variant ID")
	addCmd.Flags().IntVar(&addArgs.quantity, "quantity", 1, "Quantity")
	addCmd.MarkFlagRequired("product-id")
	addCmd.MarkFlagRequired("quantity")

	updateCmd.Flags().Int64Var(&updateArgs.id, "id", 0, "Cart item ID")
	updateCmd.Flags().IntVar(&updateArgs.quantity, "quantity", 1, "Quantity")
	updateCmd.MarkFlagRequired("id")

	removeCmd.Flags().Int64Var(&removeArgs.id, "id", 0, "Cart item ID")
	removeCmd.MarkFlagRequired("id")

	CartCmd.AddCommand(addCmd, listCmd, updateCmd, removeCmd, clearCmd)
}

func runAdd(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"productId": addArgs.productID,
		"quantity":  addArgs.quantity,
	}
	if addArgs.variantID > 0 {
		reqBody["variantId"] = addArgs.variantID
	}
	body, _ := json.Marshal(reqBody)

	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/cart", bytes.NewBuffer(body))
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

	fmt.Println("Added to cart!")
}

func runList(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	req, _ := http.NewRequest("GET", config.ServerURL+"/api/v1/cart", nil)
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

func runUpdate(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]int{
		"quantity": updateArgs.quantity,
	}
	body, _ := json.Marshal(reqBody)
	url := fmt.Sprintf("%s/api/v1/cart/%d", config.ServerURL, updateArgs.id)
	req, _ := http.NewRequest("PUT", url, bytes.NewBuffer(body))
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

	fmt.Println("Cart updated!")
}

func runRemove(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	url := fmt.Sprintf("%s/api/v1/cart/%d", config.ServerURL, removeArgs.id)
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

	fmt.Println("Removed from cart!")
}

func runClear(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	req, _ := http.NewRequest("DELETE", config.ServerURL+"/api/v1/cart", nil)
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

	fmt.Println("Cart cleared!")
}

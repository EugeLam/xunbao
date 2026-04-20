package order

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

var OrderCmd = &cobra.Command{
	Use:   "order",
	Short: "Manage orders",
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

var shipCmd = &cobra.Command{
	Use:   "ship",
	Short: "Ship an order",
	Run:   runShip,
}

var listArgs struct {
	page int
	size int
}

var viewArgs struct {
	id int64
}

var shipArgs struct {
	id             int64
	expressCompany string
	trackingNumber string
}

func init() {
	listCmd.Flags().IntVar(&listArgs.page, "page", 0, "Page number")
	listCmd.Flags().IntVar(&listArgs.size, "size", 20, "Page size")

	viewCmd.Flags().Int64Var(&viewArgs.id, "id", 0, "Order ID")
	viewCmd.MarkFlagRequired("id")

	shipCmd.Flags().Int64Var(&shipArgs.id, "id", 0, "Order ID")
	shipCmd.Flags().StringVar(&shipArgs.expressCompany, "express", "", "Express company")
	shipCmd.Flags().StringVar(&shipArgs.trackingNumber, "tracking", "", "Tracking number")
	shipCmd.MarkFlagRequired("id")
	shipCmd.MarkFlagRequired("express")
	shipCmd.MarkFlagRequired("tracking")

	OrderCmd.AddCommand(listCmd, viewCmd, shipCmd)
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

func runShip(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]string{
		"expressCompany": shipArgs.expressCompany,
		"trackingNumber": shipArgs.trackingNumber,
	}
	body, _ := json.Marshal(reqBody)
	url := fmt.Sprintf("%s/api/v1/orders/%d/express", config.ServerURL, shipArgs.id)
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

	fmt.Println("Order shipped!")
}

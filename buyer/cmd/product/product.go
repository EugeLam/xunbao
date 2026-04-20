package product

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/buyer/pkg/config"
	"github.com/xunbao/buyer/pkg/i18n"
)

var ProductCmd = &cobra.Command{
	Use:   "product",
	Short: "Manage products",
}

var searchCmd = &cobra.Command{
	Use:   "search",
	Short: "Search products",
	Run:   runSearch,
}

var viewCmd = &cobra.Command{
	Use:   "view",
	Short: "View product details",
	Run:   runView,
}

var searchArgs struct {
	keyword     string
	categoryID  int64
	minPrice    float64
	maxPrice    float64
	inStock     bool
	page        int
	size        int
}

var viewArgs struct {
	id int64
}

func init() {
	searchCmd.Flags().StringVar(&searchArgs.keyword, "keyword", "", "Search keyword")
	searchCmd.Flags().Int64Var(&searchArgs.categoryID, "category-id", 0, "Category ID")
	searchCmd.Flags().Float64Var(&searchArgs.minPrice, "min-price", 0, "Minimum price")
	searchCmd.Flags().Float64Var(&searchArgs.maxPrice, "max-price", 0, "Maximum price")
	searchCmd.Flags().BoolVar(&searchArgs.inStock, "in-stock", false, "Only show in-stock items")
	searchCmd.Flags().IntVar(&searchArgs.page, "page", 0, "Page number")
	searchCmd.Flags().IntVar(&searchArgs.size, "size", 20, "Page size")

	viewCmd.Flags().Int64Var(&viewArgs.id, "id", 0, "Product ID")
	viewCmd.MarkFlagRequired("id")

	ProductCmd.AddCommand(searchCmd, viewCmd)
}

func runSearch(cmd *cobra.Command, args []string) {
	url := fmt.Sprintf("%s/api/v1/products?page=%d&size=%d", config.ServerURL, searchArgs.page, searchArgs.size)
	if searchArgs.keyword != "" {
		url += "&keyword=" + searchArgs.keyword
	}
	if searchArgs.categoryID > 0 {
		url += fmt.Sprintf("&categoryId=%d", searchArgs.categoryID)
	}
	if searchArgs.minPrice > 0 {
		url += fmt.Sprintf("&minPrice=%.2f", searchArgs.minPrice)
	}
	if searchArgs.maxPrice > 0 {
		url += fmt.Sprintf("&maxPrice=%.2f", searchArgs.maxPrice)
	}
	if searchArgs.inStock {
		url += "&inStock=true"
	}

	req, _ := http.NewRequest("GET", url, nil)
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
	url := fmt.Sprintf("%s/api/v1/products/%d", config.ServerURL, viewArgs.id)
	req, _ := http.NewRequest("GET", url, nil)
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

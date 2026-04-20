package review

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/merchant/pkg/config"
	"github.com/xunbao/merchant/pkg/i18n"
)

var ReviewCmd = &cobra.Command{
	Use:   "review",
	Short: "Manage reviews",
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List product reviews",
	Run:   runList,
}

var listArgs struct {
	productID int64
	page      int
	size      int
}

func init() {
	listCmd.Flags().Int64Var(&listArgs.productID, "product-id", 0, "Product ID")
	listCmd.MarkFlagRequired("product-id")
	listCmd.Flags().IntVar(&listArgs.page, "page", 0, "Page number")
	listCmd.Flags().IntVar(&listArgs.size, "size", 20, "Page size")

	ReviewCmd.AddCommand(listCmd)
}

func runList(cmd *cobra.Command, args []string) {
	url := fmt.Sprintf("%s/api/v1/products/%d/reviews?page=%d&size=%d",
		config.ServerURL, listArgs.productID, listArgs.page, listArgs.size)
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

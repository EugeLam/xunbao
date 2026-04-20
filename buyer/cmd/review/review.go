package review

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

var ReviewCmd = &cobra.Command{
	Use:   "review",
	Short: "Manage reviews",
}

var addCmd = &cobra.Command{
	Use:   "add",
	Short: "Add a review",
	Run:   runAdd,
}

var addArgs struct {
	productID int64
	orderID   int64
	rating    int
	content   string
}

func init() {
	addCmd.Flags().Int64Var(&addArgs.productID, "product-id", 0, "Product ID")
	addCmd.Flags().Int64Var(&addArgs.orderID, "order-id", 0, "Order ID")
	addCmd.Flags().IntVar(&addArgs.rating, "rating", 5, "Rating (1-5)")
	addCmd.Flags().StringVar(&addArgs.content, "content", "", "Review content")
	addCmd.MarkFlagRequired("product-id")
	addCmd.MarkFlagRequired("rating")

	ReviewCmd.AddCommand(addCmd)
}

func runAdd(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"rating": addArgs.rating,
		"content": addArgs.content,
	}
	body, _ := json.Marshal(reqBody)

	url := fmt.Sprintf("%s/api/v1/products/%d/reviews", config.ServerURL, addArgs.productID)
	req, _ := http.NewRequest("POST", url, bytes.NewBuffer(body))
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

	fmt.Println("Review added!")
}

package category

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/buyer/pkg/config"
	"github.com/xunbao/buyer/pkg/i18n"
)

var CategoryCmd = &cobra.Command{
	Use:   "category",
	Short: "Manage categories",
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List categories",
	Run:   runList,
}

var parentID int64

func init() {
	listCmd.Flags().Int64Var(&parentID, "parent-id", 0, "Parent category ID (0 for root)")

	CategoryCmd.AddCommand(listCmd)
}

func runList(cmd *cobra.Command, args []string) {
	url := config.ServerURL + "/api/v1/categories"
	if parentID > 0 {
		url = fmt.Sprintf("%s/api/v1/categories?parentId=%d", config.ServerURL, parentID)
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

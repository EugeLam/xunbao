package category

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

var CategoryCmd = &cobra.Command{
	Use:   "category",
	Short: "Manage categories",
}

var createCmd = &cobra.Command{
	Use:   "create",
	Short: "Create a category",
	Run:   runCreate,
}

var createArgs struct {
	name     string
	parentID int64
}

func init() {
	createCmd.Flags().StringVar(&createArgs.name, "name", "", "Category name")
	createCmd.MarkFlagRequired("name")
	createCmd.Flags().Int64Var(&createArgs.parentID, "parent-id", 0, "Parent category ID")

	CategoryCmd.AddCommand(createCmd)
}

func runCreate(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"name": createArgs.name,
	}
	if createArgs.parentID > 0 {
		reqBody["parentId"] = createArgs.parentID
	}

	body, _ := json.Marshal(reqBody)
	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/categories", bytes.NewBuffer(body))
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

	fmt.Println("Category created successfully!")
}

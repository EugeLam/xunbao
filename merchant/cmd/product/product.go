package product

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"github.com/xunbao/merchant/pkg/config"
	"github.com/xunbao/merchant/pkg/i18n"
)

var ProductCmd = &cobra.Command{
	Use:   "product",
	Short: "Manage products",
}

var createCmd = &cobra.Command{
	Use:   "create",
	Short: "Create a product",
	Run:   runCreate,
}

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "List products",
	Run:   runList,
}

var updateCmd = &cobra.Command{
	Use:   "update",
	Short: "Update a product",
	Run:   runUpdate,
}

var deleteCmd = &cobra.Command{
	Use:   "delete",
	Short: "Delete a product",
	Run:   runDelete,
}

var uploadImageCmd = &cobra.Command{
	Use:   "upload-image",
	Short: "Upload product image",
	Run:   runUploadImage,
}

var createArgs struct {
	name        string
	description string
	price       float64
	stock       int
	categoryID  int64
	imageURL    string
}

var updateArgs struct {
	id    int64
	name  string
	price float64
	stock int
}

var deleteArgs struct {
	id int64
}

var uploadArgs struct {
	id   int64
	file string
}

func init() {
	createCmd.Flags().StringVar(&createArgs.name, "name", "", "Product name (required)")
	createCmd.Flags().StringVar(&createArgs.description, "description", "", "Description")
	createCmd.Flags().Float64Var(&createArgs.price, "price", 0, "Price (required)")
	createCmd.Flags().IntVar(&createArgs.stock, "stock", 0, "Stock quantity (required)")
	createCmd.Flags().Int64Var(&createArgs.categoryID, "category-id", 0, "Category ID")
	createCmd.Flags().StringVar(&createArgs.imageURL, "image-url", "", "Image URL")
	createCmd.MarkFlagRequired("name")
	createCmd.MarkFlagRequired("price")
	createCmd.MarkFlagRequired("stock")

	updateCmd.Flags().Int64Var(&updateArgs.id, "id", 0, "Product ID (required)")
	updateCmd.Flags().StringVar(&updateArgs.name, "name", "", "Product name")
	updateCmd.Flags().Float64Var(&updateArgs.price, "price", 0, "Price")
	updateCmd.Flags().IntVar(&updateArgs.stock, "stock", 0, "Stock quantity")
	updateCmd.MarkFlagRequired("id")

	deleteCmd.Flags().Int64Var(&deleteArgs.id, "id", 0, "Product ID (required)")
	deleteCmd.MarkFlagRequired("id")

	uploadImageCmd.Flags().Int64Var(&uploadArgs.id, "id", 0, "Product ID (required)")
	uploadImageCmd.Flags().StringVar(&uploadArgs.file, "file", "", "Image file path (required)")
	uploadImageCmd.MarkFlagRequired("id")
	uploadImageCmd.MarkFlagRequired("file")

	ProductCmd.AddCommand(createCmd, listCmd, updateCmd, deleteCmd, uploadImageCmd)
}

func runCreate(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{
		"name":        createArgs.name,
		"description": createArgs.description,
		"price":       createArgs.price,
		"stock":       createArgs.stock,
	}
	if createArgs.categoryID > 0 {
		reqBody["categoryId"] = createArgs.categoryID
	}
	if createArgs.imageURL != "" {
		reqBody["imageUrl"] = createArgs.imageURL
	}

	body, _ := json.Marshal(reqBody)
	req, _ := http.NewRequest("POST", config.ServerURL+"/api/v1/products", bytes.NewBuffer(body))
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
	fmt.Printf("Product created! ID: %.0f\n", data["id"])
}

func runList(cmd *cobra.Command, args []string) {
	req, _ := http.NewRequest("GET", config.ServerURL+"/api/v1/products?page=0&size=20", nil)

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
	fmt.Println("Products:")
	fmt.Println(string(data))
}

func runUpdate(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	reqBody := map[string]interface{}{}
	if updateArgs.name != "" {
		reqBody["name"] = updateArgs.name
	}
	if updateArgs.price > 0 {
		reqBody["price"] = updateArgs.price
	}
	if updateArgs.stock > 0 {
		reqBody["stock"] = updateArgs.stock
	}

	body, _ := json.Marshal(reqBody)
	req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/api/v1/products/%d", config.ServerURL, updateArgs.id), bytes.NewBuffer(body))
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

	fmt.Println("Product updated!")
}

func runDelete(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	req, _ := http.NewRequest("DELETE", fmt.Sprintf("%s/api/v1/products/%d", config.ServerURL, deleteArgs.id), nil)
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

	fmt.Println("Product deleted!")
}

func runUploadImage(cmd *cobra.Command, args []string) {
	config.LoadToken(config.TokenFile)

	if config.APIKey == "" {
		fmt.Fprintln(os.Stderr, i18n.T("cli.no_token"))
		os.Exit(1)
	}

	file, err := os.Open(uploadArgs.file)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to open file: %v\n", err)
		os.Exit(1)
	}
	defer file.Close()

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile("file", uploadArgs.file)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to create form file: %v\n", err)
		os.Exit(1)
	}
	io.Copy(part, file)
	writer.Close()

	req, _ := http.NewRequest("PUT", fmt.Sprintf("%s/api/v1/products/%d/image", config.ServerURL, uploadArgs.id), body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
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

	fmt.Println("Image uploaded!")
}

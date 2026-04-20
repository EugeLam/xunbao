package cmd

import (
	"github.com/spf13/cobra"
	"github.com/xunbao/merchant/pkg/config"
	"github.com/xunbao/merchant/pkg/i18n"
	"github.com/xunbao/merchant/cmd/category"
	"github.com/xunbao/merchant/cmd/order"
	"github.com/xunbao/merchant/cmd/product"
	"github.com/xunbao/merchant/cmd/review"
)

var rootCmd = &cobra.Command{
	Use:   "merchant",
	Short: "Merchant CLI for xunbao shopping system",
}

var lang string

func Execute() error {
	return rootCmd.Execute()
}

func init() {
	rootCmd.PersistentFlags().StringVar(&config.ServerURL, "server", "http://localhost:8080", "Backend server URL")
	rootCmd.PersistentFlags().StringVar(&config.TokenFile, "token-file", "", "Token file path")
	rootCmd.PersistentFlags().StringVar(&lang, "lang", "en", "Language (en, zh)")

	rootCmd.PersistentPreRun = func(cmd *cobra.Command, args []string) {
		i18n.SetLang(lang)
	}

	rootCmd.AddCommand(category.CategoryCmd)
	rootCmd.AddCommand(product.ProductCmd)
	rootCmd.AddCommand(order.OrderCmd)
	rootCmd.AddCommand(review.ReviewCmd)
}

package cmd

import (
	"github.com/spf13/cobra"
	"github.com/xunbao/buyer/pkg/config"
	"github.com/xunbao/buyer/pkg/i18n"
	"github.com/xunbao/buyer/cmd/address"
	"github.com/xunbao/buyer/cmd/cart"
	"github.com/xunbao/buyer/cmd/category"
	"github.com/xunbao/buyer/cmd/favorite"
	"github.com/xunbao/buyer/cmd/order"
	"github.com/xunbao/buyer/cmd/product"
	"github.com/xunbao/buyer/cmd/review"
)

var rootCmd = &cobra.Command{
	Use:   "buyer",
	Short: "Buyer CLI for xunbao shopping system",
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

	rootCmd.AddCommand(loginCmd)
	rootCmd.AddCommand(registerCmd)
	rootCmd.AddCommand(category.CategoryCmd)
	rootCmd.AddCommand(product.ProductCmd)
	rootCmd.AddCommand(favorite.FavoriteCmd)
	rootCmd.AddCommand(address.AddressCmd)
	rootCmd.AddCommand(cart.CartCmd)
	rootCmd.AddCommand(order.OrderCmd)
	rootCmd.AddCommand(review.ReviewCmd)
}

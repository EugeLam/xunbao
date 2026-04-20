package i18n

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"
)

var (
	messages    map[string]string
	lang        = "en"
	mu          sync.RWMutex
)

func SetLang(l string) {
	switch l {
	case "zh", "zh-CN", "zh-TW":
		lang = "zh"
	default:
		lang = "en"
	}
	loadMessages()
}

func GetLang() string {
	return lang
}

func loadMessages() {
	mu.Lock()
	defer mu.Unlock()

	messages = make(map[string]string)

	var fileName string
	if lang == "zh" {
		fileName = "messages_zh.json"
	} else {
		fileName = "messages_en.json"
	}

	exePath, err := os.Executable()
	if err != nil {
		return
	}
	dir := filepath.Dir(exePath)

	filePath := filepath.Join(dir, "pkg", "i18n", fileName)
	if _, err := os.Stat(filePath); err == nil {
		data, err := os.ReadFile(filePath)
		if err != nil {
			return
		}
		json.Unmarshal(data, &messages)
		return
	}

	moduleRoot := findModuleRoot(dir)
	if moduleRoot != "" {
		filePath = filepath.Join(moduleRoot, "pkg", "i18n", fileName)
		data, err := os.ReadFile(filePath)
		if err != nil {
			return
		}
		json.Unmarshal(data, &messages)
	}
}

func findModuleRoot(dir string) string {
	for {
		if _, err := os.Stat(filepath.Join(dir, "go.mod")); err == nil {
			return dir
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}
	return ""
}

func T(key string, args ...interface{}) string {
	mu.RLock()
	defer mu.RUnlock()
	if messages == nil {
		return key
	}

	msg, ok := messages[key]
	if !ok {
		return key
	}

	if len(args) == 0 {
		return msg
	}

	result := msg
	for i, arg := range args {
		placeholder := fmt.Sprintf("{%d}", i)
		result = strings.Replace(result, placeholder, fmt.Sprintf("%v", arg), 1)
	}
	return result
}

func TError(code int) string {
	key := codeToKey(code)
	return T(key)
}

func codeToKey(code int) string {
	switch code {
	case 400:
		return "err.bad_request"
	case 401:
		return "err.unauthorized"
	case 403:
		return "err.forbidden"
	case 404:
		return "err.not_found"
	case 500:
		return "err.internal"
	case 1001:
		return "err.email_exists"
	case 1002:
		return "err.invalid_credentials"
	case 1003:
		return "err.invalid_refresh_token"
	case 1004:
		return "err.token_expired"
	case 1010:
		return "err.user_not_found"
	case 1011:
		return "err.product_not_found"
	case 1012:
		return "err.merchant_not_found"
	case 1013:
		return "err.access_denied"
	case 1014:
		return "err.insufficient_stock"
	case 1015:
		return "err.cart_item_not_found"
	case 1016:
		return "err.address_not_found"
	case 1017:
		return "err.order_not_found"
	case 1018:
		return "err.variant_not_found"
	case 1019:
		return "err.order_item_not_found"
	case 1020:
		return "err.sku_exists"
	case 1021:
		return "err.already_favorited"
	case 1022:
		return "err.review_not_found"
	case 1023:
		return "err.category_not_found"
	case 1024:
		return "err.invalid_status"
	default:
		if code >= 400 && code < 500 {
			return "err.bad_request"
		} else if code >= 500 {
			return "err.internal"
		}
		return strings.ReplaceAll(T("err.internal"), "Internal server error", fmt.Sprintf("Error %d", code))
	}
}

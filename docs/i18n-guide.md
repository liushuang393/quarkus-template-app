# 国際化（i18n）開発ガイド

このガイドでは、多言語対応の実装方法について説明します。

## 🌐 国際化の概要

- **多言語サポート**: 日本語、英語、中国語対応
- **動的言語切り替え**: ユーザーが実行時に言語を変更可能
- **Accept-Language対応**: ブラウザの言語設定を自動検出
- **メッセージ外部化**: ハードコードされた文字列の排除

## 📁 ファイル構成

```
src/main/resources/
├── messages.properties           # デフォルト（英語）
├── messages_ja.properties        # 日本語
├── messages_zh.properties        # 中国語
└── templates/
    └── login.html                # 国際化対応テンプレート

src/main/java/com/example/service/
└── MessageService.java          # メッセージサービス
```

## 📝 メッセージファイル

### デフォルト（英語）- messages.properties

```properties
# Authentication messages
auth.login.title=Login - Quarkus Authentication System
auth.login.header=Quarkus Authentication System
auth.login.description=Please login or register
auth.login.username=Username:
auth.login.password=Password:
auth.login.button=Login
auth.login.toggle=Register here
auth.login.failed=Login failed
auth.login.success=Login successful

# Registration messages
auth.register.title=Register - Quarkus Authentication System
auth.register.username=Username:
auth.register.email=Email:
auth.register.password=Password:
auth.register.role=Role:
auth.register.button=Register
auth.register.toggle=Login here
auth.register.success=Registration completed. Please login.
auth.register.failed=Registration failed

# Menu messages
menu.title=Menu
menu.user=User:
menu.role=Role:
menu.logout=Logout

# Menu items
menu.user.management=User Management
menu.system.settings=System Settings
menu.sales.management=Sales Management
menu.reports=Reports
menu.customer.management=Customer Management
menu.profile=Profile
menu.settings=Settings

# Error messages
error.user.already.exists=Username already exists
error.authentication.failed=Authentication failed
error.validation.error=Validation error
error.internal.server.error=Internal server error occurred
error.menu.fetch.failed=Failed to fetch menu

# Validation messages
validation.username.required=Username is required
validation.password.required=Password is required
validation.email.required=Email is required
validation.email.invalid=Invalid email format

# Business messages
business.user.registered=User registered successfully
business.user.login.success=User login successful
business.user.logout.success=User logout successful

# Role names
role.admin=ADMIN
role.user=USER
role.sales=SALES

# General messages
general.success=Success
general.error=Error
general.warning=Warning
general.info=Information
```

### 日本語 - messages_ja.properties

```properties
# Authentication messages
auth.login.title=ログイン - Quarkus認証システム
auth.login.header=Quarkus認証システム
auth.login.description=ログインまたは新規登録してください
auth.login.username=ユーザー名：
auth.login.password=パスワード：
auth.login.button=ログイン
auth.login.toggle=新規登録はこちら
auth.login.failed=ログインに失敗しました
auth.login.success=ログインに成功しました

# Registration messages
auth.register.title=新規登録 - Quarkus認証システム
auth.register.username=ユーザー名：
auth.register.email=メールアドレス：
auth.register.password=パスワード：
auth.register.role=ロール：
auth.register.button=登録
auth.register.toggle=ログインはこちら
auth.register.success=登録が完了しました。ログインしてください。
auth.register.failed=登録に失敗しました

# Menu messages
menu.title=メニュー
menu.user=ユーザー：
menu.role=ロール：
menu.logout=ログアウト

# Menu items
menu.user.management=ユーザー管理
menu.system.settings=システム設定
menu.sales.management=売上管理
menu.reports=レポート
menu.customer.management=顧客管理
menu.profile=プロフィール
menu.settings=設定

# Error messages
error.user.already.exists=ユーザー名が既に存在します
error.authentication.failed=認証に失敗しました
error.validation.error=入力データに不正があります
error.internal.server.error=内部サーバーエラーが発生しました
error.menu.fetch.failed=メニューの取得に失敗しました

# Validation messages
validation.username.required=ユーザー名は必須です
validation.password.required=パスワードは必須です
validation.email.required=メールアドレスは必須です
validation.email.invalid=メールアドレスの形式が正しくありません

# Business messages
business.user.registered=ユーザー登録が完了しました
business.user.login.success=ユーザーログインが成功しました
business.user.logout.success=ユーザーログアウトが成功しました

# Role names
role.admin=管理者
role.user=ユーザー
role.sales=営業

# General messages
general.success=成功
general.error=エラー
general.warning=警告
general.info=情報
```

### 中国語 - messages_zh.properties

```properties
# Authentication messages
auth.login.title=登录 - Quarkus认证系统
auth.login.header=Quarkus认证系统
auth.login.description=请登录或注册
auth.login.username=用户名：
auth.login.password=密码：
auth.login.button=登录
auth.login.toggle=注册账号
auth.login.failed=登录失败
auth.login.success=登录成功

# Registration messages
auth.register.title=注册 - Quarkus认证系统
auth.register.username=用户名：
auth.register.email=邮箱：
auth.register.password=密码：
auth.register.role=角色：
auth.register.button=注册
auth.register.toggle=返回登录
auth.register.success=注册完成，请登录。
auth.register.failed=注册失败

# Menu messages
menu.title=菜单
menu.user=用户：
menu.role=角色：
menu.logout=退出登录

# Menu items
menu.user.management=用户管理
menu.system.settings=系统设置
menu.sales.management=销售管理
menu.reports=报表
menu.customer.management=客户管理
menu.profile=个人资料
menu.settings=设置

# Error messages
error.user.already.exists=用户名已存在
error.authentication.failed=认证失败
error.validation.error=输入数据有误
error.internal.server.error=内部服务器错误
error.menu.fetch.failed=获取菜单失败

# Validation messages
validation.username.required=用户名必填
validation.password.required=密码必填
validation.email.required=邮箱必填
validation.email.invalid=邮箱格式无效

# Business messages
business.user.registered=用户注册成功
business.user.login.success=用户登录成功
business.user.logout.success=用户退出成功

# Role names
role.admin=管理员
role.user=用户
role.sales=销售

# General messages
general.success=成功
general.error=错误
general.warning=警告
general.info=信息
```

## 🔧 MessageService実装

```java
package com.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国際化メッセージサービス
 * Accept-Languageヘッダーに基づいて適切な言語のメッセージを返す
 */
@ApplicationScoped
public class MessageService {
    
    private static final Logger LOG = Logger.getLogger(MessageService.class);
    
    private static final String BUNDLE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    
    /**
     * メッセージキーに対応するローカライズされたメッセージを取得
     * 
     * @param key メッセージキー
     * @param locale ロケール
     * @return ローカライズされたメッセージ
     */
    public String getMessage(String key, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            return bundle.getString(key);
        } catch (Exception e) {
            LOG.warn("メッセージが見つかりません: key=" + key + ", locale=" + locale);
            // フォールバックとしてデフォルトロケールを試す
            try {
                ResourceBundle defaultBundle = ResourceBundle.getBundle(BUNDLE_NAME, DEFAULT_LOCALE);
                return defaultBundle.getString(key);
            } catch (Exception ex) {
                LOG.error("デフォルトメッセージも見つかりません: key=" + key, ex);
                return key; // キー自体を返す
            }
        }
    }
    
    /**
     * メッセージキーに対応するローカライズされたメッセージを取得（デフォルトロケール使用）
     * 
     * @param key メッセージキー
     * @return ローカライズされたメッセージ
     */
    public String getMessage(String key) {
        return getMessage(key, DEFAULT_LOCALE);
    }
    
    /**
     * Accept-Languageヘッダーからロケールを解析
     * 
     * @param acceptLanguage Accept-Languageヘッダーの値
     * @return 解析されたロケール
     */
    public Locale parseLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty()) {
            return DEFAULT_LOCALE;
        }
        
        // Accept-Languageヘッダーの最初の言語を使用
        String[] languages = acceptLanguage.split(",");
        if (languages.length > 0) {
            String primaryLanguage = languages[0].trim();
            // q値を除去
            if (primaryLanguage.contains(";")) {
                primaryLanguage = primaryLanguage.split(";")[0].trim();
            }
            
            // サポートされている言語かチェック
            switch (primaryLanguage.toLowerCase()) {
                case "ja":
                case "ja-jp":
                    return Locale.JAPANESE;
                case "zh":
                case "zh-cn":
                case "zh-hans":
                    return Locale.CHINESE;
                case "en":
                case "en-us":
                case "en-gb":
                default:
                    return Locale.ENGLISH;
            }
        }
        
        return DEFAULT_LOCALE;
    }
    
    /**
     * HttpHeadersからロケールを取得してメッセージを返す
     * 
     * @param key メッセージキー
     * @param headers HTTPヘッダー
     * @return ローカライズされたメッセージ
     */
    public String getMessage(String key, HttpHeaders headers) {
        if (headers != null) {
            String acceptLanguage = headers.getHeaderString("Accept-Language");
            Locale locale = parseLocale(acceptLanguage);
            return getMessage(key, locale);
        }
        return getMessage(key);
    }
    
    /**
     * パラメータ付きメッセージの取得
     * 
     * @param key メッセージキー
     * @param locale ロケール
     * @param params パラメータ
     * @return フォーマットされたメッセージ
     */
    public String getMessage(String key, Locale locale, Object... params) {
        String message = getMessage(key, locale);
        if (params != null && params.length > 0) {
            return String.format(message, params);
        }
        return message;
    }
    
    /**
     * 利用可能な言語一覧を取得
     * 
     * @return サポートされている言語のリスト
     */
    public List<LanguageInfo> getSupportedLanguages() {
        return Arrays.asList(
            new LanguageInfo("en", "English", "English"),
            new LanguageInfo("ja", "日本語", "Japanese"),
            new LanguageInfo("zh", "中文", "Chinese")
        );
    }
    
    /**
     * 言語情報クラス
     */
    public static class LanguageInfo {
        private final String code;
        private final String nativeName;
        private final String englishName;
        
        public LanguageInfo(String code, String nativeName, String englishName) {
            this.code = code;
            this.nativeName = nativeName;
            this.englishName = englishName;
        }
        
        // Getter methods...
        public String getCode() { return code; }
        public String getNativeName() { return nativeName; }
        public String getEnglishName() { return englishName; }
    }
}
```

## 🎨 フロントエンド国際化

### HTMLテンプレート

```html
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{messages.title}</title>
    <!-- Bootstrap CSS CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
    <!-- Language Selector -->
    <div class="language-selector">
        <select id="language-select" class="form-select form-select-sm">
            <option value="en">English</option>
            <option value="ja">日本語</option>
            <option value="zh">中文</option>
        </select>
    </div>
    
    <!-- ログイン・登録フォーム -->
    <div id="auth-container" class="auth-container">
        <div class="auth-header">
            <h1>{messages.header}</h1>
            <p>{messages.description}</p>
        </div>
        
        <!-- ログインフォーム -->
        <form id="login-form" class="auth-form">
            <div class="mb-3">
                <label for="login-username" class="form-label">{messages.username}</label>
                <input type="text" id="login-username" name="username" class="form-control" required>
            </div>
            <div class="mb-3">
                <label for="login-password" class="form-label">{messages.password}</label>
                <input type="password" id="login-password" name="password" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-primary w-100 mb-3">{messages.loginButton}</button>
        </form>
        
        <!-- フォーム切り替えリンク -->
        <div class="text-center">
            <a href="#" id="toggle-form" class="toggle-link">{messages.loginToggle}</a>
        </div>
    </div>
    
    <!-- JavaScript -->
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="/js/auth.js"></script>
</body>
</html>
```

### JavaScript言語切り替え

```javascript
$(document).ready(function() {
    // 言語切り替え
    $('#language-select').on('change', function() {
        const selectedLang = $(this).val();
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('lang', selectedLang);
        window.location.href = currentUrl.toString();
    });
    
    // URLパラメータから現在の言語を取得して選択状態を設定
    const urlParams = new URLSearchParams(window.location.search);
    const currentLang = urlParams.get('lang') || getDefaultLanguage();
    $('#language-select').val(currentLang);
    
    // ブラウザの言語設定から推測
    function getDefaultLanguage() {
        const browserLang = navigator.language || navigator.userLanguage;
        if (browserLang.startsWith('ja')) return 'ja';
        if (browserLang.startsWith('zh')) return 'zh';
        return 'en';
    }
    
    // 動的メッセージ更新（AJAX経由）
    function updateMessages(lang) {
        $.ajax({
            url: '/api/messages',
            method: 'GET',
            headers: {
                'Accept-Language': lang
            },
            success: function(messages) {
                updateUIMessages(messages);
            }
        });
    }
    
    function updateUIMessages(messages) {
        // 動的にメッセージを更新
        $('[data-message-key]').each(function() {
            const key = $(this).data('message-key');
            if (messages[key]) {
                $(this).text(messages[key]);
            }
        });
    }
});
```

## 🔧 コントローラー国際化対応

### PageController

```java
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PageController {
    
    @Inject
    Template login;
    
    @Inject
    MessageService messageService;
    
    @Context
    HttpHeaders headers;
    
    /**
     * ログインページを表示
     * @param lang 言語パラメータ（オプション）
     * @return ログインページのHTML
     */
    @GET
    @Path("/")
    public TemplateInstance index(@QueryParam("lang") String lang) {
        Locale locale = getLocale(lang);
        return login.data("messages", getMessages(locale));
    }
    
    /**
     * 言語パラメータまたはAccept-Languageヘッダーからロケールを取得
     */
    private Locale getLocale(String lang) {
        if (lang != null && !lang.trim().isEmpty()) {
            return messageService.parseLocale(lang);
        }
        return messageService.parseLocale(headers.getHeaderString("Accept-Language"));
    }
    
    /**
     * テンプレート用のメッセージマップを作成
     */
    private Map<String, String> getMessages(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        messages.put("title", messageService.getMessage("auth.login.title", locale));
        messages.put("header", messageService.getMessage("auth.login.header", locale));
        messages.put("description", messageService.getMessage("auth.login.description", locale));
        messages.put("username", messageService.getMessage("auth.login.username", locale));
        messages.put("password", messageService.getMessage("auth.login.password", locale));
        messages.put("loginButton", messageService.getMessage("auth.login.button", locale));
        messages.put("loginToggle", messageService.getMessage("auth.login.toggle", locale));
        // ... 他のメッセージ
        return messages;
    }
}
```

### API国際化対応

```java
@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
public class MessageController {
    
    @Inject
    MessageService messageService;
    
    @Context
    HttpHeaders headers;
    
    /**
     * 現在の言語のメッセージ一覧を取得
     */
    @GET
    public Response getMessages() {
        Locale locale = messageService.parseLocale(
            headers.getHeaderString("Accept-Language")
        );
        
        Map<String, String> messages = getAllMessages(locale);
        return Response.ok(messages).build();
    }
    
    /**
     * 指定言語のメッセージ一覧を取得
     */
    @GET
    @Path("/{lang}")
    public Response getMessages(@PathParam("lang") String lang) {
        Locale locale = messageService.parseLocale(lang);
        Map<String, String> messages = getAllMessages(locale);
        return Response.ok(messages).build();
    }
    
    /**
     * サポートされている言語一覧を取得
     */
    @GET
    @Path("/languages")
    public Response getSupportedLanguages() {
        List<MessageService.LanguageInfo> languages = messageService.getSupportedLanguages();
        return Response.ok(languages).build();
    }
    
    private Map<String, String> getAllMessages(Locale locale) {
        // 必要なメッセージキーを定義
        String[] messageKeys = {
            "auth.login.title", "auth.login.header", "auth.login.description",
            "auth.login.username", "auth.login.password", "auth.login.button",
            "menu.title", "menu.user", "menu.role", "menu.logout",
            "error.authentication.failed", "error.validation.error",
            // ... 他のキー
        };
        
        Map<String, String> messages = new HashMap<>();
        for (String key : messageKeys) {
            messages.put(key, messageService.getMessage(key, locale));
        }
        
        return messages;
    }
}
```

## 🧪 国際化テスト

### MessageServiceテスト

```java
@QuarkusTest
class MessageServiceTest {
    
    @Inject
    MessageService messageService;
    
    @Test
    void testGetMessageEnglish() {
        String message = messageService.getMessage("auth.login.title", Locale.ENGLISH);
        assertEquals("Login - Quarkus Authentication System", message);
    }
    
    @Test
    void testGetMessageJapanese() {
        String message = messageService.getMessage("auth.login.title", Locale.JAPANESE);
        assertEquals("ログイン - Quarkus認証システム", message);
    }
    
    @Test
    void testGetMessageChinese() {
        String message = messageService.getMessage("auth.login.title", Locale.CHINESE);
        assertEquals("登录 - Quarkus认证系统", message);
    }
    
    @Test
    void testParseLocaleFromAcceptLanguage() {
        Locale locale = messageService.parseLocale("ja-JP,ja;q=0.9,en;q=0.8");
        assertEquals(Locale.JAPANESE, locale);
    }
    
    @Test
    void testFallbackToDefaultLocale() {
        String message = messageService.getMessage("nonexistent.key", Locale.JAPANESE);
        assertEquals("nonexistent.key", message); // キー自体が返される
    }
    
    @Test
    void testParameterizedMessage() {
        // メッセージファイルに "user.welcome=Welcome, {0}!" を追加
        String message = messageService.getMessage("user.welcome", Locale.ENGLISH, "John");
        assertEquals("Welcome, John!", message);
    }
}
```

## 📚 ベストプラクティス

### 1. メッセージキーの命名規則

```properties
# 良い例：階層的で意味が明確
auth.login.title=Login
auth.register.success=Registration successful
error.validation.username.required=Username is required
menu.user.management=User Management

# 悪い例：意味が不明確
msg1=Login
success=OK
err=Error
```

### 2. パラメータ化メッセージ

```properties
# パラメータ付きメッセージ
user.welcome=Welcome, {0}!
error.user.not.found=User with ID {0} not found
validation.field.length=Field {0} must be between {1} and {2} characters

# 複数形対応
item.count.zero=No items
item.count.one=1 item
item.count.many={0} items
```

### 3. 文字エンコーディング

```properties
# UTF-8エンコーディングを使用
# ファイル保存時にUTF-8を指定
auth.login.title=ログイン画面
```

---

このガイドを参考に、多言語対応のアプリケーションを構築してください。

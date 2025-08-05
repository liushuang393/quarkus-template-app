# フロントエンド開発ガイド

このガイドでは、HTML、CSS、jQuery、Bootstrap 5を使用したフロントエンド開発について説明します。

## 🎯 技術スタック

- **テンプレートエンジン**: Qute (Quarkus公式)
- **UIフレームワーク**: Bootstrap 5
- **JavaScript**: jQuery 3.7.1
- **CSS**: カスタムCSS + Bootstrap

## 📁 ファイル構成

```
src/main/resources/
├── templates/              # Quteテンプレート
│   └── login.html         # ログインページ
└── META-INF/resources/    # 静的リソース
    ├── css/
    │   └── auth.css       # カスタムスタイル
    └── js/
        └── auth.js        # JavaScript
```

## 🎨 HTML/Quteテンプレート

### 基本構造

```html
<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>{messages.title}</title>
    <!-- Bootstrap CSS CDN -->
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <!-- Custom CSS -->
    <link href="/css/auth.css" rel="stylesheet" />
  </head>
  <body>
    <!-- コンテンツ -->

    <!-- jQuery CDN -->
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <!-- Bootstrap JS CDN -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Custom JavaScript -->
    <script src="/js/auth.js"></script>
  </body>
</html>
```

### Quteテンプレート変数

```html
<!-- メッセージ表示 -->
<h1>{messages.header}</h1>
<p>{messages.description}</p>

<!-- 条件分岐 -->
{#if user.isActive}
<span class="badge bg-success">アクティブ</span>
{#else}
<span class="badge bg-danger">非アクティブ</span>
{/if}

<!-- ループ処理 -->
{#for item in items}
<div class="item">{item.name}</div>
{/for}
```

## 🎨 CSS設計

### カスタムCSS例

```css
/* 基本レイアウト */
body {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* コンポーネントスタイル */
.auth-container {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

/* フォームスタイル */
.auth-form .form-control {
  border: 2px solid #e1e5e9;
  border-radius: 5px;
  transition: border-color 0.3s;
}

.auth-form .form-control:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 0.25rem rgba(102, 126, 234, 0.25);
}

/* ボタンスタイル */
.auth-form .btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 5px;
  transition: transform 0.2s;
}

.auth-form .btn:hover {
  transform: translateY(-2px);
}

/* レスポンシブデザイン */
@media (max-width: 576px) {
  .auth-container {
    margin: 1rem;
    padding: 1.5rem;
  }
}
```

## 📱 Bootstrap 5コンポーネント

### フォーム

```html
<!-- 基本フォーム -->
<form class="auth-form">
  <div class="mb-3">
    <label for="username" class="form-label">ユーザー名</label>
    <input type="text" id="username" class="form-control" required />
  </div>
  <div class="mb-3">
    <label for="password" class="form-label">パスワード</label>
    <input type="password" id="password" class="form-control" required />
  </div>
  <button type="submit" class="btn btn-primary w-100">ログイン</button>
</form>

<!-- セレクトボックス -->
<select class="form-select" required>
  <option value="">選択してください</option>
  <option value="USER">ユーザー</option>
  <option value="ADMIN">管理者</option>
</select>
```

### アラート

```html
<!-- 成功メッセージ -->
<div class="alert alert-success" role="alert">
  <i class="bi bi-check-circle"></i> 登録が完了しました
</div>

<!-- エラーメッセージ -->
<div class="alert alert-danger" role="alert">
  <i class="bi bi-exclamation-triangle"></i> エラーが発生しました
</div>
```

### カード

```html
<div class="card">
  <div class="card-header">
    <h5 class="card-title">ユーザー情報</h5>
  </div>
  <div class="card-body">
    <p class="card-text">ユーザーの詳細情報を表示します。</p>
    <a href="#" class="btn btn-primary">詳細を見る</a>
  </div>
</div>
```

## ⚡ jQuery実装

### 基本構造

```javascript
$(document).ready(function () {
  // 変数定義
  let currentToken = null;
  let currentUser = null;

  // DOM要素の取得
  const $loginForm = $("#login-form");
  const $messageDiv = $("#message");

  // イベントハンドラー
  $loginForm.on("submit", handleLogin);

  // 初期化処理
  initializePage();
});
```

### AJAX通信

```javascript
// POST リクエスト
function submitForm(data) {
  $.ajax({
    type: "POST",
    url: "/auth/login",
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function (response) {
      showMessage("ログインに成功しました", "success");
      handleLoginSuccess(response);
    },
    error: function (xhr) {
      const response = xhr.responseJSON;
      showMessage(response?.message || "エラーが発生しました", "danger");
    },
  });
}

// GET リクエスト
function fetchUserData(userId) {
  $.ajax({
    type: "GET",
    url: `/api/users/${userId}`,
    headers: {
      Authorization: "Bearer " + currentToken,
    },
    success: function (user) {
      displayUserInfo(user);
    },
    error: function () {
      showMessage("ユーザー情報の取得に失敗しました", "danger");
    },
  });
}
```

### DOM操作

```javascript
// 要素の表示/非表示
function showElement(selector) {
  $(selector).removeClass("d-none");
}

function hideElement(selector) {
  $(selector).addClass("d-none");
}

// メッセージ表示
function showMessage(text, type) {
  const $messageDiv = $("#message");
  $messageDiv
    .removeClass("d-none alert-success alert-danger alert-warning")
    .addClass(`alert-${type}`)
    .text(text);
}

// 動的コンテンツ生成
function createMenuItem(menu) {
  return $("<a>")
    .attr("href", "#")
    .addClass("menu-item")
    .text(menu.name)
    .on("click", function (e) {
      e.preventDefault();
      handleMenuClick(menu);
    });
}
```

### フォーム処理

```javascript
// フォーム送信処理
$("#login-form").on("submit", function (e) {
  e.preventDefault();

  // バリデーション
  if (!validateForm()) {
    return;
  }

  // データ収集
  const formData = {
    username: $("#username").val(),
    password: $("#password").val(),
  };

  // 送信
  submitLogin(formData);
});

// バリデーション
function validateForm() {
  const username = $("#username").val().trim();
  const password = $("#password").val();

  if (!username) {
    showMessage("ユーザー名を入力してください", "warning");
    return false;
  }

  if (password.length < 8) {
    showMessage("パスワードは8文字以上で入力してください", "warning");
    return false;
  }

  return true;
}
```

## 🌐 国際化対応

### 言語切り替え

```javascript
// 言語セレクター
$("#language-select").on("change", function () {
  const selectedLang = $(this).val();
  const currentUrl = new URL(window.location);
  currentUrl.searchParams.set("lang", selectedLang);
  window.location.href = currentUrl.toString();
});

// 現在の言語を設定
const urlParams = new URLSearchParams(window.location.search);
const currentLang = urlParams.get("lang") || "ja";
$("#language-select").val(currentLang);
```

## 📱 レスポンシブデザイン

### Bootstrapグリッドシステム

```html
<div class="container">
  <div class="row">
    <div class="col-12 col-md-6 col-lg-4">
      <!-- コンテンツ -->
    </div>
    <div class="col-12 col-md-6 col-lg-8">
      <!-- コンテンツ -->
    </div>
  </div>
</div>
```

### メディアクエリ

```css
/* タブレット */
@media (max-width: 768px) {
  .menu-header {
    flex-direction: column;
    gap: 1rem;
  }
}

/* スマートフォン */
@media (max-width: 576px) {
  .auth-container {
    margin: 1rem;
    padding: 1.5rem;
  }

  .user-info {
    flex-direction: column;
    gap: 0.5rem;
  }
}
```

## 🔧 デバッグとテスト

### ブラウザ開発者ツール

```javascript
// コンソールログ
console.log("デバッグ情報:", data);
console.error("エラー:", error);

// デバッガー
debugger; // ブレークポイント設定
```

### エラーハンドリング

```javascript
// グローバルエラーハンドラー
window.addEventListener("error", function (e) {
  console.error("JavaScript エラー:", e.error);
  showMessage("予期しないエラーが発生しました", "danger");
});

// Promise エラーハンドリング
window.addEventListener("unhandledrejection", function (e) {
  console.error("未処理のPromise拒否:", e.reason);
  e.preventDefault();
});
```

## 🚀 パフォーマンス最適化

### 画像最適化

```html
<!-- レスポンシブ画像 -->
<img
  src="image.jpg"
  srcset="image-small.jpg 480w, image-medium.jpg 768w, image-large.jpg 1200w"
  sizes="(max-width: 480px) 100vw, (max-width: 768px) 50vw, 25vw"
  alt="説明"
/>
```

### 遅延読み込み

```javascript
// 画像の遅延読み込み
$("img[data-src]").each(function () {
  const $img = $(this);
  const observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        $img.attr("src", $img.data("src"));
        observer.unobserve(entry.target);
      }
    });
  });
  observer.observe(this);
});
```

## 📚 参考リンク

- [Bootstrap 5 公式ドキュメント](https://getbootstrap.com/docs/5.3/)
- [jQuery 公式ドキュメント](https://api.jquery.com/)
- [Qute テンプレートガイド](https://quarkus.io/guides/qute)

---

このガイドを参考に、効率的で保守性の高いフロントエンドコードを作成してください。

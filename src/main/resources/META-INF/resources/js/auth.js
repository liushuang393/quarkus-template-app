/**
 * 认证系统 JavaScript - 使用 jQuery + Bootstrap
 */
$(document).ready(function() {
    let currentToken = null;
    let currentUser = null;

    // DOM要素の取得
    const $authContainer = $('#auth-container');
    const $menuContainer = $('#menu-container');
    const $loginForm = $('#login-form');
    const $registerForm = $('#register-form');
    const $toggleLink = $('#toggle-form');
    const $messageDiv = $('#message');
    const $languageSelect = $('#language-select');

    // 言語切り替え
    $languageSelect.on('change', function() {
        const selectedLang = $(this).val();
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('lang', selectedLang);
        window.location.href = currentUrl.toString();
    });

    // URLパラメータから現在の言語を取得して選択状態を設定
    const urlParams = new URLSearchParams(window.location.search);
    const currentLang = urlParams.get('lang') || 'ja';
    $languageSelect.val(currentLang);

    // フォーム切り替え
    $toggleLink.on('click', function(e) {
        e.preventDefault();
        const isLoginVisible = !$loginForm.hasClass('d-none');

        if (isLoginVisible) {
            $loginForm.addClass('d-none');
            $registerForm.removeClass('d-none');
            $toggleLink.text('ログインはこちら');
        } else {
            $registerForm.addClass('d-none');
            $loginForm.removeClass('d-none');
            $toggleLink.text('新規登録はこちら');
        }
        hideMessage();
    });

    // メッセージ表示
    function showMessage(text, type) {
        $messageDiv
            .removeClass('d-none alert-success alert-danger')
            .addClass('alert-' + type)
            .text(text);
    }

    function hideMessage() {
        $messageDiv.addClass('d-none');
    }

    // 登録処理
    $registerForm.on('submit', function(e) {
        e.preventDefault();

        const data = {
            username: $('#register-username').val(),
            email: $('#register-email').val(),
            password: $('#register-password').val(),
            role: $('#register-role').val()
        };

        $.ajax({
            type: 'POST',
            url: '/auth/register',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                showMessage('登録が完了しました。ログインしてください。', 'success');
                // ログインフォームに切り替え
                $registerForm.addClass('d-none');
                $loginForm.removeClass('d-none');
                $toggleLink.text('新規登録はこちら');
                // 登録したユーザー名をログインフォームに設定
                $('#login-username').val(data.username);
            },
            error: function(xhr) {
                const response = xhr.responseJSON;
                showMessage(response?.message || '登録に失敗しました', 'danger');
            }
        });
    });

    // ログイン処理
    $loginForm.on('submit', function(e) {
        e.preventDefault();

        const data = {
            username: $('#login-username').val(),
            password: $('#login-password').val()
        };

        $.ajax({
            type: 'POST',
            url: '/auth/login',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                currentToken = response.token;
                currentUser = response.user;
                showMenu();
            },
            error: function(xhr) {
                const response = xhr.responseJSON;
                showMessage(response?.message || 'ログインに失敗しました', 'danger');
            }
        });
    });

    // メニュー表示
    function showMenu() {
        $.ajax({
            type: 'GET',
            url: '/menu',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            success: function(response) {
                // ユーザー情報表示
                $('#user-name').text(currentUser.username);
                $('#user-role').text(currentUser.role);

                // メニュー項目表示
                const $menuItems = $('#menu-items');
                $menuItems.empty();

                response.menus.forEach(function(menu) {
                    const $menuItem = $('<a>')
                        .attr('href', '#')
                        .addClass('menu-item')
                        .text(menu.name)
                        .on('click', function(e) {
                            e.preventDefault();
                            alert(`${menu.name} (${menu.path}) がクリックされました`);
                        });
                    $menuItems.append($menuItem);
                });

                // 画面切り替え
                $authContainer.addClass('d-none');
                $menuContainer.removeClass('d-none');
            },
            error: function() {
                showMessage('メニューの取得に失敗しました', 'danger');
            }
        });
    }

    // ログアウト処理
    $('#logout-btn').on('click', function() {
        currentToken = null;
        currentUser = null;

        // フォームリセット
        $loginForm[0].reset();
        $registerForm[0].reset();

        // 画面切り替え
        $menuContainer.addClass('d-none');
        $authContainer.removeClass('d-none');

        // ログインフォームに戻す
        $registerForm.addClass('d-none');
        $loginForm.removeClass('d-none');
        $toggleLink.text('新規登録はこちら');

        hideMessage();
    });
});

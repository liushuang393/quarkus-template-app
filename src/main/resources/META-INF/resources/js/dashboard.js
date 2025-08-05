/**
 * Dashboard JavaScript - 管理画面の機能
 */
$(document).ready(function() {
    let currentUser = null;
    let currentToken = localStorage.getItem('authToken');

    // 初期化
    init();

    function init() {
        // 認証チェック
        if (!currentToken) {
            redirectToLogin();
            return;
        }

        // ユーザー情報取得
        loadUserInfo();

        // メニュー読み込み
        loadMenu();

        // ダッシュボードデータ読み込み
        loadDashboardData();

        // イベントハンドラー設定
        setupEventHandlers();

        // 言語設定
        setupLanguage();
    }

    function setupEventHandlers() {
        // ログアウト
        $('#logout-btn').on('click', function(e) {
            e.preventDefault();
            logout();
        });

        // サイドバーメニュー
        $('#sidebar-menu').on('click', '.nav-link', function(e) {
            e.preventDefault();
            const section = $(this).data('section');
            if (section) {
                showSection(section);
                updateActiveMenu($(this));
            }
        });

        // リフレッシュボタン
        $('#refresh-btn').on('click', function() {
            loadDashboardData();
        });

        // クイックアクション
        $('#add-user-btn').on('click', function() {
            showAddUserModal();
        });

        $('#view-reports-btn').on('click', function() {
            showSection('reports');
        });

        $('#system-settings-btn').on('click', function() {
            showSection('settings');
        });

        // 言語切り替え
        $('#language-select').on('change', function() {
            const selectedLang = $(this).val();
            changeLanguage(selectedLang);
        });
    }

    function setupLanguage() {
        const urlParams = new URLSearchParams(window.location.search);
        const currentLang = urlParams.get('lang') || getDefaultLanguage();
        $('#language-select').val(currentLang);
    }

    function getDefaultLanguage() {
        const browserLang = navigator.language || navigator.userLanguage;
        if (browserLang.startsWith('ja')) return 'ja';
        if (browserLang.startsWith('zh')) return 'zh';
        return 'en';
    }

    function changeLanguage(lang) {
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('lang', lang);
        window.location.href = currentUrl.toString();
    }

    function loadUserInfo() {
        $.ajax({
            url: '/api/users/profile',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            success: function(user) {
                currentUser = user;
                $('#user-name').text(user.username);
                updateUIForRole(user.role);
            },
            error: function() {
                redirectToLogin();
            }
        });
    }

    function loadMenu() {
        $.ajax({
            url: '/menu',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            success: function(response) {
                renderMenu(response.menus);
            },
            error: function() {
                showAlert('メニューの読み込みに失敗しました', 'danger');
            }
        });
    }

    function renderMenu(menus) {
        const $menu = $('#sidebar-menu');

        // 既存の動的メニューを削除（ダッシュボードは残す）
        $menu.find('.nav-item:not(:first)').remove();

        menus.forEach(function(menu) {
            const menuItem = $(`
                <li class="nav-item">
                    <a class="nav-link" href="#" data-section="${getMenuSection(menu.name)}">
                        <i class="${getMenuIcon(menu.name)}"></i>
                        ${menu.name}
                    </a>
                </li>
            `);
            $menu.append(menuItem);
        });
    }

    function getMenuSection(menuName) {
        const sectionMap = {
            'ユーザー管理': 'user-management',
            'User Management': 'user-management',
            '用户管理': 'user-management',
            'システム設定': 'settings',
            'System Settings': 'settings',
            '系统设置': 'settings',
            '売上管理': 'sales',
            'Sales Management': 'sales',
            '销售管理': 'sales',
            'レポート': 'reports',
            'Reports': 'reports',
            '报表': 'reports'
        };
        return sectionMap[menuName] || 'dashboard';
    }

    function getMenuIcon(menuName) {
        const iconMap = {
            'ユーザー管理': 'bi bi-people',
            'User Management': 'bi bi-people',
            '用户管理': 'bi bi-people',
            'システム設定': 'bi bi-gear',
            'System Settings': 'bi bi-gear',
            '系统设置': 'bi bi-gear',
            '売上管理': 'bi bi-graph-up',
            'Sales Management': 'bi bi-graph-up',
            '销售管理': 'bi bi-graph-up',
            'レポート': 'bi bi-file-earmark-text',
            'Reports': 'bi bi-file-earmark-text',
            '报表': 'bi bi-file-earmark-text'
        };
        return iconMap[menuName] || 'bi bi-circle';
    }

    function loadDashboardData() {
        showLoading();

        // 統計データ読み込み
        $.ajax({
            url: '/api/dashboard/stats',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            success: function(stats) {
                updateStatistics(stats);
            },
            error: function() {
                showAlert('統計データの読み込みに失敗しました', 'warning');
            }
        });

        // 最近のアクティビティ読み込み
        $.ajax({
            url: '/api/dashboard/activity',
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            success: function(activities) {
                updateActivityTable(activities);
            },
            error: function() {
                showAlert('アクティビティの読み込みに失敗しました', 'warning');
            },
            complete: function() {
                hideLoading();
            }
        });
    }

    function updateStatistics(stats) {
        $('#total-users').text(stats.totalUsers || 0);
        $('#active-users').text(stats.activeUsers || 0);
        $('#today-logins').text(stats.todayLogins || 0);

        // システムステータス更新
        const statusBadge = stats.systemStatus === 'online' ?
            '<span class="badge bg-success">オンライン</span>' :
            '<span class="badge bg-danger">オフライン</span>';
        $('#system-status').html(statusBadge);
    }

    function updateActivityTable(activities) {
        const $tbody = $('#activity-table tbody');
        $tbody.empty();

        if (activities && activities.length > 0) {
            activities.forEach(function(activity) {
                const statusBadge = getStatusBadge(activity.status);
                const row = $(`
                    <tr>
                        <td>${formatDateTime(activity.createdAt)}</td>
                        <td>${activity.username}</td>
                        <td>${activity.action}</td>
                        <td>${statusBadge}</td>
                    </tr>
                `);
                $tbody.append(row);
            });
        } else {
            $tbody.append('<tr><td colspan="4" class="text-center">データがありません</td></tr>');
        }
    }

    function getStatusBadge(status) {
        switch (status) {
            case 'SUCCESS':
                return '<span class="badge bg-success">成功</span>';
            case 'FAILURE':
                return '<span class="badge bg-danger">失敗</span>';
            case 'ERROR':
                return '<span class="badge bg-warning">エラー</span>';
            default:
                return '<span class="badge bg-secondary">不明</span>';
        }
    }

    function formatDateTime(dateTime) {
        const date = new Date(dateTime);
        return date.toLocaleString('ja-JP', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    function showSection(sectionName) {
        // 全セクションを非表示
        $('.content-section').addClass('d-none');

        // 指定セクションを表示
        $(`#${sectionName}-section`).removeClass('d-none').addClass('fade-in');

        // パンくずリスト更新
        updateBreadcrumb(sectionName);

        // セクション固有のデータ読み込み
        loadSectionData(sectionName);
    }

    function updateBreadcrumb(sectionName) {
        const sectionNames = {
            'dashboard': 'ダッシュボード',
            'user-management': 'ユーザー管理',
            'settings': 'システム設定',
            'sales': '売上管理',
            'reports': 'レポート'
        };

        $('#current-page').text(sectionNames[sectionName] || sectionName);
    }

    function updateActiveMenu($activeLink) {
        $('#sidebar-menu .nav-link').removeClass('active');
        $activeLink.addClass('active');
    }

    function loadSectionData(sectionName) {
        switch (sectionName) {
            case 'user-management':
                loadUserManagement();
                break;
            case 'settings':
                loadSettings();
                break;
            // 他のセクションも同様に実装
        }
    }

    function loadUserManagement() {
        // ユーザー管理画面のデータ読み込み
        // 実装は省略
    }

    function loadSettings() {
        // 設定画面のデータ読み込み
        // 実装は省略
    }

    function showAddUserModal() {
        // ユーザー追加モーダルを表示
        // 実装は省略
    }

    function updateUIForRole(role) {
        // ロールに応じてUIを調整
        if (role !== 'ADMIN') {
            // 管理者以外は一部機能を非表示
            $('#system-settings-btn').hide();
        }
    }

    function logout() {
        $.ajax({
            url: '/auth/logout',
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + currentToken
            },
            complete: function() {
                localStorage.removeItem('authToken');
                redirectToLogin();
            }
        });
    }

    function redirectToLogin() {
        window.location.href = '/login';
    }

    function showLoading() {
        $('#loading-spinner').removeClass('d-none');
    }

    function hideLoading() {
        $('#loading-spinner').addClass('d-none');
    }

    function showAlert(message, type) {
        const alertHtml = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        $('#alert-container').append(alertHtml);

        // 5秒後に自動削除
        setTimeout(function() {
            $('#alert-container .alert:first').alert('close');
        }, 5000);
    }
});

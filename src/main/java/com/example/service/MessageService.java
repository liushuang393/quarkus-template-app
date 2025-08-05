package com.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
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
}

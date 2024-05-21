package src;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AIAnalyze {
    public static String OpenAIAnalyze(String message) throws Exception {
        try {
            String apiKey = "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"; // 替換為你的 API 密鑰
            // 手動構建 JSON 陣列
            String escapedMessage = escapeHtml(message);
            String messageWithoutNewlines = escapedMessage.replaceAll("\\n", "");
            String jsonMessages = "[" +
                    "{\"role\": \"system\", \"content\": \"" + "You are now my personal assistant. You need to help me analyze and summarize this message in the simplest terms possible with chinese.Please ensure that each line in the message does not exceed 30 characters in English. " + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" +messageWithoutNewlines+ "\"}" +
                          "]";

            // 使用 Apache HttpClient 庫發送 HTTP POST 請求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey); // 設置 Authorization 頭部
            httpPost.setHeader("Content-Type", "application/json");

            // 設置請求主體（這裡假設郵件內容已經轉換成 JSON 格式）
            StringEntity requestEntity = new StringEntity("{\"messages\": " + jsonMessages + ", \"model\": \"gpt-3.5-turbo\"}");
            httpPost.setEntity(requestEntity);

            // 執行請求並獲取回應
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            // 解析回應
            if (responseEntity != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                String line;
                StringBuilder responseContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                // 將回應內容轉換成 JSON 物件
                JSONObject jsonResponse = new JSONObject(responseContent.toString());
                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                String content = firstChoice.getJSONObject("message").getString("content");

                // 輸出 content 欄位的值

                return content;
            }

            // 關閉 HTTP 客戶端
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String escapeHtml(String html) {
        return html.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}

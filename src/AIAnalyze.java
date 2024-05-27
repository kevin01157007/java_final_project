package src;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AIAnalyze {
    private static final int MAX_RETRIES = 3;

    public static String OpenAIAnalyze(String message, int i) throws Exception {
        return OpenAIAnalyze(message, i, 0);
    }

    private static String OpenAIAnalyze(String message, int i, int retryCount) throws Exception {
        try {
            String apiKey = "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"; // 替换为你的 API 密钥
            String prompt = null;
            if (i == 1) {
                prompt = "You are now my personal assistant. You need to help me analyze and summarize this message in the simplest terms possible with Chinese. ";
            } else {
                prompt = "You are now my personal assistant. You need to help me analyze who sent these messages to whom, summarize the content briefly, and finally report to me in Chinese.";
            }
            // 使用 Jsoup 解析 HTML 并提取纯文本
            String plainTextMessage = Jsoup.parse(message).text();
            String messageWithoutNewlines = plainTextMessage.replaceAll("\\n", "");

            String jsonMessages = "[" +
                    "{\"role\": \"system\", \"content\": \"" + prompt + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" + messageWithoutNewlines + "\"}" +
                    "]";

            // 使用 Apache HttpClient 库发送 HTTP POST 请求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey); // 设置 Authorization 头部
            httpPost.setHeader("Content-Type", "application/json");

            // 设置请求主体（这里假设邮件内容已经转换成 JSON 格式）
            StringEntity requestEntity = new StringEntity("{\"messages\": " + jsonMessages + ", \"model\": \"gpt-4-turbo\"}");
            httpPost.setEntity(requestEntity);

            // 执行请求并获取响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            // 解析响应
            if (responseEntity != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                String line;
                StringBuilder responseContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }

                // 将响应内容转换成 JSON 对象
                try {
                    JSONObject jsonResponse = new JSONObject(responseContent.toString());
                    JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                    JSONObject firstChoice = choicesArray.getJSONObject(0);
                    String content = firstChoice.getJSONObject("message").getString("content");
                    // 输出 content 字段的值
                    return content;
                } catch (JSONException e) {
                    // 捕捉 JSON 解析异常并输出错误信息
                    e.printStackTrace();
                    if (retryCount < MAX_RETRIES) {
                        return OpenAIAnalyze(message, i, retryCount + 1); // 重试
                    } else {
                        return "無法分析"; // 达到最大重试次数，返回默认消息
                    }
                }
            }

            // 关闭 HTTP 客户端
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

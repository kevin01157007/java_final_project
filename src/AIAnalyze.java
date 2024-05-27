package src;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AIAnalyze {
    public static String OpenAIAnalyze(String message,int i) throws Exception {
        try {
            String apiKey = "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"; // 替换为你的 API 密钥
            String prompt = null;
            if(i==1){
                prompt ="You are now my personal assistant. You need to help me analyze and summarize this message in the simplest terms possible with Traditional Chinese. " ;
            }else
//                prompt ="You are now my personal assistant. You need to help me analyze and summarize this message in the simplest terms possible with Chinese. " ;
                prompt ="You are now my personal assistant. You need to help me analyze who sent these messages to whom, summarize the content briefly, and finally report to me in Traditional Chinese." ;
            // 使用 Jsoup 解析 HTML 并提取纯文本
            String plainTextMessage = Jsoup.parse(message).text();
            String messageWithoutNewlines = plainTextMessage.replaceAll("\\n", "");

            String jsonMessages = "[" +
                    "{\"role\": \"system\", \"content\": \"" + prompt + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" + messageWithoutNewlines + "\"}" +
                    "]";
//            System.out.println(prompt);
//            System.out.println(messageWithoutNewlines);
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
                JSONObject jsonResponse = new JSONObject(responseContent.toString());
                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choicesArray.getJSONObject(0);
                String content = firstChoice.getJSONObject("message").getString("content");
//                System.out.println(jsonResponse);
                // 输出 content 字段的值
                return content;
            }

            // 关闭 HTTP 客户端
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

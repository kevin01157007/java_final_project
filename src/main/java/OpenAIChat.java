import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class OpenAIChat {
    public static String sendOpenAIRequest(String message) throws Exception {
        try {
            Dotenv dotenv = Dotenv.configure()
                                     .directory("src/main/resource")
                                     .load();
            String apiKey = dotenv.get("API_KEY1");
            String plainTextMessage = Jsoup.parse(message).text();
            String messageWithoutNewlines = plainTextMessage.replaceAll("\\n", "");
            String prompt = "You have to act for me to reply the email in Chinese as you are a receiver";
            JSONArray jsonMessages = new JSONArray();
            jsonMessages.put(new JSONObject().put("role", "system").put("content", prompt));
            jsonMessages.put(new JSONObject().put("role", "user").put("content", messageWithoutNewlines));

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.chatanywhere.tech/v1/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey); // 设置 Authorization 头部
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity requestEntity = new StringEntity("{\"messages\": " + jsonMessages + ", \"model\": \"gpt-3.5-turbo\"}", "UTF-8");
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8));
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

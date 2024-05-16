import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.gson.Gson;

public class OpenAIChat {
    public static void main(String[] args) {
        try {
            String emailContent = "hello";
            String apiKey = "sk-Kopp9IyshjfozvME8XcAJneLwVmgixUIhediY9EX1NysAofh"; // 替換為你的 API 密鑰

            // 使用 Apache HttpClient 庫發送 HTTP POST 請求
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.chatanywhere.cn/v1/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey); // 設置 Authorization 頭部
            httpPost.setHeader("Content-Type", "application/json");
            String jsonContent = new Gson().toJson(new Message("system", emailContent));
            // 設置請求主體（這裡假設郵件內容已經轉換成 JSON 格式）
            StringEntity requestEntity = new StringEntity("{\"messages\": [" + jsonContent + "], \"model\": \"gpt-3.5-turbo\"}");

            //StringEntity requestEntity = new StringEntity(jsonContent);
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
                System.out.println("AI's response: " + responseContent.toString());
            }

            // 關閉 HTTP 客戶端
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

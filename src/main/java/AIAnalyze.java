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
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import io.github.cdimascio.dotenv.Dotenv;
public class AIAnalyze {
    public static void OpenAIAnalyze(String message, int i, Consumer<String> responseHandler) throws Exception {
        try {
            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("API_KEY");
            String prompt = null;
            if (i == 1) {
                prompt = "You are now my personal assistant. You need to help me analyze and summarize this message in the simplest terms possible with Traditional Chinese. The fewer words the better.";
            } else {
                prompt = "You are now my personal assistant. You need to help me analyze who sent these messages to whom, summarize these email briefly, and finally report to me in Traditional Chinese. The fewer words the better.";
            }
            String plainTextMessage = Jsoup.parse(message).text();
            String messageWithoutNewlines = plainTextMessage.replaceAll("\\n", "");

            JSONArray jsonMessages = new JSONArray();
            jsonMessages.put(new JSONObject().put("role", "system").put("content", prompt));
            jsonMessages.put(new JSONObject().put("role", "user").put("content", messageWithoutNewlines));

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://api.chatanywhere.tech/v1/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
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

                JSONObject jsonResponse = new JSONObject(responseContent.toString());
                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                for (int j = 0; j < choicesArray.length(); j++) {
                    JSONObject choice = choicesArray.getJSONObject(j);
                    String content = choice.getJSONObject("message").getString("content");
                    for (char c : content.toCharArray()) {
                        responseHandler.accept(String.valueOf(c));
                        Thread.sleep(50);
                    }
                }
                httpClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseHandler.accept("Error: " + e.getMessage());
        }
    }
}
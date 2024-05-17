import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OpenAIChat {
    public static void main(String[] args) {
        try {
//            String emailContent = "Can you speak Chinese?";
//            String emailAsk = "你來自哪裡?";
            String apiKey = "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"; // 替換為你的 API 密鑰
            String prompt = "I am a college student studying in Taiwan";

            // 手動構建 JSON 陣列
            String jsonMessages = "[" +
                    "{\"role\": \"system\", \"content\": \"" + "You are a college student studying in Taiwan now ， you need to answer the question and reply the email  as you are a Taiwannese student. " + "\"}," +
//                    "{\"role\": \"user\", \"content\": \"" +"Can you speak Chinese?" + "\"}," +
//                    "{\"role\": \"system\", \"content\": \"" +"你好！我会说汉语。有什么可以帮到您的吗？" + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" + "各位同學好：海洋環境與生態研究所　暑期海洋研究體驗活動目的:本所舉辦暑期大學生海洋研究體驗營的目的即在鼓勵在校學生認識海洋環境變遷與生態議題。對象:參與對象為大學部學生，錄取名額為10-12名報名期限與申請結果：申請自即日起至2024年5月17日截止。申請結果將於2024年5月24日以電子郵件寄發通知並於本所網站公布。說明:1.2024年7月1日(一)至8月29日(四)，進入本所各研究室學習，為期兩個月。2.參與學生將安排新海研二號參訪以及本校小艇碼頭SUP立槳活動。3.計畫結束前，參與計畫學將研究成果製成海報，預計於8月29日(暫定)舉辦成果發表會。註：經實驗室指導教授認可後，7-8月學習期間每月可領取6,000元學習獎勵金。線上報名: https://docs.google.com/forms/d/e/1FAIpQLSeS1ND0eiOqPIrowHZrB4CrchJmjlUNWtdfznvsiQUOAAJ-1A/viewform 活動詳情: https://imee.ntou.edu.tw/p/406-1055-43895,r1035.php?Lang=zh-tw 承辦人：王麗真小姐" + "\"}" +
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
                System.out.println("AI's response: " + responseContent.toString());
            }

            // 關閉 HTTP 客戶端
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

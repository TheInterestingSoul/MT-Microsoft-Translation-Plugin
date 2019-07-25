import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import bin.mt.plugin.api.LocalString;
import bin.mt.plugin.api.translation.BaseTranslationEngine;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.annotation.MainThread;
import org.json.JSONArray;
import com.google.gson.*;

public class BingTranslationEngine extends BaseTranslationEngine {
    static final OkHttpClient HTTP_CLIENT;
    private Configuration configuration = (new ConfigurationBuilder()).setAcceptTranslated(false).setForceNotToSkipTranslated(false).setTargetLanguageMutable(false).setAutoRepairFormatControlError(true).setDisableAutoHideLanguage(true).build();
    private LocalString string;


    static {
        HTTP_CLIENT = (new Builder()).connectTimeout((long)8, TimeUnit.SECONDS).readTimeout((long)10, TimeUnit.SECONDS).writeTimeout((long)10, TimeUnit.SECONDS).build();
    }

    private String clear_space(String var1) {
        return var1.replace(" ", "");
        
    }

    private String getResult(String var1, String var2) throws JSONException, IOException {
        SharedPreferences var3 = this.getContext().getPreferences();
        this.getContext().log("POST后返回Json：" + var1);
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonArray ay = jsonParser.parse(var1).getAsJsonArray();
        
        JsonObject ay2 = ay.get(0).getAsJsonObject();
        
        JsonArray ay3 = ay2.get("translations").getAsJsonArray();
        
        JsonObject ay4 = ay3.get(0).getAsJsonObject();
        
        var1 = ay4.get("text").getAsString();
        if (var3.getBoolean("cs", true) && var2.equals("zh-Hans") || var2.equals("zh-Hant"))
        {
            var1 = this.clear_space(var1);
      
        }
        return var1;
    }

    private String post(String var1, String var2, String var3) throws IOException, JSONException {
        FormBody var4 = (new okhttp3.FormBody.Builder(Charset.defaultCharset())).add("text", var1).add("fromLang", var2).add("to", var3).build();
        Request var6 = (new okhttp3.Request.Builder()).url("https://cn.bing.com/ttranslatev3").header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)").post(var4).build();
        this.getContext().log("翻译引擎：微软翻译");
        this.getContext().log("提交的翻译数据：" + var1);
        Response var5 = HTTP_CLIENT.newCall(var6).execute();
        if (var5.isSuccessful()) {
            return this.getResult(var5.body().string(), var3);
        } else {
            this.getContext().log("错误：请求不成功");
            throw new IOException(this.string.get("request_failed"));
        }
    }

    public final String getLanguageDisplayName(String var1) {
        this.getContext().log("获取可视语言名称——代码：" + var1 + "   结果：" + super.getLanguageDisplayName(var1));
        return super.getLanguageDisplayName(var1);
    }

    public List loadSourceLanguages() {
        SharedPreferences sp1 = this.getContext().getPreferences();
        if (sp1.getBoolean("ca", false)){
            return Arrays.asList("en","ar","cs","da","de","el","es","fi","fr","hr","id","is","it","ja","ko","la","ms","pt","ru","sv","th","uk","vi","zh-CN","zh-TW");
        }
        else
        {return Arrays.asList("auto","en","ar","cs","da","de","el","es","fi","fr","hr","id","is","it","ja","ko","la","ms","pt","ru","sv","th","uk","vi","zh-CN","zh-TW");}
    }

    public List loadTargetLanguages(String var1) {
        return Arrays.asList("zh-CN","zh-TW","en","ar","cs","da","de","el","es","fi","fr","hr","id","is","it","ja","ko","la","ms","pt","ru","sv","th","uk","vi");   }

    @NonNull
    public String name() {
        this.string = this.getContext().getAssetLocalString("String");
        return this.string.get("mtp_name");
    }

    @NonNull
    @Override
    public String translate(String text, String source_lang, String target_lang) throws IOException {
        String var4;
        label28: {
            var4 = source_lang;
            if (target_lang.equals("auto")){
                var4 = detect_lang(text);
            }
            if (!target_lang.equals("zh-CN")) {
                source_lang = target_lang;
                if (!target_lang.equals("zh")) {
                    break label28;           }
            }

            source_lang = "zh-Hans";
        }

        if (var4.equals("zh-CN") || source_lang.equals("zh")) {
            var4 = "zh-Hans";
        }
        
        if (var4.equals("zh-TW")){
            var4 = "zh-Hant";
        }

        this.getContext().log("原语言代码:" + var4);
        this.getContext().log("翻译语言代码:" + source_lang);

        try {
            text = this.post(text, var4, source_lang);
            return text;
        } catch (JSONException var5) {
            this.getContext().log("错误：JSONException e");
            throw new IOException(this.string.get("json_failed"));
        } catch (IOException var6) {
            this.getContext().log("错误：IOException e");
            throw new IOException(this.string.get("io_failed"));
        }
    }

    private String detect_lang(String text) throws IOException 
    {



        return "auto-detect";
    }
}


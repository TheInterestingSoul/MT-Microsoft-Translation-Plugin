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

public class BingTranslationEngine extends BaseTranslationEngine
{
    static final OkHttpClient HTTP_CLIENT;
    private Configuration configuration = 
    (new ConfigurationBuilder())
    //关于下面五个boolean，请见https://doc-plugin.binmt.cc/988827
    .setAcceptTranslated(false)//设置是否接受翻译过的文本
    .setForceNotToSkipTranslated(false)//设置是否强制不跳过已翻译词条（开启后界面上将会隐藏该选项）
    .setTargetLanguageMutable(false)//设置TargetLanguage是否是可变的
    .setAutoRepairFormatControlError(true)//设置是否自动修复简单的控制符翻译错误，如%s被翻译为％S
    .setDisableAutoHideLanguage(true)//设置是否自动隐藏语言
    .build();
    
    private LocalString string;//获取本地语言包


    static {
        HTTP_CLIENT = 
        (new Builder())
        .connectTimeout((long)10, TimeUnit.SECONDS)
        .readTimeout((long)10, TimeUnit.SECONDS)
        .writeTimeout((long)10, TimeUnit.SECONDS)
        .build();
    }

    private String clear_space(String var1)
    {
        return var1.replace(" ", "");
        //设置项：清除空格
    }

    private String getResult(String var1, String var2) throws JSONException, IOException
    {
        SharedPreferences var3 = this.getContext().getPreferences();
        this.getContext().log("POST后返回Json：" + var1);// 打印日志，可在插件管理中查看
        /*
        测试Json：
         [
          {
         "detectedLanguage":
           {
         "language":"zh-Hans",
         "score":1.0
           },
         "translations":
           [
            {
         "text":"Building a harmonious society requires you and i to go",
         "to":"en"
            }
           ]
          }
         ]
        */
        JsonParser jsonParser = new JsonParser();
        JsonArray ay = jsonParser.parse(var1).getAsJsonArray();
        //把整个Json转为JsonArray（最外层方括号）
        JsonObject ay2 = ay.get(0).getAsJsonObject();
        //解析最外层花括号
        JsonArray ay3 = ay2.get("translations").getAsJsonArray();
        //解析"translations"内的方括号
        JsonObject ay4 = ay3.get(0).getAsJsonObject();
        //再解析一次花括号
        var1 = ay4.get("text").getAsString();
        //提取翻译结果
        if (var3.getBoolean("cs", true) && var2.equals("zh-Hans") || var2.equals("zh-Hant"))
        {
            var1 = this.clear_space(var1);
            //设置项：去除空格
        }
        return var1;
    }

    private String post(String text, String from, String to) throws IOException, JSONException
    {
        FormBody var4 = 
        (new okhttp3.FormBody.Builder(Charset.defaultCharset()))
        .add("text", text)
        .add("fromLang", from)
        .add("to", to)
        .build();
        
        Request var6 = 
        (new okhttp3.Request.Builder())
        .url("https://cn.bing.com/ttranslatev3")
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0;)")
        .post(var4)
        .build();
        /*
         地址：https://cn.bing.com/ttranslatev3
         方法：POST
         参数：fromLang=auto-detect&text=测试文本&to=en
        */
        
        this.getContext().log("翻译引擎：微软翻译");
        this.getContext().log("提交的翻译数据：" + text);
        Response var5 = HTTP_CLIENT.newCall(var6).execute();
        if (var5.isSuccessful())
        {
            return this.getResult(var5.body().string(), to);
        }
        else
        {
            this.getContext().log("错误：请求不成功");
            throw new IOException(this.string.get("request_failed"));
        }
    }

    public final String getLanguageDisplayName(String var1)
    {
        this.getContext().log("获取可视语言名称——代码：" + var1 + "   结果：" + super.getLanguageDisplayName(var1));
        return super.getLanguageDisplayName(var1);
    }

    public List loadSourceLanguages()//源语言列表
    {
        SharedPreferences sp1 = this.getContext().getPreferences();
        if (sp1.getBoolean("ca", false))
        {
            return Arrays.asList("en", "ar", "cs", "da", "de", "el", "es", "fi", "fr", "hr", "id", "is", "it", "ja", "ko", "la", "ms", "pt", "ru", "sv", "th", "uk", "vi", "zh-CN", "zh-TW");
        }
        else
        {
            return Arrays.asList("auto", "en", "ar", "cs", "da", "de", "el", "es", "fi", "fr", "hr", "id", "is", "it", "ja", "ko", "la", "ms", "pt", "ru", "sv", "th", "uk", "vi", "zh-CN", "zh-TW");
            //设置项：隐藏自动检测
        }
    }

    public List loadTargetLanguages(String var1)//目标语言列表
    {
        return Arrays.asList("zh-CN", "zh-TW", "en", "ar", "cs", "da", "de", "el", "es", "fi", "fr", "hr", "id", "is", "it", "ja", "ko", "la", "ms", "pt", "ru", "sv", "th", "uk", "vi");   }

    @NonNull
    public String name()
    {
        this.string = this.getContext().getAssetLocalString("String");
        return this.string.get("mtp_name");
    }

    @NonNull
    @Override
    public String translate(String text, String source_lang, String target_lang) throws IOException
    {
        String var4;
        label28:
        {
            var4 = source_lang;
            if (target_lang.equals("auto"))
            {
                var4 = detect_lang(text);
            }
            if (!target_lang.equals("zh-CN"))
            {
                source_lang = target_lang;
                if (!target_lang.equals("zh"))
                {
                    break label28;           }
            }

            source_lang = "zh-Hans";
        }

        if (var4.equals("zh-CN") || source_lang.equals("zh"))
        {
            var4 = "zh-Hans";
        }

        if (var4.equals("zh-TW"))
        {
            var4 = "zh-Hant";
        }
        /*
        微软翻译的语言映射与MT有所出入
        简体：zh-Hans
        繁体：zh-Hant
        自动检测：auto-detect（方法"detect_lang"）
        */

        this.getContext().log("原语言代码:" + var4);
        this.getContext().log("翻译语言代码:" + source_lang);

        try
        {
            text = this.post(text, var4, source_lang);
            return text;
        }
        catch (JSONException var5)
        {
            this.getContext().log("错误：JSONException e");
            throw new IOException(this.string.get("json_failed"));
        }
        catch (IOException var6)
        {
            this.getContext().log("错误：IOException e");
            throw new IOException(this.string.get("io_failed"));
        }
    }

    private String detect_lang(String text) throws IOException 
    {
        return "auto-detect";
    }
}


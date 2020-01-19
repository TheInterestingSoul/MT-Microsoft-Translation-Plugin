package tis.microsoft.translator;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;
import bin.mt.plugin.api.preference.PluginPreference.Builder;

public class Preference implements PluginPreference {
   @Override
   public void onBuild(MTPluginContext var1, Builder var2) {
      var2.setLocalString(var1.getLocalString());//获取本地语言包
      var2.addHeader("{settings}");
      var2.addSwitch("{cs}", "cs").defaultValue(true).summary("{cs_descr}");
      var2.addHeader("{about}");
      var2.addText("{author}").summary("{author_descr}");
       var2.addText("{qq}").summary("{qq_descr}").url("mqqapi://card/show_pslcard?src_type=internal&amp;source=sharecard&amp;version=1&amp;uin=1974224207");//可以使用其他软件的协议
      var2.addText("{email}").summary("{email_descr}");
      var2.addText("{mtbbs}").summary("{mtbbs_descr}").url("https://bbs.binmt.cc");
	  var2.addHeader("{donate}");
	   var2.addText("{wechatpay}").summary("{wechatpay_descr}").url("wxp://f2f0VLWoanBMwaWbypYuZv6ATO4JLry5rPE1");
   }
}

package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.job.JobManager;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.utils.AESUtils;
import me.kuku.utils.HexUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.logic.HostLocLogic;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HostLocLogicImpl implements HostLocLogic {

	@Inject
	private JobManager jobManager;

	@Override
	public Result<String> login(String username, String password) throws IOException {
		String cookie = preventCookie();
		Map<String, String> map = new HashMap<>();
		map.put("fastloginfield", "username");
		map.put("username", username);
		map.put("cookietime", "2592000");
		map.put("password", password);
		map.put("quickforward", "yes");
		map.put("handlekey", "ls");
		Response response = OkHttpUtils.post("https://hostloc.com/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1",
				map, OkHttpUtils.addHeaders(cookie, "https://hostloc.com/forum.php", UA.PC));
		String str = OkHttpUtils.getStr(response);
		if (str.contains("https://hostloc.com/forum.php")){
			return Result.success(OkHttpUtils.getCookie(response));
		}else return Result.failure("账号或密码错误或其他原因登录失败！");
	}

	@Override
	public boolean isLogin(String cookie) throws IOException {
		String preventCookie = preventCookie();
		cookie += preventCookie;
		String html = OkHttpUtils.getStr("https://hostloc.com/home.php?mod=spacecp",
				OkHttpUtils.addHeaders(cookie, null, UA.PC));
		String text = Jsoup.parse(html).getElementsByTag("title").first().text();
		return text.contains("个人资料");
	}

	@Override
	public void sign(String cookie) throws IOException {
		String preventCookie = preventCookie();
		String newCookie = cookie + preventCookie;
		jobManager.registerTimer(() -> {
			List<String> urlList = new ArrayList<>();
			for (int i = 0; i < 12; i++){
				int num = MyUtils.randomInt(10000, 50000);
				urlList.add("https://hostloc.com/space-uid-" + num + ".html");
			}
			for (String url: urlList){
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					OkHttpUtils.get(url, OkHttpUtils.addHeaders(newCookie, "https://hostloc.com/forum.php", UA.PC))
							.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0);
	}

	@Override
	public List<Map<String, String>> post() {
		String cookie = "";
		try {
			cookie = preventCookie();
		} catch (IOException ignore) {
		}
		List<Map<String, String>> list = new ArrayList<>();
		String html;
		try {
			html = OkHttpUtils.getStr("https://hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
					OkHttpUtils.addHeaders(cookie, "https://hostloc.com/forum.php", UA.PC));
		} catch (IOException e) {
//            e.printStackTrace();
			return list;
		}
		Elements elements = Jsoup.parse(html).getElementsByTag("tbody");
		for (Element ele: elements){
			if (!ele.attr("id").startsWith("normalth")) continue;
			Element s = ele.getElementsByClass("s").first();
			String title = s.text();
			String url = "https://hostloc.com/" + s.attr("href");
			String time,name;
			try {
				time = ele.select("em a span").first().text();
				name = ele.select("cite a").first().text();
			} catch (Exception e) {
//                e.printStackTrace();
				return list;
			}
			String id = MyUtils.regex("tid=", "&", url);
			Map<String, String> map = new HashMap<>();
			map.put("title", title);
			map.put("url", url);
			map.put("name", name);
			map.put("time", time);
			map.put("id", id);
			list.add(map);
		}
		return list;
	}

	@Override
	public String postContent(String url) throws IOException {
		String str = OkHttpUtils.getStr(url, OkHttpUtils.addUA(UA.PC));
		Elements pct = Jsoup.parse(str).getElementsByClass("pct");
		return pct.first().text();
	}

	private String preventCookie() throws IOException {
		String html = OkHttpUtils.getStr("https://hostloc.com");
		if (!html.matches("toNumbers\\(\"(.*?)\"\\)")){
			return "";
		}
		String aTemp = MyUtils.regex("a = toNumbers\\(\"", "\"\\),", html);
		String bTemp = MyUtils.regex("b = toNumbers\\(\"", "\"\\),", html);
		String cTemp = MyUtils.regex("c = toNumbers\\(\"", "\"\\);", html);
		byte[] a = intArrToByteArr(toNumbers(aTemp));
		byte[] b = intArrToByteArr(toNumbers(bTemp));
		byte[] c = intArrToByteArr(toNumbers(cTemp));
		byte[] bytes = AESUtils.decryptLoc(a, b, c);
		return "cnL7=" + HexUtils.bytesToHexString(bytes) + "; ";
	}

	private int[] toNumbers(String secret) {
		int length = secret.length();
		int[] arr = new int[length / 2];
		int num = 0;
		for (int i = 0; i < length; i += 2) {
			int lastNum = i + 2;
			if (lastNum > length) {
				lastNum = i + 1;
			}
			String ss = secret.substring(i, lastNum);
			if (num < arr.length)
				arr[num++] = Integer.valueOf(ss, 16);
		}
		return arr;
	}

	private byte[] intArrToByteArr(int[] intArr){
		byte[] bytes = new byte[intArr.length];
		for (int i = 0; i < intArr.length; i++){
			int num = intArr[i];
			bytes[i] = (byte) num;
		}
		return bytes;
	}
}

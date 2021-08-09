package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.utils.MyUtils;
import me.kuku.utils.gif.AnimatedGifEncoder;
import me.kuku.utils.gif.GifDecoder;
import me.kuku.yuq.utils.ImageHandle;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@GroupController
public class ImageController {

	@Inject
	private MessageItemFactory mif;


	@Action("丢{qqStr}")
	public Message diuAt(long qqStr){
		return getDiu(acatarUrl(qqStr));
	}

	@Action("丢")
	public Message diu(long qq){
		return getDiu(acatarUrl(qq));
	}

	@Action("爬{qqStr}")
	public Message paAt(long qqStr){
		String url = acatarUrl(qqStr);
		if (MyUtils.randomInt(0, 100) > 50)
			return getPa(url);
		else return getSuperPa(url);
	}

	@Action(value = "爬")
	public Message pa(long qq){
		String url = acatarUrl(qq);
		if (MyUtils.randomInt(0, 100) > 50)
			return getPa(url);
		else return getSuperPa(url);
	}

	@Action("嚼{qqStr}")
	@Synonym("恰{qqStr}")
	public Message jiAo(long qqStr){
		String url = acatarUrl(qqStr);
		return getJiao(url);
	}

	@Action("mua{qqStr}")
	public Message mua(long qqStr){
		String url = acatarUrl(qqStr);
		return getMuaGif(url);
	}

	@Action("摸{qqStr}")
	@Synonym("rua{qqStr}")
	public Message mo(long qqStr){
		String url = acatarUrl(qqStr);
		return getRua(url);
	}


	private String acatarUrl(long qq){
		return "https://q2.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";
	}

	private Message getDiu(String url){
		BufferedImage headImage;
		BufferedImage bgImage;

		int hdW = 146;
		try {
			headImage = (BufferedImage) ImageHandle.getHeadImage(url, hdW);
			bgImage = ImageIO.read(getClass().getClassLoader().getResource("image/diu.png"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		//处理头像
		BufferedImage formatAvatarImage = new BufferedImage(hdW, hdW, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = formatAvatarImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
		//图片是一个圆型
		Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, hdW, hdW);
		//需要保留的区域
		graphics.setClip(shape);
		graphics.rotate(Math.toRadians(-50),hdW / 2,hdW / 2);
		graphics.drawImage(headImage.getScaledInstance(hdW,hdW,Image.SCALE_SMOOTH), 0, 0, hdW, hdW, null);
		graphics.dispose();

		//重合图片
		Graphics2D graphics2D = bgImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
		graphics2D.drawImage(formatAvatarImage,110 - hdW / 2,275 - hdW / 2,hdW,hdW,null);//头画背景上
		graphics2D.dispose();

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bgImage, "PNG", bos);
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		} catch (IOException e) {
			return mif.text("图片发送失败：" + e.getMessage()).toMessage();
		}
	}

	private Message getPa(String url){
		BufferedImage headImage;
		BufferedImage bgImage;
		int hdW = 65;

		try {
			headImage = (BufferedImage) ImageHandle.getHeadImage(url, hdW);
			bgImage = ImageIO.read(getClass().getClassLoader().getResource("image/pa/" + MyUtils.randomInt(1,16) + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		//处理头像
		BufferedImage formatAvatarImage = new BufferedImage(hdW, hdW, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = formatAvatarImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
		//图片是一个圆型
		Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, hdW, hdW);
		//需要保留的区域
		graphics.setClip(shape);
		graphics.drawImage(headImage.getScaledInstance(hdW,hdW,Image.SCALE_SMOOTH), 0, 0, hdW, hdW, null);
		graphics.dispose();

		//重合图片
		Graphics2D graphics2D = bgImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
		graphics2D.drawImage(formatAvatarImage,0,bgImage.getHeight() - hdW,hdW,hdW,null);//头画背景上
		graphics2D.dispose();

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bgImage, "PNG", bos);
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return mif.text("图片发送失败：" + e.getMessage()).toMessage();
		}
	}

	private Message getJiao(String url){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedImage jiao_top = null;
		try {
			jiao_top = ImageIO.read(getClass().getClassLoader().getResource("image/jiao/jiao_top"));
		} catch (IOException e) {
			e.printStackTrace();
			return mif.text("在生成图片时出现了一点点错误").toMessage();
		}

		GifDecoder decoder = new GifDecoder();
		int status = 0;
		//w:60 h:67
		status = decoder.read(getClass().getResourceAsStream("/image/jiao/jiao.gif"));
		if (status != GifDecoder.STATUS_OK) {
			return mif.text("在生成图片时出现了一点点错误:" + status).toMessage();
		}
		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		//保存的目标图片
		animatedGifEncoder.start(bos);
		animatedGifEncoder.setRepeat(decoder.getLoopCount());
		animatedGifEncoder.setDelay(decoder.getDelay(0));
		for (int i = 0; i < decoder.getFrameCount(); i++) {
			BufferedImage image = decoder.getFrame(i);
			//加入头像  直径：38
			int f = 38;
			//得到头
			BufferedImage headImage = (BufferedImage) ImageHandle.getHeadImage(url, f);
			//编辑头，加缺口
			Graphics2D headGraphics2D = headImage.createGraphics();
			headGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
			headGraphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));
			headGraphics2D.fillOval(21, 0, 8, 10);//画椭圆
			headGraphics2D.dispose();
			//把头放上去
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
			graphics.drawImage(headImage,0,image.getHeight() - headImage.getHeight() + 6,f,f,null);//头画背景上
			//手
			graphics.drawImage(jiao_top,0,0,image.getWidth(),image.getHeight(),null);//把手画上
			graphics.dispose();

			animatedGifEncoder.addFrame(image);
		}
		if(animatedGifEncoder.finish()){
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		}

		return mif.text("在生成图片时出现了一点点错误").toMessage();
	}

	private Message getRua(String url){
		int hw = 80;//头像宽度
		//得到头像
		BufferedImage headImage = (BufferedImage) ImageHandle.getHeadImage(url, hw);
		//得到mo的gif
		GifDecoder decoder = new GifDecoder();
		int status = 0;
		//w:60 h:67
		status = decoder.read(getClass().getResourceAsStream("/image/mo/mo.gif"));
		if (status != GifDecoder.STATUS_OK) {
			return mif.text("在生成图片时出现了一点点错误:" + status).toMessage();
		}
		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		//保存的目标图片
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		animatedGifEncoder.start(bos);
		animatedGifEncoder.setRepeat(decoder.getLoopCount());
		animatedGifEncoder.setDelay(decoder.getDelay(0));

		for (int i = 0; i < decoder.getFrameCount(); i++) {
			BufferedImage image = decoder.getFrame(i);
			//清除背景
			Graphics2D graphics2D = image.createGraphics();
			graphics2D.setColor(Color.getColor("#fffbf3"));
			graphics2D.fillRect(0,0,image.getWidth(),image.getHeight());
			//开始生成
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
			try {
				int mfd = 15;//最大幅度
				int fd = 0;//幅度
				//得到手
				BufferedImage shou = ImageIO.read(getClass().getClassLoader().getResource("image/mo/" + (i + 1) + ".png"));
				int y = (image.getHeight() - hw - 10);
				//拉伸头
				switch (i + 1){
					case 1:
						fd = 0;
						graphics2D.drawImage(headImage,20 - (fd / 2), y + fd,hw + fd, hw - fd,null);
						break;
					case 2:
						fd = mfd / 2;
						graphics2D.drawImage(headImage,20 - (fd / 2), y + fd,hw + fd, hw - fd,null);
						break;
					case 3:
						fd = mfd;
						graphics2D.drawImage(headImage,20 - (fd / 2),y + fd,hw + fd, hw - fd,null);
						break;
					case 4:
						fd = mfd / 2 + 1;
						graphics2D.drawImage(headImage,20 - (fd / 2),y + fd,hw + fd, hw - fd,null);
						break;
					case 5:
						fd = 1;
						graphics2D.drawImage(headImage,20,y,hw,hw,null);
						break;
				}
				//放手
				graphics2D.drawImage(shou,0,0,shou.getWidth(),shou.getHeight(),null);
			} catch (IOException e) {
				e.printStackTrace();
				return mif.text("在生成图片时出现了一点点错误").toMessage();
			}
			graphics2D.dispose();

			animatedGifEncoder.addFrame(image);
		}
		if(animatedGifEncoder.finish()){
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		}
		return mif.text("在生成图片时出现了一点点错误").toMessage();
	}

	private Message getSuperPa(String url){
		int w = 360;
		int h = 360;
		int delay = 50;
		int hdW = 65;
		int paIndex = 16;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		BufferedImage headImage = (BufferedImage) ImageHandle.getHeadImage(url, hdW);;

		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		animatedGifEncoder.setSize(w,h);
		animatedGifEncoder.start(bos);
		animatedGifEncoder.setDelay(delay);
		animatedGifEncoder.setRepeat(paIndex);

		for (int i = 0; i < paIndex; i++){
			BufferedImage bgImage;
			try {
				bgImage = ImageIO.read(getClass().getClassLoader().getResource("image/pa/" + (i + 1) + ".jpg"));
				BufferedImage bufferedImage = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D graphics2D = bufferedImage.createGraphics();
				graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
				graphics2D.drawImage(bgImage,0,0,w,h,null);
				graphics2D.drawImage(headImage,0,h - hdW,hdW,hdW,null);//头画背景上
				graphics2D.dispose();
				animatedGifEncoder.addFrame(bufferedImage);
			} catch (IOException e) {
				e.printStackTrace();
				return mif.text("在生成图片时出现了一点点错误").toMessage();
			}
		}

		if(animatedGifEncoder.finish()){
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		}
		return mif.text("在生成图片时出现了一点点错误").toMessage();
	}

	private Message getMuaGif(String url){
		int bgWH = 240;//背景宽高
		int delay = 50;//每张图之间的延迟
		BufferedImage headImage = (BufferedImage) ImageHandle.getMemberHeadImageNoFrame(url,80);//得到头像

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
		animatedGifEncoder.setSize(bgWH,bgWH);
		animatedGifEncoder.start(bos);
		animatedGifEncoder.setDelay(delay);
		animatedGifEncoder.setRepeat(13);

		//x,y,w,h
		int imageHeadInfo[][] = {
				{46,117,62,64},//1
				{68,107,63,66},//2
				{76,107,58,69},//3
				{55,123,58,63},//4
				{66,123,56,68},//5
				{71,122,54,66},//6
				{24,146,57,56},//7
				{32,128,71,72},//8
				{73,110,55,72},//9
				{57,118,54,65},//10
				{76,114,60,69},//11
				{47,137,56,66},//12
				{22,149,68,65}//13
		};//头像位置信息

		for (int i = 0; i < 13; i++){
			BufferedImage bgImage;
			try {
				//得到背景
				bgImage = ImageIO.read(getClass().getClassLoader().getResource("image/mua/" + (i + 1) + ".png"));
				//空白底
				BufferedImage bufferedImage = new BufferedImage(bgWH,bgWH,BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D graphics2D = bufferedImage.createGraphics();
				graphics2D.setColor(Color.WHITE);
				graphics2D.fillRect(0,0,bgWH,bgWH);
				graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
				//先画头
				graphics2D.drawImage(headImage,imageHeadInfo[i][0],imageHeadInfo[i][1],imageHeadInfo[i][2],imageHeadInfo[i][3],null);
				//画背景
				graphics2D.drawImage(bgImage,0,0,bgWH,bgWH,null);
				graphics2D.dispose();
				animatedGifEncoder.addFrame(bufferedImage);
			} catch (IOException e) {
				e.printStackTrace();
				return mif.text("在生成图片时出现了一点点错误").toMessage();
			}
		}

		if(animatedGifEncoder.finish()){
			return mif.imageByByteArray(bos.toByteArray()).toMessage();
		}
		return mif.text("在生成图片时出现了一点点错误").toMessage();
	}

}

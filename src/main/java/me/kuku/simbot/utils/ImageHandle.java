package me.kuku.simbot.utils;

import me.kuku.utils.IOUtils;
import me.kuku.utils.OkHttpUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageHandle {

	public static Image getHeadImage(String url, int size){
		InputStream is = null;
		try {
			is = OkHttpUtils.getByteStream(url);
			return getHeadImage(ImageIO.read(is), size, size);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.close(is);
		}
	}

	public static Image getMemberHeadImageNoFrame(String url, int size){
		InputStream is = null;
		try {
			is = OkHttpUtils.getByteStream(url);
			return getHeadImageNoFrame(ImageIO.read(is), size, size);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.close(is);
		}
	}

	/**
	 * 得到圆头像
	 * @param image
	 * @param width
	 * @param high
	 * @return
	 */
	public static Image getHeadImage(Image image, int width, int high){
		// 透明底的图片
		BufferedImage formatAvatarImage = new BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = formatAvatarImage.createGraphics();
		//把图片切成一个园
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//留一个像素的空白区域，这个很重要，画圆的时候把这个覆盖
		int border = 1;
		//图片是一个圆型
		Ellipse2D.Double shape = new Ellipse2D.Double(border, border, width - border * 2, width - border * 2);
		//需要保留的区域
		graphics.setClip(shape);
		graphics.drawImage(image.getScaledInstance(width,high,Image.SCALE_SMOOTH), border, border, width - border * 2, width - border * 2, null);
		graphics.dispose();
		//在圆图外面再画一个圆
		//新创建一个graphics，这样画的圆不会有锯齿
		graphics = formatAvatarImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int border1 = 1;
		//画笔是4.5个像素，BasicStroke的使用可以查看下面的参考文档
		//使画笔时基本会像外延伸一定像素，具体可以自己使用的时候测试
		Stroke s = new BasicStroke(1F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		graphics.setStroke(s);
		graphics.setColor(Color.WHITE);
		graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);
		graphics.dispose();

		return formatAvatarImage;
	}

	/**
	 * 得到圆头像
	 * @param image
	 * @param width
	 * @param high
	 * @return
	 */
	public static Image getHeadImageNoFrame(Image image, int width, int high){
		// 透明底的图片
		BufferedImage formatAvatarImage = new BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = formatAvatarImage.createGraphics();
		//把图片切成一个园
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//留一个像素的空白区域，这个很重要，画圆的时候把这个覆盖
		int border = 0;
		//图片是一个圆型
		Ellipse2D.Double shape = new Ellipse2D.Double(border, border, width - border * 2, width - border * 2);
		//需要保留的区域
		graphics.setClip(shape);
		graphics.drawImage(image.getScaledInstance(width,high,Image.SCALE_SMOOTH), border, border, width - border * 2, width - border * 2, null);
		graphics.dispose();
		//在圆图外面再画一个圆
		//新创建一个graphics，这样画的圆不会有锯齿
		graphics = formatAvatarImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int border1 = 1;
		//画笔是4.5个像素，BasicStroke的使用可以查看下面的参考文档
		//使画笔时基本会像外延伸一定像素，具体可以自己使用的时候测试
		Stroke s = new BasicStroke(1F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		graphics.setStroke(s);
		graphics.setColor(Color.WHITE);
		graphics.drawOval(border1, border1, width - border1 * 2, width - border1 * 2);
		graphics.dispose();

		return formatAvatarImage;
	}

	/**
	 * Image 转 Buf
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage)image;
		}
		image = new ImageIcon(image).getImage();
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(
					image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			e.printStackTrace();
		}

		if (bimage == null) {
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		Graphics g = bimage.createGraphics();

		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}
}
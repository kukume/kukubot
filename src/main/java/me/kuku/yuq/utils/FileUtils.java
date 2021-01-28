package me.kuku.yuq.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileUtils {

	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();

	static {
		getAllFileType(); // 初始化文件类型信息
	}

	/**
	 * 将常见文件类型放入到map中
	 *
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:35:22
	 */
	private static void getAllFileType() {
		FILE_TYPE_MAP.put("jpg", "FFD8FF"); // JPEG (jpg)
		FILE_TYPE_MAP.put("png", "89504E47"); // PNG (png)
		FILE_TYPE_MAP.put("gif", "47494638"); // GIF (gif)
		FILE_TYPE_MAP.put("tif", "49492A00"); // TIFF (tif)
		FILE_TYPE_MAP.put("bmp", "424D"); // Windows Bitmap (bmp)
		FILE_TYPE_MAP.put("dwg", "41433130"); // CAD (dwg)
		FILE_TYPE_MAP.put("html", "68746D6C3E"); // HTML (html)
		FILE_TYPE_MAP.put("rtf", "7B5C727466"); // Rich Text Format (rtf)
		FILE_TYPE_MAP.put("xml", "3C3F786D6C");
		FILE_TYPE_MAP.put("zip", "504B0304");
		FILE_TYPE_MAP.put("rar", "52617221");
		FILE_TYPE_MAP.put("psd", "38425053"); // Photoshop (psd)
		FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A"); // Email
		FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F"); // Outlook Express (dbx)
		FILE_TYPE_MAP.put("pst", "2142444E"); // Outlook (pst)
		FILE_TYPE_MAP.put("xls", "D0CF11E0"); // MS Word
		FILE_TYPE_MAP.put("doc", "D0CF11E0"); // MS Excel 注意：word 和 excel的文件头一样
		FILE_TYPE_MAP.put("mdb", "5374616E64617264204A"); // MS Access (mdb)
		FILE_TYPE_MAP.put("wpd", "FF575043"); // WordPerfect (wpd)
		FILE_TYPE_MAP.put("eps", "252150532D41646F6265");
		FILE_TYPE_MAP.put("ps", "252150532D41646F6265");
		FILE_TYPE_MAP.put("pdf", "255044462D312E"); // Adobe Acrobat (pdf)
		FILE_TYPE_MAP.put("qdf", "AC9EBD8F"); // Quicken (qdf)
		FILE_TYPE_MAP.put("pwl", "E3828596"); // Windows Password (pwl)
		FILE_TYPE_MAP.put("wav", "57415645"); // Wave (wav)
		FILE_TYPE_MAP.put("avi", "41564920");
		FILE_TYPE_MAP.put("ram", "2E7261FD"); // Real Audio (ram)
		FILE_TYPE_MAP.put("rm", "2E524D46"); // Real Media (rm)
		FILE_TYPE_MAP.put("mpg", "000001BA"); //
		FILE_TYPE_MAP.put("mov", "6D6F6F76"); // Quicktime (mov)
		FILE_TYPE_MAP.put("asf", "3026B2758E66CF11"); // Windows Media (asf)
		FILE_TYPE_MAP.put("mid", "4D546864"); // MIDI (mid)
	}

//  /**
//   * 获取图片文件实际类型,若不是图片则返回null]
//   *
//   * @author Xewee.Zhiwei.Wang
//   * @version 2011-9-18 下午12:35:59
//   * @param f
//   * @return
//   */
//  public final static String getImageFileType(File f) {
//      if (isImage(f)) {
//          try {
//              ImageInputStream iis = ImageIO.createImageInputStream(f);
//              Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
//              if (!iter.hasNext()) {
//                  return null;
//              }
//              ImageReader reader = iter.next();
//              iis.close();
//              return reader.getFormatName();
//          } catch (IOException e) {
//              return null;
//          } catch (Exception e) {
//              return null;
//          }
//      }
//      return null;
//  }

	/**
	 * 获取文件类型,包括图片,若格式不是已配置的,则返回null
	 *
	 * @param file
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:36:32
	 */
	public static String getFileTypeByFile(File file) {
		String filetype = null;
		byte[] b = new byte[50];
		try {
			InputStream is = new FileInputStream(file);
			is.read(b);
			filetype = getFileTypeByStream(b);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filetype;
	}

	/**
	 * 通过字节流获得文件类型
	 *
	 * @param b
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:37:03
	 */
	public static String getFileTypeByStream(byte[] b) {
		String filetypeHex = String.valueOf(getFileHexString(b));
		Iterator<Map.Entry<String, String>> entryiterator = FILE_TYPE_MAP
				.entrySet().iterator();
		while (entryiterator.hasNext()) {
			Map.Entry<String, String> entry = entryiterator.next();
			String fileTypeHexValue = entry.getValue();
			if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
				return entry.getKey();
			}
		}
		return null;
	}

//  /**
//   * 判断文件是不是图片
//   *
//   * @author Xewee.Zhiwei.Wang
//   * @version 2011-9-18 下午12:37:54
//   * @param file
//   * @return
//   */
//  public static final boolean isImage(File file) {
//      boolean flag = false;
//      try {
//          BufferedImage bufreader = ImageIO.read(file);
//          int width = bufreader.getWidth();
//          int height = bufreader.getHeight();
//          if (width == 0 || height == 0) {
//              flag = false;
//          }
//          else {
//              flag = true;
//          }
//      } catch (IOException e) {
//          flag = false;
//      } catch (Exception e) {
//          flag = false;
//      }
//      return flag;
//  }

	/**
	 * 获得文件的16进制数据
	 *
	 * @param b
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:38:26
	 */
	public static String getFileHexString(byte[] b) {
		StringBuilder stringBuilder = new StringBuilder();
		if (b == null || b.length <= 0) {
			return null;
		}
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 删除path（该path可能是文件也可能是文件夹）
	 *
	 * @param file           要删除的文件或者目录
	 * @param includeSubFile 如果被删出的是目录，是否循环删除该目录中的子目录
	 * @return 操作是否成功
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:49:15
	 */
	public final boolean deleteFile(File file, boolean includeSubFile) {
		if (!file.exists()) {
			return false;
		}
		if (file.isFile()) {
			System.out.println("del file:" + file.toString());
			return file.delete();
		} else if (file.isDirectory()) {

			File[] fileList = file.listFiles();
			if (includeSubFile) {
				for (int i = 0; i < fileList.length; i++) {
					deleteFile(fileList[i], includeSubFile);
				}
			} else {
				for (int i = 0; i < fileList.length; i++) {
					if (fileList[i].isFile()) {
						deleteFile(fileList[i], includeSubFile);
					}
				}
			}
		}
		return true;
	}

	/**
	 * 删除path（该path可能是文件也可能是文件夹）
	 *
	 * @param path           要删除的文件或者目录
	 * @param includeSubFile 如果被删出的是目录，是否循环删除该目录中的子目录
	 * @return 操作是否成功
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:49:15
	 */
	public final boolean deleteFile(String path, boolean includeSubFile) {
		return deleteFile(new File(path), includeSubFile);
	}

	/**
	 * 赋值文件scrFile到目地目录destFolder
	 *
	 * @param scrFile        源文件，可能是文件，可能是目录
	 * @param destFolder     目地文件，只能是目录
	 * @param includeSubFile 如果源文件是目录，是否循环复制源文件的子目录
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:48:34
	 */
	public final boolean copyFile(File scrFile, File destFolder, boolean includeSubFile) {
		//未实现
		return true;
	}

	/**
	 * 赋值文件scrFile到目地目录destFolder
	 *
	 * @param scrFile        源文件，可能是文件，可能是目录
	 * @param destFolder     目地文件，只能是目录
	 * @param includeSubFile 如果源文件是目录，是否循环复制源文件的子目录
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:48:34
	 */
	public final boolean copyFile(String scrFile, String destFolder, boolean includeSubFile) {
		//未实现
		return copyFile(new File(scrFile), new File(destFolder), includeSubFile);
	}

	/**
	 * 创建文件或者目录
	 *
	 * @param file
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:48:15
	 */
	public final boolean createFile(File file) {
		try {
			if (file.isDirectory()) {
				return file.mkdirs();
			} else {
				return file.createNewFile();
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 创建文件或者目录
	 *
	 * @param file
	 * @return
	 * @author Xewee.Zhiwei.Wang
	 * @version 2011-9-18 下午12:48:05
	 */
	public final boolean createFile(String file) {
		return createFile(new File(file));
	}
}

package me.kuku.yuq.utils;

import com.IceCreamQAQ.Yu.util.IO;

import java.io.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class IOUtils {

	private static final File tmpLocation = new File("tmp");
	private static final String tmp = "tmp";

	public static File writeTmpFile(String fileName, InputStream is, boolean isClose){
		if (!tmpLocation.exists())
			tmpLocation.mkdir();
		File file = new File(tmp, fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (isClose)
				IO.copy(is, fos);
			else IO.copy(is, fos, false);
		}catch (IOException e){
			e.printStackTrace();
		}finally {
			if (isClose) {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	public static File writeTmpFile(String fileName, InputStream is){
		return writeTmpFile(fileName, is, true);
	}

	public static File writeTmpFile(String fileName, byte[] bytes){
		if (!tmpLocation.exists())
			tmpLocation.mkdir();
		File file = new File(tmp, fileName);
		IO.writeFile(file, bytes);
		return file;
	}

	public static void close(InputStream is){
		if (is != null){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] read(File file){
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static byte[] read(InputStream fis, boolean isClose){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isClose) {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bos.toByteArray();
	}

	public static byte[] read(InputStream fis){
		return read(fis, true);
	}


}

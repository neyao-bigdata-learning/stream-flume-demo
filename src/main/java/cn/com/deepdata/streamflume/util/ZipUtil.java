package cn.com.deepdata.streamflume.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
	private static int smartCM = -1;
	private static Logger LOG = LoggerFactory.getLogger(ZipUtil.class);
	static final int version = 1;
	public static int OSTypeUnknown = -1;
	public static int OSTypeWindows = 0;
	public static int OSTypeMacOS = 1;
	public static int OSTypeIOS = 2;
	public static int OSTypeAndroid = 3;
	public static int OSTypeLinux = 4;

	public final static int NoCompress = 0;
	public final static int GzipCompress = 1;
	public final static int ZipCompress = 2;
	public final static int BZip2Compress = 3;

	/**
	 * 
	 * 浣跨敤gzip杩涜鍘嬬缉
	 */
	public static String gzip(String primStr) {
		if (primStr == null || primStr.length() == 0) {
			return primStr;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(primStr.getBytes());
		} catch (IOException e) {
			LOG.error(e.toString());
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					LOG.error(e.toString());
				}
			}
		}

		return new String(new Base64().encode(out.toByteArray()));
	}

	/**
	 * 
	 * <p>
	 * Description:浣跨敤gzip杩涜瑙ｅ帇缂�
	 * </p>
	 * 
	 * @param compressedStr
	 * @return
	 */
	public static String gunzip(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream ginzip = null;
		byte[] compressed = null;
		String decompressed = null;
		try {
			compressed = new Base64().decode(compressedStr);
			in = new ByteArrayInputStream(compressed);
			ginzip = new GZIPInputStream(in);

			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginzip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			LOG.error(e.toString());
		} finally {
			if (ginzip != null) {
				try {
					ginzip.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

		return decompressed;
	}

	/**
	 * 浣跨敤zip杩涜鍘嬬缉
	 * 
	 * @param str
	 *            鍘嬬缉鍓嶇殑鏂囨湰
	 * @return 杩斿洖鍘嬬缉鍚庣殑鏂囨湰
	 */
	public static final String zipBase64(String str) {
		if (str == null)
			return null;
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		String compressedStr = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str.getBytes());
			zout.closeEntry();
			compressed = out.toByteArray();
			compressedStr = new String(new Base64().encode(compressed));
		} catch (IOException e) {
			compressed = null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return compressedStr;
	}

	public static final byte[] zip(String str) {
		if (str == null)
			return null;
		// long start = System.currentTimeMillis();
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str.getBytes("UTF-8"));
			zout.closeEntry();
			compressed = out.toByteArray();
		} catch (IOException e) {
			compressed = null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

		return compressed;
	}

	/**
	 * 浣跨敤zip杩涜瑙ｅ帇缂�
	 * 
	 * @param compressed
	 *            鍘嬬缉鍚庣殑鏂囨湰
	 * @return 瑙ｅ帇鍚庣殑瀛楃涓�
	 */
	public static final String unzipBase64(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}

		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		String decompressed = null;
		try {
			byte[] compressed = new Base64().decode(compressedStr);
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			decompressed = null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}

	public static final byte[] unzip(byte[] compressed) {
		if (compressed == null) {
			return null;
		}

		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		byte[] decompressed = null;
		try {
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toByteArray();
		} catch (IOException e) {
			decompressed = null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}

	public static String bzip2Base64(String s) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
			bzip2.write(s.getBytes("UTF-8"));
			bzip2.flush();
			bzip2.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			LOG.error(ex.toString());
		}
		String r = new String(new Base64().encode(b));

		return r;
	}

	public static byte[] bzip2(String s) {
		// long start = System.currentTimeMillis();
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
			bzip2.write(s.getBytes("UTF-8"));
			bzip2.flush();
			bzip2.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			LOG.error(ex.toString());
		}

		return b;
	}

	public static String bunzip2Base64(String s)
			throws UnsupportedEncodingException {
		byte[] b = null;
		try {
			byte[] compressed = new Base64().decode(s);
			ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
			CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			bzip2.close();
			bis.close();
		} catch (Exception ex) {
			LOG.error(ex.toString());
		}
		return new String(b, "UTF-8");
	}

	public static byte[] bunzip2(byte[] compressed)
			throws UnsupportedEncodingException {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
			CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			bzip2.close();
			bis.close();
		} catch (Exception ex) {
			LOG.error(ex.toString());
		}
		return b;
	}

	public static int os() {
		String osn = System.getProperty("os.name").toLowerCase(
				Locale.SIMPLIFIED_CHINESE);
		if (osn.contains("windows"))
			return OSTypeWindows;
		if (osn.contains("linux")) {
			String vmvendor = System.getProperty("java.vm.vendor", "")
					.toLowerCase(Locale.SIMPLIFIED_CHINESE);
			if (vmvendor.contains("android")) {
				return OSTypeAndroid;
			} else {
				return OSTypeLinux;
			}
		}
		if (osn.contains("mac")) {
			return OSTypeMacOS;
		}
		if (osn.contains("ios")) {
			return OSTypeIOS;
		}
		return OSTypeUnknown;
	}

	public static int smartCompressMethod() {
		if (smartCM >= 0) {
			return smartCM;
		}
		int os = os();
		if (os == OSTypeWindows || os == OSTypeMacOS || os == OSTypeLinux) {
			smartCM = ZipUtil.BZip2Compress;
		} else {
			smartCM = ZipUtil.ZipCompress;
		}
		smartCM = ZipUtil.ZipCompress;
		return smartCM;
	}

	public static byte[] compressedData(String pdata, int compressMethod) {

		byte[] compressed = null;
		if (compressMethod == ZipUtil.GzipCompress) {
			// compressed = ZipUtil.gzip(pdata);
		} else if (compressMethod == ZipUtil.BZip2Compress) {
			compressed = ZipUtil.bzip2(pdata);
		} else if (compressMethod == ZipUtil.ZipCompress) {
			compressed = ZipUtil.zip(pdata);
		}
		if (compressed == null) {
			compressMethod = ZipUtil.NoCompress;
			try {
				compressed = pdata.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {

			}
		}
		byte[] r = new byte[compressed.length + 6];
		int ri = 0;
		for (int i = 0; i < 4; i++) {
			r[ri++] = (byte) (version >> 8 * i & 0xFF);
		}
		for (int i = 0; i < 2; i++) {
			r[ri++] = (byte) (compressMethod >> 8 * i & 0xFF);
		}
		System.arraycopy(compressed, 0, r, ri, compressed.length);
		return r;
	}

}

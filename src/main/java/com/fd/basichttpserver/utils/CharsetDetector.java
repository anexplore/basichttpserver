package com.fd.basichttpserver.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

public class CharsetDetector {
	private static class NotifyObject {
		boolean found = false;
		String charset;

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

		public boolean isFound() {
			return found;
		}

		public void setFound(boolean found) {
			this.found = found;
		}

	}

	public static String getCharset(byte[] bytes) {
		InputStream in = new ByteArrayInputStream(bytes);
		return getCharset(in);
	}

	private static String getCharset(InputStream in) {
		nsDetector det = new nsDetector(3);
		final NotifyObject target = new NotifyObject();
		nsICharsetDetectionObserver cdo = new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				target.setFound(true);
				target.setCharset(charset);
			}
		};

		det.Init(cdo);

		int len;
		byte[] buf = new byte[1024];
		boolean done = false;
		boolean isAscii = true;
		try {
			while ((len = in.read(buf, 0, buf.length)) != -1) {
				if (isAscii)
					isAscii = det.isAscii(buf, len);
				if (!isAscii && !done) {
					done = det.DoIt(buf, len, false);
				}
				if (done) {
					break;
				}
			}
			det.DataEnd();// 最后要调用此方法，此时，Notify被调用。
			if (isAscii) {
				return "ASCII";
			}
			if (!target.isFound()) {
				return "UTF-8";
			} else {
				return target.getCharset();
			}
		} catch (IOException ex) {
			return "UTF-8";
		}
	}
}

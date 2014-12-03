package com.fd.basichttpserver.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.Args;

public class StringEntityWithGzipCompress extends StringEntity {

	public StringEntityWithGzipCompress(String string)
			throws UnsupportedEncodingException {
		super(string);
	}

	public StringEntityWithGzipCompress(String result, ContentType create) {
		super(result, create);
	}

	public long getContentLength() {
		return -1;
	}

	public boolean isChunked() {
		// force content chunking
		return true;
	}

	public InputStream getContent() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		Args.notNull(outstream, "GZIP Output stream");
		GZIPOutputStream gzip = new GZIPOutputStream(outstream);
		gzip.write(this.content);
		gzip.flush();
		gzip.close();
		outstream.flush();
	}
}

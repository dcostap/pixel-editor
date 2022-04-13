package dev.dcostap.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by Darius on 20-Aug-19.
 */
public class Zlib {
	/**
	 * Compresses a file with zlib compression.
	 */
	public static String compress(String string)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(baos);
		dos.write(string.getBytes());
		dos.flush();
		dos.close();
		return baos.toString();
	}

	/**
	 * Decompresses a zlib compressed file.
	 */
	public static String decompress(String string)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(string.getBytes());
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		InflaterInputStream iis = new InflaterInputStream(bais);

		StringBuilder result = new StringBuilder();
		byte[] buf = new byte[5];
		int rlen;
		while ((rlen = iis.read(buf)) != -1) {
			result.append(new String(Arrays.copyOf(buf, rlen)));
		}
		return result.toString();
	}
}

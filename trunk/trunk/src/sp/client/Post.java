/*
 * Program ...... SinglePaint
 * File ......... Post.java
 * Author ....... Harald Hetzner
 * 
 * Copyright (C) 2006  Harald Hetzner
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 * 
 * Harald Hetzner <singlepaint [at] mkultra [dot] dyndns [dot] org>
 */

package sp.client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import sp.Settings;
import sp.util.Base64;

/**
 * This class implements a method that is able to save a
 * <code>BufferedImage</code> to a PNG-file using an HTTP-connection. The HTTP
 * request method used for this purpose is the POST method.
 * 
 * @author Harald Hetzner
 * 
 */
public class Post {

	/**
	 * This method is able to save an image as a PNG using an HTTP-connection to
	 * a server. Therefor this method will use the HTTP request method POST. The
	 * <code>BufferedImage</code> is converted to a PNG-image, encoded using
	 * base64-encoding and then send to the server via the HTTP-connection.
	 * 
	 * @param image
	 *            the image which should be saved as a PNG
	 * @return <code>true</code> if it seems that transferring the data
	 *         succeeded, <code>false</code> if an error occurred
	 */
	public static boolean postImage(BufferedImage image) {

		URL url;
		try {
			url = new URL(Settings.postURL);
		} catch (MalformedURLException e) {
			System.err.println(e.toString());
			return false;
		}
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			connection.setDoOutput(true);
			PrintWriter out = new PrintWriter(connection.getOutputStream());

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

			synchronized (image) {
				ImageIO.write(image, "png", byteStream);
			}
			byte[] pngBytes = byteStream.toByteArray();
			byteStream.close();

			char[] base64Data = Base64.encode(pngBytes);

			// encode the message
			StringBuffer sb = new StringBuffer(Settings.POST_VARIABLE_IMAGE
					.length()
					+ 1 + base64Data.length);
			sb.append(Settings.POST_VARIABLE_IMAGE);
			sb.append('=');
			sb.append(base64Data);

			// send the encoded message
			out.println(sb.toString());
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			/*
			 * String line; while ((line = in.readLine()) != null) {
			 * System.out.println(line); }
			 */
			while (in.readLine() != null) {
			}
			in.close();
		} catch (IOException e) {
			System.err.println(e.toString());
			return false;
		}
		return true;
	}
}

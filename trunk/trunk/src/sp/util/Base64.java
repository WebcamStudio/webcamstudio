//////////////////////license & copyright header///////////////////////
//                                                                   //
//                Copyright (c) 1998 by Kevin Kelley                 //
//                                                                   //
// This program is free software; you can redistribute it and/or     //
// modify it under the terms of the GNU General Public License as    //
// published by the Free Software Foundation; either version 2 of    //
// the License, or (at your option) any later version.               //
//                                                                   //
// This program is distributed in the hope that it will be useful,   //
// but WITHOUT ANY WARRANTY; without even the implied warranty of    //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the     //
// GNU General Public License for more details.                      //
//                                                                   //
// You should have received a copy of the GNU General Public License //
// along with this program in the file 'gpl.html'; if not, write to  //
// the Free Software Foundation, Inc., 59 Temple Place - Suite 330,  //
// Boston, MA 02111-1307, USA, or contact the author:                //
//                                                                   //
//                       Kevin Kelley  <kelley@iguana.ruralnet.net>  //
//                                                                   //
////////////////////end license & copyright header/////////////////////

package sp.util;

/**
 * Provides encoding of raw bytes to base64, and decoding of base64 to raw
 * bytes.
 */
public class Base64 {

	static char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
			.toCharArray();

	static byte[] codes = new byte[256];
	static {
		for (int i = 0; i < 256; i++)
			codes[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			codes[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			codes[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			codes[i] = (byte) (52 + i - '0');
		codes['+'] = 62;
		codes['/'] = 63;
	}

	static public char[] encode(byte[] data) {
		char[] out = new char[((data.length + 2) / 3) * 4];

		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;

			int val = (0xFF & (int) data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & (int) data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & (int) data[i + 2]);
				quad = true;
			}
			out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = alphabet[val & 0x3F];
			val >>= 6;
			out[index + 0] = alphabet[val & 0x3F];
		}
		return out;
	}

	static public byte[] decode(char[] data) {
		int len = ((data.length + 3) / 4) * 3;
		if (data.length > 0 && data[len - 1] == '=')
			--len;
		if (data.length > 0 && data[len - 2] == '=')
			--len;
		byte[] out = new byte[len];

		int shift = 0; // # of excess bits stored in accum
		int accum = 0; // excess bits
		int index = 0;

		for (int ix = 0; ix < data.length; ix++) {
			int value = codes[data[ix] & 0xFF]; // ignore high byte of char
			if (value >= 0) { // skip over non-code
				accum <<= 6; // bits shift up by 6 each time thru
				shift += 6; // loop, with new bits being put in
				accum |= value; // at the bottom.
				if (shift >= 8) { // whenever there are 8 or more shifted in,
					shift -= 8; // write them out (from the top, leaving any
					out[index++] = // excess at the bottom for next iteration.
					(byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length)
			throw new Error("miscalculated data length!");

		return out;
	}

	public static void main(String[] args) {
		byte[] data = { 0, -1, 2, -3, 4, 5, -6, 7, 8, -9, 11, -12, 14, 15 };
		if (args.length == 1)
			data = args[0].getBytes();

		char[] code = Base64.encode(data);
		byte[] datb = Base64.decode(code);
		char[] recd = Base64.encode(datb);

		System.out.println("data = " + fromBytes(data));
		System.out.println("code = " + fromBytes(code));
		System.out.println("datb = " + fromBytes(datb));
		System.out.println("recd = " + fromBytes(recd));
		compare(data, datb);
		compare(code, recd);
	}

	static void compare(char[] b1, char[] b2) {
		if (b1 == null || b2 == null) {
			System.out.println("Null array!");
		} else if (b1.length != b2.length) {
			System.out.println("arrays are different lengths!");
		} else
			for (int i = 0; i < b1.length; i++) {
				if (b1[i] != b2[i]) {
					System.out.println("arrays disagree at byte " + i);
					return;
				}
			}
	}

	static void compare(byte[] b1, byte[] b2) {
		if (b1 == null || b2 == null) {
			System.out.println("Null array!");
			return;
		}
		if (b1.length != b2.length) {
			System.out.println("arrays are different lengths!");
			return;
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				System.out.println("arrays disagree at byte " + i);
				return;
			}
		}
	}

	static String fromBytes(byte[] data) {
		StringBuffer buf = new StringBuffer(data.length * 3);
		for (int i = 0; i < data.length; i++) {
			if (i > 0)
				buf.append(' ');
			String hex = Integer.toHexString(0xff & data[i]);
			if (hex.length() < 2)
				buf.append(' ');
			buf.append(hex);
		}
		return new String(buf);
	}

	static String fromBytes(char[] data) {
		return new String(data);
	}
}

package si.tomazk.taplinx_desfire.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Array
{
	private static final String TAG = Array.class.getName();
	private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/ Byte.SIZE);

	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}

	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		
		return data;
	}
	
	public static int byteArrayToIntegerBE(byte[] by)
	{
		int value = 0;

		for (int i = by.length; i > 0; i--)
		{
		   value = (value << 8) + (by[i - 1] & 0xff);
		}
		
		return value;
	}

	public static int byteArrayToIntegerLE(byte[] by)
	{
		int value = 0;

		for (int i = 0; i < by.length; i++)
		{
		   value = (value << 8) + (by[i] & 0xff);
		}
		
		return value;
	}
	
	public static long byteArrayToLong(byte[] by)
	{
		long value = 0;

		for (int i = by.length; i > 0; i--)
		{
		   value = (value << 8) + (by[i - 1] & 0xff);
		}
		
		return value;
	}

	public static long byteArrayToLongBE(byte[] by)
	{
		long value = 0;

		for (int i = 0; i < by.length; i++)
		{
		   value = (value << 8) + (by[i] & 0xff);
		}
		
		return value;
	}

	public static String byteArrayToHex(byte[] bytes)
	{
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static final byte[] integerToByteArrayBE(int value) 
	{
	    return new byte[] { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
	}
	
	public static final byte[] integerToByteArrayLE(int value) 
	{
	    return new byte[] { (byte)value , (byte)(value >>> 8), (byte)(value >>> 16), (byte)(value >>> 24) };
	}
	
	public static final byte[] byteArrayShiftLeft(int howMuch, byte[] bytes)
	{
		byte[] tmp = new byte[bytes.length];
		
		Log.d(TAG, "byteArrayShiftLeft [1]: (" + howMuch +  "): "+ byteArrayToHex(bytes));
		
		System.arraycopy(bytes, howMuch, tmp, 0, (bytes.length - howMuch));
		
		Log.d(TAG, "byteArrayShiftLeft [2]: " + byteArrayToHex(tmp));
		
		System.arraycopy(bytes, 0, tmp, (bytes.length - howMuch), howMuch);
		
		Log.d(TAG, "byteArrayShiftLeft [3]: " + byteArrayToHex(tmp));
		
		return tmp;
	}
	
	public static final byte[] byteArrayShiftRight(int howMuch, byte[] bytes)
	{
		byte[] tmp = new byte[bytes.length];
		
		Log.d(TAG, "byteArrayShiftRight [1]: (" + howMuch +  "): "+ byteArrayToHex(bytes));
		
		System.arraycopy(bytes, 0, tmp, howMuch, (bytes.length - howMuch));
		
		Log.d(TAG, "byteArrayShiftRight [2]: " + byteArrayToHex(tmp));
		
		System.arraycopy(bytes, (bytes.length - howMuch), tmp, 0, howMuch);
		
		return tmp;
	}
	
	public static int byteArrayFind(byte[] array, byte value) 
	{
	    for(int i=0; i<array.length; i++)
	    {
	         if(array[i] == value)
	             return i;
	    }
	    return -1;
	}

	public static int byteArrayIndexOf(byte[] outerArray, byte[] smallerArray)
	{
		for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
			boolean found = true;
			for(int j = 0; j < smallerArray.length; ++j) {
				if (outerArray[i+j] != smallerArray[j]) {
					found = false;
					break;
				}
			}
			if (found) return i;
		}
		return -1;
	}

	public static byte[] byteArrayAppand(byte[] a, byte[] b)
	{
	    byte[] result = new byte[a.length + b.length]; 
	    System.arraycopy(a, 0, result, 0, a.length);
	    System.arraycopy(b, 0, result, a.length, b.length);
	    return result;
	}

	public static short bytesToShort(byte byte_1, byte byte_2)
	{
		byte[] bytes = new byte[]{byte_1,byte_2};
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}

	public static byte[] shortToBytes(short value)
	{
		return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
	}

	public static boolean byteArrayEquals(byte[] a, byte[] a2, int length)
	{
		if (a==a2)
			return true;
		if (a==null || a2==null)
			return false;

		for (int i=0; i<length; i++)
			if (a[i] != a2[i])
				return false;

		return true;
	}

	public static byte[] longToBytesArray(long x)
	{
		buffer.putLong(0, x);
		return buffer.array();
	}

	public static long bytesArrayToLong(byte[] bytes)
	{
		buffer.put(bytes, 0, bytes.length);
		buffer.flip();//need flip
		return buffer.getLong();
	}
}


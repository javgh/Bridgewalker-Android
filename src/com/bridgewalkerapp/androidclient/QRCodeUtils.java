package com.bridgewalkerapp.androidclient;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/* requires zxing core jar */
public class QRCodeUtils {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	public static Bitmap encodeAsBitmap(String contents, int size) {
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix result = writer.encode(contents, BarcodeFormat.QR_CODE, size, size);
		
			int width = result.getWidth();
		    int height = result.getHeight();
		    int[] pixels = new int[width * height];
		    for (int y = 0; y < height; y++) {
		      int offset = y * width;
		      for (int x = 0; x < width; x++) {
		        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
		      }
		    }
	
		    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		    return bitmap;
		} catch (WriterException e) {
			throw new RuntimeException(e);
		}
	}
}

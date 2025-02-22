package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.util.Log;
import static android.graphics.Bitmap.createBitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    @Override
    protected void runPlan1() {
        // astrobee is undocked and the mission starts
        api.startMission();
        // move to Point A
        moveToWrapper(11.21, -9.8, 4.79, 0, 0, -0.707, 0.707);
        getQR();
    }
    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    // You can add your method
    private void moveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w) {
        final int LOOP_MAX = 3;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float) qua_x, (float) qua_y,
                (float) qua_z, (float) qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while (!result.hasSucceeded() || loopCounter < LOOP_MAX) {
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

    private void getQR(){
        api.flashlightControlFront(0.025f);
        try {
            java.lang.Thread.sleep(2000);
        } catch(InterruptedException ex){
            java.lang.Thread.currentThread().interrupt();
        }
        Bitmap bitmap;
        bitmap = api.getBitmapNavCam();
        api.flashlightControlFront(0);
        String QRCodeString = readQR(bitmap);
        api.sendDiscoveredQR(QRCodeString);
    }

    private String readQR(Bitmap bitmap){
        String result = "error";
        double n = 0.5;
        double m = 0.5;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (int) (width * n);
        int newHeight = (int) (height * n);
        int startX = (width - newWidth) / 2;
        int startY = (height - newHeight) / 2;
        Bitmap bitmapTriming = createBitmap(bitmap, startX, startY, newWidth, newHeight);
        Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmapTriming, (int) (newWidth * m), (int) (newHeight * m), true);
        width = bitmapResize.getWidth();
        height = bitmapResize.getHeight();
        int[] pixels = new int[width * height];
        bitmapResize.getPixels(pixels, 0, width, 0, 0, width, height);
        try {
            LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new QRCodeReader();
            Map<DecodeHintType, Object> tmpHintsMap;
            tmpHintsMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
            tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            com.google.zxing.Result decodeResult = reader.decode(binaryBitmap, tmpHintsMap);
            result = decodeResult.getText();
        } catch (Exception e) {
        }
        return result;
    }
}


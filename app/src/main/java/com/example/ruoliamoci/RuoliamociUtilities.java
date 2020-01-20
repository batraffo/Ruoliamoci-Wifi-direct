package com.example.ruoliamoci;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.TimeZone;

public class RuoliamociUtilities {

    public static final int PORT_NUMBER=6969;

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.d("copyFile", e.toString());
            return false;
        }
        return true;
    }

    //now with brand new compression
    public static boolean setImage(Uri u, ImageView im, Activity activity) {
        Log.d("Utilities","i'm setting an image");
        InputStream inputStream = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//don't go in memory
        if(u!=null) {
            try {
                inputStream = activity.getContentResolver().openInputStream(u);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                u = null;
            }
        }
        if(u!=null) {
            BitmapFactory.decodeStream(inputStream, null, options);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_hero_image, options);
        String imageType = options.outMimeType;
        options.inSampleSize = calculateInSampleSize(options, 500, 500);
        options.inJustDecodeBounds = false;

        if(u!=null) {
            try {
                inputStream= activity.getContentResolver().openInputStream(u);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            im.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, options));
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            im.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_hero_image, options));

        return true;
    }

    /*
    LINK: https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void setDdEvent(Context c, long date){
        ContentResolver cr = c.getContentResolver();
        ContentValues values = new ContentValues();
        date=date+TimeZone.getDefault().getOffset(date);
        values.put(CalendarContract.Events.DTSTART, date);
        values.put(CalendarContract.Events.DTEND, date );//query don't go without it
        values.put(CalendarContract.Events.ALL_DAY, 1);
        values.put(CalendarContract.Events.TITLE, "Sessione D&D");
        values.put(CalendarContract.Events.DESCRIPTION, "Evento settato dal DM attraverso Ruoliamoci");
        values.put(CalendarContract.Events.CALENDAR_ID, RuoliamociUtilities.getCalendarID(c));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
        Toast.makeText(c,"Session set, check your calendar",Toast.LENGTH_SHORT).show();
    }

    public static long getCalendarID(Context c) {

        String projection[] = {"_id"};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        if (managedCursor.moveToFirst()){
            String calID;
            int idCol = managedCursor.getColumnIndex(projection[0]);
            calID = managedCursor.getString(idCol);
            managedCursor.close();
            return Long.parseLong(calID);
        }
        return -1;

    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }


}

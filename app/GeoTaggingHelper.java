import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class GeoTaggingHelper {
    private static final String TAG = GeoTaggingHelper.class.getSimpleName();

    private Context context;

    public GeoTaggingHelper(Context context) {
        this.context = context;
    }

    public void addGeoTag(String imagePath) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    try {
                        ExifInterface exifInterface = new ExifInterface(imagePath);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        long time = location.getTime();
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, decimalToDMS(latitude));
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, decimalToDMS(longitude));
                        if (latitude > 0) {
                            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                        } else {
                            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                        }
                        if (longitude > 0) {
                            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                        } else {
                            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                        }
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, String.valueOf(time));
                        exifInterface.saveAttributes();
                    } catch (IOException e) {
                        Log.e(TAG, "Error writing exif data: " + e.getMessage());
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied: " + e.getMessage());
        }
    }

    private String decimalToDMS(double coord) {
        coord = Math.abs(coord);
        int degrees = (int) coord;
        coord = (coord - degrees) * 60;
        int minutes = (int) coord;
        coord = (coord - minutes) * 60;
        int seconds = (int) (coord * 1000);
        return degrees + "/1," + minutes + "/1," + seconds + "/1000";
    }
}

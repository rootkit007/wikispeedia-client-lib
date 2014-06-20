package com.greatnowhere.wikispeedia.client;

import java.util.List;

import org.wikispeedia.models.Marker;
import org.wikispeedia.models.Response;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.greatnowhere.wikispeedia.client.GeoFencer.GeoFence;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import de.greenrobot.event.EventBus;

public class WikiSpeedChangeListener implements GooglePlayServicesClient.ConnectionCallbacks,  
		GooglePlayServicesClient.OnConnectionFailedListener {

	/**
	 * Max difference (degrees) between cogs (bearings) in order for marker to be used
	 */
	public static final double COG_DIFFERENCE_TOLERANCE = 30;
	
	private static final String TAG = WikiSpeedChangeListener.class.getCanonicalName();
	
	protected WikiSpeediaClient client;
	protected WikiSpeedChangedListener ls;
	protected Marker currentMarker;
	protected List<Marker> nearbyMarkers;
	protected LocationClient lC; 
	protected GeoFencer gf;
	protected Context ctx;
	protected EventBus eventBus;
	/**
	 * Radius for the current request
	 */
	protected float requestRadius = WikiSpeediaClient.PRIMARY_RADIUS_METERS;
	
	public WikiSpeedChangeListener(Context ctx, String userName) {
		client = new WikiSpeediaClient(ctx,userName);
		this.ctx = ctx;
		checkLooper();
		int gpsCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
		if ( gpsCode == ConnectionResult.SUCCESS ) {
			lC = new LocationClient(ctx, this, this);
			gf = new GeoFencer(lC);
			eventBus = EventBus.getDefault();
			eventBus.register(this);
			lC.connect();
		} else {
			throw new RuntimeException("Google Play Services not available, error " + gpsCode);
		}
	}
	
	protected void checkLooper() {
		if ( Looper.myLooper() == null )
			Looper.prepare();
	}
	
	public void stop() {
		checkLooper();
		gf.removeMarkerFences();
		gf.removeFence(gf.primaryFenceId.toString());
		client.stop();
		lC.disconnect();
		if ( eventBus.isRegistered(this) ) 
			eventBus.unregister(this);
		ls = null;
	}
	
	protected void setCurrentMarker(Marker m) {
		Log.i(TAG,"Current marker " + m.label + " kph " + m.kph + " mph " + m.mph);
		if ( ls != null && m != currentMarker ) { 
			ls.onWikiSpeedChangedListener(m);
		}
		currentMarker = m;
	}
	
	public void setWikiSpeedChangedListener(WikiSpeedChangedListener l) {
		ls = l;
	}
	
	/**
	 * Sets up geofence around current location
	 */
	private synchronized void setUpPrimaryGeofence() {
		Log.i(TAG,"Adding primary fence");
		gf.addFence(new GeoFence(GeoFencer.FenceType.PRIMARY, lC.getLastLocation(), requestRadius), 
				getPrimaryAreaIntent());
		// get marker fences in the vicinity
		client.getMarker(lC.getLastLocation(), new RequestListener<Response>() {
			public void onRequestSuccess(Response result) {
				Log.i(TAG,"Got WS result, " + ( result.markers != null ? result.markers.size() : 0 ) + " markers");
				if ( result.markers.size() > WikiSpeediaClient.MAX_GEOFENCES ) {
					requestRadius *= (1 - WikiSpeediaClient.RADIUS_ADJUSTMENT_PCT );
					setUpPrimaryGeofence();
					return;
				}
				if ( result.markers.size() < WikiSpeediaClient.MIN_GEOFENCES ) {
					requestRadius *= (1 + WikiSpeediaClient.RADIUS_ADJUSTMENT_PCT );
					setUpPrimaryGeofence();
					return;
				}
				nearbyMarkers = result.markers;
				setUpMarkerFences();
			}
			public void onRequestFailure(SpiceException spiceException) {
				Log.w(TAG,spiceException);
			}
		});
	}
	
	private synchronized void setUpMarkerFences() {
		Log.i(TAG,"Adding marker fences");
		int numFences = 0;
		gf.removeMarkerFences();
		if ( nearbyMarkers != null ) {
			for ( Marker m : nearbyMarkers ) {
				if ( m.getDeletedOnDate() == null ) { 
					gf.addFence(new GeoFence(m), getMarkerAreaIntent());
					numFences++;
				}
			}
		}
		Log.i(TAG,"Added " + numFences + " marker fences");
	}
	
	/**
	 * Primary fence exited, must set up new one
	 * @param e
	 */
	public void onEvent(Services.EventPrimaryFenceExited e) {
		Log.i(TAG, "Primary geofence exited");
		setUpPrimaryGeofence();
	}
	
	/**
	 * A marker fence has been entered
	 * We will use that marker if it has similar cog (bearing)
	 * to current bearing
	 * @param e
	 */
	public void onEvent(Services.EventMarkerFenceEntered e) {
		Log.i(TAG, "Marker " + e.fenceId + " geofence entered");
		Location currentLoc = lC.getLastLocation();
		GeoFence f = gf.getFence(e.fenceId);
		if ( f != null && f.mark != null ) {
			double bearingDiff = f.mark.compareToCog(new Double(currentLoc.getBearing()));
			if ( bearingDiff < COG_DIFFERENCE_TOLERANCE ) {
				// we have a marker
				setCurrentMarker(f.mark);
			}
		}
	}
	
	
	private PendingIntent getPrimaryAreaIntent() {
		return PendingIntent.getService(ctx, 0, new Intent(ctx, Services.PrimaryAreaExitService.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private PendingIntent getMarkerAreaIntent() {
		return PendingIntent.getService(ctx, 0, new Intent(ctx, Services.MarkerAreaEnterService.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "Google Play Services connection failed " + result.getErrorCode());
	}

	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Google Play Services connected");
		setUpPrimaryGeofence();
	}

	public void onDisconnected() {
		Log.i(TAG, "Google Play Services disconnected");
	}

	public interface WikiSpeedChangedListener {
		public void onWikiSpeedChangedListener(Marker m);
	}

}

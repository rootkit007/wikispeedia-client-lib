package com.greatnowhere.wikispeedia.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.wikispeedia.models.Marker;

import android.app.PendingIntent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class GeoFencer {

	protected Map<String, GeoFence> fences = new HashMap<String, GeoFence>();
	protected LocationClient lc;
	public final UUID primaryFenceId = UUID.randomUUID();
	
	public GeoFencer(LocationClient lc) {
		this.lc = lc;
	}
	
	public void addFence(GeoFence f, PendingIntent i) {
		if ( f.fenceType == FenceType.PRIMARY ) {
			// there can only be one!
			f.id = primaryFenceId;
		}
		fences.put(f.getId(), f);
		lc.addGeofences(obj2List(f.getFence()), i, new LocationClient.OnAddGeofencesResultListener() {
			public void onAddGeofencesResult(int statusCode,
					String[] geofenceRequestIds) {
			}
			
		});
	}
	
	public GeoFence getFence(String id) {
		return fences.get(id);
	}
	
	public void removeMarkerFences() {
		for (String fenceId : fences.keySet() ) {
			if ( fences.get(fenceId).fenceType == FenceType.MARKER ) {
				removeFence(fenceId);
			}
		}
	}
	
	public void removeFence(String fenceId) {
		if ( fences.containsKey(fenceId) ) {
			removeFence(fences.get(fenceId));
		}
	}
	
	public void removeFence(GeoFence f) {
		final CountDownLatch l =  new CountDownLatch(1);
		lc.removeGeofences(obj2List(f.getId()), new LocationClient.OnRemoveGeofencesResultListener() {
			public void onRemoveGeofencesByRequestIdsResult(int statusCode,
					String[] geofenceRequestIds) {
					l.countDown();
			}
			
			public void onRemoveGeofencesByPendingIntentResult(int statusCode,
					PendingIntent pendingIntent) {
			}
		});
		fences.remove(f.getId());
		try {
			l.await();
		} catch (InterruptedException e) {
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> obj2List(T f) {
		return (List<T>) Arrays.asList(new Object[] {f});
	}
	
	protected static class GeoFence {

		protected UUID id = UUID.randomUUID();
		protected Location center;
		protected float radius;
		protected FenceType fenceType;
		protected Marker mark;

		protected GeoFence(Marker m) {
			this(FenceType.MARKER, getLocation(m), (float) WikiSpeediaClient.MARKER_RADIUS_METERS);
			mark = m;
		}
		
		private static Location getLocation(Marker m) {
			Location l = new Location("flp");
			l.setLatitude(m.lat);
			l.setLongitude(m.lng);
			return l;
		}
		
		protected GeoFence(FenceType type,Location c,float r) {
			fenceType = type;
			center = c;
			radius = r;
		}
		
		public String getId() {
			return id.toString();
		}
		
		public Geofence getFence() {
			return new Geofence.Builder().setCircularRegion(center.getLatitude(), center.getLongitude(), radius).
					setExpirationDuration(Geofence.NEVER_EXPIRE).
					setNotificationResponsiveness(1).
					setRequestId(id.toString()).
					setTransitionTypes(
							( fenceType == FenceType.PRIMARY ? Geofence.GEOFENCE_TRANSITION_EXIT : Geofence.GEOFENCE_TRANSITION_ENTER )
					).build();
		}
		
	}
	
	protected enum FenceType {
		PRIMARY, MARKER;
	}
	
}

package com.greatnowhere.wikispeedia.client;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import de.greenrobot.event.EventBus;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class Services {
	
	/**
	 * Helper method to return triggering fences, or null if error 
	 * @param i
	 * @param tag
	 * @return
	 */
	private static List<Geofence> getTriggeringFences(Intent i, String tag) {
		if ( LocationClient.hasError(i) ) {
			int errorCode = LocationClient.getErrorCode(i);
			Log.e(tag, "LocationClient error " + errorCode);
		} else {
			// Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(i);				
            if ( (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                     ||
                    (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
                ) {
                    List<Geofence> triggerList = LocationClient.getTriggeringGeofences(i);
                    return triggerList;
            }
		}
		return null;
	}

	/**
	 * Triggered when primary geofence is exited
	 * @author pzeltins
	 *
	 */
	public static class PrimaryAreaExitService extends IntentService {
		
		private static String TAG = PrimaryAreaExitService.class.getCanonicalName();
		private EventBus eventBus;
		
		public PrimaryAreaExitService() {
			super(PrimaryAreaExitService.class.getCanonicalName());
			eventBus = EventBus.getDefault();
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			List<Geofence> fences = getTriggeringFences(intent, TAG);
			if ( fences != null ) {
				for ( Geofence f : fences ) {
					eventBus.post(new EventPrimaryFenceExited(f.getRequestId()));
				}
			}
		}
		
	}
	
	/**
	 * Triggered when any of marker fences are entered
	 * @author pzeltins
	 *
	 */
	public static class MarkerAreaEnterService extends IntentService {

		private static String TAG = MarkerAreaEnterService.class.getCanonicalName();
		private EventBus eventBus;
		
		public MarkerAreaEnterService() {
			super(MarkerAreaEnterService.class.getCanonicalName());
			eventBus = EventBus.getDefault();
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			List<Geofence> fences = getTriggeringFences(intent, TAG);
			if ( fences != null ) {
				for ( Geofence f : fences ) {
					eventBus.post(new EventMarkerFenceEntered(f.getRequestId()));
				}
			}
		}
		
	}
	
	public static class EventPrimaryFenceExited {
		
		public String fenceId;
		
		public EventPrimaryFenceExited(String id) {
			fenceId = id;
		}
		
	}
	
	public static class EventMarkerFenceEntered {

		public String fenceId;
		
		public EventMarkerFenceEntered(String id) {
			fenceId = id;
		}
		
	}
	
}

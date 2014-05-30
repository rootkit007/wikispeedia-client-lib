package com.greatnowhere.wikispeedia.client;

import java.net.URI;

import org.wikispeedia.models.Response;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.greatnowhere.gisutils.GISUtils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.XmlSpringAndroidSpiceService;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class WikiSpeediaClient {
	
	private static final String TAG = WikiSpeediaClient.class.getCanonicalName();

	private String serviceURL;
	private SpiceManager sm = new SpiceManager(XmlSpringAndroidSpiceService.class);
	private Context ctx;
	/**
	 * Default radius for wikispeedia requests, meters
	 */
	public static final double PRIMARY_RADIUS_METERS = 1000D; // 1 km
	/**
	 * Marker geofence radius, meters
	 */
	public static final double MARKER_RADIUS_METERS = 50D; // 50 m
	public static final String DEFAULT_SERVICE_URL = "http://www.wikispeedia.org/a/marks_bb2.php?name=all";

	public WikiSpeediaClient(Context ctx) {
		this.ctx = ctx;
		sm.start(ctx);
		serviceURL = DEFAULT_SERVICE_URL;
	}
	
	public WikiSpeediaClient(String serviceURL, Context ctx) {
		this(ctx);
		this.serviceURL = serviceURL;
	}
	
	protected Context getContext() {
		return ctx;
	}
	
	public void stop() {
		sm.shouldStop();
	}
	
	public void getMarker(Location loc, RequestListener<Response> requestListener) {
		sm.execute(new WikiSpeediaRequest(loc, PRIMARY_RADIUS_METERS), requestListener);
	}
	
	private class WikiSpeediaRequest extends SpringAndroidSpiceRequest<Response> {

		private String requestUrl;
		
		public WikiSpeediaRequest(Location loc, double boxRadius) {
			super(Response.class);
			double[] bBox = GISUtils.getBoundingBoxCoords(loc, boxRadius);
			requestUrl = serviceURL + "&" + String.format("swlng=%f&swlat=%f&nelng=%f&nelat=%f", bBox[0], bBox[1], bBox[2], bBox[3]);
			Log.i(TAG, requestUrl);
		}

		@Override
		public Response loadDataFromNetwork() throws Exception {
			URI uri = new URI(requestUrl);
			return getRestTemplate().getForObject(uri, Response.class);
		}
		
	}
	
}

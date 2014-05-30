wikispeedia-client-lib
======================

Android client for Wikispeedia

Proof-of-concept Android Java client for wikispeedia.org

Usage:

WikiSpeedChangeListener listener = new WikiSpeedChangeListener();
listener.setWikiSpeedChangedListener(new WikiSpeedChangedListener() {
  		public void onWikiSpeedChangedListener(Marker m) {
  		  // got wikispeedia marker
  		}
  	});

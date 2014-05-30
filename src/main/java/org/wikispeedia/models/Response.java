package org.wikispeedia.models;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="markers",strict=false)
public class Response {

	@ElementList(entry="marker",inline=true)
	public List<Marker> markers;
	
}

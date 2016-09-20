package cn.com.deepdata.streamflume;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.http.HTTPBadRequestException;
import org.apache.flume.source.http.HTTPSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadarSourceHandler implements HTTPSourceHandler {

	long totalSize = 0;
	long count = 0;
	private static final Logger LOG = LoggerFactory
			.getLogger(RadarSourceHandler.class);

	public void configure(Context context) {
		// TODO Auto-generated method stub

	}

	public List<Event> getEvents(HttpServletRequest request)
			throws HTTPBadRequestException, Exception {
		// TODO Auto-generated method stub
		BufferedInputStream bis = new BufferedInputStream(
				request.getInputStream());

		int byteMaxLen = 2048;
		byte[] bytes = new byte[byteMaxLen];
		int byteLen = 0;
		int readLen = 0;
		while ((readLen = bis.read(bytes, byteLen, byteMaxLen - byteLen)) >= 0) {
			byteLen += readLen;
			if (byteLen == byteMaxLen) {
				byte[] newBytes = new byte[byteMaxLen * 2];
				for (int i = 0; i < byteMaxLen; i++)
					newBytes[i] = bytes[i];
				byteMaxLen *= 2;
				bytes = newBytes;
			}
		}
		totalSize += byteLen;
		count++;
		ArrayList<Event> ret = new ArrayList<Event>(0);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("bodySize", Integer.toString(byteLen));
		ret.add(EventBuilder.withBody(bytes, header));
		return ret;
	}

}

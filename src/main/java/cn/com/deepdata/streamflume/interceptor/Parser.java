package cn.com.deepdata.streamflume.interceptor;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.deepdata.streamflume.util.ZipUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

class Parser implements Interceptor {

    public static class HYEvent {
        public HashMap<String, Object> headers;
        public String body;

        public byte[] getBody() {
            // if (body == null)
            // Log.info(headers.toString());
            if (body == null || body.length() == 0)
                return "Null Body".getBytes();
            return body.getBytes();
        }
    }

    public static class ErrorEvent {

        static public Event Create(String info, String content) {
            Map<String, String> errorHeader = new HashMap<String, String>(0);
            errorHeader.put("scc_errorInfo", info);
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            if (stacks.length > 2) {
                StackTraceElement stack = stacks[2];
                errorHeader.put("scc_errorPos", stack.getClassName()
                        + " (line " + stack.getLineNumber() + ")");
            }
            errorHeader.put("action", "addLogError");
            return EventBuilder.withBody(content.getBytes(), errorHeader);
        }
    }

    double contentcompressrate = 0;
    long contentcompresscount = 0;
    double listcompressrate = 0;
    long listcompresscount = 0;
    int recvedEvent = 0;

    private final Type listType = new TypeToken<ArrayList<HYEvent>>() {
    }.getType();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    ;
    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    public void close() {
        // TODO Auto-generated method stub

    }

    public void initialize() {
        // TODO Auto-generated method stub

    }

    public int byteArrayToInt(byte[] bRefArr, int off, int len) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = off; i < off + len; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    private List<Event> getSimpleEvents(List<HYEvent> events) {
        List<Event> newEvents = new ArrayList<Event>(events.size());
        for (HYEvent e : events) {
            HashMap<String, Object> headers = e.headers;
            if (headers.containsKey("action") == false
                    || headers.get("action") == null) {
                LOG.error("no action in event: {}", new Gson().toJson(headers));
                continue;
            }
            try {
                byte[] body = e.getBody();
                String strBody = new String(body, "UTF-8");
                if (!strBody.equals("Null Body"))
                    headers.put("scc_content", strBody);
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                // e1.printStackTrace();
                LOG.error("UnsupportedEncodingException:{}", e1);
            }
            byte[] newBody = new Gson().toJson(headers).getBytes();
            if (newBody.length > 1000000) {
                LOG.error("Message is too large. length: " + newBody.length);
                LOG.error("Message title: " + headers.get("scc_title"));
            } else {
                newEvents.add(EventBuilder.withBody(newBody, new HashMap<String, String>(0)));
            }
        }
        return newEvents;
    }

    public List<Event> interceptList(Event event) {
        // TODO Auto-generated method stub
        byte[] bytes = event.getBody();
        int byteLen = Integer.parseInt(event.getHeaders().get("bodySize"));
        if (byteLen == 0)
            return null;

        ArrayList<HYEvent> eventList = new ArrayList<HYEvent>(0);
        String json = "";
        try {
            int version = byteArrayToInt(bytes, 0, 4);
            double compressrate = 1;
            if (version == 1) {
                byte[] uncompress = null;
                int compType = byteArrayToInt(bytes, 4, 2);
                uncompress = new byte[byteLen - 6];
                System.arraycopy(bytes, 6, uncompress, 0, byteLen - 6);
                if (compType == 3) {// bzip2
                    uncompress = ZipUtil.bunzip2(uncompress);
                    double ratio = (byteLen - 6) * 1.0 / uncompress.length;
                    compressrate = ratio;
                } else if (compType == 2) {// zip
                    uncompress = ZipUtil.unzip(uncompress);
                    double ratio = (byteLen - 6) * 1.0 / uncompress.length;
                    compressrate = ratio;
                }
                json = new String(uncompress, 0, uncompress.length, "UTF-8");
            } else if (version == 0x32) {
                byte[] uncompress = new byte[byteLen - 4];
                System.arraycopy(bytes, 4, uncompress, 0, byteLen - 4);
                json = new String(uncompress, 0, uncompress.length, "UTF-8");
            }
            eventList = gson.fromJson(json, listType);
            if (eventList == null || eventList.size() == 0)
                return new ArrayList<Event>(0);

            HYEvent hyEvent = eventList.get(0);
            if (hyEvent.headers.containsKey("links")) {
                listcompressrate += compressrate;
                listcompresscount++;
            } else {
                contentcompressrate += compressrate;
                contentcompresscount++;
            }
        } catch (JsonSyntaxException ex) {
            LOG.error("JsonSyntaxException");
            LOG.error("json:{}", json);
            LOG.error("ex:{}", ex);
            List<Event> ret = new ArrayList<Event>(0);
            ret.add(ErrorEvent.Create(ex.toString(), json));
            return ret;
        } catch (UnsupportedEncodingException ex) {
            // TODO Auto-generated catch block
            LOG.error("UnsupportedEncodingException");
            LOG.error("json:{}", json);
            LOG.error("ex:{}", ex);
            List<Event> ret = new ArrayList<Event>(0);
            ret.add(ErrorEvent.Create(ex.toString(), json));
            return ret;
        }
        return getSimpleEvents(eventList);
    }

    public Event intercept(Event arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Event> intercept(List<Event> events) {
        // TODO Auto-generated method stub
        List<Event> newEvents = new ArrayList<Event>();
        for (Event event : events) {
            List<Event> eventsList = interceptList(event);
            if (eventsList != null && eventsList.size() > 0) {
                newEvents.addAll(eventsList);
                recvedEvent += eventsList.size();
            }
        }
        return newEvents;
    }

    public static class Builder implements Interceptor.Builder {

        public Interceptor build() {
            return new Parser();
        }

        public void configure(Context arg0) {
            // TODO Auto-generated method stub

        }

    }

}

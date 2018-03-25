package edu.niu.csci.z1697841.assignment4;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 3/24/2018.
 */

public class MyXmlParser {
    private  static final String ns = null;

    public static class Item {
        public final String description;
        public final String link;

        public Item(String description, String link) {
            this.description = description;
            this.link = link;
        }
    }

    public static class Channel {
        public final List<Item> items;

        public Channel(List<Item> items) {
            this.items = items;
        }
    }

    public Channel parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private Channel readFeed(XmlPullParser parser) throws XmlPullParserException, IOException{
        Channel channel = null;

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("channel")) {
                channel = readChannel(parser);
            } else {
                skip(parser);
            }
        }
        return channel;
    }

    private Channel readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Item> items = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return new Channel(items);
    }

    private Item readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String description = null;
        String link = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("description")) {
                description = readText(parser);
            } else if (name.equals("link")) {
                link = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new Item(description, link);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
        int depth = 1;

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = null;

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}

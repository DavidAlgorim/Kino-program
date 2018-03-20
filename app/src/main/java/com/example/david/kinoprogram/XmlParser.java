package com.example.david.kinoprogram;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Work on 20.03.2018.
 */

public class XmlParser {
    private static final String ns = null;
    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
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
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();
        parser.require(XmlPullParser.START_TAG, ns, "rdf:RDF");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("kult:Institution")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    public static class Entry {
        public final String email;
        public final String phone;

        private Entry(String email,String phone) {
            this.email = email;
            this.phone=phone;
        }
    }
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "kult:Institution");
        String email = null;
        String phone = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("kult:email")) {
                email = readTitle(parser);
            } else if (name.equals("kult:phone")) {
                phone = readPhone(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(email,phone);
    }
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "kult:email");
        String email = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "kult:email");
        return email;
    }
    private String readPhone(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "kult:phone");
        String phone = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "kult:phone");
        return phone;
    }
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        if (tag.equals("link")) {
            link = parser.getAttributeValue(null, "url");
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
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
}

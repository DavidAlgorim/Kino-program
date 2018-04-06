package com.example.david.kinoprogram;

import android.os.Parcel;
import android.os.Parcelable;
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
    private int order = 1;
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
            if (name.equals("item")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    public static class Entry implements Parcelable{
        public final int id;
        public final String title;
        public final String description;
        public final String link;
        public final String startDate;
        public final String endDate;
        public final String location;
        public final String organizer;

        private Entry(int id, String title,String description, String link,
                      String startDate, String endDate, String location, String organizer) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.link = link;
            this.startDate = startDate;
            this.endDate = endDate;
            this.location = location;
            this.organizer = organizer;
        }

        protected Entry(Parcel in) {
            id = in.readInt();
            title = in.readString();
            description = in.readString();
            link = in.readString();
            startDate = in.readString();
            endDate = in.readString();
            location = in.readString();
            organizer = in.readString();
        }

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel in) {
                return new Entry(in);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        public int getId() { return id; }

        public String getTitle() { return title; }

        public String getDescription() {
            return description;
        }

        public String getLink() {
            return link;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getLocation() {
            return location;
        }

        public String getOrganizer() {
            return organizer;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeString(description);
            dest.writeString(link);
            dest.writeString(startDate);
            dest.writeString(endDate);
            dest.writeString(location);
            dest.writeString(organizer);
        }
    }

    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        int id = order;
        String title = null;
        String description = null;
        String link = null;
        String startDate = null;
        String endDate = null;
        String location = null;
        String organizer = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
                order++;
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else if (name.equals("ev:startdate")) {
                startDate = readStart(parser);
            } else if (name.equals("ev:enddate ")) {
                endDate = readEnd(parser);
            } else if (name.equals("ev:location ")) {
                location = readLocation(parser);
            } else if (name.equals("ev:organizer ")) {
                organizer = readOrganizer(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(id, title,description, link,
                startDate, endDate, location,organizer);
    }
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }
    private String readStart(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "ev:startdate");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "ev:startdate");
        return startDate;
    }
    private String readEnd(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "ev:enddate");
        String endDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "ev:enddate");
        return endDate;
    }
    private String readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "ev:location");
        String location = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "ev:location");
        return location;
    }
    private String readOrganizer(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "ev:organizer");
        String organizer = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "ev:organizer");
        return organizer;
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

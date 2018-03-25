package edu.niu.csci.z1697841.assignment4;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String URL = "https://rss.itunes.apple.com/api/v1/us/movies/top-movies/all/100/explicit.rss";
    private static boolean wifiConnected = false;
    private static boolean mobileConnected = false;
    public static boolean refreshDisplay = true;
    public static String sPref = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPage();
    }

    public void loadPage() {
        new MyXmlTask().execute(URL);
    }

    private class MyXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_main);
            WebView view = findViewById(R.id.webview);
            view.loadData(result, "text/html", null);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXml(urls[0]);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return getResources().getString(R.string.xml_error);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            }
        }

        private String loadXml(String url) throws XmlPullParserException, IOException{
            InputStream stream = null;
            MyXmlParser parser = new MyXmlParser();
            List<MyXmlParser.Item> items = null;
            String description = null;
            String link = null;
            Calendar rightNow = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("MMM dd h:mmaa");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            boolean pref = preferences.getBoolean("summaryPref", false);

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<div style='text-align: center;'>").append("<h3>" + getResources().getString(R.string.page_title) + "</h3>")
                .append("<em>Updated: " + format.format(rightNow.getTime()) + "</em>");

            try {
                stream = downloadUrl(url);
                items = parser.parse(stream).items;
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            for (MyXmlParser.Item item : items) {
                htmlBuilder.append("<p><a href='").append(item.link).append("'>").append(item.description).append("</a></p>");
            }
            return htmlBuilder.toString();
        }

        private InputStream downloadUrl(String urlString) throws IOException {
            java.net.URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            return connection.getInputStream();
        }
    }
}

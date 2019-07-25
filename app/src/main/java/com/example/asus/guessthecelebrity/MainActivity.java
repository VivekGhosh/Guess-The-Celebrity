package com.example.asus.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
{
    ArrayList<String> celebUrls = new ArrayList();
    ArrayList<String> celebNames = new ArrayList<>();
    String[] answers = new String[4];

    int locationOfCorrectAnswer = 0;
    int chosenCeleb = 0;

    Button button1;
    Button button2;
    Button button3;
    Button button4;

    ImageView imageView;

    public void celebChosen(View view)
    {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer)))
        {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                            "Wrong! That was " + answers[locationOfCorrectAnswer],
                                Toast.LENGTH_SHORT).show();
        }

        createNewQuestion();
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls)
        {
            try
            {

                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream in = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                Bitmap myImage = BitmapFactory.decodeStream(in);

                return myImage;

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            String result = "";

            try
            {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream in = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1)
                {
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }

                return result;

            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void createNewQuestion()
    {
        Random random = new Random();

        chosenCeleb = random.nextInt(celebUrls.size());

        ImageDownloader imageDownloaderTask = new ImageDownloader();

        Bitmap celebImage = null;

        try
        {
            celebImage = imageDownloaderTask.execute(celebUrls.get(chosenCeleb)).get();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        imageView.setImageBitmap(celebImage);

        locationOfCorrectAnswer = random.nextInt(4);

        int incorrectAnswerLocation;

        for(int i=0;i<4;i++)
        {
            if(i == locationOfCorrectAnswer)
            {
                answers[i] = celebNames.get(chosenCeleb);
            }
            else
            {
                incorrectAnswerLocation = random.nextInt(celebUrls.size());

                while(incorrectAnswerLocation == chosenCeleb)
                {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());
                }

                answers[i] = celebNames.get(incorrectAnswerLocation);
            }
        }

        button1.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        imageView = findViewById(R.id.imageView);

        DownloadTask task = new DownloadTask();

        String result = "";

        try
        {
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitString = result.split("<div class=\"sidebarContainer\">");

            Pattern P = Pattern.compile("<img src=\"(.*?)\"");
            Matcher M = P.matcher(splitString[0]);

            while(M.find())
            {
                celebUrls.add(M.group(1));
            }

            P = Pattern.compile("alt=\"(.*?)\"");
            M = P.matcher(splitString[0]);

            while(M.find())
            {
                celebNames.add(M.group(1));
            }

            createNewQuestion();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

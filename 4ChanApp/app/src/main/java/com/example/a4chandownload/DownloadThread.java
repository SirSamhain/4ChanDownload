package com.example.a4chandownload;

import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {

    int pos;
    String board;
    String path;
    private volatile int counter = 0;

    public DownloadThread(int pos, String board, String path){
        this.pos = pos;
        this.board = board;
        this.path = path;
    }

    public void startDownloading() throws IOException{
        String path = "https://a.4cdn.org" + board + "/" + Integer.toString(pos) + ".json";
        getThreads(path, board);
    }

    @Override
    public void run(){
        while(!Thread.interrupted()) {
            try {
                startDownloading();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //passes a thread to readposts
    private void getThreads(String url, String board) throws IOException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Threads threads = gson.fromJson(getJson(url), Threads.class);
        for (ThreadN thread : threads.getThreads()) {
            String postNo = Long.toString(thread.getPosts().get(0).getNo());
            String postUrl = "https://a.4cdn.org" + board + "/thread/" + postNo + ".json";
            readPosts(postUrl, board);
        }

    }

    //reads the posts in a given thread
    private void readPosts(String url, String board) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ThreadN posts = gson.fromJson(getJson(url), ThreadN.class);
        String postNum = Long.toString(posts.getPosts().get(0).getNo());
        int count = 0;
        for(Posts post : posts.getPosts()){
            if(post.getFilename() != null){
                retrieveImage(Long.toString(post.getTim()),post.getFilename() , post.getExt(), board, post.getFsize(), postNum, Integer.toString(count));
                count++;
            }
        }
    }

    private void retrieveImage(String tim, String fileName, String ext, String board, int size, String postNum, String pos) throws IOException {
        String bo = path + "/4Downloads/"+board.split("/")[1];
        File postDir = new File(bo+"/"+postNum);
        fileName = fileName.replaceAll("\\s", "-");
        String name = postDir + "/"+ pos + "_" + board.split("/")[1] +"_"+ postNum + "_" + fileName+ext;
        URL url = new URL("https://i.4cdn.org" + board + '/' + tim + ext);
        if(checkDirs(bo, postDir, name)){
            return;
        }

        InputStream inputStream = url.openStream();
        OutputStream outputStream = new FileOutputStream(name);

        byte[] b = new byte[size];
        int length;
        while((length = inputStream.read(b)) != -1){
            outputStream.write(b, 0, length);
        }
        inputStream.close();
        outputStream.close();
        counter++;
    }

    private boolean checkDirs(String bo, File postDir, String name){
        File down = new File(path + "/4Downloads");
        File test = new File(path);

//        System.out.println("======================"+test.isDirectory()+"======================\n======================" + path);

        if(!down.isDirectory()){
            boolean made = down.mkdir();
//            System.out.println("made Dir: " + down + "  MADE: " + made);
        }
        File dir = new File(bo);
        if(!dir.isDirectory()){
            boolean made = dir.mkdir();
//            System.out.println("made Dir: " + dir + "  MADE: " + made);
        }
        if(!postDir.isDirectory()){
            boolean made = postDir.mkdir();
//            System.out.println("made Dir: " + postDir + "  MADE: " + made);
        }

        File check = new File(name);
        if(check.exists()){
            return true;
        }
        return false;
    }

    private String getJson(String path) throws IOException {
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        HttpURLConnection connection;
        URL url = new URL(path);
        connection = (HttpURLConnection) url.openConnection();

        //request setup
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        //Response
        int status = connection.getResponseCode();
        if(status > 229){
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            while((line = reader.readLine()) != null){
                responseContent.append(line);
            }
            reader.close();
        }else{
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null){
                responseContent.append(line);
            }
            reader.close();
        }
        //System.out.println(responseContent.toString());
        return responseContent.toString();
    }

    public int getCount(){return counter;}





}

package com.sith.pingclientgui;

public class Transcript {
    private String thumb_url; //Optional
    private String content;
    private String username; //Optional
    private String ref_id;

    public Transcript() {

    }

    public Transcript(String content, String username, String thumb_url) {
        this.content = content;
        this.username = username;
        this.thumb_url = thumb_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getRef_ID() {
        return ref_id;
    }



    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRef_ID(String ref_id) {
        this.ref_id = ref_id;
    }
}

package com.client.talkster.classes;

import okhttp3.MediaType;

public class FileContent {
    private byte[] content;
    private String filename;
    private MediaType type;

    public void setContent(byte[] content) {this.content = content;}
    public byte[] getContent() {return content;}
    public void setType(MediaType type) {this.type = type;}
    public MediaType getType() {return type;}
    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}
}

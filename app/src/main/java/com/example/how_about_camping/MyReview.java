package com.example.how_about_camping;

public class MyReview {
    //각 리뷰들을 담을 리스트아이템 Layout 만들기
    private String uri;
    private String spot;
    private String review;
    private String time;

    public MyReview() {    }

    public MyReview(String uri, String spot, String review, String time) {
        this.uri = uri;
        this.spot = spot;
        this.review = review;
        this.time = time;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
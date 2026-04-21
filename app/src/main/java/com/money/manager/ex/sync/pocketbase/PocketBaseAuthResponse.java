package com.money.manager.ex.sync.pocketbase;

public class PocketBaseAuthResponse {
    public String token;
    public Record record;

    public static class Record {
        public String id;
        public String email;
    }
}

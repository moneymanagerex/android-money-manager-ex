package com.money.manager.ex.sync.pocketbase;

public class PocketBaseAuthRequest {
    public String identity;
    public String password;

    public PocketBaseAuthRequest(String identity, String password) {
        this.identity = identity;
        this.password = password;
    }
}

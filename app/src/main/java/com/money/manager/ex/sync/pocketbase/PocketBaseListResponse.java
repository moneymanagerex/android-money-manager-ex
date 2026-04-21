package com.money.manager.ex.sync.pocketbase;

import java.util.List;
import java.util.Map;

public class PocketBaseListResponse {
    public int page;
    public int perPage;
    public int totalItems;
    public int totalPages;
    public List<Map<String, Object>> items;
}

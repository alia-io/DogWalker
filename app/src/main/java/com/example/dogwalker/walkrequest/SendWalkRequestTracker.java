package com.example.dogwalker.walkrequest;

import java.util.Map;

public interface SendWalkRequestTracker {
    void setIsTargetWalker(String targetUserId, String targetUserName, boolean isTargetWalker);
    void setWalkRequest(String targetUserId, String targetUserName, boolean isTargetWalker, Map<String, String> dogs,
                        long walkTime, float payment, String currency, String message);
}

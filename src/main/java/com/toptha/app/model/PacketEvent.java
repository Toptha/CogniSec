package com.toptha.app.model;

import java.time.LocalDateTime;

public record PacketEvent(
        String srcIp,
        String dstIp,
        int srcPort,
        int dstPort,
        String protocol,
        int size,
        LocalDateTime timestamp) {
}

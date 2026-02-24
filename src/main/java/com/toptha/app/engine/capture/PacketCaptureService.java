package com.toptha.app.engine.capture;

import com.toptha.app.model.PacketEvent;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class PacketCaptureService implements Runnable {

    private final Consumer<PacketEvent> packetListener;
    private volatile boolean running = true;
    private PcapHandle handle;

    public PacketCaptureService(Consumer<PacketEvent> packetListener) {
        this.packetListener = packetListener;
    }

    @Override
    public void run() {
        try {
            PcapNetworkInterface nif = null;
            for (PcapNetworkInterface itf : Pcaps.findAllDevs()) {
                if (!itf.isLoopBack() && itf.isUp() && !itf.getAddresses().isEmpty()) {
                    String desc = itf.getDescription() != null ? itf.getDescription().toLowerCase() : "";
                    if (!desc.contains("bluetooth") && !desc.contains("virtual") && !desc.contains("vmware")) {
                        nif = itf;
                        break;
                    }
                }
            }
            if (nif == null) {
                for (PcapNetworkInterface itf : Pcaps.findAllDevs()) {
                    if (!itf.isLoopBack() && itf.isUp() && !itf.getAddresses().isEmpty()) {
                        nif = itf;
                        break;
                    }
                }
            }

            if (nif == null) {
                System.err.println("No suitable network interface found for capture.");
                return;
            }

            System.out.println("Starting capture on " + nif.getName() + " - " + nif.getDescription());

            int snapshotLength = 65536; // in bytes
            int readTimeout = 50; // in milliseconds
            handle = nif.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS, readTimeout);

            PacketListener listener = packet -> {
                if (packet.contains(IpV4Packet.class)) {
                    IpV4Packet ipv4 = packet.get(IpV4Packet.class);
                    String srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
                    String dstIp = ipv4.getHeader().getDstAddr().getHostAddress();
                    int payloadSize = packet.length();
                    String protocol = "UNKNOWN";
                    int srcPort = 0;
                    int dstPort = 0;

                    if (packet.contains(TcpPacket.class)) {
                        TcpPacket tcp = packet.get(TcpPacket.class);
                        protocol = "TCP";
                        srcPort = tcp.getHeader().getSrcPort().valueAsInt();
                        dstPort = tcp.getHeader().getDstPort().valueAsInt();
                    } else if (packet.contains(UdpPacket.class)) {
                        UdpPacket udp = packet.get(UdpPacket.class);
                        protocol = "UDP";
                        srcPort = udp.getHeader().getSrcPort().valueAsInt();
                        dstPort = udp.getHeader().getDstPort().valueAsInt();
                    }

                    if (!protocol.equals("UNKNOWN")) {
                        PacketEvent event = new PacketEvent(srcIp, dstIp, srcPort, dstPort, protocol, payloadSize,
                                LocalDateTime.now());
                        packetListener.accept(event);
                    }
                }
            };

            while (running) {
                try {
                    handle.dispatch(10, listener);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // Ignore transient dispatch errors
                }
            }
        } catch (PcapNativeException e) {
            System.err.println("Pcap4j error: " + e.getMessage());
        } finally {
            if (handle != null && handle.isOpen()) {
                handle.close();
            }
        }
    }

    public void stop() {
        running = false;
        if (handle != null) {
            try {
                handle.breakLoop();
            } catch (Exception ignore) {
            }
        }
    }
}

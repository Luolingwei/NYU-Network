package edu.nyu.cs.sdn.apps.sps;

import net.floodlightcontroller.routing.Link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NetGraph {

    Map<Long, Map<Long, LDPair>> table;

    public static class LDPair {
        public int dist;
        public Link link;

        LDPair(Link link, int dist) {
            this.link = link;
            this.dist = dist;
        }

        @Override
        public String toString() {
            return "LDPair[link:" + link + ", distance:" + dist + "]\n";
        }
    }

    NetGraph(Collection<Long> switchIds) {
        initTable(switchIds);
    }

    public void addSwitch(Long switchId) {
        if (!table.containsKey(switchId)) table.put(switchId, new HashMap<Long, LDPair>());
    }

    public void dropSwitch(Long switchId) {
        table.remove(switchId);
        for (Map<Long, LDPair> map : table.values()) map.remove(switchId);
    }

    private void initTable(Collection<Long> switchIds) {
        table = new HashMap<Long, Map<Long, LDPair>>();
        for (long switchId : switchIds) table.put(switchId, new HashMap<Long, LDPair>());
    }

    public void reCalculateTable(Collection<Link> links) {
        initTable(new ArrayList<Long>(table.keySet()));
        updateTable(links);
    }

    public void updateTable(Collection<Link> links) {
        for (Link link : links) {
            addSwitch(link.getSrc());
            addSwitch(link.getDst());
            table.get(link.getSrc()).put(link.getDst(), new LDPair(link, 1));
            table.get(link.getDst()).put(link.getSrc(), new LDPair(link, 1));
        }

        while (true) {
            boolean update_success = false;
            for (Link link : links) {
                for (long dst : table.keySet()) {
                    if (dst == link.getSrc() || dst == link.getDst()) continue;
                    LDPair leftLDP = table.get(link.getSrc()).get(dst), rightLDP = table.get(link.getDst()).get(dst);
                    if (leftLDP == null && rightLDP == null) continue;
                    if (leftLDP == null) {
                        table.get(link.getSrc()).put(dst, new LDPair(link, rightLDP.dist + 1));
                    } else if (rightLDP == null) {
                        table.get(link.getDst()).put(dst, new LDPair(link, leftLDP.dist + 1));
                    } else if (leftLDP.dist > rightLDP.dist+1) {
                        leftLDP.link = link;
                        leftLDP.dist = rightLDP.dist+1;
                    } else if (rightLDP.dist > leftLDP.dist+1) {
                        rightLDP.link = link;
                        rightLDP.dist = leftLDP.dist+1;
                    } else {
                        continue;
                    }
                    update_success = true;
                }
            }
            if (!update_success) break;
        }
    }
}

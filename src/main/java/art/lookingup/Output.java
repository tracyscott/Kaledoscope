package art.lookingup;

import art.lookingup.ui.UIPixliteConfig;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.ArtSyncDatagram;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles output from our 'colors' buffer to our DMX lights.  Currently using E1.31.
 */
public class Output {
  private static final Logger logger = Logger.getLogger(Output.class.getName());

  public static final int MAX_OUTPUTS = 32;  // 32 outputs in expanded mode.

  public static List<List<Integer>> outputs = new ArrayList<List<Integer>>(MAX_OUTPUTS);

  public static void configurePixliteOutput(LX lx) {
    List<ArtNetDatagram> datagrams = new ArrayList<ArtNetDatagram>();
    List<Integer> countsPerOutput = new ArrayList<Integer>();
    // For each output, track the number of points per panel type so we can log the details to help
    // with output verification.

    String artNetIpAddress = KaledoscopeApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_IP).getString();
    int artNetIpPort = Integer.parseInt(KaledoscopeApp.pixliteConfig.getStringParameter(UIPixliteConfig.PIXLITE_1_PORT).getString());
    logger.log(Level.INFO, "Using ArtNet: " + artNetIpAddress + ":" + artNetIpPort);

    // For each non-empty mapping output parameter, collect all points in wire order from each strand listed.  One
    // output can have multiple strands.
    // Distribute all points across the necessary number of 170-led sized universes.
    int curUniverseNum = 0;
    for (int outputNum = 0; outputNum < 16; outputNum++) {
      logger.info("Loading mapping for output " + (outputNum+1));
      String strandIds = KaledoscopeApp.mappingConfig.getStringParameter("output" + (outputNum+1)).getString();
      logger.info("strand ids: " + strandIds);
      if (strandIds.length() > 0) {
        List<LXPoint> pointsWireOrder = new ArrayList<LXPoint>();


        String[] ids = strandIds.split(",");
        for (int i = 0; i < ids.length; i++) {
          int strandId = Integer.parseInt(ids[i]);
          if (strandId < KaledoscopeModel.allStrands.size()) {
            KaledoscopeModel.Strand strand = KaledoscopeModel.allStrands.get(strandId);
            // The default construction of LED points in a strand is already in wire-order.
            pointsWireOrder.addAll(strand.allPoints);
          }
        }

        int numUniversesThisWire = (int) Math.ceil((float) pointsWireOrder.size() / 170f);
        int univStartNum = curUniverseNum;
        int lastUniverseCount = pointsWireOrder.size() - 170 * (numUniversesThisWire - 1);
        int maxLedsPerUniverse = (pointsWireOrder.size()>170)?170:pointsWireOrder.size();
        int[] thisUniverseIndices = new int[maxLedsPerUniverse];
        int curIndex = 0;
        int curUnivOffset = 0;
        for (LXPoint pt : pointsWireOrder) {
          thisUniverseIndices[curIndex] = pt.index;
          curIndex++;
          if (curIndex == 170 || (curUnivOffset == numUniversesThisWire - 1 && curIndex == lastUniverseCount)) {
            logger.log(Level.INFO, "Adding datagram: universe=" + (univStartNum + curUnivOffset) + " points=" + curIndex);
            ArtNetDatagram datagram = new ArtNetDatagram(lx, thisUniverseIndices, curIndex * 3, univStartNum + curUnivOffset);
            try {
              InetAddress address;
              address = InetAddress.getByName(artNetIpAddress);
              datagram.setAddress(address).setPort(artNetIpPort);
            } catch (UnknownHostException uhex) {
              logger.log(Level.SEVERE, "Configuring ArtNet: " + artNetIpAddress + ":" + artNetIpPort, uhex);
            }
            datagrams.add(datagram);
            curUnivOffset++;
            curIndex = 0;
            if (curUnivOffset == numUniversesThisWire - 1) {
              thisUniverseIndices = new int[lastUniverseCount];
            } else {
              thisUniverseIndices = new int[maxLedsPerUniverse];
            }
          }
        }
        curUniverseNum += curUnivOffset;
      }
    }

    for (ArtNetDatagram datagram : datagrams) {
      lx.engine.output.addChild(datagram);
    }
    ArtSyncDatagram syncDatagram = new ArtSyncDatagram(lx);
    syncDatagram.setPort(artNetIpPort);
    try {
      syncDatagram.setAddress(InetAddress.getByName(artNetIpAddress));
    } catch (UnknownHostException uhex) {
      logger.log(Level.SEVERE, "Artnet Sync Datagram bad IP address: " + uhex.getMessage());
    }
    lx.engine.output.addChild(syncDatagram);
  }
}

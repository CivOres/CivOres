package uk.co.froogo.civores.generation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class OreChunkVerticalPacket {
    private ArrayList<WrappedBlockData> blockData;
    private ArrayList<Short> positions;

    /**
     * Add block to the vertical packet.
     *
     * @param material material to set the block to.
     * @param block block to replace.
     */
    public void addBlock(Material material, Block block) {
        if (blockData == null) {
            blockData = new ArrayList<>();
            positions = new ArrayList<>();
        }

        blockData.add(WrappedBlockData.createData(material));

        positions.add((short) (
                (block.getX() & 0xF) << 8 |
                (block.getZ() & 0xF) << 4 |
                (block.getY() & 0xF)
        ));
    }

    /**
     * Send the vertical packet to the player if there are blocks to be sent.
     *
     * If there are no blocks, nothing will be sent.
     *
     * @param player player to send the packet to.
     * @param x chunk's X co-ordinate.
     * @param y chunk's Y co-ordinate.
     * @param z chunk's Z co-ordinate.
     * @throws InvocationTargetException if the packet could not be sent.
     */
    public void send(Player player, int x, int y, int z) throws InvocationTargetException {
        if (blockData == null)
            return;

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

        packet.getSectionPositions().writeSafely(0, new BlockPosition(x, y, z));
        packet.getBlockDataArrays().writeSafely(0, blockData.toArray(new WrappedBlockData[0]));
        packet.getShortArrays().writeSafely(0, ArrayUtils.toPrimitive(positions.toArray(new Short[0])));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }
}

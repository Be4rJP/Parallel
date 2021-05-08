package be4rjp.parallel.nms;
import be4rjp.parallel.Parallel;
import be4rjp.parallel.nms.manager.BlockChangePacketManager;
import be4rjp.parallel.nms.manager.ChunkPacketManager;
import be4rjp.parallel.nms.manager.FlyingPacketManager;
import be4rjp.parallel.nms.manager.MultiBlockChangePacketManager;
import io.netty.channel.*;
import org.bukkit.entity.Player;

import java.nio.channels.ClosedChannelException;

public class PacketHandler extends ChannelDuplexHandler{
    
    private final Player player;
    
    public PacketHandler(Player player){
        this.player = player;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
    
        if(packet.getClass().getSuperclass().getSimpleName().equalsIgnoreCase("PacketPlayInFlying")){
            FlyingPacketManager manager = new FlyingPacketManager(channelHandlerContext, packet, this, player);
            manager.runTaskAsynchronously(Parallel.getPlugin());
            return;
        }
        
        super.channelRead(channelHandlerContext, packet);
    }
    
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
        
        if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutMapChunk")){
            ChunkPacketManager manager = new ChunkPacketManager(channelHandlerContext, packet, channelPromise, this, player);
            manager.runTaskAsynchronously(Parallel.getPlugin());
            return;
        }
        
        if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutBlockChange")){
            BlockChangePacketManager manager = new BlockChangePacketManager(channelHandlerContext, packet, channelPromise, this, player);
            manager.runTaskAsynchronously(Parallel.getPlugin());
            return;
        }
        
        if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutMultiBlockChange")){
            MultiBlockChangePacketManager manager = new MultiBlockChangePacketManager(channelHandlerContext, packet, channelPromise, this, player);
            manager.runTaskAsynchronously(Parallel.getPlugin());
            return;
        }
        
        super.write(channelHandlerContext, packet, channelPromise);
    }
    
    public void doRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception{
        try {
            Channel channel = NMSUtil.getChannel(player);
            
            ChannelHandler channelHandler = channel.pipeline().get(Parallel.getPlugin().getName() + "PacketInjector:" + player.getName());
            if(channelHandler != null && player.isOnline()) {
                super.channelRead(channelHandlerContext, packet);
            }
        }catch (ClosedChannelException e){}
    }
    
    public void doWrite(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception{
        try {
            Channel channel = NMSUtil.getChannel(player);
            
            ChannelHandler channelHandler = channel.pipeline().get(Parallel.getPlugin().getName() + "PacketInjector:" + player.getName());
            if(channelHandler != null && player.isOnline()) {
                super.write(channelHandlerContext, packet, channelPromise);
            }
        }catch (ClosedChannelException e){}
    }
}
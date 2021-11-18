package be4rjp.parallel;

import be4rjp.parallel.chunk.AsyncChunkCache;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.TaskHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
    
    
    @EventHandler
    public void onjoin(PlayerJoinEvent event){
        //Inject packet handler
        Player player = event.getPlayer();
        
        PacketHandler packetHandler = new PacketHandler(player);
        
        try {
            ChannelPipeline pipeline = NMSUtil.getChannel(player).pipeline();
            pipeline.addBefore("packet_handler", Parallel.getPlugin().getName() + "PacketInjector:" + player.getName(), packetHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @EventHandler
    public void onleave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        
        try {
            Channel channel = NMSUtil.getChannel(player);
            
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(Parallel.getPlugin().getName() + "PacketInjector:" + player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        World world = event.getWorld();
        Chunk chunk = event.getChunk();
    
        new BukkitRunnable(){
            @Override
            public void run() {
                TaskHandler.runWorldSync(world, () -> {
                    AsyncChunkCache asyncChunkCache = AsyncChunkCache.computeIfAbsentWorldAsyncChunkCash(world.getName());
                    chunk.load();
                    asyncChunkCache.addLoadedChunk(chunk);
                });
            }
        }.runTaskLater(Parallel.getPlugin(), 20);
        
        
    }
}

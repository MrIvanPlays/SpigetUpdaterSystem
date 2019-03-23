package com.github.mrivanplays.updater.bukkit;

import java.io.File;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.mrivanplays.updater.both.Updater;

/**
 * Represents a spigot updater
 */
public class UpdateDownloader extends Updater implements Listener
{

    private JavaPlugin plugin;
    private String permission;

    public UpdateDownloader(JavaPlugin plugin, int resourceID, String permission)
    {
        super( resourceID, plugin.getDescription().getVersion(), plugin.getName() );
        this.plugin = plugin;
        this.permission = permission;
    }

    /**
     * Gets the new version of the plugin
     *
     * @return new version if update available
     */
    public String getNewVersion()
    {
        if ( updateAvailable() )
        {
            return getLastUpdate()[0];
        } else
        {
            return plugin.getDescription().getVersion();
        }
    }

    /**
     * Fetchs (starts) the updater
     */
    public void fetch()
    {
        plugin.getServer().getScheduler().runTaskAsynchronously( plugin, () ->
        {
            if ( updateAvailable() )
            {
                updateMessageConsole( plugin.getLogger() );
                plugin.getServer().getPluginManager().registerEvents( UpdateDownloader.this, plugin );
            }
        } );
        checkUpdates();
    }

    /**
     * Checks for update every 2 hours
     * If update is available the players with the specified permission
     * are notified and in the console is send message that update is available
     */
    private void checkUpdates()
    {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask( plugin, () ->
        {
            if ( updateAvailable() )
            {
                updateMessageConsole( plugin.getLogger() );
                plugin.getServer().getOnlinePlayers().forEach( player ->
                {
                    if ( player.hasPermission( permission ) )
                    {
                        player.spigot().sendMessage( message() );
                    }
                } );
            }
        }, 144000, 144000 );
    }

    /**
     * Downloads update if available
     *
     * @param sender command sender
     */
    public void downloadUpdate(CommandSender sender)
    {
        sender.sendMessage( "Checking for updates..." );
        if ( updateAvailable() )
        {
            sender.spigot().sendMessage( new ComponentBuilder( "Update found, downloading..." ).color( ChatColor.YELLOW ).create() );
            File jar = new File( plugin.getDataFolder() + File.separator + "Update" + File.separator, plugin.getName() + "-" + getNewVersion() + ".jar" );
            createFile( jar );
            plugin.getServer().getScheduler().runTaskAsynchronously( plugin, () -> download( jar ) );
            sender.sendMessage( color( "&aSuccessfully deployed new version of " + plugin.getName() + " inside plugins/" + plugin.getName() + "/Update" ) );
            sender.sendMessage( color( "&aExtract, move inside plugins, stop server, delete old and start the server!" ) );
        } else
        {
            sender.spigot().sendMessage( new ComponentBuilder( "No updates found for download!" ).color( ChatColor.RED ).create() );
        }
    }

    /**
     * Event, registered when an update is available
     *
     * @param event player join event
     */
    @EventHandler
    public void on(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if ( player.hasPermission( permission ) )
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask( plugin, () -> player.spigot().sendMessage( message() ), 100 );
        }
    }
}

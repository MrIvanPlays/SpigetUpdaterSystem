package com.github.mrivanplays.updater.bungee;

import java.io.File;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import com.github.mrivanplays.updater.both.Updater;

public class UpdateDownloader extends Updater implements Listener
{

    private Plugin plugin;
    private String permission;

    public UpdateDownloader(Plugin plugin, int resourceID, String permission)
    {
        super( resourceID, plugin.getDescription().getVersion(), plugin.getDescription().getName() );
        this.plugin = plugin;
        this.permission = permission;
    }

    public String getNewVersion()
    {
        if ( updateAvailable() )
        {
            return getLastUpdate()[ 0 ];
        } else
        {
            return plugin.getDescription().getVersion();
        }
    }

    public void fetch()
    {
        plugin.getProxy().getScheduler().runAsync( plugin, () ->
        {
            if ( updateAvailable() )
            {
                updateMessageConsole( plugin.getLogger() );
                plugin.getProxy().getPluginManager().registerListener( plugin, UpdateDownloader.this );
            }
        } );
        checkUpdates();
    }

    private void checkUpdates()
    {
        plugin.getProxy().getScheduler().schedule( plugin, () ->
        {
            if ( updateAvailable() )
            {
                updateMessageConsole( plugin.getLogger() );
                plugin.getProxy().getPlayers().forEach( player ->
                {
                    if ( player.hasPermission( permission ) )
                    {
                        player.sendMessage( message() );
                    }
                } );
            }
        }, 2, 2, TimeUnit.HOURS );
    }

    public void downloadUpdate(CommandSender sender)
    {
        sender.sendMessage( new TextComponent( "Checking for updates" ) );
        if ( updateAvailable() )
        {
            String pluginName = plugin.getDescription().getName();
            sender.sendMessage( new ComponentBuilder( "Update found, downloading" ).color( ChatColor.YELLOW ).create() );
            File zip = new File( plugin.getDataFolder() + File.separator + "Update" + File.separator, pluginName + "-" + getNewVersion() + ".zip" );
            createFile( zip );
            plugin.getProxy().getScheduler().runAsync( plugin, () -> download( zip ) );
            sender.sendMessage( new ComponentBuilder( "Successfully deployed new version of " + pluginName + " inside plugins/" + pluginName + "/Update" ).color( ChatColor.GREEN ).create() );
            sender.sendMessage( new ComponentBuilder( "Extract, move inside plugins, stop server, delete old and start the server!" ).color( ChatColor.GREEN ).create() );
        } else
        {
            sender.sendMessage( new ComponentBuilder( "No updates were found to download" ).color( ChatColor.RED ).create() );
        }
    }

    @EventHandler
    public void on(ServerConnectEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        if ( event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY )
        {
            if ( player.hasPermission( permission ) )
            {
                plugin.getProxy().getScheduler().schedule( plugin, () -> player.sendMessage( message() ), 5, TimeUnit.SECONDS );
            }
        }
    }
}

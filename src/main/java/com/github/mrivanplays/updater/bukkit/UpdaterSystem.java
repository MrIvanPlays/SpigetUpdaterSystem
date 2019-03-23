package com.github.mrivanplays.updater.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Dummy plugin, exampling how to fetch updater
 */
public class UpdaterSystem extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        UpdateDownloader updater = new UpdateDownloader( this, 12345, "my.permission" );
        updater.fetch();
        // Example command
        getCommand( "downloadnudes" ).setExecutor( (sender, command, label, args) ->
        {
            if ( sender.hasPermission( "something" ) )
            {
                updater.downloadUpdate( sender );
                return true;
            }
            return true;
        } );
    }
}

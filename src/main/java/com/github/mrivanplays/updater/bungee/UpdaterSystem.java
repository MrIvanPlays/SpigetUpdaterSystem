package com.github.mrivanplays.updater.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Dummy plugin, showing how to fetch updater
 */
public class UpdaterSystem extends Plugin
{

    @Override
    public void onEnable()
    {
        UpdateDownloader updater = new UpdateDownloader( this, 12345, "my.permission" );
        updater.fetch();
        // Example command
        getProxy().getPluginManager().registerCommand( this, new Command( "downloadnudes", "something" )
        {

            @Override
            public void execute(CommandSender sender, String[] args)
            {
                updater.downloadUpdate( sender );
            }
        } );
    }
}

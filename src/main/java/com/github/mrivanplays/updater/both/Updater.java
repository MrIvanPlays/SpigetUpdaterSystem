package com.github.mrivanplays.updater.both;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Represents parent class for the updaters,
 * giving a bunch of abilities to the updater classes
 */
public abstract class Updater
{

    private final String VERSION_URL;
    private final String DESCRIPTION_URL;
    private int resourceID;
    private String currentVersion;
    private String pluginName;

    public Updater(int resourceID, String currentVersion, String pluginName)
    {
        this.resourceID = resourceID;
        this.VERSION_URL = "https://api.spiget.org/v2/resources/" + resourceID + "/versions?size=" + Integer.MAX_VALUE;
        this.DESCRIPTION_URL = "https://api.spiget.org/v2/resources/" + resourceID + "/updates?size=" + Integer.MAX_VALUE;
        this.currentVersion = currentVersion;
        this.pluginName = pluginName;
    }

    /**
     * Downloads a plugins
     *
     * @param output the outputted file
     */
    protected void download(File output)
    {
        try
        {
            URL url = new URL( "https://api.spiget.org/v2/resources/" + resourceID + "/download" );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod( "GET" );

            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream( output );
            byte[] b = new byte[1024];
            int n = in.read( b );
            while ( n != -1 )
            {
                out.write( b, 0, n );
                n = in.read( b );
            }
            out.close();
            in.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Fetchs the latest version and update title from spiget into a String array.
     * [0] - version
     * [1] - update title
     *
     * @return string array with the current version and update title
     * on spigot, fetched via spiget
     */
    protected String[] getLastUpdate()
    {

        try
        {
            JSONArray versionsArray = (JSONArray) JSONValue.parseWithException( IOUtils.toString( new URL( VERSION_URL ), Charset.defaultCharset() ) );
            String lastVersion = ( (JSONObject) versionsArray.get( versionsArray.size() - 1 ) ).get( "name" ).toString();
            if ( !lastVersion.equalsIgnoreCase( currentVersion ) )
            {
                JSONArray updatesArray = (JSONArray) JSONValue.parseWithException( IOUtils.toString( new URL( DESCRIPTION_URL ), Charset.defaultCharset() ) );
                String updateName = ( (JSONObject) updatesArray.get( updatesArray.size() - 1 ) ).get( "title" ).toString();

                return new String[] { lastVersion, updateName };
            }
        } catch ( IOException | ParseException e )
        {
            return new String[0];
        }
        return new String[0];
    }

    /**
     * Desired update message, which is send to the player.
     * Created via component api.
     *
     * @return a base component array, with all extras and stuff going around
     */
    protected BaseComponent[] message()
    {
        ComponentBuilder builder = new ComponentBuilder( "[" )
                .color( ChatColor.GOLD ).append( pluginName ).color( ChatColor.YELLOW ).append( "]" ).color( ChatColor.GOLD ).append( " " )
                .append( "New update available" ).color( ChatColor.GREEN ).append( "\n" ).append( "You are currently running version " + currentVersion )
                .color( ChatColor.RED ).append( "\n" ).append( "New version: " + getLastUpdate()[0] ).color( ChatColor.GREEN ).append( "\n" )
                .append( "What's new: \"" + getLastUpdate()[1] + "\"" ).color( ChatColor.AQUA ).append( "\n" ).append( "You can download via clicking " ).color( ChatColor.BLUE )
                .append( "\n" );
        TextComponent linkComponent = new TextComponent( "here" );
        linkComponent.setBold( true );
        linkComponent.setColor( ChatColor.AQUA );
        linkComponent.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, getResourceURL() ) );
        linkComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Click me to get redirect to download page" ).color( ChatColor.DARK_GREEN ).create() ) );
        builder.append( linkComponent );
        return builder.create();
    }

    protected String color(String text)
    {
        return ChatColor.translateAlternateColorCodes( '&', text );
    }

    /**
     * Gets the resource url of the specified resource
     *
     * @return resource url
     */
    private String getResourceURL()
    {
        return "https://spigotmc.org/resources/" + resourceID;
    }

    /**
     * Checks if update is available
     *
     * @return true, if update available
     */
    protected boolean updateAvailable()
    {
        return getLastUpdate().length == 2;
    }

    /**
     * Sends update message to the console
     *
     * @param logger desired logger
     */
    protected void updateMessageConsole(Logger logger)
    {
        logger.warning( "Stable version " + getLastUpdate()[0] + " is out! You are still running version " + currentVersion );
        logger.info( "What's new: \"" + getLastUpdate()[1] + "\"" );
        logger.info( "Download from here: " + getResourceURL() );
    }

    /**
     * Creates a file
     *
     * @param jar created file
     */
    protected void createFile(File jar)
    {
        if ( !jar.exists() )
        {
            if ( !jar.getParentFile().exists() )
            {
                jar.getParentFile().mkdirs();
            }
            try
            {
                jar.createNewFile();
            } catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
}

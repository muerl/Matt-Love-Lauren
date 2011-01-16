/**
 * CommunicationSimulatorThread.java
 * 
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.quarkmobilty.sample.mattloveslauren;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import net.rim.device.api.collection.ReadableList;
import net.rim.device.api.io.IOCancelledException;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.UiApplication;


/**
 * This Thread subclass simulates communication with a server and
 * generates message actions. It can create new messages or update
 * and delete existing ones.
 */
public final class CommunicationSimulatorThread extends Thread
{

    private boolean _keepRunning;
	private boolean _stop;
	private static Random _random = new Random();
    private static String[] NAMES = {"Scott Wyatt", "Tanya Wahl", "Kate Strike", "Mark McMullen", "Beth Horton", "John Graham",
                    "Ho Sung Chan", "Long Feng Wu", "Kevil Wilhelm", "Trevor Van Daele"};

    private static String[] PICTURES = {"BlueDress.png", "BlueSuit.png", "BlueSweatshirt.png", "BrownShirt.png", "Construction.png",
                    "DarkJacket.png", "DarkSuit.png", "FemaleDoctor.png", "GreenJacket.png", "GreenShirt.png", "GreenTop.png",
                    "LeatherJacket.png", "MaleDoctor.png", "Mechanic.png", "OrangeShirt.png", "PatternShirt.png", "PurpleTop.png",
                    "RedCap.png", "RedJacket.png", "RedShirt.png"};

    private static String messageUrl = "lovemessage.html?index=";
    private static String bisAddition = ";deviceside=false"; //Replace with the magic BIS connection string
    										//Or use the 5.0 connection api to be even better
	private static String baseUrl = "http://dev.mattloveslauren.com/";
        /**
     * Creates a new CommunicationSimulatorThread object
     */    
    public CommunicationSimulatorThread()
    {
        _keepRunning = true;
    }


    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // Perform random actions to the message store every three seconds
        MessageListDemoStore messageStore = MessageListDemoStore.getInstance();
        while(_keepRunning)
        {
        	try{
	            synchronized(messageStore)
	            {
	                performAction(messageStore);
	            }
        	}
        	catch (Exception e){
        		
        	}
            try
            {
                synchronized(this)
                {
                    wait(300000);
                }
            }
            catch(final InterruptedException e)
            {
                UiApplication.getUiApplication().invokeLater(new Runnable()
                {
                    public void run()
                    {
                        Dialog.alert("Thread#wait(long) threw " + e.toString());
                    }                });

                return;
            }
        }
    }
    private static int inserted = 0;

    /**
     * Performs a random action. The action can either be: updating an existing
     * message, deleting an inbox message or deleting a message completely.
     * 
     * @param messageStore The message store to perform the random action to
     */
    private void performAction(MessageListDemoStore messageStore)
    {
    	if(availableMessages() > inserted)
    		addInboxMessage(messageStore);
    }


    private int availableMessages() {
    	String content;
        try 
        {                        
            StreamConnection s = null;
            s = (StreamConnection)Connector.open(getAvailableUrl());
            HttpConnection httpConn = (HttpConnection)s;                        
            
            int status = httpConn.getResponseCode();
            
            
			if (status == HttpConnection.HTTP_OK)
            {
                // Is this html?

                InputStream input = s.openInputStream();

                byte[] data = new byte[256];
                int len = 0;
                int size = 0;
                StringBuffer raw = new StringBuffer();
                    
                while ( -1 != (len = input.read(data)) )
                {
                    // Exit condition for the thread. An IOException is 
                    // thrown because of the call to  httpConn.close(), 
                    // causing the thread to terminate.
                    if ( _stop )
                    {
                        httpConn.close();
                        s.close();
                        input.close();
                    } 
                    raw.append(new String(data, 0, len));
                    size += len;    
                }   
                         
       //         raw.insert(0, "bytes received]\n");
         //       raw.insert(0, size);
           //     raw.insert(0, '[');
                content = raw.toString();
                input.close();                      
            } 
            else 
            {                            
                content = "response code = " + status;
            }  
            s.close();                    
        } 
        catch (IOCancelledException e) 
        {       
            System.out.println(e.toString());                        
            return inserted;
        }
        catch (Exception e) 
        {       
        	System.out.println(e.toString());                        
            return inserted;
        }
        String intString = content;
        int i = 0;
        for(; i < content.length();i++){
        	char aChar = content.charAt(i);
        	if(aChar < '0' || aChar >'9')
        		break;
        }
        if(i > 0)
        	return Integer.parseInt(content.substring(0,i));
        return inserted;
        
        

	}


	private String getAvailableUrl() {
		 
		return baseUrl+"available.txt"+ bisAddition;
	}


	/**
     * Adds a predefined message to the specified message store
     * 
     * @param messageStore The message store to add the message to
     * @param inserted 
     */
    private void addInboxMessage(MessageListDemoStore messageStore)
    {
        DemoMessage message = new DemoMessage();
        String name = "Matt Loves Lauren";
        message.setSender(name);
        int insertedplus1 = (inserted+1);
        message.setSubject("Love Message " + insertedplus1);
        message.setMessage(baseUrl + messageUrl  + Integer.toString(inserted) + bisAddition);
        message.setReceivedTime(System.currentTimeMillis());

        // Assign random preview picture
        message.setPreviewPicture(getRandomPhotoImage());

        // Store message
        messageStore.addInboxMessage(message);

        // Notify folder
        messageStore.getInboxFolder().fireElementAdded(message);
        inserted++;
    }


    /**
     * Retrieves a random predefined image
     * 
     * @return The hard coded photo image
     */
    public static EncodedImage getRandomPhotoImage()
    {
        String pictureName = "photo/" + PICTURES[_random.nextInt(PICTURES.length)];
        return EncodedImage.getEncodedImageResource(pictureName);
    }


    /**
     * Stops the thread from continuing its processing
     */
    void stopRunning()
    {
        synchronized(this)
        {
            _keepRunning = false;
            notifyAll();
        }
    }
}

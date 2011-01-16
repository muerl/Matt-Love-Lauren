/**
 * MessageListDemoStore.java
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

import java.util.Vector;

import net.rim.blackberry.api.messagelist.*;
import net.rim.device.api.collection.ReadableList;
import net.rim.device.api.system.RuntimeStore;



/**
 * This class is used to facilitate the storage of messages. For the sake of
 * simplicitly, we are saving messages in the device runtime store. In a real
 * world situation, messages would be saved in device persistent store and/or
 * on a mail server.
 */
public final class MessageListDemoStore
{
    // com.quarkmobility.messagelistdemo.MessageListDemoStore
    private static final long MSG_KEY = 0x7967bd0fb1dff49fL;

    private static MessageListDemoStore _instance;

    private ReadableListImpl _inboxMessages;
    private ReadableListImpl _deletedMessages;
    private ApplicationMessageFolder _mainFolder;
    private ApplicationMessageFolder _deletedFolder;
    private ApplicationIndicator _indicator;


    /**
     * Creates a new MessageListDemoStore object
     */
    private MessageListDemoStore()
    {
        _inboxMessages = new ReadableListImpl();
        _deletedMessages = new ReadableListImpl();
        _indicator = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
    }


    /**
     * Gets the singleton instance of the MessageListDemoStore
     * 
     * @return The singleton instance of the MessagelistDemoStore
     */
    public static synchronized MessageListDemoStore getInstance()
    {
        // Keep messages as singleton in the RuntimeStore
        if(_instance == null)
        {
            RuntimeStore rs = RuntimeStore.getRuntimeStore();

            synchronized(rs)
            {
                _instance = (MessageListDemoStore) rs.get(MSG_KEY);

                if(_instance == null)
                {
                    _instance = new MessageListDemoStore();
                    rs.put(MSG_KEY, _instance);
                }
            }
        }

        return _instance;
    }


    /**
     * Sets the main and deleted folders
     * 
     * @param mainFolder The main folder to use
     * @param deletedFolder The deleted folder to use
     */
    void setFolders(ApplicationMessageFolder mainFolder, ApplicationMessageFolder deletedFolder)
    {
        _mainFolder = mainFolder;
        _deletedFolder = deletedFolder;
    }


    /**
     * Retrieves the inbox folder
     * 
     * @return The inbox folder
     */
    ApplicationMessageFolder getInboxFolder()
    {
        return _mainFolder;
    }


    /**
     * Retrieves the deleted folder
     * 
     * @return The deleted folder
     */
    ApplicationMessageFolder getDeletedFolder()
    {
        return _deletedFolder;
    }


    /**
     * Moves a message into the deleted folder
     * 
     * @param message The message to move to the deleted folder
     */
    void deleteInboxMessage(DemoMessage message)
    {
        if(message.isNew())
        {
            // Update indicator        
            _indicator.setValue(_indicator.getValue() - 1);
            if(_indicator.getValue() <= 0)
            {
                _indicator.setVisible(false);
            }
        }

        message.messageDeleted();
        _inboxMessages.removeMessage(message);
        _deletedMessages.addMessage(message);
    }


    /**
     * Commits a message to the persistent store
     * 
     * @param message The message to commit
     */
    void commitMessage(DemoMessage message)
    {
        // This empty method exists to reinforce the idea that in a real world
        // situation messages would be saved in device persistent store and/or
        // on a mail server.
    }


    /**
     * Adds a message to the inbox
     * 
     * @param message The message to add to the inbox
     */
    void addInboxMessage(DemoMessage message)
    {
        _inboxMessages.addMessage(message);

        if(message.isNew())
        {
            // Update indicator                
            _indicator.setValue(_indicator.getValue() + 1);
            _indicator.setNotificationState(true);
            if(!_indicator.isVisible())
            {
                _indicator.setVisible(true);
            }
        }
    }


    /**
     * Completely deletes the message from the message store
     * 
     * @param message The message to delete from the message store
     */
    void deleteMessageCompletely(DemoMessage message)
    {
        _deletedMessages.removeMessage(message);
    }


    /**
     * Retrieves the inbox messages as a readable list
     * 
     * @return The readable list of all the inbox messages
     */
    ReadableListImpl getInboxMessages()
    {
        return _inboxMessages;
    }


    /**
     * Gets the deleted messages as a readable list
     * 
     * @return The readable list of all the deleted messages
     */
    ReadableListImpl getDeletedMessages()
    {
        return _deletedMessages;
    }
    

    /**
     * This is an implementation of the ReadableList interface which stores the
     * list of messages using a Vector.
     */
    static class ReadableListImpl implements ReadableList
    {
        private Vector messages;

        /**
         * Creates a empty instance of ReadableListImpl
         */
        ReadableListImpl()
        {
            messages = new Vector();
        }


        /**
         * @see net.rim.device.api.collection.ReadableList#getAt(int)
         */
        public Object getAt(int index)
        {
            return messages.elementAt(index);
        }


        /**
         * @see net.rim.device.api.collection.ReadableList#getAt(int, int, Object, int)
         */
        public int getAt(int index, int count, Object[] elements, int destIndex)
        {
            return 0;
        }


        /**
         * @see net.rim.device.api.collection.ReadableList#getIndex(Object)
         */
        public int getIndex(Object element)
        {
            return messages.indexOf(element);
        }


        /**
         * @see net.rim.device.api.collection.ReadableList#size()
         */
        public int size()
        {
            return messages.size();
        }


        /**
         * Add a message to this list
         * 
         * @param message The message to add to this list
         */
        void addMessage(DemoMessage message)
        {
            messages.addElement(message);
        }
        

        /**
         * Removes a message from this list
         * 
         * @param message The message to remove from this list
         */
        void removeMessage(DemoMessage message)
        {
            messages.removeElement(message);
        }
    }
}

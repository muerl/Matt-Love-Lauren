/**
 * MessageListDemoDaemon.java
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

import java.util.Date;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.messagelist.ApplicationFolderIntegrationConfig;
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.blackberry.api.messagelist.ApplicationMessage;
import net.rim.blackberry.api.messagelist.ApplicationMessageFolder;
import net.rim.blackberry.api.messagelist.ApplicationMessageFolderListener;
import net.rim.blackberry.api.messagelist.ApplicationMessageFolderRegistry;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.image.Image;
import net.rim.device.api.ui.image.ImageFactory;



/**
 * Daemon process that runs in the background. It's tasks include non-gui
 * message operations such as message deletion, marking messages as read/unread
 * and (in a typical real world scenario) synchronization with a mail server.
 */
public final class MessageListDemoDaemon extends Application implements ApplicationMessageFolderListener
{
    private static String APPLICATION_NAME = "Matt Loves Lauren";


    /**
     * Called during device startup. Registers application descriptors, message
     * folder listeners, message icons and menu items.
     */
    void init()
    {
        // 1. Register folders and application descriptors ----------------------

        ApplicationMessageFolderRegistry reg = ApplicationMessageFolderRegistry.getInstance();

        // Some context menu items don't need a GUI (e.g. an item for deleting a
        // message) and will be run in the current daemon application.
        ApplicationDescriptor daemonDescr = ApplicationDescriptor.currentApplicationDescriptor();

        // Main application descriptor - causes application to be launched with
        // default welcome screen if a user clicks on the "Message List Demo"
        // header in the home screen notifications view.
        ApplicationDescriptor mainDescr = new ApplicationDescriptor(daemonDescr, APPLICATION_NAME, new String[] {});

        // This application descriptor launches this application with a GUI to
        // execute listener callbacks, e.g. to display a message.
        ApplicationDescriptor uiCallbackDescr = new ApplicationDescriptor(daemonDescr, APPLICATION_NAME, new String[] {"gui"});

        // Get existing messages from storage and register them in folders        
        ApplicationFolderIntegrationConfig inboxIntegration = new ApplicationFolderIntegrationConfig(true, true, mainDescr);
        ApplicationFolderIntegrationConfig deletedIntegration = new ApplicationFolderIntegrationConfig(false);

        MessageListDemoStore messages = MessageListDemoStore.getInstance();
        ApplicationMessageFolder inbox = reg.registerFolder(MattLovesLauren.INBOX_FOLDER_ID, "Love Messages", messages.getInboxMessages(),
                        inboxIntegration);
        ApplicationMessageFolder deleted = reg.registerFolder(MattLovesLauren.DELETED_FOLDER_ID, "Deleted Love Messages", messages.getDeletedMessages(), deletedIntegration);

        // Register as a listener for callback notifications
        inbox.addListener(this, ApplicationMessageFolderListener.MESSAGE_DELETED | ApplicationMessageFolderListener.MESSAGE_MARKED_OPENED
            | ApplicationMessageFolderListener.MESSAGE_MARKED_UNOPENED | ApplicationMessageFolderListener.MESSAGES_MARKED_OLD, daemonDescr);
            deleted.addListener(this, ApplicationMessageFolderListener.MESSAGE_DELETED, daemonDescr);

        messages.setFolders(inbox, deleted);

        // We've registered two folders, specify root folder name for the
        // [View Folder] screen.
        reg.setRootFolderName(APPLICATION_NAME);

        // 2. Register message icons -------------------------------------------

        ApplicationIcon newIcon = new ApplicationIcon(EncodedImage.getEncodedImageResource("img/new.png"));
        ApplicationIcon readIcon = new ApplicationIcon(EncodedImage.getEncodedImageResource("img/read.png"));

        reg.registerMessageIcon(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_NEW, newIcon);
        reg.registerMessageIcon(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_OPENED, readIcon);

        // 3. Register message menu items --------------------------------------

        ApplicationMenuItem openMenuItem = new OpenContextMenu(0x230010);
        ApplicationMenuItem markOpenedMenuItem = new MarkOpenedContextMenu(0x230030);
        ApplicationMenuItem markUnopenedMenuItem = new MarkUnreadContextMenu(0x230040);

        ApplicationMenuItem[] newGuiMenuItems = new ApplicationMenuItem[] {openMenuItem};
        ApplicationMenuItem[] newDaemonMenuItems = new ApplicationMenuItem[] {markOpenedMenuItem};
        ApplicationMenuItem[] openedGuiMenuItems = new ApplicationMenuItem[] {openMenuItem};
        ApplicationMenuItem[] openedDaemonMenuItems = new ApplicationMenuItem[] {markUnopenedMenuItem};
        ApplicationMenuItem[] deletedGuiMenuItems = new ApplicationMenuItem[] {openMenuItem,};

        reg.registerMessageMenuItems(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_NEW, newGuiMenuItems, uiCallbackDescr);
        reg.registerMessageMenuItems(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_NEW, newDaemonMenuItems, daemonDescr);
        reg.registerMessageMenuItems(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_OPENED, openedGuiMenuItems, uiCallbackDescr);
        reg.registerMessageMenuItems(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_OPENED, openedDaemonMenuItems, daemonDescr);

        reg.setBulkMarkOperationsSupport(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_NEW, true, false);
        reg.setBulkMarkOperationsSupport(DemoMessage.DEMO_MESSAGE_TYPE, MattLovesLauren.STATUS_OPENED, false, true);
    }


    /**
     * Changes the indicator value by the specified amount
     * 
     * @param value The amount by which the indicator must change
     */
    public static void changeIndicator(int value)
    {
        // Update indicator
        ApplicationIndicator indicator = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
        if(indicator == null)
        {
            return;
        }

        indicator.setValue(indicator.getValue() + value);

        // Check if there are any new messages and show indicator accordingly
        if(indicator.getValue() <= 0)
        {
            indicator.setVisible(false);
        }
        else
        {
            indicator.setVisible(true);
        }
    }
    

    /**
     * Mark Opened menu item. After the method marks the message read, it fires
     * an update event.
     */
    private static class MarkOpenedContextMenu extends ApplicationMenuItem
    {
        /**
         * Creates a new ApplicationMenuItem instance with provided menu position
         * 
         * @param order Display order of this item, lower numbers correspond to  higher placement in the menu
         */
        MarkOpenedContextMenu(int order)
        {
            super(order);

            // Set icon for GCM menu
            EncodedImage eiMarkOpened = EncodedImage.getEncodedImageResource("img/sm_mark_opened.png");
            if(eiMarkOpened != null)
            {
                Image image = ImageFactory.createImage(eiMarkOpened);
                this.setIcon(image);
            }
        }


        /**
         * Marks the context message opened
         * 
         * @see ApplicationMenuItem#run(Object)
         */
        public Object run(Object context)
        {
            if(context instanceof DemoMessage)
            {
                DemoMessage message = (DemoMessage) context;
                message.markRead();
                ApplicationMessageFolderRegistry reg = ApplicationMessageFolderRegistry.getInstance();
                ApplicationMessageFolder folder = reg.getApplicationFolder(MattLovesLauren.INBOX_FOLDER_ID);
                folder.fireElementUpdated(message, message);
                changeIndicator(-1);
            }
            
            return context;
        }


        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "Mark Love Message Read";
        }
    }
    

    /**
     * Mark Unread menu item. After the method marks the message unread, it
     * fires an update event.
     */
    private static class MarkUnreadContextMenu extends ApplicationMenuItem
    {
        /**
         * Creates a new ApplicationMenuItem instance with provided menu position
         * 
         * @param order Display order of this item, lower numbers correspond to higher placement in the menu
         */
        MarkUnreadContextMenu(int order)
        {
            super(order);

            // Set icon for GCM menu
            EncodedImage eiMarkUnOpened = EncodedImage.getEncodedImageResource("img/sm_mark_unopened.png");
            if(eiMarkUnOpened != null)
            {
                Image image = ImageFactory.createImage(eiMarkUnOpened);
                this.setIcon(image);
            }
        }


        /**
         * @see ApplicationMenuItem#run(Object)
         */
        public Object run(Object context)
        {
            if(context instanceof DemoMessage)
            {
                // Mark the context message unread
                DemoMessage message = (DemoMessage) context;
                message.markAsNew();
                ApplicationMessageFolderRegistry reg = ApplicationMessageFolderRegistry.getInstance();
                ApplicationMessageFolder folder = reg.getApplicationFolder(MattLovesLauren.INBOX_FOLDER_ID);
                folder.fireElementUpdated(message, message);
                changeIndicator(1);
            }
            
            return context;
        }


        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "Mark Love Message Unread";
        }
    }
    

    /**
     * Open Context menu item. Marks read and opens the selected message for
     * viewing.
     */
    static class OpenContextMenu extends ApplicationMenuItem
    {
        /**
         * Creates a new ApplicationMenuItem instance with provided menu position
         * 
         * @param order Display order of this item, lower numbers correspond to higher placement in the menu
         */
        public OpenContextMenu(int order)
        {
            super(order);

            // Set icon for GCM menu
            EncodedImage eiOpen = EncodedImage.getEncodedImageResource("img/sm_open.png");
            if(eiOpen != null)
            {
                Image image = ImageFactory.createImage(eiOpen);
                this.setIcon(image);
            }
        }


        /**
         * @see ApplicationMenuItem#run(Object)
         */
        public Object run(Object context)
        {
            if(context instanceof DemoMessage)
            {
                DemoMessage message = (DemoMessage) context;

                // Update status if message is new
                if(message.isNew())
                {
                    message.markRead();
                    ApplicationMessageFolderRegistry reg = ApplicationMessageFolderRegistry.getInstance();
                    ApplicationMessageFolder folder = reg.getApplicationFolder(MattLovesLauren.INBOX_FOLDER_ID);
                    folder.fireElementUpdated(message, message);
                    changeIndicator(-1);
                }

                // Show message
                DemoMessageScreen previewScreen = new DemoMessageScreen(message);
                UiApplication uiApplication = UiApplication.getUiApplication();
                uiApplication.pushScreen(previewScreen);
                uiApplication.requestForeground();
            }
            return context;
        }


        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "View Love Message";
        }
    }


    /**
     * @see ApplicationMessageFolderListener#actionPerformed(int, ApplicationMessage[], ApplicationMessageFolder)
     */
    public void actionPerformed(int action, ApplicationMessage[] messages, ApplicationMessageFolder folder)
    {
        MessageListDemoStore messageStore = MessageListDemoStore.getInstance();

        // Check if user opened the Message list and marked messages as not 'new'
        if(action == ApplicationMessageFolderListener.MESSAGES_MARKED_OLD)
        {
            // User opened the message list and viewed messages, remove the
            // 'notification' state from the indicator.
            ApplicationIndicator indicator = ApplicationIndicatorRegistry.getInstance().getApplicationIndicator();
            if(indicator != null)
            {
                indicator.setNotificationState(false);
            }

            // No further processing
            return;
        }

        synchronized(messageStore)
        {
            // Check if action was performed on multiple messages
            if(messages.length == 1)
            {
                DemoMessage message = (DemoMessage) messages[0];

                switch(action)
                {
                    case ApplicationMessageFolderListener.MESSAGE_DELETED:
                        if(folder.getId() == MattLovesLauren.INBOX_FOLDER_ID)
                        {
                            // Message from Inbox was deleted, update storage,
                            // the message will go into the Deleted folder
                            messageStore.deleteInboxMessage(message);

                            // Notify GUI that message has moved to
                            // another folder.
                            messageStore.getDeletedFolder().fireElementAdded(message);

                            // Note: There is no need to fireElementRemoved(),
                            // message was already deleted.                            
                        }
                        else
                        {
                            // Message was deleted completely from the Deleted
                            // folder, update storage folder.
                            messageStore.deleteMessageCompletely(message);

                            // Note: There is no need to fireElementRemoved(),
                            // message was already deleted.
                        }
                        break;
                    case ApplicationMessageFolderListener.MESSAGE_MARKED_OPENED:

                        // Update the indicator
                        if(message.isNew())
                        {
                            changeIndicator(-1);
                        }

                        // Update message
                        message.markRead();

                        // Update storage
                        messageStore.commitMessage(message);

                        // Notify GUI that message has changed
                        folder.fireElementUpdated(message, message);
                        break;
                    case ApplicationMessageFolderListener.MESSAGE_MARKED_UNOPENED:

                        // Update the indicator
                        if(!message.isNew())
                        {
                            changeIndicator(1);
                        }

                        // Update message
                        message.markAsNew();

                        // Update storage
                        messageStore.commitMessage(message);

                        // Notify GUI that message has changed
                        folder.fireElementUpdated(message, message);

                        break;
                }
            }
            else
            {
                // Multiple messages were affected, optimize notifications
                ApplicationMessageFolder resetFolder = folder;

                for(int i = 0; i < messages.length; i++)
                {
                    DemoMessage message = (DemoMessage) messages[i];

                    switch(action)
                    {
                        case ApplicationMessageFolderListener.MESSAGE_DELETED:
                            if(folder.getId() == MattLovesLauren.INBOX_FOLDER_ID)
                            {
                                // Update storage, the message will go
                                // into the Deleted folder.
                                messageStore.deleteInboxMessage(message);
                            }
                            else
                            {
                                // Message was deleted completely from the
                                // Deleted folder, update storage.
                                messageStore.deleteMessageCompletely(message);
                            }
                            break;
                        case ApplicationMessageFolderListener.MESSAGE_MARKED_OPENED:

                            if(message.isNew())
                            {
                                changeIndicator(-1);
                            }

                            // Update message
                            message.markRead();

                            // Update storage
                            messageStore.commitMessage(message);

                            // Notify GUI that message has changed
                            folder.fireElementUpdated(message, message);
                            break;
                        case ApplicationMessageFolderListener.MESSAGE_MARKED_UNOPENED:

                            if(!message.isNew())
                            {
                                changeIndicator(1);
                            }

                            // Update message
                            message.markAsNew();

                            // Update storage
                            messageStore.commitMessage(message);

                            // Notify GUI that message has changed
                            folder.fireElementUpdated(message, message);
                            break;
                    }
                }

                if(action == ApplicationMessageFolderListener.MESSAGE_DELETED && folder.getId() == MattLovesLauren.INBOX_FOLDER_ID)
                {
                    // There is no need to reset the Inbox folder, all the
                    // messages have already been deleted from it. We need to
                    // reset Deleted folder because messages were appended to it.
                    resetFolder = messageStore.getDeletedFolder();
                }

                if(resetFolder != null)
                {
                    resetFolder.fireReset();
                }
            }
        }
    }
}

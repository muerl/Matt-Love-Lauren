/**
 * MessageListDemoViewer.java
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


import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;


/**
 * A MainScreen subclass for displaying messages
 */
public final class DemoMessageScreen extends MainScreen
{
    private BrowserField _browserField;
	private BrowserFieldRequest _request;

	/**
     * Constructs a screen to view a message
     * 
     * @param demoMessage The message to display
     */
    public DemoMessageScreen(DemoMessage demoMessage)
    {
        LabelField title = new LabelField("Matt Loves Lauren");
        setTitle(title);
        BrowserFieldConfig config = new BrowserFieldConfig();  
        config.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);        
        _browserField = new BrowserField(config);
        add(_browserField);
        _request = new BrowserFieldRequest(demoMessage.getMessage());
        
    }
    protected void onUiEngineAttached(boolean attached)
    {
        if(attached)
        {
            try
            {
                _browserField.requestContent(_request);
            }
            catch(Exception e)
            {                
                deleteAll();
                add(new LabelField("ERROR:\n\n"));
                add(new LabelField(e.getMessage()));
            }
        }
    }
}

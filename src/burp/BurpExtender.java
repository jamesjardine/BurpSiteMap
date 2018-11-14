package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author jamesjardine
 */
public class BurpExtender implements IBurpExtender, IContextMenuFactory
{
    IBurpExtenderCallbacks callbacks = null;
    IExtensionHelpers helpers;
    
    
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;
        this.callbacks.registerContextMenuFactory(this);
        this.callbacks.setExtensionName("Site Map");
    }
    
    @Override
    public ArrayList<JMenuItem> createMenuItems(IContextMenuInvocation iContextMenuInvocation)
    {
        ArrayList<JMenuItem> menus = new ArrayList<>();
        
        JMenuItem menu = new JMenuItem("Export Site Map");

        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Starting the analysis");
                IHttpRequestResponse site = iContextMenuInvocation.getSelectedMessages()[0];
                String fileName = site.getHttpService().getHost() + ".txt";
                IExtensionHelpers helpers = callbacks.getHelpers();
                IHttpRequestResponse[] messages = callbacks.getSiteMap(site.getHttpService().getProtocol() + "://" + site.getHttpService().getHost());
                String Urls = "";
                for (int i = 0; i < messages.length; i++)
                {
                    Urls += helpers.analyzeRequest(messages[i]).getUrl().toString();
                    Urls += "\n";
                }

                try
                {
                  WriteFile(Urls, fileName);  
                }
                catch(IOException ex)
                {}
            }
        });
        
        menus.add(menu);
        
        menu.setEnabled((iContextMenuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TREE));

        
        return menus;
    }
    
    // Separated this out in case we wanted to use a Directory Chooser.
    // Currently, requires a Documents folder in the users home folder.
    String GetSavePath(String fileName)
    {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        String filePath = Paths.get(currentPath.toString(), "Documents", fileName).toString();

        System.out.println("The Path: " + filePath);
        return filePath;
    }
    
    // Write out the urls into a file.
    public void WriteFile(String urls, String fileName) throws IOException
    {
        String txtFile = GetSavePath(fileName);
        //Write out the cookies to a CSV File
        System.out.println("Writing Text");
        FileWriter writer = new FileWriter(txtFile);
        writer.append(urls);
        writer.flush();
        writer.close();
        System.out.println("Done");
    }
}


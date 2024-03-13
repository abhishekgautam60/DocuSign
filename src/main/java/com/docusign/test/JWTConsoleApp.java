/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.docusign.test;


import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth.OAuthToken;
import com.docusign.esign.client.auth.OAuth.UserInfo;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.CarbonCopy;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.docusign.esign.model.SignHere;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.awt.Desktop;
import java.net.URI;

/**
 * Starter class for JWTConsoleApp application.
 */

public class JWTConsoleApp {

    static String DevCenterPage = "https://developers.docusign.com/platform/auth/consent";
    /**
     * Application entry point.
     *
     * @param args application command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws java.io.IOException  {

        Scanner scanner = new Scanner(System. in);
        System.out.println ("Welcome to the JWT Code example! ");
        System.out.print("Enter the signer's email address: ");
        String signerEmail = scanner. nextLine();
        System.out.print("Enter the signer's name: ");
        String signerName = scanner. nextLine();
        System.out.print("Enter the carbon copy's email address: ");
        String ccEmail = scanner. nextLine();
        System.out.print("Enter the carbon copy's name: ");
        String ccName = scanner. nextLine();
        
        // Get information fro app.config
        Properties prop = new Properties();
        String fileName = "C://Users//user//Desktop//Test.txt";
        FileInputStream fis = new FileInputStream(fileName);
        prop.load(fis);
        try
        {
            // Get access token and accountId
            ApiClient apiClient = new ApiClient("https://demo.docusign.net/restapi");
            apiClient.setOAuthBasePath("account-d.docusign.com");
            ArrayList<String> scopes = new ArrayList<>();
            scopes.add("signature");
            scopes.add("impersonation");
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(prop.getProperty("rsaKeyFile")));
            OAuthToken oAuthToken = apiClient.requestJWTUserToken(
                "7f0d3a73-74a3-4a8d-89ee-447dbb008014",
                "83f2d737-8e30-4129-af97-78e0ca84a988",
                scopes,
                privateKeyBytes,
                3600);
            String accessToken = oAuthToken.getAccessToken();
            UserInfo userInfo = apiClient.getUserInfo(accessToken);
            String accountId = userInfo.getAccounts().get(0).getAccountId();

            // Create envelopeDefinition object
            EnvelopeDefinition envelope = new EnvelopeDefinition();
            envelope.setEmailSubject("Please sign this document set");
            envelope.setStatus("sent");

            // Create tabs object
            SignHere signHere = new SignHere();
            signHere.setDocumentId("1");
            signHere.setPageNumber("1");
            signHere.setXPosition("191");
            signHere.setYPosition("148");
            Tabs tabs = new Tabs();
            tabs.setSignHereTabs(Arrays.asList(signHere));
            // Set recipients
            Signer signer = new Signer();
            signer.setEmail(signerEmail);
            signer.setName(signerName);
            signer.recipientId("1");
            signer.setTabs(tabs);
            CarbonCopy cc = new CarbonCopy();
            cc.setEmail(ccEmail);
            cc.setName(ccName);
            cc.recipientId("2");
            Recipients recipients = new Recipients();
            recipients.setSigners(Arrays.asList(signer));
            recipients.setCarbonCopies(Arrays.asList(cc));
            envelope.setRecipients(recipients);
    
            // Add document
            Document document = new Document();
            document.setDocumentBase64("VGhhbmtzIGZvciByZXZpZXdpbmcgdGhpcyEKCldlJ2xsIG1vdmUgZm9yd2FyZCBhcyBzb29uIGFzIHdlIGhlYXIgYmFjay4=");
            document.setName("Test");
            document.setFileExtension("txt");
            document.setDocumentId("1");
            envelope.setDocuments(Arrays.asList(document));            

            // Send envelope
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary results = envelopesApi.createEnvelope(accountId, envelope);
            System.out.println("Successfully sent envelope with envelopeId " + results.getEnvelopeId());
        }
        catch (ApiException exp)
        {
            if (exp.getMessage().contains("consent_required"))
            {
                try
                {
                    System.out.println ("Consent required, please provide consent in browser window and then run this app again.");
                    Desktop.getDesktop().browse(new URI("https://account-d.docusign.com/oauth/auth?response_type=code&scope=impersonation%20signature&client_id=" + prop.getProperty("clientId") + "&redirect_uri=" + DevCenterPage));
                }
                catch (Exception e)
                {
                    System.out.print ("Error!!!  ");
                    System.out.print (e.getMessage());
                }
                    }
        }
        catch (Exception e)
        {
            System.out.print ("Error!!!  ");
            System.out.print (e.getMessage());
        }
    }
}

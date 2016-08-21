/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package is.ejb.bl.notificationSystems.gcm.test;

import is.ejb.bl.notificationSystems.gcm.server.Constants;
import is.ejb.bl.notificationSystems.gcm.server.Message;
import is.ejb.bl.notificationSystems.gcm.server.MulticastResult;
import is.ejb.bl.notificationSystems.gcm.server.Result;
import is.ejb.bl.notificationSystems.gcm.server.Sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestGoogleNotificationSender {

	
  protected static final Logger logger = Logger.getLogger(TestGoogleNotificationSender.class.getName());
  //@Inject
  //private Logger logger;

  private static final int MULTICAST_SIZE = 1000;

  private String ACCESS_KEY_VALUE = "AIzaSyCi8uDWtGCukXi_eiovUqL75sUn1HG5HGA";
  private Sender sender = new Sender(ACCESS_KEY_VALUE);

  private static final Executor threadPool = Executors.newFixedThreadPool(5);

  public static void main(String[] args) throws IOException {
	  new TestGoogleNotificationSender();
  }
  
  public TestGoogleNotificationSender() throws IOException {
	  logger.info("Initialised GCM notification sender...");
	  sendMessage();
  }
  
  //Processes the request to add a new message.
  protected void sendMessage() throws IOException {
    List<String> devices = new ArrayList<String>();
//    String regId = "APA91bGaTK1EzFhFMwc7QkGQB22l5etoLd53-VyUyH81d81dIbz8143X0XR-svX9bibvIpIzz1_96oB7UbXHi1PIww_qf9GvUHs1uj_pFPPc7mIYTrvSyWrLtTtJ3Vd8SuX1lKhlK4UD";
    String regId = "APA91bFgltkkoUbw6KlCa_KshJqWb5rZxg8sOJNvYrtaeoBynVPizJUmmGIxh0Iqp9xzeZP5Xk1KaSbphA1wq_6TCVLyPTnokaeqTtA7uWcnNrCRCkWKFz0Syv56Jb5-qdteMoRXCfBE";
    devices.add(regId); //specify registration id to which message is sent

    String status;
    if (devices.isEmpty()) {
      status = "Message ignored as there is no device registered!";
    } else {
      // NOTE: check below is for demonstration purposes; a real application
      // could always send a multicast, even for just one recipient
      if (devices.size() == 1) {
        // send a single message using plain post
        String registrationId = devices.get(0);
         
        Message message = new Message.Builder()
           .dryRun(false)
           .addData("alert", "You have been rewarded 10 Ksh reward") //you have been reward ... - message displayed to user
           .addData("category", "[adbroker]")
           .build();

        //Message message = new Message.Builder().build(); //simplest message
        String strMessageContent = message.toString();
        System.out.println("message content: "+message.isDryRun()+" "+strMessageContent);

        Result result = sender.send(message, registrationId, 5);
        status = "Sent message to one device: " + result;
      } else {
        // send a multicast message using JSON
        // must split in chunks of 1000 devices (GCM limit)
        int total = devices.size();
        List<String> partialDevices = new ArrayList<String>(total);
        int counter = 0;
        int tasks = 0;
        for (String device : devices) {
          counter++;
          partialDevices.add(device);
          int partialSize = partialDevices.size();
          if (partialSize == MULTICAST_SIZE || counter == total) {
            asyncSend(partialDevices);
            partialDevices.clear();
            tasks++;
          }
        }
        status = "Asynchronously sending " + tasks + " multicast messages to " +
            total + " devices";
      }
    }
  
    System.out.println("Send status is: "+status.toString());
  }

  private void asyncSend(List<String> partialDevices) {
    // make a copy
    final List<String> devices = new ArrayList<String>(partialDevices);
    threadPool.execute(new Runnable() {

      public void run() {
        Message message = new Message.Builder().build();
        MulticastResult multicastResult;
        try {
          multicastResult = sender.send(message, devices, 5);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error posting messages", e);
          return;
        }
        List<Result> results = multicastResult.getResults();
        // analyze the results
        for (int i = 0; i < devices.size(); i++) {
          String regId = devices.get(i);
          Result result = results.get(i);
          String messageId = result.getMessageId();
          if (messageId != null) {
        	  
            logger.fine("Succesfully sent message to device: " + regId +"; messageId = " + messageId);
            String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {
              // same device has more than on registration id: update it
              logger.info("canonicalRegId " + canonicalRegId);
              //Datastore.updateRegistration(regId, canonicalRegId);
            }
          } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
              // application has been removed from device - unregister it
              logger.info("Unregistered device: " + regId);
              //Datastore.unregister(regId);
            } else {
              logger.severe("Error sending message to " + regId + ": " + error);
            }
          }
        }
      }});
  }

}

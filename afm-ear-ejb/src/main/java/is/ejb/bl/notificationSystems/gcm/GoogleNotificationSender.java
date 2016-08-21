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
package is.ejb.bl.notificationSystems.gcm;

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

public class GoogleNotificationSender {

  //http://stackoverflow.com/questions/22261024/gcm-java-server-example
  protected static final Logger logger = Logger.getLogger(GoogleNotificationSender.class.getName());
  //@Inject
  //private Logger logger;

  private static final int MULTICAST_SIZE = 1000;

  private String ACCESS_KEY_VALUE = "";
  private Sender sender = null;
  
  private static final Executor threadPool = Executors.newFixedThreadPool(5);

  public GoogleNotificationSender(String accessKey) throws IOException {
	  ACCESS_KEY_VALUE = accessKey;
	  logger.info("Initialised GCM notification sender with key: "+ACCESS_KEY_VALUE);
	  sender = new Sender(ACCESS_KEY_VALUE);
  }
  
  //Processes the request to add a new message.
  public String sendMessage(String registrationId, String messageContent) throws IOException {
	  logger.info("sending message to registration id: "+registrationId+" content: "+messageContent);
    List<String> devices = new ArrayList<String>();
    devices.add(registrationId); //specify registration id to which message is sent
    Message message = null;
    String status;
    if (devices.isEmpty()) {
      status = "Message ignored as there is no device registered!";
    } else {
      // NOTE: check below is for demonstration purposes; a real application
      // could always send a multicast, even for just one recipient
      if (devices.size() == 1) {
        // send a single message using plain post
        message = new Message.Builder()
        .dryRun(false)
        .addData("alert", messageContent) //you have been reward ... - message displayed to user
        .addData("category", "[adbroker]")
        .build();

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
  
    if(message!=null) {
    	return "Send message: "+message.toString()+" status is: "+status.toString();
    } else {
    	return "Error creating message, unable to send it";
    }
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
